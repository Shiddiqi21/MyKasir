package com.example.mykasir.core_data.remote

import com.example.mykasir.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Event untuk token expired - akan di-observe oleh MainActivity
 */
object TokenExpiredEvent {
    private var listener: (() -> Unit)? = null
    
    fun setListener(callback: () -> Unit) {
        listener = callback
    }
    
    fun removeListener() {
        listener = null
    }
    
    fun notifyTokenExpired() {
        listener?.invoke()
    }
}

/**
 * Interceptor untuk menangani response 401 (Unauthorized/Token Expired)
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        // Jika response 401 (Unauthorized), notify app untuk logout
        if (response.code == 401) {
            TokenExpiredEvent.notifyTokenExpired()
        }
        
        return response
    }
}

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
    
    // Auth interceptor untuk handle 401
    private val authInterceptor = AuthInterceptor()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor) // Tambahkan auth interceptor
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