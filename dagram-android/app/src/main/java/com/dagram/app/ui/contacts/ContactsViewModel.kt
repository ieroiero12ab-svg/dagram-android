package com.dagram.app.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagram.app.data.models.User
import com.dagram.app.data.repository.ChatRepository
import com.dagram.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactsUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val searchQuery: String = ""
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(users = emptyList(), isLoading = false)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = chatRepository.searchUsers(query)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, users = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, users = emptyList()
                )
                else -> {}
            }
        }
    }
}
