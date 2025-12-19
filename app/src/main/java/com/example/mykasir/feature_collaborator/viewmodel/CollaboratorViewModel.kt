package com.example.mykasir.feature_collaborator.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykasir.core_data.local.TokenManager
import com.example.mykasir.core_data.remote.RetrofitClient
import com.example.mykasir.feature_collaborator.model.Collaborator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface CollaboratorUiState {
    object Loading : CollaboratorUiState
    data class Success(val collaborators: List<Collaborator>) : CollaboratorUiState
    data class Error(val message: String) : CollaboratorUiState
}

sealed interface AddCollaboratorState {
    object Idle : AddCollaboratorState
    object Loading : AddCollaboratorState
    object Success : AddCollaboratorState
    data class Error(val message: String) : AddCollaboratorState
}

class CollaboratorViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenManager = TokenManager(application)
    private val apiService = RetrofitClient.apiService
    
    private val _uiState = MutableStateFlow<CollaboratorUiState>(CollaboratorUiState.Loading)
    val uiState: StateFlow<CollaboratorUiState> = _uiState.asStateFlow()
    
    private val _addState = MutableStateFlow<AddCollaboratorState>(AddCollaboratorState.Idle)
    val addState: StateFlow<AddCollaboratorState> = _addState.asStateFlow()
    
    init {
        loadCollaborators()
    }
    
    fun loadCollaborators() {
        _uiState.value = CollaboratorUiState.Loading
        
        viewModelScope.launch {
            try {
                val token = tokenManager.getAuthHeader()
                val response = apiService.getCollaborators(token)
                
                if (response.status == "success" && response.data != null) {
                    _uiState.value = CollaboratorUiState.Success(response.data)
                } else {
                    _uiState.value = CollaboratorUiState.Success(emptyList())
                }
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Sesi habis, silakan login ulang"
                    403 -> "Anda tidak memiliki akses"
                    else -> "Error: ${e.code()}"
                }
                Log.e("CollaboratorVM", "HTTP Error: $errorMsg", e)
                _uiState.value = CollaboratorUiState.Error(errorMsg)
            } catch (e: IOException) {
                Log.e("CollaboratorVM", "Network Error", e)
                _uiState.value = CollaboratorUiState.Error("Tidak dapat terhubung ke server")
            } catch (e: Exception) {
                Log.e("CollaboratorVM", "Error", e)
                _uiState.value = CollaboratorUiState.Error("Error: ${e.message}")
            }
        }
    }
    
    fun addCollaborator(email: String, password: String, name: String) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            _addState.value = AddCollaboratorState.Error("Semua field harus diisi")
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _addState.value = AddCollaboratorState.Error("Format email tidak valid")
            return
        }
        
        if (password.length < 4) {
            _addState.value = AddCollaboratorState.Error("Password minimal 4 karakter")
            return
        }
        
        _addState.value = AddCollaboratorState.Loading
        
        viewModelScope.launch {
            try {
                val token = tokenManager.getAuthHeader()
                val response = apiService.addCollaborator(
                    token = token,
                    email = email,
                    password = password,
                    name = name
                )
                
                if (response.status == "success") {
                    _addState.value = AddCollaboratorState.Success
                    loadCollaborators() // Refresh list
                } else {
                    _addState.value = AddCollaboratorState.Error(response.message ?: "Gagal menambah kasir")
                }
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    400 -> "Email sudah terdaftar"
                    401 -> "Sesi habis, silakan login ulang"
                    403 -> "Anda tidak memiliki akses"
                    else -> "Error: ${e.code()}"
                }
                Log.e("CollaboratorVM", "HTTP Error: $errorMsg", e)
                _addState.value = AddCollaboratorState.Error(errorMsg)
            } catch (e: IOException) {
                Log.e("CollaboratorVM", "Network Error", e)
                _addState.value = AddCollaboratorState.Error("Tidak dapat terhubung ke server")
            } catch (e: Exception) {
                Log.e("CollaboratorVM", "Error", e)
                _addState.value = AddCollaboratorState.Error("Error: ${e.message}")
            }
        }
    }
    
    fun deleteCollaborator(id: Int) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getAuthHeader()
                apiService.deleteCollaborator(token, id.toLong())
                loadCollaborators() // Refresh list
            } catch (e: Exception) {
                Log.e("CollaboratorVM", "Delete Error", e)
                // Could show error, but for now just log
            }
        }
    }
    
    fun resetAddState() {
        _addState.value = AddCollaboratorState.Idle
    }
}
