package com.example.sharechat

import android.app.Application
import android.util.Log
import com.example.sharechat.util.CrashlyticsHelper
import com.google.firebase.FirebaseApp

/**
 * Base Application class initializing Firebase services and Crashlytics tracking.
 */
class ShareChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase initialized successfully.")
        } catch (exception: Exception) {
            Log.w(TAG, "Firebase initialization fallback: ${exception.message}")
        }

        CrashlyticsHelper.logEvent("ShareChat application started.")
        CrashlyticsHelper.setCustomMetric("build_type", "demo_presentation")
    }

    companion object {
        private const val TAG = "ShareChatApp"
    }
}
