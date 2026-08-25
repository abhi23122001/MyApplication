package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.models.AttendanceRecord
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AttendanceRepository(
    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()
) {

    private val attendanceCollection =
        firestore.collection(
            FirebaseConstants.COLLECTION_ATTENDANCE
        )

    // =========================================================
    // INDIA DATE
    // =========================================================

    private fun getTodayDate(): String {

        return SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.ENGLISH
        ).apply {

            timeZone =
                TimeZone.getTimeZone("GMT+05:30")

        }.format(Date())
    }

    // =========================================================
    // GET TODAY ATTENDANCE - EMPLOYEE
    // =========================================================

    suspend fun getTodayAttendance(
        uid: String
    ): AttendanceRecord? {

        val today = getTodayDate()

        val snapshot =
            attendanceCollection
                .whereEqualTo("uid", uid)
                .whereEqualTo("date", today)
                .limit(1)
                .get()
                .await()

        return snapshot
            .documents
            .firstOrNull()
            ?.toObject(
                AttendanceRecord::class.java
            )
    }

    // =========================================================
    // PUNCH IN
    // =========================================================

    suspend fun punchIn(
        record: AttendanceRecord
    ) {

        val today = getTodayDate()

        val docId =
            "${record.uid}_$today"

        attendanceCollection
            .document(docId)
            .set(
                record.copy(
                    id = docId,
                    date = today
                )
            )
            .await()
    }

    // =========================================================
    // PUNCH OUT
    // =========================================================

    suspend fun punchOut(
        uid: String,
        lat: Double,
        lng: Double
    ) {

        val today = getTodayDate()

        val docId =
            "${uid}_$today"

        val document =
            attendanceCollection
                .document(docId)
                .get()
                .await()

        val record =
            document.toObject(
                AttendanceRecord::class.java
            )

        val punchOutTimestamp =
            com.google.firebase.Timestamp.now()

        val workingMinutes =
            if (record?.punchInTime != null) {
                ((punchOutTimestamp.toDate().time - record.punchInTime.toDate().time) / 60_000L)
                    .coerceAtLeast(0L)
                    .toInt()
            } else {
                0
            }

        attendanceCollection
            .document(docId)
            .update(
                mapOf(
                    "punchOutTime" to punchOutTimestamp,
                    "punchOutLat" to lat,
                    "punchOutLng" to lng,
                    "workingMinutes" to workingMinutes,
                    "punchOutMissing" to false
                )
            )
            .await()
    }

    // =========================================================
    // MARK A RECORD AS MISSING PUNCH OUT
    // =========================================================

    suspend fun markPunchOutMissing(
        uid: String,
        date: String
    ) {
        val docId = "${uid}_$date"

        attendanceCollection
            .document(docId)
            .update(
                mapOf(
                    "punchOutMissing" to true,
                    "status" to "MISSING_PUNCH_OUT"
                )
            )
            .await()
    }

    // =========================================================
    // UPDATE ATTENDANCE STATUS / PAYROLL METRICS
    // =========================================================

    suspend fun updateAttendanceClassification(
        uid: String,
        date: String,
        status: String,
        lateMinutes: Int = 0,
        earlyOutMinutes: Int = 0,
        overtimeMinutes: Int = 0,
        leaveRequestId: String = ""
    ) {
        val docId = "${uid}_$date"

        attendanceCollection
            .document(docId)
            .update(
                mapOf(
                    "status" to status,
                    "lateMinutes" to lateMinutes.coerceAtLeast(0),
                    "earlyOutMinutes" to earlyOutMinutes.coerceAtLeast(0),
                    "overtimeMinutes" to overtimeMinutes.coerceAtLeast(0),
                    "isLate" to (lateMinutes > 0),
                    "isEarlyOut" to (earlyOutMinutes > 0),
                    "leaveRequestId" to leaveRequestId
                )
            )
            .await()
    }

    // =========================================================
    // EMPLOYEE ATTENDANCE HISTORY
    // =========================================================

    suspend fun getAttendanceHistory(
        uid: String,
        limit: Long = 30
    ): List<AttendanceRecord> {

        val snapshot =
            attendanceCollection
                .whereEqualTo("uid", uid)
                .orderBy(
                    "punchInTime",
                    Query.Direction.DESCENDING
                )
                .limit(limit)
                .get()
                .await()

        return snapshot.toObjects(
            AttendanceRecord::class.java
        )
    }

    // =========================================================
    // ADMIN - ALL ATTENDANCE
    // =========================================================

    suspend fun getAllAttendance(
        limit: Long = 100
    ): List<AttendanceRecord> {

        val snapshot =
            attendanceCollection
                .orderBy(
                    "punchInTime",
                    Query.Direction.DESCENDING
                )
                .limit(limit)
                .get()
                .await()

        return snapshot.toObjects(
            AttendanceRecord::class.java
        )
    }

    // =========================================================
    // ADMIN - TODAY ATTENDANCE
    // =========================================================

    suspend fun getTodayAllAttendance(): List<AttendanceRecord> {

        val today = getTodayDate()

        val snapshot =
            attendanceCollection
                .whereEqualTo(
                    "date",
                    today
                )
                .get()
                .await()

        return snapshot.toObjects(
            AttendanceRecord::class.java
        )
    }
}