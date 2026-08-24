package com.shahsurveyors.myapplication.ui.ops

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.ExpenseRepository
import com.shahsurveyors.myapplication.data.FirebaseConstants
import com.shahsurveyors.myapplication.data.StorageRepository
import com.shahsurveyors.myapplication.models.ExpenseRecord
import com.shahsurveyors.myapplication.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    fun submitClaim(
        uid: String,
        userName: String,
        amount: String,
        category: String,
        remarks: String,
        receiptBitmap: Bitmap?,
        projectId: String = "",
        projectName: String = ""
    ) {
        val amountValue = amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            statusMessage = "Please enter a valid amount"
            return
        }
        if (remarks.isBlank()) {
            statusMessage = "Please enter remarks"
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                var receiptUrl = ""
                if (receiptBitmap != null) {
                    statusMessage = "Uploading receipt..."
                    val bytes = withContext(Dispatchers.Default) {
                        BitmapUtils.compressBitmap(receiptBitmap, 1024, 500, 80)
                    }
                    receiptUrl = storageRepository.uploadBytes(FirebaseConstants.STORAGE_RECEIPTS, bytes)
                }

                statusMessage = "Saving expense..."
                val expense = ExpenseRecord(
                    uid = uid,
                    userName = userName,
                    projectId = projectId,
                    projectName = projectName,
                    amount = amountValue,
                    category = category,
                    description = remarks,
                    receiptUrl = receiptUrl,
                    date = com.google.firebase.Timestamp.now(),
                    status = "PENDING"
                )
                expenseRepository.saveExpense(expense)
                statusMessage = "Expense submitted successfully"
            } catch (e: Exception) {
                statusMessage = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearStatus() {
        statusMessage = null
    }
}
