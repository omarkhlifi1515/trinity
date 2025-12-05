package com.trinity.mobile.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.trinity.mobile.network.TrinityApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Provides networking dependencies: Gson, OkHttpClient, Retrofit, and TrinityApi.
 * The base URL should be configured in `BuildConfig.TRINITY_BASE_URL` or replaced
 * directly when building the Retrofit instance.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, client: OkHttpClient): Retrofit {
        // Change this to your backend URL. For Android emulator use http://10.0.2.2:8000/
        val baseUrl = BuildConfig.TRINITY_BASE_URL
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideTrinityApi(retrofit: Retrofit): TrinityApi = retrofit.create(TrinityApi::class.java)
}
