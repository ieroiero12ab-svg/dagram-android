package com.dagram.app.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("dagram_prefs", Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null

    companion object {
        private const val KEY_TOKEN = "auth_token"
    }
}
