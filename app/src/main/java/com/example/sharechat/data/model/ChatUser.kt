package com.example.sharechat.data.model

/**
 * Represents a chat participant in the application.
 */
data class ChatUser(
    val id: String = "",
    val displayName: String = "",
    val avatarInitials: String = "",
    val isOnline: Boolean = true
)
