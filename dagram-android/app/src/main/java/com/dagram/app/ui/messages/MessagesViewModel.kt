package com.dagram.app.ui.messages

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagram.app.data.models.Message
import com.dagram.app.data.repository.ChatRepository
import com.dagram.app.data.repository.Result
import com.dagram.app.utils.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessagesUiState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val messageText: String = "",
    val error: String? = null,
    val isSending: Boolean = false
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val wsManager: WebSocketManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chatId: Int = savedStateHandle.get<Int>("chatId") ?: 0

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
        wsManager.joinChat(chatId)
        observeWebSocket()
    }

    fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = chatRepository.getMessages(chatId)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, messages = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun onMessageTextChange(text: String) {
        _uiState.value = _uiState.value.copy(messageText = text)
    }

    fun sendMessage() {
        val text = _uiState.value.messageText.trim()
        if (text.isBlank()) return

        _uiState.value = _uiState.value.copy(messageText = "", isSending = true)

        viewModelScope.launch {
            when (val result = chatRepository.sendMessage(chatId, text)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        messages = _uiState.value.messages + result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        messageText = text,
                        error = result.message
                    )
                }
                else -> {}
            }
        }
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            wsManager.events.collect { event ->
                when (event.type) {
                    "new_message" -> {
                        val msg = event.message ?: return@collect
                        if (msg.chatId == chatId) {
                            val current = _uiState.value.messages
                            if (!current.any { it.id == msg.id }) {
                                _uiState.value = _uiState.value.copy(
                                    messages = current + msg
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        wsManager.leaveChat(chatId)
    }
}
