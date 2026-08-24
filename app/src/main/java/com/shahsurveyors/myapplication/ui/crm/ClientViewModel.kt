package com.shahsurveyors.myapplication.ui.crm

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.ClientRepository
import com.shahsurveyors.myapplication.models.ClientModel
import kotlinx.coroutines.launch

class ClientViewModel(
    private val repository: ClientRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    val clients = mutableStateListOf<ClientModel>()

    fun fetchClients() {
        viewModelScope.launch {
            isLoading = true
            val list = repository.getAllClients()
            clients.clear()
            clients.addAll(list)
            isLoading = false
        }
    }

    fun saveClient(client: ClientModel) {
        viewModelScope.launch {
            repository.saveClient(client)
            fetchClients()
        }
    }
}
