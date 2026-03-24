package com.dagram.app.data.models

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val email: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val bio: String?,
    val isOnline: Boolean,
    val lastSeen: String?,
    val createdAt: String
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String,
    val displayName: String?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UpdateProfileRequest(
    val username: String? = null,
    val displayName: String? = null,
    val bio: String? = null
)

data class UsernameCheckResponse(
    val available: Boolean,
    val message: String?
)

data class Message(
    val id: Int,
    val chatId: Int,
    val senderId: Int,
    val senderName: String,
    val senderAvatar: String?,
    val content: String,
    val type: String,
    val mediaUrl: String?,
    val readBy: List<Int>,
    val createdAt: String
)

data class Chat(
    val id: Int,
    val type: String,
    val name: String?,
    val description: String?,
    val avatarUrl: String?,
    val username: String?,
    val inviteLink: String?,
    val isPrivate: Boolean,
    val memberCount: Int,
    val lastMessage: Message?,
    val unreadCount: Int,
    val createdAt: String,
    val otherUser: User?
) {
    fun getDisplayName(): String = when (type) {
        "direct" -> otherUser?.displayName ?: name ?: "Unknown"
        else -> name ?: "Unknown Chat"
    }

    fun getDisplayAvatar(): String? = when (type) {
        "direct" -> otherUser?.avatarUrl
        else -> avatarUrl
    }
}

data class CreateChatRequest(val targetUserId: Int)
data class SendMessageRequest(val content: String, val type: String = "text")
data class CreateGroupRequest(
    val name: String,
    val description: String?,
    val memberIds: List<Int>,
    val username: String? = null,
    val isPrivate: Boolean = false
)
data class CreateChannelRequest(
    val name: String,
    val description: String?,
    val username: String? = null,
    val isPrivate: Boolean = false
)
data class AddMemberRequest(val userId: Int)

data class UploadResponse(
    val url: String,
    val type: String
)

data class ErrorResponse(
    val error: String,
    val message: String?
)

data class SuccessResponse(
    val success: Boolean,
    val message: String?
)
