package com.ttsvoiceapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.NotificationCompat
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

// Data class to hold speech parameters
data class SpeechRequest(
    val text: String,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f
)

class TTSForegroundService : Service(), TextToSpeech.OnInitListener {
    
    private var httpServer: TTSHttpServer? = null
    private var textToSpeech: TextToSpeech? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val ttsQueue = ConcurrentLinkedQueue<SpeechRequest>()
    private var isSpeaking = false
    private var ttsInitialized = false
    private var musicManager: BackgroundMusicManager? = null
    
    companion object {
        private const val TAG = "TTSForegroundService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "tts_service_channel"
        private const val HTTP_PORT = 8765
        
        var isRunning = false
        var lastStatus = "Stopped"
        
        // Singleton instance for module access
        private var instance: TTSForegroundService? = null
        
        fun getInstance(): TTSForegroundService? = instance
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        
        // Set singleton instance
        instance = this
        
        // Initialize BackgroundMusicManager
        musicManager = BackgroundMusicManager(this)
        
        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this, this)
        
        // Acquire wake lock to keep service running
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TTSVoiceApp::TTSWakeLock"
        )
        wakeLock?.acquire()
        
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand - action: ${intent?.action}")
        
        // Handle stop service action from notification
        if (intent?.action == "STOP_SERVICE") {
            Log.d(TAG, "Stop service requested from notification")
            stopSelf()
            return START_NOT_STICKY
        }
        
        val deviceIp = getDeviceIpAddress()
        val notification = createNotification("Running on $deviceIp:$HTTP_PORT")
        startForeground(NOTIFICATION_ID, notification)
        
        isRunning = true
        lastStatus = "Running on port $HTTP_PORT"
        
        // Start HTTP server
        startHttpServer()
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        
        isRunning = false
        lastStatus = "Stopped"
        
        // Stop and release music
        musicManager?.stop()
        musicManager?.releasePlayer()
        musicManager = null
        
        // Stop HTTP server
        httpServer?.stop()
        httpServer = null
        
        // Shutdown TTS
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        
        // Release wake lock
        wakeLock?.release()
        wakeLock = null
        
        ttsQueue.clear()
        
        // Clear singleton instance
        instance = null
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                // Check available languages
                val availableLanguages = tts.availableLanguages
                Log.d(TAG, "Available TTS languages: ${availableLanguages?.map { it.toString() }}")
                
                // Try to set Thai language using modern API
                val thaiLocale = Locale.Builder()
                    .setLanguage("th")
                    .setRegion("TH")
                    .build()
                
                val thaiResult = tts.setLanguage(thaiLocale)
                
                if (thaiResult == TextToSpeech.LANG_MISSING_DATA || 
                    thaiResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Thai language not fully supported: $thaiResult")
                    Log.e(TAG, "Please install Thai language pack in TTS settings")
                    // Try alternative Thai locale
                    val thaiResult2 = tts.setLanguage(Locale("th"))
                    if (thaiResult2 == TextToSpeech.LANG_MISSING_DATA || 
                        thaiResult2 == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "Thai language not available, defaulting to English")
                        tts.language = Locale.US
                    } else {
                        Log.d(TAG, "Thai language set with fallback method")
                    }
                } else {
                    Log.d(TAG, "Thai language set successfully with result: $thaiResult")
                }
                
                tts.setPitch(1.0f)
                tts.setSpeechRate(1.0f)
                
                // Set utterance progress listener
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d(TAG, "TTS started: $utteranceId")
                    }
                    
                    override fun onDone(utteranceId: String?) {
                        Log.d(TAG, "TTS completed: $utteranceId")
                        isSpeaking = false
                        
                        // Resume music after TTS if it was playing
                        musicManager?.resume()
                        
                        processNextInQueue()
                    }
                    
                    override fun onError(utteranceId: String?) {
                        Log.e(TAG, "TTS error: $utteranceId")
                        isSpeaking = false
                        
                        // Resume music on error too
                        musicManager?.resume()
                        
                        processNextInQueue()
                    }
                })
                
                ttsInitialized = true
                Log.d(TAG, "TTS initialized successfully")
            }
        } else {
            Log.e(TAG, "TTS initialization failed")
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TTS Voice Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service for Thai/English Text-to-Speech REST API"
                setShowBadge(true)
                enableLights(true)
                enableVibration(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun getDeviceIpAddress(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        return address.hostAddress ?: "Unknown"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP address", e)
        }
        return "Unknown"
    }
    
    private fun createNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Stop service intent
        val stopIntent = Intent(this, TTSForegroundService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val deviceIp = getDeviceIpAddress()
        val serverUrl = "http://$deviceIp:$HTTP_PORT"
        
        // Build big text style notification
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText("$contentText\n\n" +
                "🌐 API: $serverUrl\n" +
                "📊 Queue: ${ttsQueue.size} items\n" +
                "🎤 Speaking: ${if (isSpeaking) "Yes" else "No"}\n" +
                "✅ TTS Ready: ${if (ttsInitialized) "Yes" else "Initializing..."}")
            .setBigContentTitle("🎙️ TTS Voice Service Active")
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🎙️ TTS Voice Service")
            .setContentText("Tap for details • $serverUrl")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setStyle(bigTextStyle)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                android.R.drawable.ic_delete,
                "Stop Service",
                stopPendingIntent
            )
            .setColor(0xFF4169E1.toInt())
            .setColorized(false)
            .build()
    }
    
    private fun updateNotification(contentText: String) {
        val notification = createNotification(contentText)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        // Also update lastStatus for API
        lastStatus = contentText
    }
    
    private fun startHttpServer() {
        try {
            httpServer = TTSHttpServer(HTTP_PORT)
            httpServer?.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            Log.d(TAG, "HTTP server started on 0.0.0.0:$HTTP_PORT (accessible from network)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start HTTP server", e)
        }
    }
    
    fun addToTTSQueue(request: SpeechRequest) {
        ttsQueue.offer(request)
        Log.d(TAG, "Added to queue: ${request.text} (speed: ${request.speed}x, pitch: ${request.pitch}) - Queue size: ${ttsQueue.size}")
        processNextInQueue()
    }
    
    private fun processNextInQueue() {
        if (!isSpeaking && ttsQueue.isNotEmpty() && ttsInitialized) {
            val request = ttsQueue.poll()
            if (request != null) {
                isSpeaking = true
                speakText(request)
            }
        }
    }
    
    private fun speakText(request: SpeechRequest) {
        textToSpeech?.let { tts ->
            // Pause background music before speaking
            if (musicManager?.isPlaying() == true) {
                musicManager?.pause()
                Log.d(TAG, "Music paused for TTS")
            }
            
            // Apply speech parameters BEFORE language detection
            tts.setSpeechRate(request.speed)
            tts.setPitch(request.pitch)
            
            // Detect language - if contains Thai characters, use Thai, otherwise English
            val hasThai = request.text.any { it in '\u0E00'..'\u0E7F' }
            
            val locale = if (hasThai) {
                // Use modern Locale API for Thai
                Locale.Builder()
                    .setLanguage("th")
                    .setRegion("TH")
                    .build()
            } else {
                Locale.US
            }
            
            // Set language and verify it's available
            val langResult = tts.setLanguage(locale)
            val langName = if (hasThai) "Thai" else "English"
            
            when (langResult) {
                TextToSpeech.LANG_MISSING_DATA -> {
                    Log.e(TAG, "$langName language data is missing!")
                    updateNotification("Error: $langName TTS data missing")
                    lastStatus = "Error: Missing $langName TTS data"
                    isSpeaking = false
                    processNextInQueue()
                    return
                }
                TextToSpeech.LANG_NOT_SUPPORTED -> {
                    Log.e(TAG, "$langName language not supported!")
                    // For Thai, try fallback locale
                    if (hasThai) {
                        Log.d(TAG, "Trying fallback Thai locale...")
                        val fallbackResult = tts.setLanguage(Locale("th"))
                        if (fallbackResult == TextToSpeech.LANG_MISSING_DATA || 
                            fallbackResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                            updateNotification("Error: Thai TTS not available")
                            lastStatus = "Error: Thai TTS not supported"
                            isSpeaking = false
                            processNextInQueue()
                            return
                        }
                    } else {
                        updateNotification("Error: $langName TTS not supported")
                        lastStatus = "Error: $langName not supported"
                        isSpeaking = false
                        processNextInQueue()
                        return
                    }
                }
                else -> {
                    Log.d(TAG, "Language set to $langName successfully (result: $langResult)")
                }
            }
            
            val utteranceId = UUID.randomUUID().toString()
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            
            val speakResult = tts.speak(request.text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            
            if (speakResult == TextToSpeech.SUCCESS) {
                val speedInfo = if (request.speed != 1.0f) " ${request.speed}x" else ""
                updateNotification("Speaking ($langName$speedInfo): ${request.text.take(25)}...")
                lastStatus = "Speaking ($langName) at ${request.speed}x speed"
                Log.d(TAG, "Speaking ($langName) at speed ${request.speed}x, pitch ${request.pitch}: ${request.text}")
            } else {
                Log.e(TAG, "Failed to speak text, error code: $speakResult")
                updateNotification("Error: Failed to speak")
                lastStatus = "Error: Speech failed"
                isSpeaking = false
                processNextInQueue()
            }
        }
    }
    
    inner class TTSHttpServer(port: Int) : NanoHTTPD("0.0.0.0", port) {
        
        override fun serve(session: IHTTPSession): Response {
            val uri = session.uri
            val method = session.method
            
            Log.d(TAG, "HTTP Request: $method $uri")
            
            return when {
                method == Method.POST && uri == "/speak" -> handleSpeak(session)
                method == Method.GET && uri == "/status" -> handleStatus()
                method == Method.GET && uri == "/health" -> handleHealth()
                else -> newFixedLengthResponse(
                    Response.Status.NOT_FOUND,
                    "application/json; charset=utf-8",
                    """{"error":"Endpoint not found","message":"Available endpoints: POST /speak, GET /status, GET /health"}"""
                )
            }
        }
        
        private fun handleSpeak(session: IHTTPSession): Response {
            return try {
                // Read raw request body with UTF-8 encoding directly from InputStream
                val inputStream = session.inputStream
                val contentLength = session.headers["content-length"]?.toIntOrNull() ?: 0
                
                if (contentLength == 0) {
                    return newFixedLengthResponse(
                        Response.Status.BAD_REQUEST,
                        "application/json; charset=utf-8",
                        """{"error":"Empty request body"}"""
                    )
                }
                
                // Read bytes and decode as UTF-8
                val bodyBytes = ByteArray(contentLength)
                inputStream.read(bodyBytes)
                val bodyString = String(bodyBytes, Charsets.UTF_8)
                
                Log.d(TAG, "Request body: $bodyString")
                
                val jsonObject = JSONObject(bodyString)
                val text = jsonObject.optString("text", "")
                val speed = jsonObject.optDouble("speed", 1.0).toFloat()
                val pitch = jsonObject.optDouble("pitch", 1.0).toFloat()
                
                if (text.isEmpty()) {
                    return newFixedLengthResponse(
                        Response.Status.BAD_REQUEST,
                        "application/json; charset=utf-8",
                        """{"error":"Missing 'text' field"}"""
                    )
                }
                
                // Clamp speed and pitch to valid Android TTS ranges (0.5 - 2.0)
                val clampedSpeed = speed.coerceIn(0.5f, 2.0f)
                val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
                
                if (!ttsInitialized) {
                    return newFixedLengthResponse(
                        Response.Status.SERVICE_UNAVAILABLE,
                        "application/json; charset=utf-8",
                        """{"error":"TTS engine not initialized yet"}"""
                    )
                }
                
                // Create speech request with parameters
                val request = SpeechRequest(text, clampedSpeed, clampedPitch)
                addToTTSQueue(request)
                
                val response = JSONObject().apply {
                    put("status", "queued")
                    put("text", text)
                    put("speed", clampedSpeed)
                    put("pitch", clampedPitch)
                    put("queueSize", ttsQueue.size + (if (isSpeaking) 1 else 0))
                    put("message", "Text added to speech queue")
                }
                
                newFixedLengthResponse(
                    Response.Status.OK,
                    "application/json; charset=utf-8",
                    response.toString()
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Error handling /speak request", e)
                newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR,
                    "application/json; charset=utf-8",
                    """{"error":"${e.message}"}"""
                )
            }
        }
        
        private fun handleStatus(): Response {
            val status = JSONObject().apply {
                put("service", "running")
                put("port", HTTP_PORT)
                put("ttsInitialized", ttsInitialized)
                put("isSpeaking", isSpeaking)
                put("queueSize", ttsQueue.size)
                put("lastStatus", lastStatus)
            }
            
            return newFixedLengthResponse(
                Response.Status.OK,
                "application/json; charset=utf-8",
                status.toString()
            )
        }
        
        private fun handleHealth(): Response {
            return newFixedLengthResponse(
                Response.Status.OK,
                "application/json; charset=utf-8",
                """{"status":"healthy"}"""
            )
        }
    }
    
    // Public methods for music control (called from React Native modules)
    
    fun loadBackgroundMusic(uri: android.net.Uri) {
        musicManager?.loadMusicFile(uri)
        updateNotification("Music loaded: ${musicManager?.getCurrentTrackName()}")
    }
    
    fun loadBackgroundPlaylist(uris: List<android.net.Uri>) {
        musicManager?.loadPlaylist(uris)
        updateNotification("Playlist loaded: ${musicManager?.getPlaylistSize()} tracks")
    }
    
    fun playBackgroundMusic() {
        musicManager?.play()
        updateNotification("Playing: ${musicManager?.getCurrentTrackName()}")
    }
    
    fun pauseBackgroundMusic() {
        musicManager?.pause()
        updateNotification("Music paused")
    }
    
    fun stopBackgroundMusic() {
        musicManager?.stop()
        updateNotification("Music stopped")
    }
    
    fun nextTrack() {
        musicManager?.playNext()
        updateNotification("Next: ${musicManager?.getCurrentTrackName()}")
    }
    
    fun previousTrack() {
        musicManager?.playPrevious()
        updateNotification("Previous: ${musicManager?.getCurrentTrackName()}")
    }
    
    fun setBackgroundMusicVolume(volume: Float) {
        musicManager?.setVolume(volume)
        Log.d(TAG, "Music volume set to: $volume")
    }
    
    fun getMusicState(): Map<String, Any> {
        return musicManager?.getMusicState() ?: mapOf(
            "isPlaying" to false,
            "isLoaded" to false,
            "isPaused" to false,
            "volume" to 0.5f,
            "currentTrack" to "No music loaded",
            "trackNumber" to 0,
            "playlistSize" to 0
        )
    }
}
