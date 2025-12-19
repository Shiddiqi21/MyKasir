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

// Definisikan state UI
sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val token: String, val email: String, val role: String) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(application)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Fungsi ini dipanggil dari LoginScreen untuk memulai login
     */
    fun login(email: String, password: String) {
        Log.d("LoginViewModel", "Attempting login for: $email")
        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Calling API...")
                // Memanggil API dengan parameter FormUrlEncoded
                val response = apiService.login(
                    email = email,
                    password = password
                )

                Log.d("LoginViewModel", "Login success: ${response.status}")

                // Ambil data dari response
                val data = response.data
                if (data != null) {
                    // Simpan token ke SharedPreferences
                    tokenManager.saveToken(data.token)
                    tokenManager.saveUserInfo(
                        email = data.email,
                        name = data.name,
                        role = data.role,
                        storeId = data.storeId,
                        storeName = data.storeName ?: ""
                    )
                    Log.d("LoginViewModel", "Token saved successfully. Role: ${data.role}")

                    _uiState.value = LoginUiState.Success(data.token, data.email, data.role)
                } else {
                    _uiState.value = LoginUiState.Error("Response data kosong")
                }

            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Email atau password salah"
                    404 -> "Server tidak ditemukan"
                    500 -> "Server error, coba lagi nanti"
                    else -> "Error: ${e.code()} - ${e.message}"
                }
                Log.e("LoginViewModel", "HTTP Error: $errorMsg", e)
                _uiState.value = LoginUiState.Error(errorMsg)
            } catch (e: IOException) {
                val errorMsg = "Tidak dapat terhubung ke server. Pastikan backend berjalan di http://10.0.2.2:3000"
                Log.e("LoginViewModel", "Network Error: $errorMsg", e)
                _uiState.value = LoginUiState.Error(errorMsg)
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message ?: "Unknown Error"}"
                Log.e("LoginViewModel", "Unknown Error: $errorMsg", e)
                _uiState.value = LoginUiState.Error(errorMsg)
            }
        }
    }
}
