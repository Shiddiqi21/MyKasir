package com.example.mykasir.feature_profil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykasir.feature_profil.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Ganti dengan API call sebenarnya
                val mockProfile = UserProfile(
                    id = "1",
                    name = "Nama Pengguna",
                    email = "user@example.com",
                    phone = "08xxxxxxxxxx",
                    address = "Alamat Pengguna",
                    joinDate = "2024-01-01"
                )
                _userProfile.value = mockProfile
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // TODO: Ganti dengan API call sebenarnya
                _userProfile.value = profile
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal update profil"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
