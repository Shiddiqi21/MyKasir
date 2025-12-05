package com.example.mykasir.core_data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Ganti dengan IP lokal kamu (cek dengan ipconfig di CMD)
    // Untuk emulator Android Studio, gunakan 10.0.2.2
    // Untuk device fisik, gunakan IP komputer kamu di jaringan yang sama
    private const val BASE_URL = "http://10.0.2.2:3000/"
    
    // Alternative URLs (uncomment yang sesuai):
    // private const val BASE_URL = "http://192.168.1.X:3000/" // Ganti X dengan IP komputer kamu
    // private const val BASE_URL = "http://localhost:3000/" // Hanya jika testing di browser/postman

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log semua request & response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}