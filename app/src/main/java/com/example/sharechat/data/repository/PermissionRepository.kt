package com.example.sharechat.data.repository

import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository contract abstracting runtime permission verification, status observation,
 * secure FileProvider URI creation, and system settings navigation.
 */
interface PermissionRepository {

    /**
     * Observable state representing whether the camera permission is granted.
     */
    val hasCameraPermission: StateFlow<Boolean>

    /**
     * Checks the real-time granted status of the camera runtime permission.
     */
    fun checkCameraPermission(): Boolean

    /**
     * Updates the local cached permission state based on system callback results.
     */
    fun updateCameraPermission(isGranted: Boolean)

    /**
     * Creates a temporary file in secure cache and returns a content URI for camera capture.
     */
    fun createTempCameraUri(): Uri

    /**
     * Launches the system settings screen for this application to resolve permanent denials.
     */
    fun openAppSettings()
}
