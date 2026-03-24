package com.dagram.app.data.repository

import android.content.Context
import android.net.Uri
import com.dagram.app.data.api.DagramApi
import com.dagram.app.data.models.*
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val api: DagramApi,
    @ApplicationContext private val context: Context
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

    suspend fun sendMessage(chatId: Int, content: String, type: String = "text"): Result<Message> {
        return try {
            val response = api.sendMessage(chatId, SendMessageRequest(content, type))
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Failed to send message", response.code())
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun uploadAndSendMedia(chatId: Int, uri: Uri, mediaType: String): Result<Message> {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return Result.Error("Cannot open file")
            val bytes = inputStream.readBytes()
            inputStream.close()

            val mimeType = contentResolver.getType(uri) ?: when (mediaType) {
                "image" -> "image/jpeg"
                "audio" -> "audio/mpeg"
                else -> "application/octet-stream"
            }
            val extension = when {
                mimeType.startsWith("image/") -> mimeType.substringAfter("image/")
                mimeType.startsWith("audio/") -> mimeType.substringAfter("audio/")
                else -> "bin"
            }
            val fileName = "${mediaType}_${System.currentTimeMillis()}.$extension"

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", fileName, requestBody)

            val uploadResponse = api.uploadMedia(part)
            if (!uploadResponse.isSuccessful) {
                return Result.Error("Upload failed", uploadResponse.code())
            }

            val uploadedUrl = uploadResponse.body()!!.url
            val msgResponse = api.sendMessage(chatId, SendMessageRequest(uploadedUrl, mediaType))
            if (msgResponse.isSuccessful) Result.Success(msgResponse.body()!!)
            else Result.Error("Failed to send message", msgResponse.code())
        } catch (e: Exception) {
            Result.Error("Upload failed: ${e.message}")
        }
    }

    suspend fun sendAudioMessage(chatId: Int, audioFilePath: String): Result<Message> {
        return try {
            val file = java.io.File(audioFilePath)
            if (!file.exists()) return Result.Error("Audio file not found")
            val bytes = file.readBytes()
            val requestBody = bytes.toRequestBody("audio/aac".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

            val uploadResponse = api.uploadMedia(part)
            if (!uploadResponse.isSuccessful) {
                return Result.Error("Upload failed", uploadResponse.code())
            }

            val uploadedUrl = uploadResponse.body()!!.url
            val msgResponse = api.sendMessage(chatId, SendMessageRequest(uploadedUrl, "audio"))
            if (msgResponse.isSuccessful) Result.Success(msgResponse.body()!!)
            else Result.Error("Failed to send message", msgResponse.code())
        } catch (e: Exception) {
            Result.Error("Upload failed: ${e.message}")
        }
    }

    suspend fun createGroup(name: String, description: String?, memberIds: List<Int>, username: String? = null, isPrivate: Boolean = false): Result<Chat> {
        return try {
            val response = api.createGroup(CreateGroupRequest(name, description, memberIds, username, isPrivate))
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Failed to create group", response.code())
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun createChannel(name: String, description: String?, username: String? = null, isPrivate: Boolean = false): Result<Chat> {
        return try {
            val response = api.createChannel(CreateChannelRequest(name, description, username, isPrivate))
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Failed to create channel", response.code())
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun checkGroupUsername(username: String): Result<UsernameCheckResponse> {
        return try {
            val response = api.checkUsername(username)
            if (response.isSuccessful) Result.Success(response.body()!!)
            else Result.Error("Check failed", response.code())
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
