package com.shahsurveyors.myapplication.ui.ops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shahsurveyors.myapplication.data.DSRRepository
import com.shahsurveyors.myapplication.data.StorageRepository

class DSRViewModelFactory(
    private val dsrRepository: DSRRepository,
    private val storageRepository: StorageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DSRViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DSRViewModel(dsrRepository, storageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
