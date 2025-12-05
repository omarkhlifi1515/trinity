package com.example.mobiletrinity.network

import com.example.mobiletrinity.api.AgentApiService
import com.example.mobiletrinity.api.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL_WEB = "https://trinity-web-04bi.onrender.com/"
    private const val BASE_URL_AGENT = "https://trinity-agent.onrender.com/"

    val webApi: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_WEB)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val agentApi: AgentApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_AGENT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AgentApiService::class.java)
    }
}
