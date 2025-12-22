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
        private const val KEY_USER_ID: String = "user_id"
        private const val KEY_ROLE: String = "user_role"
        private const val KEY_STORE_ID: String = "store_id"
        private const val KEY_STORE_NAME: String = "store_name"
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    // Overloaded function for LoginViewModel (with role, storeId, storeName)
    fun saveUserInfo(email: String, name: String, role: String, storeId: Int, storeName: String) {     
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_NAME, name)
            .putString(KEY_ROLE, role)
            .putInt(KEY_STORE_ID, storeId)
            .putString(KEY_STORE_NAME, storeName)
            .apply()
    }

    // Overloaded function for ProfileViewModel (only email and name)
    fun saveUserInfo(email: String, name: String) {     
        prefs.edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_NAME, name)
            .apply()
    }

    fun getStoreId(): Int? {
        val id = prefs.getInt(KEY_STORE_ID, -1)
        return if (id == -1) null else id
    }

    fun saveStoreName(storeName: String) {
        prefs.edit().putString(KEY_STORE_NAME, storeName).apply()
    }

    fun getStoreName(): String? {
        return prefs.getString(KEY_STORE_NAME, null)
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
            .remove(KEY_USER_ID)
            .remove(KEY_ROLE)
            .remove(KEY_STORE_ID)
            .remove(KEY_STORE_NAME)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun saveUserId(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): Long? {
        val id = prefs.getLong(KEY_USER_ID, -1L)
        return if (id == -1L) null else id
    }

    fun saveUserRole(role: String) {
        prefs.edit().putString(KEY_ROLE, role).apply()
    }

    fun getUserRole(): String? {
        return prefs.getString(KEY_ROLE, null)
    }

    fun getAuthHeader(): String {
        return "Bearer ${getToken()}"
    }

    fun isOwner(): Boolean {
        return getUserRole()?.lowercase() == "owner"
    }
}
