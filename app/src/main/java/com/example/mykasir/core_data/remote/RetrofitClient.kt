package com.example.mykasir.core_data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // !! PENTING: GANTI URL INI DENGAN URL BEECEPTOR ANDA !!
    private const val BASE_URL = "https://mykasir-testing.free.beeceptor.com"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}