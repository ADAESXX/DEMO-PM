package com.example.sharechat.ui.chat

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sharechat.data.model.ChatMessage
import com.example.sharechat.data.model.ChatUser
import com.example.sharechat.data.repository.ChatRepository
import com.example.sharechat.data.repository.FirebaseChatRepository
import com.example.sharechat.util.CrashlyticsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel managing chat business logic, coroutine dispatching, and unidirectional state.
 * Employs [SavedStateHandle] to guarantee draft message persistence across configuration changes.
 */
class ChatViewModel @JvmOverloads constructor(
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
    private val repository: ChatRepository = FirebaseChatRepository()
) : ViewModel() {

    private val initialDraft: String = savedStateHandle.get<String>(KEY_INPUT_DRAFT) ?: ""

    private val _uiState = MutableStateFlow(
        ChatUiState(
            currentUser = repository.getCurrentUser(),
            availableUsers = repository.getAvailableUsers(),
            inputMessage = initialDraft
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Side effect channel for launch commands such as system Sharesheet intents
    private val _shareSheetActionFlow = MutableSharedFlow<ShareAction>()
    val shareSheetActionFlow: SharedFlow<ShareAction> = _shareSheetActionFlow.asSharedFlow()

    init {
        CrashlyticsHelper.setUserIdentifier(repository.getCurrentUser().id)
        CrashlyticsHelper.setCustomMetric("initial_user", repository.getCurrentUser().displayName)

        // Observe real-time messages from repository
        viewModelScope.launch {
            repository.observeMessages().collect { messageList ->
                _uiState.update { currentState ->
                    currentState.copy(messages = messageList)
                }
            }
        }
    }

    /**
     * Centralized event handler enforcing Unidirectional Data Flow (UDF).
     */
    fun onEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.OnInputMessageChanged -> {
                savedStateHandle[KEY_INPUT_DRAFT] = event.text
                _uiState.update { it.copy(inputMessage = event.text) }
            }

            is ChatUiEvent.OnSendMessage -> {
                sendMessageInternal()
            }

            is ChatUiEvent.OnPhotoCaptured -> {
                _uiState.update { it.copy(selectedImageUri = event.uri) }
                CrashlyticsHelper.logEvent("Photo captured successfully: ${event.uri}")
            }

            is ChatUiEvent.OnRemovePhoto -> {
                _uiState.update { it.copy(selectedImageUri = null) }
            }

            is ChatUiEvent.OnSwitchUser -> {
                switchUserInternal(event.selectedUser)
            }

            is ChatUiEvent.OnShareMessageViaSheet -> {
                triggerShareMessage(event.message)
            }

            is ChatUiEvent.OnShareCurrentDraftViaSheet -> {
                triggerShareCurrentDraft()
            }

            is ChatUiEvent.OnTriggerTestCrash -> {
                CrashlyticsHelper.triggerDemoCrash()
            }

            is ChatUiEvent.OnCameraPermissionResult -> {
                _uiState.update { it.copy(hasCameraPermission = event.isGranted) }
                CrashlyticsHelper.setCustomMetric("camera_permission_granted", event.isGranted.toString())
            }

            is ChatUiEvent.OnSetPendingCaptureUri -> {
                _uiState.update { it.copy(pendingCameraCaptureUri = event.uri) }
            }

            is ChatUiEvent.OnDismissInfoMessage -> {
                _uiState.update { it.copy(infoMessage = null, errorMessage = null) }
            }
        }
    }

    private fun sendMessageInternal() {
        val currentState = _uiState.value
        val currentDraft = currentState.inputMessage.trim()
        val currentImage = currentState.selectedImageUri
        val currentUser = currentState.currentUser

        if (currentDraft.isEmpty() && currentImage == null) {
            return
        }

        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            senderId = currentUser.id,
            senderName = currentUser.displayName,
            messageText = currentDraft,
            imageUriString = currentImage?.toString(),
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSending = true) }
            val sendResult = repository.sendMessage(message)
            _uiState.update { it.copy(isSending = false) }

            if (sendResult.isSuccess) {
                savedStateHandle[KEY_INPUT_DRAFT] = ""
                _uiState.update { it.copy(inputMessage = "", selectedImageUri = null) }
                CrashlyticsHelper.logEvent("Message sent by ${currentUser.displayName}")
            } else {
                val exception = sendResult.exceptionOrNull()
                _uiState.update {
                    it.copy(errorMessage = "Failed to send message: ${exception?.localizedMessage}")
                }
                CrashlyticsHelper.recordNonFatal(exception ?: Exception("Unknown send failure"))
            }
        }
    }

    private fun switchUserInternal(user: ChatUser) {
        repository.setCurrentUser(user)
        _uiState.update {
            it.copy(
                currentUser = user,
                infoMessage = "Switched active participant to ${user.displayName}"
            )
        }
        CrashlyticsHelper.logEvent("Switched user context to: ${user.displayName}")
    }

    private fun triggerShareMessage(message: ChatMessage) {
        val imageUri = message.imageUriString?.let { Uri.parse(it) }
        viewModelScope.launch {
            _shareSheetActionFlow.emit(
                ShareAction(
                    text = "${message.senderName}: ${message.messageText}",
                    imageUri = imageUri,
                    title = "Share Chat Message"
                )
            )
        }
        CrashlyticsHelper.logEvent("Initiated Sharesheet for message id: ${message.id}")
    }

    private fun triggerShareCurrentDraft() {
        val currentState = _uiState.value
        val text = currentState.inputMessage.trim()
        val imageUri = currentState.selectedImageUri

        if (text.isEmpty() && imageUri == null) {
            _uiState.update { it.copy(infoMessage = "Enter text or take a photo before sharing") }
            return
        }

        viewModelScope.launch {
            _shareSheetActionFlow.emit(
                ShareAction(
                    text = text,
                    imageUri = imageUri,
                    title = "Share from ShareChat"
                )
            )
        }
        CrashlyticsHelper.logEvent("Initiated Sharesheet for current draft")
    }

    /**
     * Model representing a target request for launching the Android Sharesheet.
     */
    data class ShareAction(
        val text: String,
        val imageUri: Uri? = null,
        val title: String = "Share Content"
    )

    companion object {
        private const val KEY_INPUT_DRAFT = "key_input_message_draft"
    }
}
