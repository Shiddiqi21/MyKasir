package com.example.mykasir.core_data.remote

import com.example.mykasir.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // URL Configuration
    // Development: emulator localhost
    private const val DEV_BASE_URL = "http://10.0.2.2:3000/"
    
    // Production: ganti dengan URL server production Anda (HTTPS)
    // Contoh: "https://api.mykasir.com/"
    private const val PROD_BASE_URL = "http://10.0.2.2:3000/"
    
    // Otomatis pilih URL berdasarkan build type
    private val BASE_URL: String
        get() = if (BuildConfig.DEBUG) DEV_BASE_URL else PROD_BASE_URL

    // Logging interceptor (hanya aktif di debug mode)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
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