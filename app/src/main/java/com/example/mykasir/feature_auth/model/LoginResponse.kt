package com.example.mykasir.feature_auth.model

data class LoginResponse(
    val status: String,
    val email: String,
    val token: String
)