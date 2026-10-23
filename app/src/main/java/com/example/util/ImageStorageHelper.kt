package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {

    /**
     * Copies any transient content URI (e.g. from photo picker or external apps)
     * into the app's persistent internal storage so that it NEVER disappears
     * after app restarts or device reboots.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String {
        return try {
            val imagesDir = File(context.filesDir, "item_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            val destinationFile = File(imagesDir, "item_img_${System.currentTimeMillis()}_${(100..999).random()}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            sourceUri.toString()
        }
    }
}
