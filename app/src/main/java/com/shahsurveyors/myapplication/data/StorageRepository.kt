package com.shahsurveyors.myapplication.data

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

class StorageRepository {

    private val client = OkHttpClient()

    private val supabaseUrl =
        "https://faozvduwjzixnpccjzua.supabase.co"

    private val supabaseKey =
        "sb_publishable_p177LuN59uxSW5ZRNZk3rg_bKxj1lT2"

    private val bucket =
        "attendance-selfies"

    suspend fun uploadBytes(
        path: String,
        bytes: ByteArray
    ): String = withContext(Dispatchers.IO) {

        val fileName =
            "${path.trimEnd('/')}/${UUID.randomUUID()}.jpg"

        val uploadUrl =
            "$supabaseUrl/storage/v1/object/$bucket/$fileName"

        val requestBody =
            bytes.toRequestBody(
                "image/jpeg".toMediaType()
            )

        val request =
            Request.Builder()
                .url(uploadUrl)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer $supabaseKey")
                .addHeader("Content-Type", "image/jpeg")
                .post(requestBody)
                .build()

        client.newCall(request).execute().use { response ->

            if (!response.isSuccessful) {
                throw Exception(
                    "Supabase upload failed: ${response.code} ${response.message}"
                )
            }
        }

        // Public bucket ka URL
        "$supabaseUrl/storage/v1/object/public/$bucket/$fileName"
    }

    suspend fun uploadFile(
        path: String,
        uri: Uri
    ): String {
        throw UnsupportedOperationException(
            "uploadFile is not used for attendance selfie"
        )
    }

    suspend fun deleteFile(url: String) {
        // Baad me zarurat ke according add karenge
    }
}