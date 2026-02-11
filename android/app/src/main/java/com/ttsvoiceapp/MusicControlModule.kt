package com.ttsvoiceapp

import android.net.Uri
import com.facebook.react.bridge.*

class MusicControlModule(reactContext: ReactApplicationContext) : 
    ReactContextBaseJavaModule(reactContext) {
    
    companion object {
        private const val TAG = "MusicControlModule"
    }
    
    override fun getName() = "MusicControl"
    
    @ReactMethod
    fun loadMusic(uriString: String, promise: Promise) {
        try {
            val service = TTSForegroundService.getInstance()
            if (service == null) {
                promise.reject("SERVICE_NOT_RUNNING", "TTS service is not running. Please start the service first.")
                return
            }
            
            val uri = Uri.parse(uriString)
            service.loadBackgroundMusic(uri)
            promise.resolve("Music loaded successfully")
        } catch (e: Exception) {
            promise.reject("LOAD_ERROR", "Failed to load music: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun loadPlaylist(uriStrings: ReadableArray, promise: Promise) {
        try {
            val service = TTSForegroundService.getInstance()
            if (service == null) {
                promise.reject("SERVICE_NOT_RUNNING", "TTS service is not running. Please start the service first.")
                return
            }
            
            val uris = mutableListOf<Uri>()
            for (i in 0 until uriStrings.size()) {
                val uriString = uriStrings.getString(i)
                if (uriString != null) {
                    uris.add(Uri.parse(uriString))
                }
            }
            
            if (uris.isEmpty()) {
                promise.reject("NO_FILES", "No valid audio files provided")
                return
            }
            
            service.loadBackgroundPlaylist(uris)
            promise.resolve("Playlist loaded with ${uris.size} tracks")
        } catch (e: Exception) {
            promise.reject("LOAD_ERROR", "Failed to load playlist: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun playMusic(promise: Promise) {
        try {
            val service = TTSForegroundService.getInstance()
            if (service == null) {
                promise.reject("SERVICE_NOT_RUNNING", "TTS service is not running")
                return
            }
            
            service.playBackgroundMusic()
            promise.resolve("Music playing")
        } catch (e: Exception) {
            promise.reject("PLAY_ERROR", "Failed to play music: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun pauseMusic(promise: Promise) {
        try {
            val service = TTSForegroundService.getInstance()
            if (service == null) {
                promise.reject("SERVICE_NOT_RUNNING", "TTS service is not running")
                return
            }
            
            service.pauseBackgroundMusic()
            promise.resolve("Music paused")
        } catch (e: Exception) {
            promise.reject("PAUSE_ERROR", "Failed to pause music: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun stopMusic(promise: Promise) {
        try {
            val service = TTSForegroundService.getInstance()
            if (service == null) {
                promise.reject("SERVICE_NOT_RUNNING", "TTS service is not running")
                return
            }
            
            service.stopBackgroundMusic()
            promise.resolve("Music stopped")
        } catch (e: Exception) {
            promise.reject("STOP_ERROR", "Failed to stop music: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun nextTrack(promise: Promise) {
        try {
            val service = TTSForegroundService.getInstance()
            if (service == null) {
                promise.reject("SERVICE_NOT_RUNNING", "TTS service is not running")
                return
            }
            
            service.nextTrack()
            promise.resolve("Playing next track")
        } catch (e: Exception) {
            promise.reject("NEXT_ERROR", "Failed to play next track: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun previousTrack(promise: Promise) {
        try {
            val service = TTSForegroundService.getInstance()
            if (service == null) {
                promise.reject("SERVICE_NOT_RUNNING", "TTS service is not running")
                return
            }
            
            service.previousTrack()
            promise.resolve("Playing previous track")
        } catch (e: Exception) {
            promise.reject("PREVIOUS_ERROR", "Failed to play previous track: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun setVolume(volume: Double, promise: Promise) {
        try {
            val service = TTSForegroundService.getInstance()
            if (service == null) {
                promise.reject("SERVICE_NOT_RUNNING", "TTS service is not running")
                return
            }
            
            val volumeFloat = volume.toFloat().coerceIn(0f, 1f)
            service.setBackgroundMusicVolume(volumeFloat)
            promise.resolve("Volume set to $volumeFloat")
        } catch (e: Exception) {
            promise.reject("VOLUME_ERROR", "Failed to set volume: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun getMusicState(promise: Promise) {
        try {
            val service = TTSForegroundService.getInstance()
            if (service == null) {
                promise.reject("SERVICE_NOT_RUNNING", "TTS service is not running")
                return
            }
            
            val state = service.getMusicState()
            val result = Arguments.createMap().apply {
                putBoolean("isPlaying", state["isPlaying"] as? Boolean ?: false)
                putBoolean("isLoaded", state["isLoaded"] as? Boolean ?: false)
                putBoolean("isPaused", state["isPaused"] as? Boolean ?: false)
                putDouble("volume", (state["volume"] as? Float)?.toDouble() ?: 0.5)
                putString("currentTrack", state["currentTrack"] as? String ?: "No music loaded")
                putInt("trackNumber", state["trackNumber"] as? Int ?: 0)
                putInt("playlistSize", state["playlistSize"] as? Int ?: 0)
            }
            
            promise.resolve(result)
        } catch (e: Exception) {
            promise.reject("STATE_ERROR", "Failed to get music state: ${e.message}", e)
        }
    }
}
