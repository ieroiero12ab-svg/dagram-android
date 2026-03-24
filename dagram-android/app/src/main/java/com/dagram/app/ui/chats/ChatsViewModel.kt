package com.dagram.app.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagram.app.data.models.Chat
import com.dagram.app.data.models.User
import com.dagram.app.data.repository.ChatRepository
import com.dagram.app.data.repository.Result
import com.dagram.app.utils.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatsUiState(
    val isLoading: Boolean = false,
    val isCreatingChat: Boolean = false,
    val chats: List<Chat> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val wsManager: WebSocketManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsUiState())
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    init {
        loadChats()
        observeWebSocket()
    }

    fun loadChats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = chatRepository.getChats()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, chats = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun searchUsers(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, error = null)
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            when (val result = chatRepository.searchUsers(query)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isSearching = false, searchResults = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isSearching = false, searchResults = emptyList()
                )
                else -> {}
            }
        }
    }

    fun createChatWithUser(userId: Int, onSuccess: (Int) -> Unit) {
        if (_uiState.value.isCreatingChat) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingChat = true, error = null)
            when (val result = chatRepository.createChat(userId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isCreatingChat = false)
                    loadChats()
                    onSuccess(result.data.id)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(isCreatingChat = false)
                    val existingChat = _uiState.value.chats.find {
                        it.type == "direct" && it.otherUser?.id == userId
                    }
                    if (existingChat != null) {
                        onSuccess(existingChat.id)
                    } else {
                        _uiState.value = _uiState.value.copy(error = result.message)
                    }
                }
                else -> _uiState.value = _uiState.value.copy(isCreatingChat = false)
            }
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "", searchResults = emptyList(), isSearching = false
        )
    }

    private fun observeWebSocket() {
        viewModelScope.launch {
            wsManager.events.collect { event ->
                when (event.type) {
                    "new_message" -> loadChats()
                    "member_added" -> loadChats()
                }
            }
        }
    }
}
