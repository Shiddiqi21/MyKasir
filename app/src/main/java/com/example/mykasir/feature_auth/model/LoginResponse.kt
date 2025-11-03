package com.example.mykasir.feature_auth.model

data class LoginResponse(
    val token: String,
    val user: UserData
)

data class UserData(
    val id: Int,
    val name: String,
    val email: String
)