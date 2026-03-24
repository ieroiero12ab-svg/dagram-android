package com.dagram.app.ui.channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagram.app.data.models.Chat
import com.dagram.app.data.repository.ChatRepository
import com.dagram.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateChannelUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val createdChannel: Chat? = null
)

@HiltViewModel
class CreateChannelViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateChannelUiState())
    val uiState: StateFlow<CreateChannelUiState> = _uiState.asStateFlow()

    fun createChannel(name: String, description: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = chatRepository.createChannel(name, description)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, createdChannel = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }
}
