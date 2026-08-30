package com.shahsurveyors.myapplication.ui.ops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shahsurveyors.myapplication.data.ExpenseRepository
import com.shahsurveyors.myapplication.data.StorageRepository

class ExpenseViewModelFactory(
    private val expenseRepository: ExpenseRepository,
    private val storageRepository: StorageRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(expenseRepository, storageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
