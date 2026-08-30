package com.shahsurveyors.myapplication.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * API interface for the current Google Apps Script backend.
 *
 * NOTE:
 * This is the existing backend layer.
 * Later, when Firebase migration is completed,
 * these API calls can be replaced with Firebase services.
 */
interface WebhookApi {

    /**
     * Sends an action/request to Google Apps Script.
     *
     * Example:
     * {
     *     "action": "VERIFY_USER_LOGIN",
     *     "phone": "XXXXXXXXXX",
     *     "password": "********"
     * }
     */
    @POST("exec")
    suspend fun handleAction(
        @Body payload: Map<String, String>
    ): ApiResponse


    /**
     * Fetches data from Google Apps Script using GET.
     *
     * Example:
     * ?action=FETCH_ALL_SYNC_DATA
     */
    @GET("exec")
    suspend fun fetchData(
        @Query("action") action: String
    ): ApiResponse
}


/**
 * Standard response returned by the current backend.
 *
 * data is kept as Any? because different actions can return
 * different structures such as:
 *
 * - Map
 * - List
 * - String
 * - null
 */
data class ApiResponse(

    val status: String? = null,

    val message: String? = null,

    val data: Any? = null
)