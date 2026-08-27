package com.shahsurveyors.myapplication.ui.billing

import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.BillingRepository
import com.shahsurveyors.myapplication.data.local.BankDetails
import com.shahsurveyors.myapplication.data.local.BillingDocumentEntity
import com.shahsurveyors.myapplication.data.local.BillingItemEntity
import com.shahsurveyors.myapplication.data.local.CompanyProfile
import com.shahsurveyors.myapplication.data.local.TermConditionEntity
import com.shahsurveyors.myapplication.models.DocType
import com.shahsurveyors.myapplication.network.RetrofitClient
import com.shahsurveyors.myapplication.utils.BillingDocumentGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BillingViewModel(
    private val repository: BillingRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    val companyProfile: StateFlow<CompanyProfile?> =
        repository.companyProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val bankDetails: StateFlow<BankDetails?> =
        repository.bankDetails.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allTerms: StateFlow<List<TermConditionEntity>> =
        repository.allTerms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments: StateFlow<List<BillingDocumentEntity>> =
        repository.allDocuments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveGeneratedDocument(
        data: BillingDocumentGenerator.DocumentData
    ) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Saving billing document..."
            try {
                val subTotal = data.items.sumOf { it.amount }
                val taxAmount = when {
                    data.docType == DocType.NON_GST_BILL -> 0.0
                    data.gstType == "IGST" -> subTotal * (data.gstPercentage / 100.0)
                    else -> subTotal * (data.gstPercentage / 100.0)
                }
                val document = BillingDocumentEntity(
                    docType = data.docType,
                    docNumber = data.docNumber,
                    clientName = data.clientName,
                    clientAddress = data.clientAddress,
                    clientGstin = data.clientGstin,
                    date = data.date,
                    gstType = data.gstType,
                    gstPercentage = data.gstPercentage,
                    subTotal = subTotal,
                    taxAmount = taxAmount,
                    grandTotal = subTotal + taxAmount,
                    status = "DRAFT",
                    termsAndConditions = data.terms.joinToString("\n")
                )
                repository.saveDocument(document, data.items)
                statusMessage = "Billing document saved successfully."
            } catch (e: Exception) {
                statusMessage = "Save failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isLoading = false
            }
        }
    }

    fun updatePaymentStatus(
        document: BillingDocumentEntity,
        paymentStatus: String,
        paidAmount: Double = document.paidAmount
    ) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Updating payment status..."
            try {
                val safePaid = paidAmount.coerceIn(0.0, document.grandTotal)
                repository.updateDocument(
                    document.copy(
                        paymentStatus = paymentStatus,
                        paidAmount = safePaid
                    ),
                    repository.getItemsForDocument(document.id).first()
                )
                statusMessage = "Payment status updated successfully."
            } catch (e: Exception) {
                statusMessage = "Payment update failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isLoading = false
            }
        }
    }

    fun convertQuotationToInvoice(quotation: BillingDocumentEntity) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Converting quotation..."
            try {
                val items = repository.getItemsForDocument(quotation.id).first()
                if (items.isEmpty()) {
                    statusMessage = "Quotation has no billing items."
                    return@launch
                }
                val invoice = quotation.copy(
                    id = 0,
                    docType = DocType.TAX_INVOICE,
                    docNumber = repository.getNextDocNumber(DocType.TAX_INVOICE),
                    date = System.currentTimeMillis(),
                    referenceDocNumber = quotation.docNumber,
                    status = "DRAFT",
                    paymentStatus = "UNPAID",
                    paidAmount = 0.0
                )
                repository.saveDocument(invoice, items)
                statusMessage = "Quotation converted to Invoice successfully."
            } catch (e: Exception) {
                statusMessage = "Conversion failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isLoading = false
            }
        }
    }

    fun uploadAndSyncDoc(file: File, clientName: String, docType: String) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Preparing PDF for upload..."
            try {
                if (!file.exists()) {
                    statusMessage = "PDF file not found."
                    return@launch
                }
                val bytes = withContext(Dispatchers.IO) { file.readBytes() }
                if (bytes.isEmpty()) {
                    statusMessage = "PDF file is empty."
                    return@launch
                }
                val base64 = withContext(Dispatchers.Default) {
                    Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
                statusMessage = "Uploading PDF..."
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.handleAction(
                        mapOf(
                            "action" to "UPLOAD_BILLING_DOC",
                            "client" to clientName,
                            "docType" to docType,
                            "fileData" to base64,
                            "fileName" to file.name
                        )
                    )
                }
                statusMessage = if (response.status?.equals("SUCCESS", ignoreCase = true) == true) {
                    response.message?.ifBlank { "PDF uploaded successfully." } ?: "PDF uploaded successfully."
                } else {
                    response.message?.ifBlank { "PDF upload failed." } ?: "PDF upload failed."
                }
            } catch (e: Exception) {
                statusMessage = "Sync failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun getNextDocumentNumber(docType: DocType): String = repository.getNextDocNumber(docType)

    fun clearStatus() {
        statusMessage = null
    }
}
