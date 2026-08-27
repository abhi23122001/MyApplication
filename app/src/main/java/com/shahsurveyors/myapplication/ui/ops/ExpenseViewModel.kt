package com.shahsurveyors.myapplication.ui.ops

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.shahsurveyors.myapplication.data.ExpenseRepository
import com.shahsurveyors.myapplication.data.FirebaseConstants
import com.shahsurveyors.myapplication.data.StorageRepository
import com.shahsurveyors.myapplication.models.ExpenseRecord
import com.shahsurveyors.myapplication.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf<String?>(null)
        private set
    var myExpenses by mutableStateOf<List<ExpenseRecord>>(emptyList())
        private set

    fun loadMyExpenses(uid: String) {
        if (uid.isBlank() || isLoading) return
        viewModelScope.launch {
            try {
                myExpenses = expenseRepository.getExpensesForUser(uid)
            } catch (e: Exception) {
                statusMessage = "Unable to load expense history"
            }
        }
    }

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
        if (uid.isBlank()) { statusMessage = "Please sign in again"; return }
        if (amountValue == null || amountValue <= 0) { statusMessage = "Please enter a valid amount"; return }
        if (amountValue > 10_000_000) { statusMessage = "Amount is above the allowed limit"; return }
        if (category.isBlank()) { statusMessage = "Please select a category"; return }
        if (remarks.isBlank()) { statusMessage = "Please enter remarks"; return }
        if (receiptBitmap == null) { statusMessage = "Receipt is required"; return }

        viewModelScope.launch {
            isLoading = true
            try {
                val now = Timestamp.now()
                val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now.toDate())
                val duplicateKey = buildDuplicateKey(uid, dateKey, amountValue, category, projectId, remarks)
                if (expenseRepository.hasDuplicateExpense(uid, duplicateKey)) {
                    statusMessage = "A similar expense has already been submitted"
                    return@launch
                }

                statusMessage = "Uploading receipt..."
                val bytes = withContext(Dispatchers.Default) {
                    BitmapUtils.compressBitmap(receiptBitmap, 1024, 500, 80)
                }
                val receiptUrl = storageRepository.uploadBytes(FirebaseConstants.STORAGE_RECEIPTS, bytes)

                statusMessage = "Saving expense..."
                val expense = ExpenseRecord(
                    uid = uid,
                    userName = userName,
                    projectId = projectId,
                    projectName = projectName,
                    amount = amountValue,
                    category = category.trim(),
                    description = remarks.trim(),
                    receiptUrl = receiptUrl,
                    date = now,
                    status = "PENDING",
                    paymentStatus = "UNPAID",
                    duplicateKey = duplicateKey
                )
                expenseRepository.saveExpense(expense)
                myExpenses = expenseRepository.getExpensesForUser(uid)
                statusMessage = "Expense submitted successfully"
            } catch (e: Exception) {
                statusMessage = "Unable to submit expense: ${e.localizedMessage ?: "Please try again"}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun buildDuplicateKey(
        uid: String,
        date: String,
        amount: Double,
        category: String,
        projectId: String,
        remarks: String
    ): String {
        val raw = listOf(
            uid.trim(), date, "%.2f".format(Locale.US, amount),
            category.trim().lowercase(Locale.US), projectId.trim(),
            remarks.trim().lowercase(Locale.US).replace(Regex("\\s+"), " ")
        ).joinToString("|")
        return raw.hashCode().toUInt().toString(16)
    }

    fun clearStatus() { statusMessage = null }
}
