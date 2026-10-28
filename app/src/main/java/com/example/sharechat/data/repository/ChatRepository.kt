package com.example.sharechat.data.repository

import com.example.sharechat.data.model.ChatMessage
import com.example.sharechat.data.model.ChatUser
import kotlinx.coroutines.flow.Flow

/**
 * Contract defining operations for real-time messaging and user management.
 */
interface ChatRepository {

    /**
     * Emits continuous updates of conversation messages ordered by timestamp.
     */
    fun observeMessages(): Flow<List<ChatMessage>>

    /**
     * Dispatches a new message with text and/or an image to the backend store.
     */
    suspend fun sendMessage(message: ChatMessage): Result<Unit>

    /**
     * Retrieves the list of demo users available for presentation switching.
     */
    fun getAvailableUsers(): List<ChatUser>

    /**
     * Returns the currently active participant.
     */
    fun getCurrentUser(): ChatUser

    /**
     * Changes active user context to simulate multi-device conversations in demo.
     */
    fun setCurrentUser(user: ChatUser)
}
