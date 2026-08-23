package com.shahsurveyors.myapplication.ui.ops

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.network.RetrofitClient
import com.shahsurveyors.myapplication.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpenseViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var statusMessage by mutableStateOf<String?>(null)

    fun submitClaim(amount: String, category: String, receiptBitmap: Bitmap?) {
        if (amount.isEmpty()) {
            statusMessage = "Please enter amount"
            return
        }

        viewModelScope.launch {
            isLoading = true
            statusMessage = "Processing Receipt..."
            
            try {
                val base64Image = if (receiptBitmap != null) {
                    withContext(Dispatchers.Default) {
                        val compressedBytes = BitmapUtils.compressBitmap(receiptBitmap, maxDimension = 1024, targetSizeKb = 240, quality = 70)
                        BitmapUtils.toBase64(compressedBytes)
                    }
                } else null

                statusMessage = "Uploading Claim..."
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.api.handleAction(mutableMapOf(
                        "action" to "SUBMIT_EXPENSE_CLAIM",
                        "amount" to amount,
                        "category" to category
                    ).apply {
                        if (base64Image != null) put("image", base64Image)
                    })
                }

                statusMessage = response.message
            } catch (e: Exception) {
                statusMessage = "Upload failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }
}
