package com.dagram.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagram.app.data.models.User
import com.dagram.app.data.repository.AuthRepository
import com.dagram.app.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isNewUser: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val profileUpdateSuccess: Boolean = false,
    val usernameAvailable: Boolean? = null,
    val isCheckingUsername: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var usernameCheckJob: Job? = null

    init {
        if (authRepository.isLoggedIn()) {
            loadCurrentUser()
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = authRepository.getMe()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, isLoggedIn = true, currentUser = result.data
                )
                is Result.Error -> {
                    authRepository.logout()
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = false)
                }
                else -> {}
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false, isLoggedIn = true, currentUser = result.data.user
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun register(email: String, password: String, username: String, displayName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = authRepository.register(email, password, username, displayName)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    isNewUser = true,
                    currentUser = result.data.user
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun updateProfile(username: String?, displayName: String?, bio: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, profileUpdateSuccess = false)
            when (val result = authRepository.updateProfile(username, displayName, bio)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentUser = result.data,
                    profileUpdateSuccess = true,
                    isNewUser = false
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isLoading = false, error = result.message
                )
                else -> {}
            }
        }
    }

    fun checkUsernameAvailability(username: String) {
        usernameCheckJob?.cancel()
        if (username.length < 5) {
            _uiState.value = _uiState.value.copy(usernameAvailable = null, isCheckingUsername = false)
            return
        }
        usernameCheckJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingUsername = true, usernameAvailable = null)
            delay(500)
            when (val result = authRepository.checkUsernameAvailable(username)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(
                    isCheckingUsername = false, usernameAvailable = result.data
                )
                is Result.Error -> _uiState.value = _uiState.value.copy(
                    isCheckingUsername = false, usernameAvailable = null
                )
                else -> {}
            }
        }
    }

    fun completeProfileSetup() {
        _uiState.value = _uiState.value.copy(isNewUser = false)
    }

    fun clearProfileUpdateSuccess() {
        _uiState.value = _uiState.value.copy(profileUpdateSuccess = false)
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState(isLoggedIn = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
