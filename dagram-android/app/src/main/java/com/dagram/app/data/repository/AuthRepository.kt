package com.dagram.app.data.repository

import com.dagram.app.data.api.DagramApi
import com.dagram.app.data.models.*
import com.dagram.app.utils.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int = 0) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val api: DagramApi,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val auth = response.body()!!
                tokenManager.saveToken(auth.token)
                Result.Success(auth)
            } else {
                Result.Error("Invalid email or password", response.code())
            }
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun register(email: String, password: String, username: String, displayName: String): Result<AuthResponse> {
        return try {
            val response = api.register(RegisterRequest(email, password, username, displayName))
            if (response.isSuccessful) {
                val auth = response.body()!!
                tokenManager.saveToken(auth.token)
                Result.Success(auth)
            } else {
                when (response.code()) {
                    409 -> Result.Error("Email or username already exists", 409)
                    400 -> Result.Error("Please check your input", 400)
                    else -> Result.Error("Registration failed", response.code())
                }
            }
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun getMe(): Result<User> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to get user", response.code())
            }
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun updateProfile(username: String?, displayName: String?, bio: String?): Result<User> {
        return try {
            val response = api.updateProfile(UpdateProfileRequest(username, displayName, bio))
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                when (response.code()) {
                    409 -> Result.Error("Username already taken", 409)
                    400 -> Result.Error("Invalid input", 400)
                    else -> Result.Error("Failed to update profile", response.code())
                }
            }
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    suspend fun checkUsernameAvailable(username: String): Result<Boolean> {
        return try {
            val response = api.checkUsername(username)
            if (response.isSuccessful) {
                Result.Success(response.body()!!.available)
            } else {
                Result.Error("Check failed", response.code())
            }
        } catch (e: Exception) {
            Result.Error("Connection failed: ${e.message}")
        }
    }

    fun logout() {
        tokenManager.clearToken()
    }

    fun isLoggedIn() = tokenManager.isLoggedIn()
}
