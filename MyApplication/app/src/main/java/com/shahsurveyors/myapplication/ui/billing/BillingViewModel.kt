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
import com.shahsurveyors.myapplication.data.local.CompanyProfile
import com.shahsurveyors.myapplication.data.local.TermConditionEntity
import com.shahsurveyors.myapplication.models.DocType
import com.shahsurveyors.myapplication.network.RetrofitClient
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

    // =========================================================
    // COMPANY PROFILE
    // =========================================================

    val companyProfile: StateFlow<CompanyProfile?> =
        repository.companyProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // =========================================================
    // BANK DETAILS
    // =========================================================

    val bankDetails: StateFlow<BankDetails?> =
        repository.bankDetails.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // =========================================================
    // TERMS & CONDITIONS
    // =========================================================

    val allTerms: StateFlow<List<TermConditionEntity>> =
        repository.allTerms.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // =========================================================
    // BILLING HISTORY
    // =========================================================

    val allDocuments: StateFlow<List<BillingDocumentEntity>> =
        repository.allDocuments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // =========================================================
    // CONVERT QUOTATION TO TAX INVOICE
    // =========================================================

    fun convertQuotationToInvoice(
        quotation: BillingDocumentEntity
    ) {

        if (isLoading) return

        viewModelScope.launch {

            isLoading = true
            statusMessage = "Converting quotation..."

            try {

                val items =
                    repository
                        .getItemsForDocument(quotation.id)
                        .first()

                if (items.isEmpty()) {

                    statusMessage =
                        "Quotation has no billing items."

                    return@launch
                }

                val invoice =
                    quotation.copy(

                        id = 0,

                        docType =
                            DocType.TAX_INVOICE,

                        docNumber =
                            repository.getNextDocNumber(
                                DocType.TAX_INVOICE
                            ),

                        date =
                            System.currentTimeMillis(),

                        referenceDocNumber =
                            quotation.docNumber,

                        status =
                            "DRAFT"
                    )

                repository.saveDocument(
                    document = invoice,
                    items = items
                )

                statusMessage =
                    "Quotation converted to Invoice successfully."

            } catch (e: Exception) {

                statusMessage =
                    "Conversion failed: ${
                        e.localizedMessage
                            ?: "Unknown error"
                    }"

            } finally {

                isLoading = false
            }
        }
    }

    // =========================================================
    // UPLOAD PDF TO GOOGLE APPS SCRIPT / WEBHOOK
    // =========================================================

    fun uploadAndSyncDoc(
        file: File,
        clientName: String,
        docType: String
    ) {

        if (isLoading) return

        viewModelScope.launch {

            isLoading = true
            statusMessage =
                "Preparing PDF for upload..."

            try {

                // ---------------------------------------------
                // CHECK FILE
                // ---------------------------------------------

                if (!file.exists()) {

                    statusMessage =
                        "PDF file not found."

                    return@launch
                }

                // ---------------------------------------------
                // READ PDF
                // ---------------------------------------------

                val bytes =
                    withContext(Dispatchers.IO) {
                        file.readBytes()
                    }

                if (bytes.isEmpty()) {

                    statusMessage =
                        "PDF file is empty."

                    return@launch
                }

                // ---------------------------------------------
                // ENCODE PDF
                // ---------------------------------------------

                statusMessage =
                    "Encoding PDF..."

                val base64 =
                    withContext(Dispatchers.Default) {

                        Base64.encodeToString(
                            bytes,
                            Base64.NO_WRAP
                        )
                    }

                // ---------------------------------------------
                // UPLOAD
                // ---------------------------------------------

                statusMessage =
                    "Uploading PDF..."

                val response =
                    withContext(Dispatchers.IO) {

                        RetrofitClient.api.handleAction(

                            mapOf(

                                "action" to
                                        "UPLOAD_BILLING_DOC",

                                "client" to
                                        clientName,

                                "docType" to
                                        docType,

                                "fileData" to
                                        base64,

                                "fileName" to
                                        file.name
                            )
                        )
                    }

                // ---------------------------------------------
                // RESPONSE
                // ---------------------------------------------

                if (
                    response.status
                        ?.equals(
                            "SUCCESS",
                            ignoreCase = true
                        ) == true
                ) {

                    statusMessage =
                        response.message
                            ?.ifBlank {
                                "PDF uploaded successfully."
                            }
                            ?: "PDF uploaded successfully."

                } else {

                    statusMessage =
                        response.message
                            ?.ifBlank {
                                "PDF upload failed."
                            }
                            ?: "PDF upload failed."
                }

            } catch (e: Exception) {

                statusMessage =
                    "Sync failed: ${
                        e.localizedMessage
                            ?: "Unknown error"
                    }"

            } finally {

                isLoading = false
            }
        }
    }

    // =========================================================
    // CLEAR STATUS
    // =========================================================

    suspend fun getNextDocumentNumber(
        docType: DocType
    ): String {
        return repository.getNextDocNumber(docType)
    }

    fun clearStatus() {
        statusMessage = null
    }
}