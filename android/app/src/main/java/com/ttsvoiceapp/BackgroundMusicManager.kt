package com.ttsvoiceapp

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import java.io.File

class BackgroundMusicManager(private val context: Context) {
    
    companion object {
        private const val TAG = "BackgroundMusicManager"
    }
    
    private var mediaPlayer: MediaPlayer? = null
    private var playlist = mutableListOf<Uri>()
    private var currentTrackIndex = 0
    private var userVolume = 0.5f // Default 50% volume
    private var isPaused = false
    private var isLoaded = false
    
    /**
     * Load a single music file
     */
    fun loadMusicFile(uri: Uri) {
        try {
            playlist.clear()
            playlist.add(uri)
            currentTrackIndex = 0
            loadCurrentTrack()
            Log.d(TAG, "Music file loaded: $uri")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load music file", e)
            throw e
        }
    }
    
    /**
     * Load multiple music files (playlist from folder)
     */
    fun loadPlaylist(uris: List<Uri>) {
        try {
            playlist.clear()
            playlist.addAll(uris.filter { isAudioFile(it) })
            currentTrackIndex = 0
            
            if (playlist.isEmpty()) {
                throw IllegalArgumentException("No valid audio files found")
            }
            
            loadCurrentTrack()
            Log.d(TAG, "Playlist loaded with ${playlist.size} tracks")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load playlist", e)
            throw e
        }
    }
    
    /**
     * Load the current track from playlist
     */
    private fun loadCurrentTrack() {
        if (playlist.isEmpty()) {
            Log.w(TAG, "Playlist is empty")
            return
        }
        
        releasePlayer()
        
        val uri = playlist[currentTrackIndex]
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(context, uri)
                setVolume(userVolume, userVolume)
                
                // Set completion listener to play next track
                setOnCompletionListener {
                    playNextTrack()
                }
                
                // Set error listener
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    playNextTrack() // Skip to next track on error
                    true
                }
                
                prepare()
                isLoaded = true
                Log.d(TAG, "Track prepared: ${getTrackName(uri)}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to prepare track", e)
                isLoaded = false
                throw e
            }
        }
    }
    
    /**
     * Play the music
     */
    fun play() {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    isPaused = false
                    Log.d(TAG, "Music started")
                }
            } ?: run {
                Log.w(TAG, "MediaPlayer not initialized")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play music", e)
        }
    }
    
    /**
     * Pause the music
     */
    fun pause() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    isPaused = true
                    Log.d(TAG, "Music paused")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause music", e)
        }
    }
    
    /**
     * Resume the music after pause
     */
    fun resume() {
        if (isPaused) {
            play()
        }
    }
    
    /**
     * Stop the music
     */
    fun stop() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                isPaused = false
                Log.d(TAG, "Music stopped")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop music", e)
        }
    }
    
    /**
     * Play next track in playlist
     */
    fun playNext() {
        playNextTrack()
    }
    
    /**
     * Play previous track in playlist
     */
    fun playPrevious() {
        if (playlist.isEmpty()) return
        
        currentTrackIndex = if (currentTrackIndex > 0) {
            currentTrackIndex - 1
        } else {
            playlist.size - 1 // Loop to last track
        }
        
        loadCurrentTrack()
        play()
    }
    
    /**
     * Internal method to play next track
     */
    private fun playNextTrack() {
        if (playlist.isEmpty()) return
        
        currentTrackIndex = (currentTrackIndex + 1) % playlist.size
        loadCurrentTrack()
        play()
    }
    
    /**
     * Set music volume (0.0 - 1.0)
     */
    fun setVolume(volume: Float) {
        userVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(userVolume, userVolume)
        Log.d(TAG, "Volume set to: $userVolume")
    }
    
    /**
     * Get current volume
     */
    fun getVolume(): Float = userVolume
    
    /**
     * Check if music is currently playing
     */
    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if music is loaded
     */
    fun isLoaded(): Boolean = isLoaded
    
    /**
     * Get current track name
     */
    fun getCurrentTrackName(): String {
        if (playlist.isEmpty()) return "No music loaded"
        return getTrackName(playlist[currentTrackIndex])
    }
    
    /**
     * Get playlist size
     */
    fun getPlaylistSize(): Int = playlist.size
    
    /**
     * Get current track index (1-based for display)
     */
    fun getCurrentTrackNumber(): Int = currentTrackIndex + 1
    
    /**
     * Release MediaPlayer resources
     */
    fun releasePlayer() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            isLoaded = false
            Log.d(TAG, "MediaPlayer released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer", e)
        }
    }
    
    /**
     * Extract track name from URI
     */
    private fun getTrackName(uri: Uri): String {
        return try {
            val path = uri.lastPathSegment ?: uri.toString()
            File(path).name
        } catch (e: Exception) {
            uri.toString()
        }
    }
    
    /**
     * Check if URI is an audio file
     */
    private fun isAudioFile(uri: Uri): Boolean {
        val path = uri.toString().lowercase()
        return path.endsWith(".mp3") || 
               path.endsWith(".m4a") || 
               path.endsWith(".wav") || 
               path.endsWith(".aac") ||
               path.endsWith(".flac") ||
               path.endsWith(".ogg")
    }
    
    /**
     * Get current music state as map for React Native
     */
    fun getMusicState(): Map<String, Any> {
        return mapOf(
            "isPlaying" to isPlaying(),
            "isLoaded" to isLoaded,
            "isPaused" to isPaused,
            "volume" to userVolume,
            "currentTrack" to getCurrentTrackName(),
            "trackNumber" to getCurrentTrackNumber(),
            "playlistSize" to getPlaylistSize()
        )
    }
}
