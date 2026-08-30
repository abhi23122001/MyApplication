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
import com.google.firebase.Timestamp
import com.shahsurveyors.myapplication.data.AttendanceRepository
import com.shahsurveyors.myapplication.data.FirebaseConstants
import com.shahsurveyors.myapplication.data.StorageRepository
import com.shahsurveyors.myapplication.models.AttendanceRecord
import com.shahsurveyors.myapplication.utils.BitmapUtils
import com.shahsurveyors.myapplication.utils.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AttendanceViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    // =========================================================
    // UI STATE
    // =========================================================

    var isLoading by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    var currentStatus by mutableStateOf("NOT PUNCHED")
        private set

    var punchInTime by mutableStateOf<String?>(null)
        private set


    // =========================================================
    // CHECK TODAY'S ATTENDANCE
    // =========================================================

    fun checkStatus(uid: String) {

        if (uid.isBlank()) {

            statusMessage =
                "User ID not available"

            return
        }

        viewModelScope.launch {

            try {

                val record =
                    attendanceRepository
                        .getTodayAttendance(uid)

                if (record == null) {

                    currentStatus =
                        "NOT PUNCHED"

                    punchInTime =
                        null

                } else {

                    currentStatus =
                        if (record.punchOutTime != null) {
                            "PUNCHED OUT"
                        } else {
                            "PUNCHED IN"
                        }

                    punchInTime =
                        formatTimestamp(
                            record.punchInTime
                        )
                }

            } catch (e: Exception) {

                e.printStackTrace()

                statusMessage =
                    "Unable to check attendance: ${
                        e.localizedMessage
                            ?: "Unknown error"
                    }"
            }
        }
    }


    // =========================================================
    // PUNCH IN / PUNCH OUT
    // =========================================================

    fun punchAttendance(
        context: Context,
        uid: String,
        userName: String,
        bitmap: Bitmap,
        siteName: String
    ) {

        if (isLoading) {
            return
        }

        if (uid.isBlank()) {

            statusMessage =
                "User ID is missing"

            return
        }

        if (currentStatus == "PUNCHED OUT") {

            statusMessage =
                "Today's attendance is already completed"

            return
        }

        viewModelScope.launch {

            isLoading = true

            try {

                // =================================================
                // GET GPS LOCATION
                // =================================================

                statusMessage =
                    "Getting current location..."

                val location =
                    withContext(Dispatchers.IO) {

                        LocationHelper
                            .getCurrentLocation(context)
                    }

                if (location == null) {

                    throw Exception(
                        "Unable to get GPS location. Please turn ON location."
                    )
                }

                val latitude =
                    location.first

                val longitude =
                    location.second


                // =================================================
                // PUNCH IN
                // =================================================

                if (currentStatus == "NOT PUNCHED") {

                    statusMessage =
                        "Preparing selfie..."


                    // -------------------------------------------------
                    // ADD WATERMARK
                    // -------------------------------------------------

                    val watermarkedBitmap =
                        addWatermark(
                            bitmap = bitmap,
                            name = userName,
                            site = siteName,
                            location = location
                        )


                    // -------------------------------------------------
                    // COMPRESS IMAGE
                    // -------------------------------------------------

                    statusMessage =
                        "Preparing photo..."

                    val bytes =
                        withContext(Dispatchers.Default) {

                            BitmapUtils.compressBitmap(
                                watermarkedBitmap,
                                1024,
                                240,
                                70
                            )
                        }


                    // -------------------------------------------------
                    // UPLOAD SELFIE TO FIREBASE STORAGE
                    // -------------------------------------------------

                    statusMessage =
                        "Uploading selfie..."

                    val imageUrl =
                        withContext(Dispatchers.IO) {

                            storageRepository.uploadBytes(
                                FirebaseConstants.STORAGE_SELFIES,
                                bytes
                            )
                        }


                    // -------------------------------------------------
                    // CREATE ATTENDANCE RECORD
                    // -------------------------------------------------

                    statusMessage =
                        "Saving attendance..."

                    val punchInTimestamp =
                        Timestamp.now()

                    val record =
                        AttendanceRecord(

                            uid = uid,

                            userName = userName,

                            date = getTodayDate(),

                            punchInTime =
                                punchInTimestamp,

                            punchOutTime =
                                null,

                            punchInLat =
                                latitude,

                            punchInLng =
                                longitude,

                            punchOutLat =
                                null,

                            punchOutLng =
                                null,

                            siteName =
                                siteName,

                            selfieUrl =
                                imageUrl,

                            status =
                                "PRESENT"
                        )


                    // -------------------------------------------------
                    // SAVE FIRESTORE
                    // -------------------------------------------------

                    attendanceRepository
                        .punchIn(record)


                    // -------------------------------------------------
                    // UPDATE UI
                    // -------------------------------------------------

                    currentStatus =
                        "PUNCHED IN"

                    punchInTime =
                        formatTimestamp(
                            punchInTimestamp
                        )

                    statusMessage =
                        "Punched IN successfully"


                    // =================================================
                    // PUNCH OUT
                    // =================================================

                } else if (
                    currentStatus == "PUNCHED IN"
                ) {

                    statusMessage =
                        "Saving punch OUT..."


                    attendanceRepository
                        .punchOut(
                            uid = uid,
                            lat = latitude,
                            lng = longitude
                        )


                    currentStatus =
                        "PUNCHED OUT"

                    statusMessage =
                        "Punched OUT successfully"


                } else {

                    statusMessage =
                        "Invalid attendance status"
                }


            } catch (e: Exception) {

                e.printStackTrace()

                statusMessage =
                    "Error: ${
                        e.localizedMessage
                            ?: "Unable to save attendance"
                    }"

            } finally {

                isLoading = false
            }
        }
    }


    // =========================================================
    // ADD WATERMARK TO SELFIE
    // =========================================================

    private fun addWatermark(
        bitmap: Bitmap,
        name: String,
        site: String,
        location: Pair<Double, Double>
    ): Bitmap {

        val result =
            bitmap.copy(
                Bitmap.Config.ARGB_8888,
                true
            )

        val canvas =
            Canvas(result)


        val paint =
            Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {

                color =
                    Color.WHITE

                textSize =
                    32f

                isFakeBoldText =
                    true

                setShadowLayer(
                    4f,
                    2f,
                    2f,
                    Color.BLACK
                )
            }


        val dateStr =
            SimpleDateFormat(
                "dd MMM yyyy, HH:mm:ss",
                Locale.ENGLISH
            ).apply {

                timeZone =
                    TimeZone.getTimeZone(
                        "GMT+05:30"
                    )

            }.format(Date())


        val left =
            30f

        val bottom =
            result.height.toFloat()


        // ---------------------------------------------------------
        // WATERMARK TEXT
        // ---------------------------------------------------------

        canvas.drawText(
            "Staff: $name",
            left,
            bottom - 150f,
            paint
        )

        canvas.drawText(
            "Site: $site",
            left,
            bottom - 110f,
            paint
        )

        canvas.drawText(
            "$dateStr IST",
            left,
            bottom - 70f,
            paint
        )

        canvas.drawText(
            "GPS: ${location.first}, ${location.second}",
            left,
            bottom - 30f,
            paint
        )


        return result
    }


    // =========================================================
    // INDIA DATE
    // =========================================================

    private fun getTodayDate(): String {

        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.ENGLISH
        ).apply {

            timeZone =
                TimeZone.getTimeZone(
                    "GMT+05:30"
                )

        }.format(Date())
    }


    // =========================================================
    // TIMESTAMP FORMAT
    // =========================================================

    private fun formatTimestamp(
        timestamp: Timestamp?
    ): String {

        if (timestamp == null) {
            return "--:--"
        }

        return SimpleDateFormat(
            "hh:mm a",
            Locale.ENGLISH
        ).apply {

            timeZone =
                TimeZone.getTimeZone(
                    "GMT+05:30"
                )

        }.format(
            timestamp.toDate()
        )
    }
}