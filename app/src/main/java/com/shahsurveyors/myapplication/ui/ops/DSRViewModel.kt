package com.shahsurveyors.myapplication.ui.ops

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.DSRRepository
import com.shahsurveyors.myapplication.data.StorageRepository
import com.shahsurveyors.myapplication.models.DSRModel
import kotlinx.coroutines.launch

class DSRViewModel(
    private val dsrRepository: DSRRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    val dsrList = mutableStateListOf<DSRModel>()

    fun fetchDSR(projectId: String) {
        viewModelScope.launch {
            isLoading = true
            val list = dsrRepository.getDSRForProject(projectId)
            dsrList.clear()
            dsrList.addAll(list)
            isLoading = false
        }
    }

    fun submitDSR(dsr: DSRModel) {
        viewModelScope.launch {
            dsrRepository.saveDSR(dsr)
        }
    }
}
