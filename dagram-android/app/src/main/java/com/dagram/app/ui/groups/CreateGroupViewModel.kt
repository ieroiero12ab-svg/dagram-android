package com.dagram.app.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagram.app.data.models.Chat
import com.dagram.app.data.models.User
import com.dagram.app.data.repository.ChatRepository
import com.dagram.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateGroupUiState(
    val selectedUsers: List<User> = emptyList(),
    val searchResults: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdGroup: Chat? = null,
    val usernameAvailable: Boolean? = null,
    val isCheckingUsername: Boolean = false
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    fun checkUsername(username: String) {
        if (username.length < 5) {
            _uiState.value = _uiState.value.copy(usernameAvailable = null, isCheckingUsername = false)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingUsername = true)
            when (val result = chatRepository.checkGroupUsername(username)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isCheckingUsername = false,
                    usernameAvailable = result.data.available
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(isCheckingUsername = false)
                else -> {}
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            when (val result = chatRepository.searchUsers(query)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(searchResults = result.data)
                else -> {}
            }
        }
    }

    fun toggleUser(user: User) {
        val current = _uiState.value.selectedUsers.toMutableList()
        if (current.contains(user)) current.remove(user) else current.add(user)
        _uiState.value = _uiState.value.copy(selectedUsers = current)
    }

    fun createGroup(name: String, description: String?, username: String?, isPrivate: Boolean) {
        val memberIds = _uiState.value.selectedUsers.map { it.id }
        if (memberIds.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = chatRepository.createGroup(name, description, memberIds, username, isPrivate)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, createdGroup = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }
}
