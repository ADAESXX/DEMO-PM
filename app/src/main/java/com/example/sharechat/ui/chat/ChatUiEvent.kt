package com.example.sharechat.ui.chat

import android.net.Uri
import com.example.sharechat.data.model.ChatMessage
import com.example.sharechat.data.model.ChatUser

/**
 * Sealed interface representing all user intents directed to the [ChatViewModel].
 */
sealed interface ChatUiEvent {

    data class OnInputMessageChanged(val text: String) : ChatUiEvent

    data object OnSendMessage : ChatUiEvent

    data class OnPhotoCaptured(val uri: Uri) : ChatUiEvent

    data object OnRemovePhoto : ChatUiEvent

    data class OnSwitchUser(val selectedUser: ChatUser) : ChatUiEvent

    data class OnShareMessageViaSheet(val message: ChatMessage) : ChatUiEvent

    data object OnShareCurrentDraftViaSheet : ChatUiEvent

    data object OnTriggerTestCrash : ChatUiEvent

    data class OnCameraPermissionResult(val isGranted: Boolean) : ChatUiEvent

    data class OnSetPendingCaptureUri(val uri: Uri?) : ChatUiEvent

    data object OnDismissInfoMessage : ChatUiEvent
}
