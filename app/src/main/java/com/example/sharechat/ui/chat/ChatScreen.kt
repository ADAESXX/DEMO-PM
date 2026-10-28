package com.example.sharechat.ui.chat

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sharechat.data.model.ChatUser
import com.example.sharechat.ui.components.AdaptiveChatScaffold
import com.example.sharechat.ui.components.ChatBubble
import com.example.sharechat.ui.components.MessageInputBar
import com.example.sharechat.ui.theme.StatusOnlineGreen
import com.example.sharechat.util.CameraUtils
import com.example.sharechat.util.ShareSheetHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Root Composable orchestrating chat messaging, camera capture runtime permissions,
 * Android Sharesheet interaction, and adaptive responsiveness.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var isUserMenuExpanded by remember { mutableStateOf(false) }

    // Camera Picture Capture Contract
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingPhotoUri?.let { uri ->
                viewModel.onEvent(ChatUiEvent.OnPhotoCaptured(uri))
            }
        }
    }

    // Camera Runtime Permission Request Contract
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onEvent(ChatUiEvent.OnCameraPermissionResult(isGranted))
        if (isGranted) {
            val photoUri = CameraUtils.createTempImageUri(context)
            pendingPhotoUri = photoUri
            viewModel.onEvent(ChatUiEvent.OnSetPendingCaptureUri(photoUri))
            takePictureLauncher.launch(photoUri)
        } else {
            Toast.makeText(context, "Camera permission was denied.", Toast.LENGTH_SHORT).show()
        }
    }

    val launchCameraCapture: () -> Unit = {
        if (CameraUtils.hasCameraPermission(context)) {
            val photoUri = CameraUtils.createTempImageUri(context)
            pendingPhotoUri = photoUri
            viewModel.onEvent(ChatUiEvent.OnSetPendingCaptureUri(photoUri))
            takePictureLauncher.launch(photoUri)
        } else {
            cameraPermissionLauncher.launch(CameraUtils.CAMERA_PERMISSION)
        }
    }

    // Observe side effects for Android Sharesheet invocations
    LaunchedEffect(Unit) {
        viewModel.shareSheetActionFlow.collectLatest { action ->
            ShareSheetHelper.launchShareSheet(
                context = context,
                text = action.text,
                imageUri = action.imageUri,
                chooserTitle = action.title
            )
        }
    }

    // Auto-scroll to latest message on receive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Surface info or error messages
    LaunchedEffect(uiState.infoMessage, uiState.errorMessage) {
        val message = uiState.infoMessage ?: uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(ChatUiEvent.OnDismissInfoMessage)
        }
    }

    AdaptiveChatScaffold(
        uiState = uiState,
        onUserSelected = { user -> viewModel.onEvent(ChatUiEvent.OnSwitchUser(user)) },
        onTriggerCrashClicked = { viewModel.onEvent(ChatUiEvent.OnTriggerTestCrash) },
        onShareSummaryClicked = {
            ShareSheetHelper.shareAppDemoSummary(
                context = context,
                activeUserName = uiState.currentUser.displayName,
                messageCount = uiState.messages.size
            )
        },
        onCameraTriggerClicked = launchCameraCapture,
        chatContent = {
            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape),
                                    color = StatusOnlineGreen
                                ) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "ShareChat Demo",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Active: ${uiState.currentUser.displayName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        actions = {
                            // Quick Android Sharesheet summary trigger
                            IconButton(onClick = {
                                ShareSheetHelper.shareAppDemoSummary(
                                    context = context,
                                    activeUserName = uiState.currentUser.displayName,
                                    messageCount = uiState.messages.size
                                )
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share demo summary"
                                )
                            }

                            // User switcher menu
                            Box {
                                IconButton(onClick = { isUserMenuExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Menu options"
                                    )
                                }

                                DropdownMenu(
                                    expanded = isUserMenuExpanded,
                                    onDismissRequest = { isUserMenuExpanded = false }
                                ) {
                                    Text(
                                        text = "Switch Participant",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    uiState.availableUsers.forEach { user ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (user.id == uiState.currentUser.id) {
                                                        "✔ ${user.displayName}"
                                                    } else {
                                                        user.displayName
                                                    }
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.SwapHoriz,
                                                    contentDescription = null
                                                )
                                            },
                                            onClick = {
                                                viewModel.onEvent(ChatUiEvent.OnSwitchUser(user))
                                                isUserMenuExpanded = false
                                            }
                                        )
                                    }

                                    DropdownMenuItem(
                                        text = { Text("Trigger Crashlytics Crash") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.BugReport,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            isUserMenuExpanded = false
                                            viewModel.onEvent(ChatUiEvent.OnTriggerTestCrash)
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        )
                    )
                },
                bottomBar = {
                    MessageInputBar(
                        inputText = uiState.inputMessage,
                        attachedImageUri = uiState.selectedImageUri,
                        isSending = uiState.isSending,
                        onTextChanged = { text ->
                            viewModel.onEvent(ChatUiEvent.OnInputMessageChanged(text))
                        },
                        onSendClicked = {
                            viewModel.onEvent(ChatUiEvent.OnSendMessage)
                        },
                        onCameraClicked = launchCameraCapture,
                        onShareDraftClicked = {
                            viewModel.onEvent(ChatUiEvent.OnShareCurrentDraftViaSheet)
                        },
                        onRemovePhotoClicked = {
                            viewModel.onEvent(ChatUiEvent.OnRemovePhoto)
                        }
                    )
                },
                modifier = modifier.fillMaxSize()
            ) { paddingValues ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(vertical = 8.dp)
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { message ->
                        ChatBubble(
                            message = message,
                            isCurrentUser = message.isSentByCurrentUser(uiState.currentUser.id),
                            onShareClicked = { selectedMessage ->
                                viewModel.onEvent(ChatUiEvent.OnShareMessageViaSheet(selectedMessage))
                            }
                        )
                    }
                }
            }
        }
    )
}
