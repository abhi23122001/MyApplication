package com.shahsurveyors.myapplication.ui.crm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.ClientRepository
import com.shahsurveyors.myapplication.models.ClientModel
import kotlinx.coroutines.launch

class ClientViewModel(private val repository: ClientRepository) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var operationMessage by mutableStateOf<String?>(null)
        private set

    val clients = mutableStateListOf<ClientModel>()

    fun fetchClients() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val list = repository.getAllClients()
                clients.clear()
                clients.addAll(list)
            } catch (e: Exception) {
                errorMessage = userMessage(e, "Unable to load clients")
            } finally {
                isLoading = false
            }
        }
    }

    fun saveClient(client: ClientModel) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            operationMessage = null
            try {
                repository.saveClient(client)
                operationMessage = "Client saved successfully"
                val list = repository.getAllClients()
                clients.clear()
                clients.addAll(list)
            } catch (e: Exception) {
                errorMessage = userMessage(e, "Unable to save client")
            } finally {
                isLoading = false
            }
        }
    }

    fun clearMessages() {
        errorMessage = null
        operationMessage = null
    }

    private fun userMessage(error: Exception, fallback: String): String {
        val message = error.message.orEmpty()
        return when {
            message.contains("PERMISSION_DENIED", ignoreCase = true) -> "You do not have permission to access client data."
            message.contains("network", ignoreCase = true) -> "Network error. Please check your internet connection."
            message.isNotBlank() -> message
            else -> fallback
        }
    }
}
