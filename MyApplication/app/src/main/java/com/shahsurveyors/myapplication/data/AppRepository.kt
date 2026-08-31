package com.shahsurveyors.myapplication.data

import com.shahsurveyors.myapplication.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class AppRepository {

    private val _syncData = MutableStateFlow<Map<String, Any>?>(null)
    val syncData: StateFlow<Map<String, Any>?> = _syncData

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    suspend fun refreshAllSyncData() = withContext(Dispatchers.IO) {
        _isSyncing.value = true
        try {
            val response = RetrofitClient.api.fetchData("FETCH_ALL_SYNC_DATA")
            _syncData.value = response
        } catch (e: Exception) {
            try {
                val response = RetrofitClient.api.handleAction(mapOf("action" to "FETCH_ALL_SYNC_DATA"))
                _syncData.value = response
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun postEnterpriseAction(action: String, params: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        _isSyncing.value = true
        try {
            val payload = params.toMutableMap().apply { put("action", action) }
            val response = RetrofitClient.api.handleAction(payload)
            val status = (response["status"] as? String)?.uppercase() ?: ""
            if (status == "SUCCESS" || status == "OK" || response.isNotEmpty()) {
                refreshAllSyncData()
                return@withContext true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isSyncing.value = false
        }
        false
    }
}
