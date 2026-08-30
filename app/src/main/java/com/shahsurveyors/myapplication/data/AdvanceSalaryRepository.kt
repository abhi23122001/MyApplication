package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.AdvanceSalaryRequest
import kotlinx.coroutines.tasks.await

class AdvanceSalaryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection = firestore.collection("advanceSalaryRequests")

    suspend fun createRequest(request: AdvanceSalaryRequest) {
        val id = request.id.ifBlank { collection.document().id }
        collection.document(id).set(request.copy(id = id)).await()
    }

    suspend fun getForUser(uid: String): List<AdvanceSalaryRequest> = try {
        collection.whereEqualTo("uid", uid)
            .get()
            .await()
            .toObjects(AdvanceSalaryRequest::class.java)
            .sortedByDescending { it.requestedAt }
    } catch (_: Exception) { emptyList() }

    suspend fun getPending(): List<AdvanceSalaryRequest> = try {
        collection.whereEqualTo("status", "PENDING")
            .get()
            .await()
            .toObjects(AdvanceSalaryRequest::class.java)
            .sortedBy { it.requestedAt }
    } catch (_: Exception) { emptyList() }

    suspend fun updateDecision(
        id: String,
        status: String,
        approvedAmount: Double,
        adminUid: String,
        adminName: String,
        decidedAt: Long = System.currentTimeMillis()
    ) {
        collection.document(id).update(
            mapOf(
                "status" to status,
                "approvedAmount" to approvedAmount,
                "adminUid" to adminUid,
                "adminName" to adminName,
                "decidedAt" to decidedAt
            )
        ).await()
    }
}
