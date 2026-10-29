package com.example.sharechat.data.repository

import android.util.Log
import com.example.sharechat.data.model.ChatMessage
import com.example.sharechat.data.model.ChatUser
import com.example.sharechat.util.CrashlyticsHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Concrete implementation of [ChatRepository] integrating Cloud Firestore
 * with reactive Flow emissions, coroutines, and local fallback resilience.
 */
class FirebaseChatRepository : ChatRepository {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (exception: Exception) {
            Log.w(TAG, "Firestore initialization fallback: ${exception.message}")
            null
        }
    }

    private val availableUsers = listOf(
        ChatUser(id = "user_alice", displayName = "Alice Chen", avatarInitials = "AC"),
        ChatUser(id = "user_bob", displayName = "Bob Martinez", avatarInitials = "BM"),
        ChatUser(id = "user_clara", displayName = "Clara Gomez", avatarInitials = "CG")
    )

    private var activeUser: ChatUser = availableUsers.first()

    // Local in-memory cache to guarantee instant demo functionality even if offline
    private val localFallbackMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "seed_1",
                senderId = "user_bob",
                senderName = "Bob Martinez",
                messageText = "Hi! Welcome to the Android Sharesheet and Firebase demo.",
                timestamp = System.currentTimeMillis() - 120_000
            ),
            ChatMessage(
                id = "seed_2",
                senderId = "user_alice",
                senderName = "Alice Chen",
                messageText = "Great! You can take a photo or share any message to other apps via Sharesheet.",
                timestamp = System.currentTimeMillis() - 60_000
            )
        )
    )

    override fun observeMessages(): Flow<List<ChatMessage>> = callbackFlow {
        val db = firestore
        if (db == null) {
            // If Firebase is unavailable, emit in-memory demo data
            val job = launch {
                localFallbackMessages.collect { messages ->
                    trySend(messages)
                }
            }
            awaitClose { job.cancel() }
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            val messagesCollection = db.collection(COLLECTION_MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)

            registration = messagesCollection.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Firestore listen failed, using fallback cache: ${error.message}")
                    CrashlyticsHelper.recordNonFatal(error)
                    trySend(localFallbackMessages.value)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val messageList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                    }
                    trySend(messageList)
                } else {
                    trySend(localFallbackMessages.value)
                }
            }
        } catch (exception: Exception) {
            Log.w(TAG, "Listener setup failed: ${exception.message}")
            trySend(localFallbackMessages.value)
        }

        awaitClose {
            registration?.remove()
        }
    }

    override suspend fun sendMessage(message: ChatMessage): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            // Update in-memory state immediately for instant UI feedback
            val updatedLocalList = localFallbackMessages.value + message
            localFallbackMessages.value = updatedLocalList

            val db = firestore
            if (db != null) {
                try {
                    db.collection(COLLECTION_MESSAGES)
                        .document(message.id)
                        .set(message)
                        .await()
                    Log.d(TAG, "Message persisted in Firestore: ${message.id}")
                } catch (firestoreException: Exception) {
                    Log.w(TAG, "Remote Firestore sync delayed: ${firestoreException.message}")
                    CrashlyticsHelper.logEvent("Failed to sync message ${message.id} to Firestore")
                    CrashlyticsHelper.recordNonFatal(firestoreException)
                }
            }
            Unit
        }
    }

    override fun getAvailableUsers(): List<ChatUser> {
        return availableUsers
    }

    override fun getCurrentUser(): ChatUser {
        return activeUser
    }

    override fun setCurrentUser(user: ChatUser) {
        activeUser = user
        CrashlyticsHelper.setUserIdentifier(user.id)
        CrashlyticsHelper.setCustomMetric("active_user_name", user.displayName)
    }

    companion object {
        private const val TAG = "FirebaseChatRepo"
        private const val COLLECTION_MESSAGES = "chat_messages"
    }
}
