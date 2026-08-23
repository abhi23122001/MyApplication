package com.shahsurveyors.myapplication.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shahsurveyors.myapplication.data.BillingRepository

class AdminViewModelFactory(private val repository: BillingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
