package com.example.mykasir.feature_auth.viewmodel

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
import retrofit2.HttpException
import java.io.IOException

sealed interface RegisterUiState {
    object Idle : RegisterUiState
    object Loading : RegisterUiState
    data class Success(val token: String, val email: String) : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, name: String) {
        Log.d("RegisterViewModel", "Attempting register for: $email")
        
        // Validasi input
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _uiState.value = RegisterUiState.Error("Semua field harus diisi")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = RegisterUiState.Error("Format email tidak valid")
            return
        }

        if (password.length < 4) {
            _uiState.value = RegisterUiState.Error("Password minimal 4 karakter")
            return
        }

        _uiState.value = RegisterUiState.Loading

        viewModelScope.launch {
            try {
                Log.d("RegisterViewModel", "Calling register API...")
                val response = apiService.register(
                    email = email,
                    password = password,
                    name = name,
                    role = "kasir"
                )

                Log.d("RegisterViewModel", "Register success: ${response.status}")

                // Ambil token dari response.data
                val token = response.data?.token ?: ""
                val userName = response.data?.name ?: name
                
                // Simpan token ke SharedPreferences
                tokenManager.saveToken(token)
                tokenManager.saveUserInfo(email, userName)
                Log.d("RegisterViewModel", "Token saved successfully")
                
                _uiState.value = RegisterUiState.Success(token, email)

            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    400 -> "Data tidak valid. Cek email dan password"
                    409 -> "Email sudah terdaftar"
                    500 -> "Server error, coba lagi nanti"
                    else -> "Error: ${e.code()} - ${e.message}"
                }
                Log.e("RegisterViewModel", "HTTP Error: $errorMsg", e)
                _uiState.value = RegisterUiState.Error(errorMsg)
            } catch (e: IOException) {
                val errorMsg = "Tidak dapat terhubung ke server. Pastikan backend berjalan"
                Log.e("RegisterViewModel", "Network Error: $errorMsg", e)
                _uiState.value = RegisterUiState.Error(errorMsg)
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message ?: "Unknown Error"}"
                Log.e("RegisterViewModel", "Unknown Error: $errorMsg", e)
                _uiState.value = RegisterUiState.Error(errorMsg)
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}
