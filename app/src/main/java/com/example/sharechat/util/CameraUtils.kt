package com.example.sharechat.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility helper for managing runtime camera permissions and secure FileProvider photo URIs.
 */
object CameraUtils {

    const val CAMERA_PERMISSION: String = Manifest.permission.CAMERA

    /**
     * Checks if the camera runtime permission is granted.
     */
    fun hasCameraPermission(context: Context): Boolean {
        val permissionResult = ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION)
        return permissionResult == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Creates a temporary image file in the app's cache directory and returns
     * a secure content URI using [FileProvider].
     */
    fun createTempImageUri(context: Context): Uri {
        val imageFolder = File(context.cacheDir, "images").apply {
            if (!exists()) {
                mkdirs()
            }
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val tempImageFile = File(imageFolder, "IMG_${timeStamp}.jpg")

        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, tempImageFile)
    }
}
