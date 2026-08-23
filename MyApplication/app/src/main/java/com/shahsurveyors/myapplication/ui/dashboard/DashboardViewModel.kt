package com.shahsurveyors.myapplication.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.network.RetrofitClient
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var noticeMessage by mutableStateOf("Notice: Maintain Leica Equipment logs daily.")

    fun fetchAllSyncData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.api.handleAction(mapOf("action" to "FETCH_ALL_SYNC_DATA"))
                if (response.status == "SUCCESS") {
                    // Parse global notices, equipment status, etc.
                    val data = response.data as? Map<String, Any>
                    noticeMessage = data?.get("admin_notice") as? String ?: noticeMessage
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }
}
