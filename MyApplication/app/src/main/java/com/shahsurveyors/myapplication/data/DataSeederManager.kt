package com.shahsurveyors.myapplication.data

import com.shahsurveyors.myapplication.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DataSeederManager {

    suspend fun seedDemoDataIfEmpty() = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.fetchData("FETCH_ALL_SYNC_DATA")
            // If data is null or empty, seed demo data
            if (response.status == "SUCCESS" && response.data == null) {
                executeSeeding()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun executeSeeding() = withContext(Dispatchers.IO) {
        val seedPayloads = listOf(
            // Users
            mapOf("action" to "ADD_USER", "phone" to "7974831659", "password" to "admin123", "role" to "ADMIN", "access" to "ALL", "name" to "Admin Shah"),
            mapOf("action" to "ADD_USER", "phone" to "9876543210", "password" to "survey123", "role" to "SURVEYOR", "access" to "ATTENDANCE,TASKS,CALCULATOR,DSR", "name" to "John Surveyor"),
            mapOf("action" to "ADD_USER", "phone" to "9123456780", "password" to "market123", "role" to "MARKETING", "access" to "CLIENTS,TASKS,CHAT", "name" to "Sarah Marketing"),
            
            // Equipment
            mapOf("action" to "ADD_EQUIPMENT", "type" to "DGPS", "model" to "Leica GS16", "id" to "GS16_001", "site" to "NTPC Singrauli Site"),
            mapOf("action" to "ADD_EQUIPMENT", "type" to "Total Station", "model" to "Leica TS04", "id" to "TS04_U1", "site" to "Main Lab"),
            
            // Clients
            mapOf("action" to "ADD_CLIENT", "name" to "M/s Northern Coalfields Ltd", "phone" to "9425000000", "service" to "Topographical Survey", "location" to "NCL Singrauli"),
            mapOf("action" to "ADD_CLIENT", "name" to "M/s Reliance Power Sasan", "phone" to "9826000000", "service" to "Boundary Demarcation", "location" to "Sasan")
        )

        seedPayloads.forEach { payload ->
            RetrofitClient.api.handleAction(payload)
        }
    }
}
