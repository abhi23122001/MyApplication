package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.models.AttendanceCorrectionRequest
import kotlinx.coroutines.tasks.await

class AttendanceCorrectionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val requests = firestore.collection("attendance_correction_requests")

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
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit).get().await().toObjects(AttendanceCorrectionRequest::class.java)
    }

    suspend fun getPendingRequests(limit: Long = 100): List<AttendanceCorrectionRequest> {
        return requests.whereEqualTo("status", "PENDING")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit).get().await().toObjects(AttendanceCorrectionRequest::class.java)
    }

    suspend fun reviewRequest(id: String, approved: Boolean, adminUid: String, remark: String) {
        requests.document(id).update(
            mapOf(
                "status" to if (approved) "APPROVED" else "REJECTED",
                "adminRemark" to remark,
                "reviewedBy" to adminUid,
                "reviewedAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )
        ).await()
    }
}
