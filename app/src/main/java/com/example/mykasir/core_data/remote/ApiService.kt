package com.example.mykasir.core_data.remote

import com.example.mykasir.feature_auth.model.LoginRequest
import com.example.mykasir.feature_auth.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("v1/login") // Path yang Anda buat di Beeceptor
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse
}