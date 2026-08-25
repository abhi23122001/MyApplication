package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.LeaveRequestModel
import kotlinx.coroutines.tasks.await

class LeaveRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val leaveCollection = firestore.collection("leaveRequests")

    suspend fun getRequestsForUser(uid: String): List<LeaveRequestModel> {
        return try {
            leaveCollection
                .whereEqualTo("userUid", uid)
                .get()
                .await()
                .toObjects(LeaveRequestModel::class.java)
                .sortedByDescending { it.createdAt?.seconds ?: 0L }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllRequests(): List<LeaveRequestModel> {
        return try {
            leaveCollection
                .get()
                .await()
                .toObjects(LeaveRequestModel::class.java)
                .sortedByDescending { it.createdAt?.seconds ?: 0L }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun submitRequest(request: LeaveRequestModel): String {
        val ref = if (request.id.isBlank()) leaveCollection.document() else leaveCollection.document(request.id)
        val saved = request.copy(id = ref.id, createdAt = request.createdAt ?: Timestamp.now())
        ref.set(saved).await()
        return ref.id
    }

    suspend fun updateStatus(id: String, status: String, approvedBy: String) {
        leaveCollection.document(id).update(
            mapOf(
                "status" to status,
                "approvedBy" to approvedBy,
                "updatedAt" to Timestamp.now()
            )
        ).await()
    }
}
