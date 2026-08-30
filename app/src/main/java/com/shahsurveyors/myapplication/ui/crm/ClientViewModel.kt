package com.shahsurveyors.myapplication.ui.crm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.shahsurveyors.myapplication.data.ClientRepository
import com.shahsurveyors.myapplication.models.ClientModel
import com.shahsurveyors.myapplication.models.ClientPaymentRecord
import kotlinx.coroutines.launch

class ClientViewModel(private val repository: ClientRepository) : ViewModel() {
    var isLoading by mutableStateOf(false); private set
    var errorMessage by mutableStateOf<String?>(null); private set
    var operationMessage by mutableStateOf<String?>(null); private set
    val clients = mutableStateListOf<ClientModel>()

    fun fetchClients() { viewModelScope.launch { isLoading=true; errorMessage=null; try { clients.clear(); clients.addAll(repository.getAllClients()) } catch(e:Exception) { errorMessage=userMessage(e,"Unable to load clients") } finally { isLoading=false } } }
    fun saveClient(client: ClientModel) { viewModelScope.launch { isLoading=true; errorMessage=null; operationMessage=null; try { repository.saveClient(client); operationMessage="Client saved successfully"; refresh() } catch(e:Exception) { errorMessage=userMessage(e,"Unable to save client") } finally { isLoading=false } } }

    fun addReceivable(clientId:String, amount:Double, onDone:()->Unit={}) { viewModelScope.launch { isLoading=true; errorMessage=null; try { repository.addReceivable(clientId,amount); operationMessage="Receivable added"; refresh(); onDone() } catch(e:Exception) { errorMessage=userMessage(e,"Unable to add receivable") } finally { isLoading=false } } }

    fun recordPayment(client:ClientModel, amount:Double, date:Timestamp, mode:String, reference:String, remark:String, onDone:()->Unit={}) { viewModelScope.launch { isLoading=true; errorMessage=null; try { repository.recordPayment(client,amount,date,mode,reference,remark); operationMessage="Payment recorded successfully"; refresh(); onDone() } catch(e:Exception) { errorMessage=userMessage(e,"Unable to record payment") } finally { isLoading=false } } }

    fun getPaymentHistory(clientId:String, onResult:(List<ClientPaymentRecord>)->Unit) { viewModelScope.launch { onResult(repository.getPaymentHistory(clientId)) } }

    private suspend fun refresh() { clients.clear(); clients.addAll(repository.getAllClients()) }
    fun clearMessages() { errorMessage=null; operationMessage=null }
    private fun userMessage(error:Exception,fallback:String):String { val m=error.message.orEmpty(); return when { m.contains("PERMISSION_DENIED",true)->"You do not have permission to access client data."; m.contains("network",true)->"Network error. Please check your internet connection."; m.isNotBlank()->m; else->fallback } }
}
