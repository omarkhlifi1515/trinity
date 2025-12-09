package com.example.smarthr_app.data.remote

import android.content.Context
import com.example.smarthr_app.BuildConfig
import com.example.smarthr_app.data.local.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    // ✅ UPDATED: Use BuildConfig for configurable API URL
    // Set this in build.gradle.kts or use environment-specific build variants
    const val BASE_URL = BuildConfig.BASE_URL

    private var dataStoreManager: DataStoreManager? = null

    fun initialize(context: Context) {
        dataStoreManager = DataStoreManager(context)
    }

    // Interceptor to automatically add Bearer token
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = runBlocking {
            dataStoreManager?.authToken?.first()
        }
        
        val newRequest = if (token != null && !originalRequest.url.encodedPath.contains("/login")) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()
        }
        
        chain.proceed(newRequest)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        val gson = com.google.gson.GsonBuilder()
            .setLenient()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
