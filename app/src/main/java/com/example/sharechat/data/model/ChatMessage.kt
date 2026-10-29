package com.example.sharechat.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Entity representing a single message within a conversation.
 * Optimized for seamless Firebase Firestore serialization and deserialization.
 */
data class ChatMessage(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",

    @get:PropertyName("senderId") @set:PropertyName("senderId")
    var senderId: String = "",

    @get:PropertyName("senderName") @set:PropertyName("senderName")
    var senderName: String = "",

    @get:PropertyName("messageText") @set:PropertyName("messageText")
    var messageText: String = "",

    @get:PropertyName("imageUriString") @set:PropertyName("imageUriString")
    var imageUriString: String? = null,

    @get:PropertyName("timestamp") @set:PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Determines whether this message was sent by the provided user ID.
     */
    fun isSentByCurrentUser(currentUserId: String): Boolean {
        return this.senderId == currentUserId
    }
}
