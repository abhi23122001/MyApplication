package com.shahsurveyors.myapplication.ui.billing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shahsurveyors.myapplication.data.BillingRepository

class BillingViewModelFactory(private val repository: BillingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
