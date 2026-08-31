package com.shahsurveyors.myapplication.network

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * API interface for the Google Apps Script Webhook / Google Sheets backend.
 */
interface WebhookApi {

    @POST("exec")
    suspend fun handleAction(
        @Body payload: Map<String, Any>
    ): Map<String, Any>

    @GET("exec")
    suspend fun fetchData(
        @Query("action") action: String
    ): Map<String, Any>

    @GET("exec")
    suspend fun fetchRawJson(
        @Query("action") action: String
    ): JsonObject
}

/**
 * Standard response structure helper.
 */
data class ApiResponse(
    val status: String? = null,
    val message: String? = null,
    val data: Any? = null
)