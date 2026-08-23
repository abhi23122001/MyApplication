package com.shahsurveyors.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shahsurveyors.myapplication.data.SessionManager

class AuthViewModelFactory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
