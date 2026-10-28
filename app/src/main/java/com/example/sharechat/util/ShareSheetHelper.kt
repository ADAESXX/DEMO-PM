package com.example.sharechat.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Utility class providing convenient methods to invoke the native Android Sharesheet.
 */
object ShareSheetHelper {

    /**
     * Launches the native Android Sharesheet to share text and an optional image URI
     * across other applications.
     *
     * @param context Host Android Context.
     * @param text The message string to share.
     * @param imageUri Optional content URI of an image captured by the camera.
     * @param chooserTitle Title to display at the top of the Android Sharesheet.
     */
    fun launchShareSheet(
        context: Context,
        text: String,
        imageUri: Uri? = null,
        chooserTitle: String = "Share Message"
    ) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            if (imageUri != null) {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                clipData = ClipData.newUri(context.contentResolver, "Shared Image", imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }

            if (text.isNotBlank()) {
                putExtra(Intent.EXTRA_TEXT, text)
            }
        }

        val chooserIntent = Intent.createChooser(shareIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooserIntent)
    }

    /**
     * Generates a rich summary share intent showcasing the app's real-time chat capabilities.
     */
    fun shareAppDemoSummary(context: Context, activeUserName: String, messageCount: Int) {
        val summaryText = "🚀 ShareChat Demo Highlights:\n" +
                "• Active Presenter: $activeUserName\n" +
                "• Total Messages: $messageCount\n" +
                "• Powered by: Jetpack Compose, Android Sharesheet, Firebase & Crashlytics!"

        launchShareSheet(
            context = context,
            text = summaryText,
            imageUri = null,
            chooserTitle = "Share Demo Summary"
        )
    }
}
