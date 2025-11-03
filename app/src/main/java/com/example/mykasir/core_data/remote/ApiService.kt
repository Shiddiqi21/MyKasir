package com.example.mykasir.core_data.remote

import com.example.mykasir.feature_auth.model.LoginResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    // Menggunakan FormUrlEncoded sesuai API UNAND
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse
}