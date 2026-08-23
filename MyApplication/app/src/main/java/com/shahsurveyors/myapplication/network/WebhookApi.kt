package com.shahsurveyors.myapplication.network

import okhttp3.MultipartBody
import retrofit2.http.*

interface WebhookApi {
    @POST("exec")
    suspend fun handleAction(@Body payload: Map<String, String>): ApiResponse
}

data class ApiResponse(
    val status: String,
    val message: String,
    val data: Any? = null
)
