package com.shahsurveyors.myapplication.ui.billing

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.util.Base64

class BillingViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var statusMessage by mutableStateOf<String?>(null)

    fun uploadAndSyncDoc(context: Context, file: File, clientName: String, docType: String) {
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Syncing PDF to Drive..."
            try {
                val bytes = withContext(Dispatchers.IO) { file.readBytes() }
                val base64 = Base64.encodeToString(bytes, Base64.DEFAULT)
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.handleAction(mapOf(
                        "action" to "UPLOAD_BILLING_DOC",
                        "client" to clientName,
                        "docType" to docType,
                        "fileData" to base64,
                        "fileName" to file.name
                    ))
                }
                statusMessage = response.message
            } catch (e: Exception) {
                statusMessage = "Sync failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
