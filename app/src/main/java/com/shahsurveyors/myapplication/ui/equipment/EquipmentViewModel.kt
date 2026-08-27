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
    var errorMessage by mutableStateOf<String?>(null)
    val equipmentList = mutableStateListOf<EquipmentModel>()

    fun fetchEquipment() {
        viewModelScope.launch {
            isLoading = true
            try {
                val list = repository.getAllEquipment()
                equipmentList.clear()
                equipmentList.addAll(list)
            } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to load equipment" }
            finally { isLoading = false }
        }
    }

    fun addEquipment(name: String, modelNumber: String, serialNumber: String, category: String, onDone: () -> Unit) {
        val cleanName = name.trim(); val cleanSerial = serialNumber.trim()
        if (cleanName.isBlank() || cleanSerial.isBlank()) { errorMessage = "Equipment name and serial number are required"; return }
        viewModelScope.launch {
            isLoading = true; errorMessage = null
            try {
                if (equipmentList.any { it.serialNumber.trim().equals(cleanSerial, ignoreCase = true) }) { errorMessage = "This serial number already exists"; return@launch }
                repository.saveEquipment(EquipmentModel(name = cleanName, modelNumber = modelNumber.trim(), serialNumber = cleanSerial, category = category.trim(), status = "AVAILABLE"))
                fetchEquipment(); onDone()
            } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to save equipment" }
            finally { isLoading = false }
        }
    }

    fun updateStatus(id: String, status: String, uid: String?, name: String?) {
        viewModelScope.launch { try { repository.updateEquipmentStatus(id, status, uid, name); fetchEquipment() } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to update equipment" } }
    }

    fun clearError() { errorMessage = null }
}
