package com.example.mykasir.feature_auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykasir.core_data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Definisikan state UI
sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel : ViewModel() {

    private val apiService = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Fungsi ini dipanggil dari LoginScreen untuk memulai login
     */
    fun login(email: String, password: String) {
        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            try {
                // Memanggil API dengan parameter FormUrlEncoded
                val response = apiService.login(
                    email = email,
                    password = password
                )

                // TODO: Simpan token (response.token) ke DataStore/SharedPreferences

                _uiState.value = LoginUiState.Success

            } catch (e: Exception) {
                // Tangani error jika koneksi gagal atau data salah
                _uiState.value = LoginUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}