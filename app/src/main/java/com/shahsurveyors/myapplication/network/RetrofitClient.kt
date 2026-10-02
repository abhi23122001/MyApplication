package com.shahsurveyors.myapplication.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL =
        "https://us-central1-shah-surveyors-erp-652b0.cloudfunctions.net/"

    /**
     * HTTP logging is disabled.
     *
     * This prevents sensitive request/response data from
     * appearing in Android Studio Logcat.
     */
    private val loggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }

    /**
     * OkHttp client configuration.
     */
    private val client: OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(
                60,
                TimeUnit.SECONDS
            )
            .readTimeout(
                60,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                60,
                TimeUnit.SECONDS
            )
            .build()

    /**
     * Retrofit API client.
     */
    val api: WebhookApi =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .client(client)
            .build()
            .create(WebhookApi::class.java)
}