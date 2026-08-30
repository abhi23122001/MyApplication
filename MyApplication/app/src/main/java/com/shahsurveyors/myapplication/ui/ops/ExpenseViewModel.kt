package com.shahsurveyors.myapplication.ui.ops

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.shahsurveyors.myapplication.data.NotificationRepository
import com.shahsurveyors.myapplication.models.AppNotification
import com.shahsurveyors.myapplication.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class ExpenseViewModel(
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun submitClaim(
        amount: String,
        category: String,
        remarks: String,
        receiptBitmap: Bitmap?
    ) {
        if (amount.isBlank()) {
            statusMessage = "Please enter amount"
            return
        }

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
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    statusMessage = "Please login first"
                    return@launch
                }

                val uid = currentUser.uid

                // 1. PROCESS RECEIPT
                statusMessage = "Processing receipt..."
                var receiptUrl = ""

                if (receiptBitmap != null) {
                    val compressedBytes = withContext(Dispatchers.Default) {
                        BitmapUtils.compressBitmap(
                            receiptBitmap,
                            maxDimension = 1024,
                            targetSizeKb = 500,
                            quality = 80
                        )
                    }

                    // FIREBASE STORAGE
                    statusMessage = "Uploading receipt..."
                    val fileName = "receipt_${UUID.randomUUID()}.jpg"
                    val storageReference = storage.reference
                        .child("expense_receipts")
                        .child(uid)
                        .child(fileName)

                    storageReference.putBytes(compressedBytes).await()
                    receiptUrl = storageReference.downloadUrl.await().toString()
                }

                // 2. FIRESTORE DOCUMENT
                statusMessage = "Saving expense..."
                val expenseId = firestore.collection("expenses").document().id
                val empName = currentUser.displayName?.ifBlank { null } ?: "Staff User"

                val expenseData = hashMapOf(
                    "id" to expenseId,
                    "employeeId" to uid,
                    "employeeName" to empName,
                    "employeeEmail" to (currentUser.email ?: ""),
                    "amount" to amountValue,
                    "category" to category,
                    "remarks" to remarks,
                    "receiptUrl" to receiptUrl,
                    "status" to "PENDING",
                    "approvedBy" to "",
                    "approvedAt" to null,
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )

                firestore.collection("expenses").document(expenseId).set(expenseData).await()

                // 3. SEND ALERT NOTIFICATION TO ADMIN
                try {
                    notificationRepository.sendNotification(
                        AppNotification(
                            title = "💰 New Expense Claim: ₹ ${amountValue.toInt()}",
                            message = "$empName submitted a $category claim for '$remarks'",
                            type = "EXPENSE"
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                statusMessage = "Expense submitted successfully"

            } catch (e: Exception) {
                e.printStackTrace()
                statusMessage = "Submission failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearStatus() {
        statusMessage = null
    }
}