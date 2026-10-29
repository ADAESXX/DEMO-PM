package com.example.sharechat.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
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
     * Opens the system App Details Settings page so the user can re-enable
     * permissions that were permanently denied or marked with "Don't ask again".
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
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
