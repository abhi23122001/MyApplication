package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.models.AttendanceRecord
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AttendanceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val attendanceCollection = firestore.collection(FirebaseConstants.COLLECTION_ATTENDANCE)
    private val usersCollection = firestore.collection(FirebaseConstants.COLLECTION_USERS)
    private val notificationRepository = NotificationRepository(firestore)

    private fun requireCurrentUid(): String =
        auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Authentication required")

    private suspend fun requireAdminUid(): String {
        val uid = requireCurrentUid()
        val profile = usersCollection.document(uid).get().await().toObject(UserProfile::class.java)
        require(profile?.active == true && profile.role.equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true)) {
            "Admin authorization required"
        }
        return uid
    }

    private fun requireOwnUid(uid: String): String {
        val currentUid = requireCurrentUid()
        require(uid == currentUid) { "Users may only access their own attendance" }
        return currentUid
    }

    private fun getTodayDate(): String = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("GMT+05:30")
    }.format(Date())

    private fun documentId(uid: String, date: String): String = "${uid}_$date"

    suspend fun getTodayAttendance(uid: String): AttendanceRecord? {
        val currentUid = requireOwnUid(uid)
        val today = getTodayDate()
        return attendanceCollection.document(documentId(currentUid, today)).get().await()
            .toObject(AttendanceRecord::class.java)
    }

    suspend fun punchIn(record: AttendanceRecord) {
        val currentUid = requireCurrentUid()
        require(record.uid == currentUid) { "Attendance ownership mismatch" }
        val today = getTodayDate()
        val docId = documentId(currentUid, today)
        val ref = attendanceCollection.document(docId)
        val existing = ref.get().await().toObject(AttendanceRecord::class.java)
        if (existing != null) {
            throw IllegalStateException("Today's attendance is already recorded")
        }
        ref.set(record.copy(uid = currentUid, id = docId, date = today)).await()
        runCatching {
            notificationRepository.createForAdmins(
                type = "ATTENDANCE_PUNCH_IN",
                title = "Punch In Request",
                message = "${record.userName.ifBlank { "Employee" }} punched in at ${record.siteName.ifBlank { "current location" }}.",
                actorUid = currentUid,
                actorName = record.userName,
                referenceId = docId,
                route = "attendance"
            )
        }
    }

    suspend fun punchOut(uid: String, lat: Double, lng: Double) {
        val currentUid = requireOwnUid(uid)
        require(lat in -90.0..90.0 && lng in -180.0..180.0) { "Invalid location coordinates" }
        val today = getTodayDate()
        val docId = documentId(currentUid, today)
        val ref = attendanceCollection.document(docId)
        val record = ref.get().await().toObject(AttendanceRecord::class.java)
            ?: throw IllegalStateException("Punch IN is required before Punch OUT")

        if (record.punchInTime == null) {
            throw IllegalStateException("Punch IN time is missing")
        }
        if (record.punchOutTime != null) {
            throw IllegalStateException("Today's attendance is already completed")
        }

        val punchOutTimestamp = Timestamp.now()
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
                actorUid = currentUid,
                actorName = record.userName,
                referenceId = docId,
                route = "attendance"
            )
        }
    }

    suspend fun markPunchOutMissing(uid: String, date: String) {
        requireAdminUid()
        require(uid.isNotBlank() && date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { "Invalid attendance reference" }
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
        requireAdminUid()
        require(uid.isNotBlank() && date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { "Invalid attendance reference" }
        require(status.isNotBlank()) { "Attendance status is required" }
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
        val currentUid = requireOwnUid(uid)
        val safeLimit = limit.coerceIn(1L, 100L)
        return attendanceCollection
            .whereEqualTo("uid", currentUid)
            .orderBy("punchInTime", Query.Direction.DESCENDING)
            .limit(safeLimit)
            .get()
            .await()
            .toObjects(AttendanceRecord::class.java)
    }

    suspend fun getAttendanceForMonth(uid: String, startDate: String, endDate: String): List<AttendanceRecord> {
        val currentUid = requireOwnUid(uid)
        require(startDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) && endDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) && startDate <= endDate) {
            "Invalid attendance date range"
        }
        return attendanceCollection
            .whereEqualTo("uid", currentUid)
            .get()
            .await()
            .toObjects(AttendanceRecord::class.java)
            .filter { it.date >= startDate && it.date <= endDate }
    }

    suspend fun getAllAttendance(limit: Long = 100): List<AttendanceRecord> {
        requireAdminUid()
        val safeLimit = limit.coerceIn(1L, 500L)
        return attendanceCollection
            .orderBy("punchInTime", Query.Direction.DESCENDING)
            .limit(safeLimit)
            .get()
            .await()
            .toObjects(AttendanceRecord::class.java)
    }

    suspend fun getTodayAllAttendance(): List<AttendanceRecord> {
        requireAdminUid()
        val today = getTodayDate()
        return attendanceCollection.whereEqualTo("date", today).get().await()
            .toObjects(AttendanceRecord::class.java)
    }
}
