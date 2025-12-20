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
    data class Success(val token: String, val email: String, val role: String) : RegisterUiState
    data class Error(val message: String) : RegisterUiState
}

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, name: String, storeName: String = "") {
        Log.d("RegisterViewModel", "Attempting register for: $email")
        
        // Trim whitespace dari input
        val trimmedEmail = email.trim()
        val trimmedName = name.trim()
        
        // Validasi input
        if (trimmedEmail.isBlank() || password.isBlank() || trimmedName.isBlank()) {
            _uiState.value = RegisterUiState.Error("Semua field harus diisi")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _uiState.value = RegisterUiState.Error("Format email tidak valid")
            return
        }

        if (password.length < 8) {
            _uiState.value = RegisterUiState.Error("Password minimal 8 karakter")
            return
        }

        _uiState.value = RegisterUiState.Loading

        viewModelScope.launch {
            try {
                Log.d("RegisterViewModel", "Calling register API...")
                val response = apiService.register(
                    email = trimmedEmail.lowercase(),
                    password = password,
                    name = trimmedName,
                    storeName = storeName.ifBlank { "Toko $trimmedName" }
                )

                Log.d("RegisterViewModel", "Register success: ${response.status}")

                // Ambil data dari response
                val data = response.data
                if (data != null && data.token != null) {
                    // Simpan token ke SharedPreferences
                    tokenManager.saveToken(data.token)
                    tokenManager.saveUserInfo(
                        email = data.email,
                        name = data.name,
                        role = data.role,
                        storeId = data.storeId,
                        storeName = data.storeName ?: ""
                    )
                    Log.d("RegisterViewModel", "Token saved successfully. Role: ${data.role}")
                    
                    _uiState.value = RegisterUiState.Success(data.token, data.email, data.role)
                } else {
                    _uiState.value = RegisterUiState.Error("Response data kosong")
                }

            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    400 -> "Data tidak valid atau email sudah terdaftar"
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

