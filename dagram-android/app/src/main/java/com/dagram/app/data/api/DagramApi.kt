package com.dagram.app.data.api

import com.dagram.app.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface DagramApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<User>

    @PATCH("users/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<User>

    @GET("users/check-username")
    suspend fun checkUsername(@Query("username") username: String): Response<UsernameCheckResponse>

    @GET("users/search")
    suspend fun searchUsers(@Query("q") query: String): Response<List<User>>

    @GET("users/{userId}")
    suspend fun getUserById(@Path("userId") userId: Int): Response<User>

    @GET("chats")
    suspend fun getChats(): Response<List<Chat>>

    @POST("chats")
    suspend fun createChat(@Body request: CreateChatRequest): Response<Chat>

    @GET("chats/{chatId}/messages")
    suspend fun getMessages(
        @Path("chatId") chatId: Int,
        @Query("before") before: Int? = null,
        @Query("limit") limit: Int = 50
    ): Response<List<Message>>

    @POST("chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: Int,
        @Body request: SendMessageRequest
    ): Response<Message>

    @Multipart
    @POST("upload")
    suspend fun uploadMedia(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @POST("groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<Chat>

    @POST("groups/{groupId}/members")
    suspend fun addGroupMember(
        @Path("groupId") groupId: Int,
        @Body request: AddMemberRequest
    ): Response<SuccessResponse>

    @POST("channels")
    suspend fun createChannel(@Body request: CreateChannelRequest): Response<Chat>
}
