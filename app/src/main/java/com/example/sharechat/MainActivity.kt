package com.example.sharechat

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.sharechat.ui.chat.ChatScreen
import com.example.sharechat.ui.chat.ChatUiEvent
import com.example.sharechat.ui.chat.ChatViewModel
import com.example.sharechat.ui.theme.ShareChatTheme
import com.example.sharechat.util.CrashlyticsHelper

/**
 * Main entry activity handling incoming Android Sharesheet intents and rendering Compose UI.
 */
class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming Android Sharesheet content if launched from another app
        handleIncomingShareIntent(intent)

        setContent {
            ShareChatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChatScreen(viewModel = chatViewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingShareIntent(intent)
    }

    /**
     * Inspects incoming intents to process text or images received through Android Sharesheet.
     */
    private fun handleIncomingShareIntent(intent: Intent?) {
        if (intent == null || intent.action != Intent.ACTION_SEND) {
            return
        }

        CrashlyticsHelper.logEvent("Received incoming Android Sharesheet intent")

        // Handle incoming text from Sharesheet
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
            chatViewModel.onEvent(ChatUiEvent.OnInputMessageChanged(sharedText))
        }

        // Handle incoming image from Sharesheet
        val imageUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        }

        imageUri?.let { uri ->
            chatViewModel.onEvent(ChatUiEvent.OnPhotoCaptured(uri))
        }
    }
}
