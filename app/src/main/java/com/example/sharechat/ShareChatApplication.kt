package com.example.sharechat

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.sharechat.util.CrashlyticsHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

/**
 * Base Application class initializing Firebase services, Auth, and Crashlytics tracking.
 */
class ShareChatApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        try {
            FirebaseApp.initializeApp(this)
            Log.d(TAG, "Firebase initialized successfully.")

            // Sign in anonymously if needed to satisfy Firestore security rules
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                auth.signInAnonymously()
                    .addOnSuccessListener { result ->
                        Log.d(TAG, "Anonymous auth success: ${result.user?.uid}")
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Anonymous auth skipped/failed: ${exception.message}")
                    }
            }
        } catch (exception: Exception) {
            Log.w(TAG, "Firebase initialization fallback: ${exception.message}")
        }

        CrashlyticsHelper.logEvent("ShareChat application started.")
        CrashlyticsHelper.setCustomMetric("build_type", "demo_presentation")
    }

    companion object {
        private const val TAG = "ShareChatApp"

        @Volatile
        var instance: ShareChatApplication? = null
            private set

        val appContext: Context
            get() = instance?.applicationContext
                ?: try {
                    FirebaseApp.getInstance().applicationContext
                } catch (_: Exception) {
                    instance?.baseContext ?: throw IllegalStateException("Application context is not yet available.")
                }
    }
}
