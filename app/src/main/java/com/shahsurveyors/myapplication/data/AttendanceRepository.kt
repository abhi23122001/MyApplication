package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.models.AttendanceRecord
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AttendanceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val attendanceCollection = firestore.collection(FirebaseConstants.COLLECTION_ATTENDANCE)
    private val notificationRepository = NotificationRepository(firestore)

    private fun getTodayDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("GMT+05:30")
    }.format(Date())

    private fun documentId(uid: String, date: String): String = "${uid}_$date"

    suspend fun getTodayAttendance(uid: String): AttendanceRecord? {
        val today = getTodayDate()
        return attendanceCollection.document(documentId(uid, today)).get().await()
            .toObject(AttendanceRecord::class.java)
    }

    suspend fun punchIn(record: AttendanceRecord) {
        require(record.uid.isNotBlank()) { "Employee ID is required" }
        val today = getTodayDate()
        val docId = documentId(record.uid, today)
        val ref = attendanceCollection.document(docId)
        val existing = ref.get().await().toObject(AttendanceRecord::class.java)
        if (existing != null) {
            throw IllegalStateException("Today's attendance is already recorded")
        }
        ref.set(record.copy(id = docId, date = today)).await()
        runCatching {
            notificationRepository.createForAdmins(
                type = "ATTENDANCE_PUNCH_IN",
                title = "Punch In Request",
                message = "${record.userName.ifBlank { "Employee" }} punched in at ${record.siteName.ifBlank { "current location" }}.",
                actorUid = record.uid,
                actorName = record.userName,
                referenceId = docId,
                route = "attendance"
            )
        }
    }

    suspend fun punchOut(uid: String, lat: Double, lng: Double) {
        require(uid.isNotBlank()) { "Employee ID is required" }
        val today = getTodayDate()
        val docId = documentId(uid, today)
        val ref = attendanceCollection.document(docId)
        val record = ref.get().await().toObject(AttendanceRecord::class.java)
            ?: throw IllegalStateException("Punch IN is required before Punch OUT")

        if (record.punchInTime == null) {
            throw IllegalStateException("Punch IN time is missing")
        }
        if (record.punchOutTime != null) {
            throw IllegalStateException("Today's attendance is already completed")
        }

        val punchOutTimestamp = com.google.firebase.Timestamp.now()
        val workingMinutes = ((punchOutTimestamp.toDate().time - record.punchInTime.toDate().time) / 60_000L)
            .coerceAtLeast(0L).toInt()

        ref.update(
            mapOf(
                "punchOutTime" to punchOutTimestamp,
                "punchOutLat" to lat,
                "punchOutLng" to lng,
                "workingMinutes" to workingMinutes,
                "punchOutMissing" to false
            )
        ).await()
        runCatching {
            notificationRepository.createForAdmins(
                type = "ATTENDANCE_PUNCH_OUT",
                title = "Punch Out Request",
                message = "${record.userName.ifBlank { "Employee" }} punched out after $workingMinutes minutes.",
                actorUid = record.uid,
                actorName = record.userName,
                referenceId = docId,
                route = "attendance"
            )
        }
    }

    suspend fun markPunchOutMissing(uid: String, date: String) {
        attendanceCollection.document(documentId(uid, date)).update(
            mapOf("punchOutMissing" to true, "status" to "MISSING_PUNCH_OUT")
        ).await()
    }

    suspend fun updateAttendanceClassification(
        uid: String,
        date: String,
        status: String,
        lateMinutes: Int = 0,
        earlyOutMinutes: Int = 0,
        overtimeMinutes: Int = 0,
        leaveRequestId: String = ""
    ) {
        attendanceCollection.document(documentId(uid, date)).update(
            mapOf(
                "status" to status,
                "lateMinutes" to lateMinutes.coerceAtLeast(0),
                "earlyOutMinutes" to earlyOutMinutes.coerceAtLeast(0),
                "overtimeMinutes" to overtimeMinutes.coerceAtLeast(0),
                "isLate" to (lateMinutes > 0),
                "isEarlyOut" to (earlyOutMinutes > 0),
                "leaveRequestId" to leaveRequestId
            )
        ).await()
    }

    suspend fun getAttendanceHistory(uid: String, limit: Long = 30): List<AttendanceRecord> {
        return attendanceCollection
            .whereEqualTo("uid", uid)
            .orderBy("punchInTime", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
            .toObjects(AttendanceRecord::class.java)
    }

    suspend fun getAttendanceForMonth(uid: String, startDate: String, endDate: String): List<AttendanceRecord> {
        return attendanceCollection
            .whereEqualTo("uid", uid)
            .get()
            .await()
            .toObjects(AttendanceRecord::class.java)
            .filter { it.date >= startDate && it.date <= endDate }
    }

    suspend fun getAllAttendance(limit: Long = 100): List<AttendanceRecord> {
        return attendanceCollection
            .orderBy("punchInTime", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
            .toObjects(AttendanceRecord::class.java)
    }

    suspend fun getTodayAllAttendance(): List<AttendanceRecord> {
        val today = getTodayDate()
        return attendanceCollection.whereEqualTo("date", today).get().await()
            .toObjects(AttendanceRecord::class.java)
    }
}
