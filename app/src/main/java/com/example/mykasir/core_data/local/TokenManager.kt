package com.example.mykasir.core_data.local

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("mykasir_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN: String = "auth_token"
        private const val KEY_EMAIL: String = "user_email"
        private const val KEY_NAME: String = "user_name"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveUserInfo(email: String, name: String) {     
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_NAME, name)
            .apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    fun getUserName(): String? {
        return prefs.getString(KEY_NAME, null)
    }

    fun clearToken() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_EMAIL)
            .remove(KEY_NAME)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun getAuthHeader(): String {
        return "Bearer ${getToken()}"
    }
}
