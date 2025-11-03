package com.example.mykasir.feature_auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykasir.core_data.remote.RetrofitClient
import com.example.mykasir.feature_auth.model.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Definisikan state UI
sealed interface LoginUiState {
    object Idle : LoginUiState // Awal
    object Loading : LoginUiState // Saat tombol Login diklik
    object Success : LoginUiState // Login berhasil
    data class Error(val message: String) : LoginUiState // Login gagal
}

class LoginViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    // State internal
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    // State yang akan dibaca oleh UI
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        // 1. Set state ke Loading
        _uiState.value = LoginUiState.Loading

        // 2. Jalankan di background
        viewModelScope.launch {
            try {
                // 3. Buat request body
                val request = LoginRequest(email, password)

                // 4. Panggil API (ke Beeceptor)
                val response = apiService.login(request)

                // TODO: Simpan token (response.token) ke DataStore/SharedPreferences

                // 5. Set state ke Success
                _uiState.value = LoginUiState.Success

            } catch (e: Exception) {
                // 6. Jika gagal, set state ke Error
                _uiState.value = LoginUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}