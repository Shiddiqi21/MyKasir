package com.example.mykasir.feature_auth.model

data class LoginResponse(
    val status: String,
    val data: LoginData?
)

data class LoginData(
    val id: Int,
    val email: String,
    val name: String,
    val role: String,
    val storeId: Int,
    val storeName: String?,
    val token: String
)
