package com.example.mykasir.feature_profile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykasir.core_data.local.TokenManager
import com.example.mykasir.core_data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileData(
    val name: String,
    val email: String,
    val role: String,
    val storeName: String,
    val isOwner: Boolean
)

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val profile: ProfileData) : ProfileUiState
    object LoggedOut : ProfileUiState
}

sealed interface UpdateProfileState {
    object Idle : UpdateProfileState
    object Loading : UpdateProfileState
    data class Success(val message: String) : UpdateProfileState
    data class Error(val message: String) : UpdateProfileState
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenManager = TokenManager(application)
    private val apiService = RetrofitClient.apiService
    
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    private val _updateState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateState: StateFlow<UpdateProfileState> = _updateState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        val email = tokenManager.getUserEmail()
        val name = tokenManager.getUserName()
        val role = tokenManager.getUserRole()
        val storeName = tokenManager.getStoreName()
        val isOwner = tokenManager.isOwner()
        
        if (email != null && name != null) {
            _uiState.value = ProfileUiState.Success(
                ProfileData(
                    name = name,
                    email = email,
                    role = when (role) {
                        "owner" -> "Pemilik Toko"
                        "cashier" -> "Kasir"
                        else -> role ?: "Kasir"
                    },
                    storeName = storeName ?: "Toko Saya",
                    isOwner = isOwner
                )
            )
        } else {
            // Fallback jika tidak ada data tersimpan
            _uiState.value = ProfileUiState.Success(
                ProfileData(
                    name = "Pengguna",
                    email = email ?: "email@example.com",
                    role = "Kasir",
                    storeName = "Toko Saya",
                    isOwner = false
                )
            )
        }
    }
    
    fun updateProfile(
        name: String? = null,
        storeName: String? = null,
        oldPassword: String? = null,
        newPassword: String? = null
    ) {
        viewModelScope.launch {
            try {
                _updateState.value = UpdateProfileState.Loading
                
                val token = tokenManager.getToken()
                if (token == null) {
                    _updateState.value = UpdateProfileState.Error("Token tidak ditemukan")
                    return@launch
                }
                
                Log.d("ProfileViewModel", "Updating profile: name=$name, storeName=$storeName, hasPassword=${newPassword != null}")
                
                val response = apiService.updateProfile(
                    token = "Bearer $token",
                    name = name,
                    storeName = storeName,
                    oldPassword = oldPassword,
                    newPassword = newPassword
                )
                
                if (response.status == "success" && response.data != null) {
                    // Update local storage
                    tokenManager.saveUserInfo(
                        email = response.data.email,
                        name = response.data.name,
                        role = response.data.role,
                        storeId = response.data.storeId,
                        storeName = response.data.storeName ?: ""
                    )
                    
                    // Reload profile
                    loadProfile()
                    
                    _updateState.value = UpdateProfileState.Success(response.message ?: "Profil berhasil diperbarui")
                    Log.d("ProfileViewModel", "Profile updated successfully")
                } else {
                    _updateState.value = UpdateProfileState.Error(response.message ?: "Gagal memperbarui profil")
                    Log.e("ProfileViewModel", "Update failed: ${response.message}")
                }
                
            } catch (e: retrofit2.HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Password lama tidak sesuai"
                    403 -> "Anda tidak memiliki akses untuk mengubah profil"
                    else -> "Error: ${e.message()}"
                }
                _updateState.value = UpdateProfileState.Error(errorMsg)
                Log.e("ProfileViewModel", "HTTP Error: $errorMsg", e)
            } catch (e: Exception) {
                _updateState.value = UpdateProfileState.Error(e.message ?: "Terjadi kesalahan")
                Log.e("ProfileViewModel", "Error updating profile", e)
            }
        }
    }
    
    fun resetUpdateState() {
        _updateState.value = UpdateProfileState.Idle
    }
    
    fun logout() {
        tokenManager.clearToken()
        _uiState.value = ProfileUiState.LoggedOut
    }
}

