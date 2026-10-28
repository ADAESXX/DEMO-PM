package com.example.sharechat.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Immutable entity representing a single message within a conversation.
 * Compatible with Firebase Firestore serialization.
 */
data class ChatMessage(
    @get:PropertyName("id")
    val id: String = "",

    @get:PropertyName("senderId")
    val senderId: String = "",

    @get:PropertyName("senderName")
    val senderName: String = "",

    @get:PropertyName("messageText")
    val messageText: String = "",

    @get:PropertyName("imageUriString")
    val imageUriString: String? = null,

    @get:PropertyName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Determines whether this message was sent by the provided user ID.
     */
    fun isSentByCurrentUser(currentUserId: String): Boolean {
        return this.senderId == currentUserId
    }
}
