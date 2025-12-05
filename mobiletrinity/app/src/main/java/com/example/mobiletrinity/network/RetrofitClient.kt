package com.example.mobiletrinity.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.example.mobiletrinity.Config
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // ============ HTTP CLIENT WITH LOGGING ============
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(Config.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(Config.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(Config.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()

    // ============ WEB API RETROFIT INSTANCE ============
    private val webRetrofit = Retrofit.Builder()
        .baseUrl(Config.WEB_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val webApi: WebApiService = webRetrofit.create(WebApiService::class.java)

    // ============ AGENT API RETROFIT INSTANCE ============
    private val agentRetrofit = Retrofit.Builder()
        .baseUrl(Config.AGENT_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val agentApi: AgentApiService = agentRetrofit.create(AgentApiService::class.java)
}
