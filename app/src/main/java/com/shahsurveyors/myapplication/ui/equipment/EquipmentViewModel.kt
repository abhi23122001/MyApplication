package com.shahsurveyors.myapplication.ui.equipment

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.EquipmentRepository
import com.shahsurveyors.myapplication.models.EquipmentModel
import kotlinx.coroutines.launch

class EquipmentViewModel(
    private val repository: EquipmentRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    val equipmentList = mutableStateListOf<EquipmentModel>()

    fun fetchEquipment() {
        viewModelScope.launch {
            isLoading = true
            val list = repository.getAllEquipment()
            equipmentList.clear()
            equipmentList.addAll(list)
            isLoading = false
        }
    }

    fun updateStatus(id: String, status: String, uid: String?, name: String?) {
        viewModelScope.launch {
            repository.updateEquipmentStatus(id, status, uid, name)
            fetchEquipment()
        }
    }
}
