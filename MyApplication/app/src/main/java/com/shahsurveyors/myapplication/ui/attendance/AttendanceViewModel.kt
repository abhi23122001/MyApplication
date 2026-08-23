package com.shahsurveyors.myapplication.ui.attendance

import android.content.Context
import android.graphics.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.network.RetrofitClient
import com.shahsurveyors.myapplication.utils.LocationHelper
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AttendanceViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var statusMessage by mutableStateOf<String?>(null)

    fun punchAttendance(context: Context, bitmap: Bitmap, staffName: String) {
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Getting Location..."
            val location = LocationHelper.getCurrentLocation(context)
            
            statusMessage = "Adding Watermark..."
            val watermarkedBitmap = addWatermark(bitmap, staffName, location)
            
            statusMessage = "Syncing with Enterprise Sheets..."
            try {
                val response = RetrofitClient.api.handleAction(mapOf(
                    "action" to "PUNCH_ATTENDANCE",
                    "staffName" to staffName,
                    "lat" to (location?.first?.toString() ?: "N/A"),
                    "lng" to (location?.second?.toString() ?: "N/A"),
                    "image" to bitmapToBase64(watermarkedBitmap)
                ))
                statusMessage = response.message
            } catch (e: Exception) {
                statusMessage = "Failed: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun addWatermark(bitmap: Bitmap, name: String, location: Pair<Double, Double>?): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            isFakeBoldText = true
            setShadowLayer(2f, 1f, 1f, Color.BLACK)
        }
        
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val dateStr = sdf.format(Date())
        val locStr = location?.let { "Lat: ${it.first}, Lng: ${it.second}" } ?: "Location N/A"
        
        canvas.drawText(name, 40f, result.height - 140f, paint)
        canvas.drawText(dateStr, 40f, result.height - 90f, paint)
        canvas.drawText(locStr, 40f, result.height - 40f, paint)
        
        return result
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
    }
}
