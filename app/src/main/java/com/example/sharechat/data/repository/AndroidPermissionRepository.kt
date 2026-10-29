package com.example.sharechat.data.repository

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.sharechat.util.CrashlyticsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Concrete implementation of [PermissionRepository] handling Android runtime permissions,
 * FileProvider photo paths, and system intent dispatching.
 */
class AndroidPermissionRepository(
    private val context: Context
) : PermissionRepository {

    // Initialize directly with the permission check so _hasCameraPermission is assigned before any calls to it
    private val _hasCameraPermission = MutableStateFlow(
        try {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
    )
    override val hasCameraPermission: StateFlow<Boolean> = _hasCameraPermission.asStateFlow()

    override fun checkCameraPermission(): Boolean {
        val isGranted = try {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }
        _hasCameraPermission.value = isGranted
        return isGranted
    }

    override fun updateCameraPermission(isGranted: Boolean) {
        _hasCameraPermission.value = isGranted
        CrashlyticsHelper.setCustomMetric("camera_permission_granted", isGranted.toString())
        CrashlyticsHelper.logEvent("Camera permission status updated: $isGranted")
    }

    override fun createTempCameraUri(): Uri {
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

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        CrashlyticsHelper.logEvent("Navigated to app settings for permission management.")
    }
}
