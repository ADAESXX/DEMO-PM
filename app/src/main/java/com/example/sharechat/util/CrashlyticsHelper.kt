package com.example.sharechat.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Manages Firebase Crashlytics logging, custom metrics, and test crash triggers.
 * Includes defensive fallbacks so the app runs smoothly even with local dummy credentials.
 */
object CrashlyticsHelper {

    private const val TAG = "CrashlyticsHelper"

    private val crashlytics: FirebaseCrashlytics? by lazy {
        try {
            FirebaseCrashlytics.getInstance()
        } catch (exception: Exception) {
            Log.w(TAG, "Crashlytics is not initialized or dummy project in use: ${exception.message}")
            null
        }
    }

    /**
     * Associates subsequent crash reports with the current user ID.
     */
    fun setUserIdentifier(userId: String) {
        try {
            crashlytics?.setUserId(userId)
        } catch (exception: Exception) {
            Log.w(TAG, "Failed to set user id: ${exception.message}")
        }
    }

    /**
     * Attaches a key-value metric to Crashlytics session reports.
     */
    fun setCustomMetric(key: String, value: String) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (exception: Exception) {
            Log.w(TAG, "Failed to set custom metric: ${exception.message}")
        }
    }

    /**
     * Adds breadcrumb message to the Crashlytics session logs.
     */
    fun logEvent(message: String) {
        Log.d(TAG, message)
        try {
            crashlytics?.log("[ShareChat] $message")
        } catch (exception: Exception) {
            Log.w(TAG, "Failed to log event: ${exception.message}")
        }
    }

    /**
     * Records a handled non-fatal exception to the Firebase Console.
     */
    fun recordNonFatal(throwable: Throwable) {
        Log.w(TAG, "Non-fatal exception recorded: ${throwable.message}")
        try {
            crashlytics?.recordException(throwable)
        } catch (exception: Exception) {
            Log.w(TAG, "Failed to record non-fatal: ${exception.message}")
        }
    }

    /**
     * Triggers a synthetic exception for presentation purposes to demonstrate
     * live crash reporting in Firebase Console.
     * Note: This will intentionally crash the app to verify Firebase Crashlytics collection!
     */
    fun triggerDemoCrash() {
        logEvent("Manual test crash triggered from presentation demo button.")
        throw RuntimeException("Demo Presentation Crash: Verification of Firebase Crashlytics integration.")
    }
}
