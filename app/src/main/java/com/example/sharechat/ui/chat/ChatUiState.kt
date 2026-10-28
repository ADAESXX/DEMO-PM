package com.example.sharechat.ui.chat

import android.net.Uri
import com.example.sharechat.data.model.ChatMessage
import com.example.sharechat.data.model.ChatUser

/**
 * Single immutable state representation for the chat screen following Unidirectional Data Flow.
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentUser: ChatUser = ChatUser(),
    val availableUsers: List<ChatUser> = emptyList(),
    val inputMessage: String = "",
    val selectedImageUri: Uri? = null,
    val pendingCameraCaptureUri: Uri? = null,
    val isSending: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null
)
