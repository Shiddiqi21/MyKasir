package com.example.mykasir.feature_profile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mykasir.core_data.local.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ProfileData(
    val name: String,
    val email: String,
    val role: String = "Kasir"
)

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val profile: ProfileData) : ProfileUiState
    object LoggedOut : ProfileUiState
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenManager = TokenManager(application)
    
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        val email = tokenManager.getUserEmail()
        val name = tokenManager.getUserName()
        
        if (email != null && name != null) {
            _uiState.value = ProfileUiState.Success(
                ProfileData(
                    name = name,
                    email = email,
                    role = "Kasir"
                )
            )
        } else {
            // Fallback jika tidak ada data tersimpan
            _uiState.value = ProfileUiState.Success(
                ProfileData(
                    name = "Pengguna",
                    email = email ?: "email@example.com",
                    role = "Kasir"
                )
            )
        }
    }
    
    fun logout() {
        tokenManager.clearToken()
        _uiState.value = ProfileUiState.LoggedOut
    }
}
