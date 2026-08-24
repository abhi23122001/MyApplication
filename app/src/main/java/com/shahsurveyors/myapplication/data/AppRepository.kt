package com.shahsurveyors.myapplication.data

import com.shahsurveyors.myapplication.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository {

    private val _syncData = MutableStateFlow<Map<String, Any>?>(null)
    val syncData: StateFlow<Map<String, Any>?> = _syncData

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    suspend fun refreshAllSyncData() = withContext(Dispatchers.IO) {
        _isSyncing.value = true
        try {
            val response = RetrofitClient.api.handleAction(mapOf("action" to "FETCH_ALL_SYNC_DATA"))
            if (response.status == "SUCCESS") {
                _syncData.value = response.data as? Map<String, Any>
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun postEnterpriseAction(action: String, params: Map<String, String>): Boolean = withContext(Dispatchers.IO) {
        _isSyncing.value = true
        try {
            val payload = params.toMutableMap().apply { put("action", action) }
            val response = RetrofitClient.api.handleAction(payload)
            if (response.status == "SUCCESS") {
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
