package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.LeaveRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LeaveRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun applyLeave(request: LeaveRequest): Boolean = withContext(Dispatchers.IO) {
        try {
            val docRef = if (request.id.isNotBlank()) {
                firestore.collection("leaveRequests").document(request.id)
            } else {
                firestore.collection("leaveRequests").document()
            }

            val toSave = request.copy(
                id = docRef.id,
                status = "PENDING",
                appliedAt = System.currentTimeMillis()
            )

            docRef.set(toSave).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getLeavesForEmployee(employeeUid: String): List<LeaveRequest> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("leaveRequests")
                .whereEqualTo("employeeUid", employeeUid)
                .get()
                .await()
            snapshot.toObjects(LeaveRequest::class.java).sortedByDescending { it.appliedAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAllLeaves(): List<LeaveRequest> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("leaveRequests")
                .get()
                .await()
            snapshot.toObjects(LeaveRequest::class.java).sortedByDescending { it.appliedAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun decideLeave(
        leaveId: String,
        status: String, // APPROVED or REJECTED
        adminUid: String,
        adminName: String,
        note: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val updates = hashMapOf<String, Any>(
                "status" to status,
                "decidedAt" to System.currentTimeMillis(),
                "decidedByUid" to adminUid,
                "decidedByName" to adminName,
                "note" to note
            )

            firestore.collection("leaveRequests").document(leaveId).update(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
