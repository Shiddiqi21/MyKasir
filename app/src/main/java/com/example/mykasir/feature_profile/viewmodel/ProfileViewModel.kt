package com.example.mykasir.feature_profile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykasir.core_data.local.TokenManager
import com.example.mykasir.core_data.remote.RetrofitClient
import com.example.mykasir.core_data.remote.UserData
import com.example.mykasir.core_ui.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// State untuk UI Profile
sealed interface ProfileUiState {
    object Idle : ProfileUiState
    object Loading : ProfileUiState
    data class Success(val userData: UserData) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

// State untuk Update Profile
sealed interface UpdateProfileState {
    object Idle : UpdateProfileState
    object Loading : UpdateProfileState
    object Success : UpdateProfileState
    data class Error(val message: String) : UpdateProfileState
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(application)

    private val _profileUiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateState: StateFlow<UpdateProfileState> = _updateState.asStateFlow()

    /**
     * Load user profile dari API
     */
    fun loadProfile() {
        Log.d("ProfileViewModel", "Loading profile...")
        _profileUiState.value = ProfileUiState.Loading

        viewModelScope.launch {
            try {
                val token = tokenManager.getAuthHeader()
                Log.d("ProfileViewModel", "Token: $token")

                val response = apiService.getProfile(token)
                Log.d("ProfileViewModel", "Profile loaded: ${response.data}")

                if (response.status == "success" && response.data != null) {
                    // Simpan data user ke local storage
                    tokenManager.saveUserInfo(response.data.email, response.data.name)
                    tokenManager.saveUserId(response.data.id)
                    tokenManager.saveUserRole(response.data.role)

                    _profileUiState.value = ProfileUiState.Success(response.data)
                } else {
                    _profileUiState.value = ProfileUiState.Error(
                        response.message ?: "Gagal memuat profil"
                    )
                }
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Sesi Anda telah berakhir. Silakan login kembali"
                    404 -> "Profil tidak ditemukan"
                    500 -> "Server error, coba lagi nanti"
                    else -> "Error: ${e.code()} - ${e.message}"
                }
                Log.e("ProfileViewModel", "HTTP Error: $errorMsg", e)
                _profileUiState.value = ProfileUiState.Error(errorMsg)
            } catch (e: IOException) {
                val errorMsg = "Tidak dapat terhubung ke server"
                Log.e("ProfileViewModel", "Network Error: $errorMsg", e)
                _profileUiState.value = ProfileUiState.Error(errorMsg)
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message ?: "Unknown Error"}"
                Log.e("ProfileViewModel", "Unknown Error: $errorMsg", e)
                _profileUiState.value = ProfileUiState.Error(errorMsg)
            }
        }
    }

    /**
     * Update profile user (saat ini hanya nama)
     */
    /**
     * Update profile user (nama dan optional password)
     */
    fun updateProfile(name: String, password: String? = null) {
        Log.d("ProfileViewModel", "Updating profile. Name: $name, Pwd changed: ${password != null}")
        _updateState.value = UpdateProfileState.Loading

        viewModelScope.launch {
            try {
                val token = tokenManager.getAuthHeader()
                
                val updates = mutableMapOf("name" to name)
                if (!password.isNullOrEmpty()) {
                    updates["password"] = password
                }
                
                val response = apiService.updateProfile(token, updates)

                if (response.status == "success") {
                    // Update local storage
                    tokenManager.saveUserInfo(
                        tokenManager.getUserEmail() ?: "",
                        name
                    )
                    _updateState.value = UpdateProfileState.Success
                    // Tampilkan notifikasi profil diperbarui
                    NotificationHelper.showProfileUpdatedNotification(getApplication())
                    // Reload profile untuk update UI
                    loadProfile()
                } else {
                    _updateState.value = UpdateProfileState.Error(
                        response.message ?: "Gagal update profil"
                    )
                }
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Sesi Anda telah berakhir"
                    404 -> "User tidak ditemukan"
                    500 -> "Server error, coba lagi nanti"
                    else -> "Error: ${e.code()}"
                }
                Log.e("ProfileViewModel", "HTTP Error: $errorMsg", e)
                _updateState.value = UpdateProfileState.Error(errorMsg)
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Unknown Error"
                Log.e("ProfileViewModel", "Error: $errorMsg", e)
                _updateState.value = UpdateProfileState.Error(errorMsg)
            }
        }
    }

    /**
     * Reset update state ke idle
     */
    fun resetUpdateState() {
        _updateState.value = UpdateProfileState.Idle
    }

    /**
     * Logout - clear semua data dan token
     */
    fun logout() {
        Log.d("ProfileViewModel", "Logging out...")
        tokenManager.clearToken()
        _profileUiState.value = ProfileUiState.Idle
    }

    /**
     * Get cached user data dari local storage
     */
    fun getCachedUserData(): UserData? {
        val email = tokenManager.getUserEmail()
        val name = tokenManager.getUserName()
        val id = tokenManager.getUserId()
        val role = tokenManager.getUserRole()
        val storeId = tokenManager.getStoreId()
        val storeName = tokenManager.getStoreName()

        return if (email != null && name != null && id != null && role != null && storeId != null) {
            UserData(id, email, name, role, storeId, storeName)
        } else {
            null
        }
    }
}
