package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.network.RetrofitClient
import kotlinx.coroutines.launch

data class PendingUser(val name: String, val email: String, val dept: String)

class AdminViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    val pendingUsers = mutableStateListOf<PendingUser>()

    fun fetchPendingUsers() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.api.handleAction(mapOf("action" to "GET_PENDING_USERS"))
                if (response.status == "SUCCESS") {
                    // Logic to parse response.data into pendingUsers
                    // For now, mocking after a successful network call
                    pendingUsers.clear()
                    pendingUsers.add(PendingUser("John Doe", "john@example.com", "SURVEY"))
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    fun approveUser(email: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                RetrofitClient.api.handleAction(mapOf(
                    "action" to "ADMIN_APPROVE_USER",
                    "email" to email,
                    "status" to "APPROVED"
                ))
                pendingUsers.removeAll { it.email == email }
            } finally {
                isLoading = false
            }
        }
    }
}
