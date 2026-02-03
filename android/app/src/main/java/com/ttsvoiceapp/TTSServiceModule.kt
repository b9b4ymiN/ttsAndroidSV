package com.ttsvoiceapp

import android.content.Intent
import android.os.Build
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

class TTSServiceModule(reactContext: ReactApplicationContext) : 
    ReactContextBaseJavaModule(reactContext) {
    
    companion object {
        private const val TAG = "TTSServiceModule"
    }
    
    override fun getName(): String = "TTSServiceModule"
    
    @ReactMethod
    fun startService(promise: Promise) {
        try {
            val intent = Intent(reactApplicationContext, TTSForegroundService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                reactApplicationContext.startForegroundService(intent)
            } else {
                reactApplicationContext.startService(intent)
            }
            
            promise.resolve("Service started")
        } catch (e: Exception) {
            promise.reject("START_ERROR", "Failed to start service: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun stopService(promise: Promise) {
        try {
            val intent = Intent(reactApplicationContext, TTSForegroundService::class.java)
            reactApplicationContext.stopService(intent)
            promise.resolve("Service stopped")
        } catch (e: Exception) {
            promise.reject("STOP_ERROR", "Failed to stop service: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun getServiceStatus(promise: Promise) {
        try {
            val status = WritableNativeMap().apply {
                putBoolean("isRunning", TTSForegroundService.isRunning)
                putString("lastStatus", TTSForegroundService.lastStatus)
            }
            promise.resolve(status)
        } catch (e: Exception) {
            promise.reject("STATUS_ERROR", "Failed to get status: ${e.message}", e)
        }
    }
    
    @ReactMethod
    fun testSpeak(text: String, promise: Promise) {
        try {
            // This is a test method - actual speaking is done via HTTP API
            if (!TTSForegroundService.isRunning) {
                promise.reject("SERVICE_NOT_RUNNING", "Service is not running")
                return
            }
            
            // In production, you would send HTTP request to localhost:8765/speak
            promise.resolve("Use HTTP POST to localhost:8765/speak with {\"text\":\"$text\"}")
        } catch (e: Exception) {
            promise.reject("TEST_ERROR", "Failed: ${e.message}", e)
        }
    }
}
