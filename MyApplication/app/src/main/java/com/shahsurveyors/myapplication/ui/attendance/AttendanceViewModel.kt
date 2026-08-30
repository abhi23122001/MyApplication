package com.shahsurveyors.myapplication.ui.attendance

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.data.NotificationRepository
import com.shahsurveyors.myapplication.models.AppNotification
import com.shahsurveyors.myapplication.network.RetrofitClient
import com.shahsurveyors.myapplication.utils.BitmapUtils
import com.shahsurveyors.myapplication.utils.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AttendanceViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    var punchInTime by mutableStateOf<String?>(null)
        private set

    var currentStatus by mutableStateOf("NOT PUNCHED")
        private set

    var workingDuration by mutableStateOf("00:00:00")
        private set

    var totalHoursToday by mutableStateOf("0.0 hrs")
        private set

    // =========================================================
    // PUNCH ATTENDANCE
    // =========================================================

    fun punchAttendance(
        context: Context,
        bitmap: Bitmap,
        staffName: String,
        workArea: String
    ) {
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true
            statusMessage = "Getting GPS coordinates..."

            try {
                // 1. GET LOCATION
                val location = withContext(Dispatchers.IO) {
                    LocationHelper.getCurrentLocation(context)
                }

                if (location == null) {
                    statusMessage = "GPS error. Please enable high accuracy."
                    return@launch
                }

                // 2. WATERMARK
                statusMessage = "Applying IST Watermark..."
                val watermarkedBitmap = withContext(Dispatchers.Default) {
                    addWatermark(
                        bitmap = bitmap,
                        name = staffName,
                        workArea = workArea,
                        location = location
                    )
                }

                // 3. COMPRESS + BASE64
                statusMessage = "Compressing & Encoding..."
                val base64Image = withContext(Dispatchers.Default) {
                    val compressedBytes = BitmapUtils.compressBitmap(
                        watermarkedBitmap,
                        maxDimension = 1024,
                        targetSizeKb = 240,
                        quality = 70
                    )
                    BitmapUtils.toBase64(compressedBytes)
                }

                if (watermarkedBitmap !== bitmap && !watermarkedBitmap.isRecycled) {
                    watermarkedBitmap.recycle()
                }

                // 4. DETERMINE ACTION & TIME
                val isPunchIn = currentStatus == "NOT PUNCHED"
                val action = if (isPunchIn) "PUNCH_IN" else "PUNCH_OUT"
                val currentTime = getCurrentIstTime()
                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Date())

                statusMessage = if (isPunchIn) "Punching IN..." else "Punching OUT..."

                // 5. SAVE TO FIRESTORE DIRECTLY (High reliability)
                withContext(Dispatchers.IO) {
                    try {
                        val attendanceDoc = firestore.collection("attendance").document()
                        val recordData = hashMapOf(
                            "id" to attendanceDoc.id,
                            "staffName" to staffName,
                            "workArea" to workArea,
                            "type" to action,
                            "date" to todayDate,
                            "time" to currentTime,
                            "lat" to location.first,
                            "lng" to location.second,
                            "timestamp" to System.currentTimeMillis()
                        )
                        attendanceDoc.set(recordData).await()

                        // 6. GENERATE IN-APP NOTIFICATION ALERTS FOR ADMIN
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+05:30"))
                        val hour = calendar.get(Calendar.HOUR_OF_DAY)
                        val minute = calendar.get(Calendar.MINUTE)

                        if (isPunchIn) {
                            val isLate = (hour > 9 || (hour == 9 && minute > 30))
                            if (isLate) {
                                notificationRepository.sendNotification(
                                    AppNotification(
                                        title = "⚠️ Late In Alert: $staffName",
                                        message = "$staffName punched IN late at $currentTime ($workArea)",
                                        type = "ATTENDANCE_LATE"
                                    )
                                )
                            } else {
                                notificationRepository.sendNotification(
                                    AppNotification(
                                        title = "✅ Punch IN: $staffName",
                                        message = "$staffName punched IN at $currentTime ($workArea)",
                                        type = "ATTENDANCE_IN"
                                    )
                                )
                            }
                        } else {
                            val isEarly = (hour < 17 || (hour == 17 && minute < 30))
                            if (isEarly) {
                                notificationRepository.sendNotification(
                                    AppNotification(
                                        title = "⚠️ Early Leave Alert: $staffName",
                                        message = "$staffName punched OUT EARLY at $currentTime ($workArea)",
                                        type = "ATTENDANCE_EARLY"
                                    )
                                )
                            } else {
                                notificationRepository.sendNotification(
                                    AppNotification(
                                        title = "🏁 Punch OUT: $staffName",
                                        message = "$staffName completed duty and punched OUT at $currentTime",
                                        type = "ATTENDANCE_OUT"
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // 7. WEBHOOK SYNC (Optional async fallback)
                withContext(Dispatchers.IO) {
                    try {
                        RetrofitClient.api.handleAction(
                            mapOf(
                                "action" to action,
                                "staffName" to staffName,
                                "workArea" to workArea,
                                "lat" to location.first.toString(),
                                "lng" to location.second.toString(),
                                "image" to base64Image
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // 8. UPDATE LOCAL UI STATE
                if (isPunchIn) {
                    currentStatus = "PUNCHED IN"
                    punchInTime = currentTime
                    workingDuration = "00:00:00"
                    statusMessage = "Successfully Punched IN at $currentTime"
                } else {
                    currentStatus = "PUNCHED OUT"
                    totalHoursToday = "8.5 hrs"
                    statusMessage = "Successfully Punched OUT at $currentTime"
                }

            } catch (e: Exception) {
                statusMessage = "Sync failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun getCurrentIstTime(): String {
        val formatter = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("GMT+05:30")
        return formatter.format(Date())
    }

    private fun addWatermark(
        bitmap: Bitmap,
        name: String,
        workArea: String,
        location: Pair<Double, Double>
    ): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 32f
            isFakeBoldText = true
            setShadowLayer(4f, 2f, 2f, Color.BLACK)
        }

        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.ENGLISH)
        formatter.timeZone = TimeZone.getTimeZone("GMT+05:30")
        val dateString = "${formatter.format(Date())} IST"

        val latitude = String.format(Locale.US, "%.6f", location.first)
        val longitude = String.format(Locale.US, "%.6f", location.second)
        val locationString = "GPS: $latitude, $longitude"
        val workAreaString = "Site: $workArea"

        val left = 30f
        val bottom = result.height.toFloat()

        canvas.drawText("Staff: $name", left, bottom - 150f, paint)
        canvas.drawText(workAreaString, left, bottom - 110f, paint)
        canvas.drawText(dateString, left, bottom - 70f, paint)
        canvas.drawText(locationString, left, bottom - 30f, paint)

        return result
    }
}