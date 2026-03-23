package com.dagram.app.data.repository

import com.dagram.app.data.api.DagramApi
import com.dagram.app.data.models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: DagramApi
) {
    suspend fun getChats(): Result<List<Chat>> {
        return try {
            val response = api.getChats()
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Failed to load chats", response.code())
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun createChat(targetUserId: Int): Result<Chat> {
        return try {
            val response = api.createChat(CreateChatRequest(targetUserId))
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Failed to create chat", response.code())
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun getMessages(chatId: Int, before: Int? = null): Result<List<Message>> {
        return try {
            val response = api.getMessages(chatId, before)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Failed to load messages", response.code())
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun sendMessage(chatId: Int, content: String): Result<Message> {
        return try {
            val response = api.sendMessage(chatId, SendMessageRequest(content))
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Failed to send message", response.code())
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun createGroup(name: String, description: String?, memberIds: List<Int>): Result<Chat> {
        return try {
            val response = api.createGroup(CreateGroupRequest(name, description, memberIds))
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Failed to create group", response.code())
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun createChannel(name: String, description: String?): Result<Chat> {
        return try {
            val response = api.createChannel(CreateChannelRequest(name, description))
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Failed to create channel", response.code())
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val response = api.searchUsers(query)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Search failed", response.code())
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }
}
