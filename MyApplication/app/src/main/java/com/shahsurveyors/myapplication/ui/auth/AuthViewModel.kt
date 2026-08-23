package com.shahsurveyors.myapplication.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.network.RetrofitClient
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var authError by mutableStateOf<String?>(null)
    var isUserLoggedIn by mutableStateOf(false)
    var userStatus by mutableStateOf("PENDING")

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            isLoading = true
            authError = null
            try {
                val response = RetrofitClient.api.handleAction(mapOf(
                    "action" to "VERIFY_USER_LOGIN",
                    "email" to email,
                    "password" to pass
                ))
                if (response.status == "SUCCESS") {
                    userStatus = response.message
                    if (userStatus == "APPROVED") {
                        isUserLoggedIn = true
                    } else {
                        authError = "User is not yet approved by Admin."
                    }
                } else {
                    authError = response.message
                }
            } catch (e: Exception) {
                authError = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun signup(name: String, email: String, pass: String, dept: String) {
        viewModelScope.launch {
            isLoading = true
            authError = null
            try {
                val response = RetrofitClient.api.handleAction(mapOf(
                    "action" to "USER_SIGNUP_REQUEST",
                    "name" to name,
                    "email" to email,
                    "password" to pass,
                    "department" to dept
                ))
                if (response.status == "SUCCESS") {
                    authError = "Registration submitted. Waiting for Admin approval."
                } else {
                    authError = response.message
                }
            } catch (e: Exception) {
                authError = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
