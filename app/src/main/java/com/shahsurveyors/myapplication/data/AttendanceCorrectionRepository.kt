package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.AttendanceCorrectionRequest
import com.shahsurveyors.myapplication.models.AttendanceRecord
import kotlinx.coroutines.tasks.await

class AttendanceCorrectionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val requests = firestore.collection("attendance_correction_requests")
    private val attendance = firestore.collection(FirebaseConstants.COLLECTION_ATTENDANCE)

    suspend fun createRequest(request: AttendanceCorrectionRequest): String {
        val now = Timestamp.now()
        val doc = requests.document()
        doc.set(request.copy(id = doc.id, status = "PENDING", createdAt = now, updatedAt = now)).await()
        return doc.id
    }

    suspend fun hasPendingRequest(uid: String, attendanceDate: String, issueType: String): Boolean {
        return !requests.whereEqualTo("uid", uid)
            .whereEqualTo("attendanceDate", attendanceDate)
            .whereEqualTo("issueType", issueType)
            .whereEqualTo("status", "PENDING")
            .limit(1).get().await().isEmpty
    }

    suspend fun getEmployeeRequests(uid: String, limit: Long = 50): List<AttendanceCorrectionRequest> {
        return requests.whereEqualTo("uid", uid)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit).get().await().toObjects(AttendanceCorrectionRequest::class.java)
    }

    suspend fun getPendingRequests(limit: Long = 100): List<AttendanceCorrectionRequest> {
        return requests.whereEqualTo("status", "PENDING")
            .limit(limit).get().await()
            .toObjects(AttendanceCorrectionRequest::class.java)
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
    }

    suspend fun reviewRequest(id: String, approved: Boolean, adminUid: String, remark: String) {
        val requestRef = requests.document(id)
        val request = requestRef.get().await().toObject(AttendanceCorrectionRequest::class.java)
            ?: throw IllegalArgumentException("Attendance correction request not found")

        val newStatus = if (approved) "APPROVED" else "REJECTED"
        requestRef.update(
            mapOf(
                "status" to newStatus,
                "adminRemark" to remark,
                "reviewedBy" to adminUid,
                "reviewedAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
        ).await()

        if (approved) applyApprovedCorrection(request)
    }

    private suspend fun applyApprovedCorrection(request: AttendanceCorrectionRequest) {
        val docId = "${request.uid}_${request.attendanceDate}"
        val ref = attendance.document(docId)
        val existing = ref.get().await().toObject(AttendanceRecord::class.java)
        val current = existing ?: AttendanceRecord(
            id = docId,
            uid = request.uid,
            userName = request.employeeName,
            date = request.attendanceDate
        )

        val updates = mutableMapOf<String, Any?>(
            "id" to docId,
            "uid" to current.uid.ifBlank { request.uid },
            "userName" to current.userName.ifBlank { request.employeeName },
            "date" to request.attendanceDate,
            "status" to "PRESENT",
            "lateMinutes" to 0,
            "earlyOutMinutes" to 0,
            "overtimeMinutes" to 0,
            "isLate" to false,
            "isEarlyOut" to false,
            "punchOutMissing" to false
        )

        when (request.issueType.uppercase()) {
            "LATE" -> {
                if (request.requestedPunchInTime != null) updates["punchInTime"] = request.requestedPunchInTime
            }
            "EARLY_OUT", "MISSING_PUNCH_OUT" -> {
                if (request.requestedPunchOutTime != null) updates["punchOutTime"] = request.requestedPunchOutTime
                if (request.requestedPunchOutTime != null && current.punchInTime != null) {
                    val minutes = ((request.requestedPunchOutTime.toDate().time - current.punchInTime.toDate().time) / 60_000L)
                        .coerceAtLeast(0L).toInt()
                    updates["workingMinutes"] = minutes
                }
            }
            else -> {
                if (request.requestedPunchInTime != null) updates["punchInTime"] = request.requestedPunchInTime
                if (request.requestedPunchOutTime != null) updates["punchOutTime"] = request.requestedPunchOutTime
            }
        }

        ref.set(updates, com.google.firebase.firestore.SetOptions.merge()).await()
    }
}