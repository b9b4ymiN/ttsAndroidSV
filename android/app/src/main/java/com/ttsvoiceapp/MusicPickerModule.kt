package com.ttsvoiceapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import com.facebook.react.bridge.*

class MusicPickerModule(private val reactContext: ReactApplicationContext) : 
    ReactContextBaseJavaModule(reactContext), ActivityEventListener {
    
    private var pickFilePromise: Promise? = null
    private var pickFolderPromise: Promise? = null
    
    companion object {
        private const val TAG = "MusicPickerModule"
        private const val PICK_AUDIO_FILE = 1001
        private const val PICK_AUDIO_FOLDER = 1002
    }
    
    init {
        reactContext.addActivityEventListener(this)
    }
    
    override fun getName() = "MusicPicker"
    
    @ReactMethod
    fun pickMusicFile(promise: Promise) {
        val activity = currentActivity
        if (activity == null) {
            promise.reject("NO_ACTIVITY", "Activity not available")
            return
        }
        
        pickFilePromise = promise
        
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "audio/mpeg",       // MP3
                "audio/mp4",        // M4A
                "audio/x-m4a",      // M4A
                "audio/wav",        // WAV
                "audio/aac",        // AAC
                "audio/flac",       // FLAC
                "audio/ogg"         // OGG
            ))
        }
        
        try {
            activity.startActivityForResult(intent, PICK_AUDIO_FILE, null)
        } catch (e: Exception) {
            promise.reject("PICKER_ERROR", "Failed to open file picker: ${e.message}", e)
            pickFilePromise = null
        }
    }
    
    @ReactMethod
    fun pickMusicFolder(promise: Promise) {
        val activity = currentActivity
        if (activity == null) {
            promise.reject("NO_ACTIVITY", "Activity not available")
            return
        }
        
        pickFolderPromise = promise
        
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        
        try {
            activity.startActivityForResult(intent, PICK_AUDIO_FOLDER, null)
        } catch (e: Exception) {
            promise.reject("PICKER_ERROR", "Failed to open folder picker: ${e.message}", e)
            pickFolderPromise = null
        }
    }
    
    override fun onActivityResult(
        activity: Activity?,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        when (requestCode) {
            PICK_AUDIO_FILE -> handleFilePickResult(resultCode, data)
            PICK_AUDIO_FOLDER -> handleFolderPickResult(resultCode, data)
        }
    }
    
    private fun handleFilePickResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                try {
                    // Take persistable permission
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    reactContext.contentResolver.takePersistableUriPermission(uri, takeFlags)
                    
                    pickFilePromise?.resolve(uri.toString())
                } catch (e: Exception) {
                    pickFilePromise?.reject("PERMISSION_ERROR", "Failed to get permission: ${e.message}", e)
                }
            } else {
                pickFilePromise?.reject("NO_URI", "No file selected")
            }
        } else {
            pickFilePromise?.reject("CANCELLED", "User cancelled file selection")
        }
        pickFilePromise = null
    }
    
    private fun handleFolderPickResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val treeUri = data.data
            if (treeUri != null) {
                try {
                    // Take persistable permission for folder
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    reactContext.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                    
                    // Get all audio files in folder
                    val audioFiles = getAudioFilesFromFolder(treeUri)
                    
                    if (audioFiles.isEmpty()) {
                        pickFolderPromise?.reject("NO_AUDIO_FILES", "No audio files found in selected folder")
                    } else {
                        val uriStrings = audioFiles.map { it.toString() }
                        val result = Arguments.createMap().apply {
                            putString("folderUri", treeUri.toString())
                            putArray("audioFiles", Arguments.fromList(uriStrings))
                            putInt("count", audioFiles.size)
                        }
                        pickFolderPromise?.resolve(result)
                    }
                } catch (e: Exception) {
                    pickFolderPromise?.reject("FOLDER_ERROR", "Failed to read folder: ${e.message}", e)
                }
            } else {
                pickFolderPromise?.reject("NO_URI", "No folder selected")
            }
        } else {
            pickFolderPromise?.reject("CANCELLED", "User cancelled folder selection")
        }
        pickFolderPromise = null
    }
    
    private fun getAudioFilesFromFolder(treeUri: Uri): List<Uri> {
        val audioFiles = mutableListOf<Uri>()
        
        try {
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )
            
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            )
            
            reactContext.contentResolver.query(
                childrenUri,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val mimeColumn = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                
                while (cursor.moveToNext()) {
                    val documentId = cursor.getString(idColumn)
                    val mimeType = cursor.getString(mimeColumn)
                    
                    // Check if it's an audio file
                    if (mimeType?.startsWith("audio/") == true) {
                        val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                        audioFiles.add(documentUri)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error reading folder contents", e)
        }
        
        return audioFiles
    }
    
    override fun onNewIntent(intent: Intent?) {
        // Not needed for this module
    }
}
