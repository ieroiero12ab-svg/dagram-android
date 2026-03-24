package com.dagram.app.utils

import android.content.Context
import com.dagram.app.data.models.Message
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class WebSocketEvent(
    val type: String,
    val message: Message? = null,
    val userId: Int? = null,
    val isOnline: Boolean? = null,
    val chatId: Int? = null
)

@Singleton
class WebSocketManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager
) {
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .build()

    private val _events = MutableSharedFlow<WebSocketEvent>(extraBufferCapacity = 100)
    val events: SharedFlow<WebSocketEvent> = _events

    private val joinedChats = mutableSetOf<Int>()

    fun connect(serverUrl: String) {
        val token = tokenManager.getToken() ?: return
        val wsUrl = serverUrl
            .replace("https://", "wss://")
            .replace("http://", "ws://")
            .replace("/api/", "/ws")
            .let { if (!it.endsWith("/ws")) "$it/ws" else it }

        val request = Request.Builder()
            .url("$wsUrl?token=$token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                joinedChats.forEach { chatId ->
                    ws.send("""{"type":"join_chat","chatId":$chatId}""")
                }
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val json = JsonParser.parseString(text).asJsonObject
                    val type = json.get("type")?.asString ?: return

                    when (type) {
                        "new_message" -> {
                            val msgJson = json.getAsJsonObject("message")
                            val msg = gson.fromJson(msgJson, Message::class.java)
                            _events.tryEmit(WebSocketEvent("new_message", message = msg))
                        }
                        "user_status" -> {
                            val userId = json.get("userId")?.asInt
                            val isOnline = json.get("isOnline")?.asBoolean
                            _events.tryEmit(WebSocketEvent("user_status", userId = userId, isOnline = isOnline))
                        }
                        "member_added" -> {
                            val chatId = json.get("chatId")?.asInt
                            _events.tryEmit(WebSocketEvent("member_added", chatId = chatId))
                        }
                        "connected" -> {
                            joinedChats.forEach { chatId ->
                                ws.send("""{"type":"join_chat","chatId":$chatId}""")
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                _events.tryEmit(WebSocketEvent("disconnected"))
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                _events.tryEmit(WebSocketEvent("disconnected"))
            }
        })
    }

    fun joinChat(chatId: Int) {
        joinedChats.add(chatId)
        webSocket?.send("""{"type":"join_chat","chatId":$chatId}""")
    }

    fun leaveChat(chatId: Int) {
        joinedChats.remove(chatId)
        webSocket?.send("""{"type":"leave_chat","chatId":$chatId}""")
    }

    fun disconnect() {
        webSocket?.close(1000, "Closing")
        webSocket = null
    }

    fun ping() {
        webSocket?.send("""{"type":"ping"}""")
    }
}
