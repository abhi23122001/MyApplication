package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.models.DSRModel
import kotlinx.coroutines.tasks.await

class DSRRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val dsrCollection = firestore.collection(FirebaseConstants.COLLECTION_DSR)

    suspend fun getDSRForProject(projectId: String): List<DSRModel> {
        return try {
            val snapshot = dsrCollection
                .whereEqualTo("projectId", projectId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(DSRModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveDSR(dsr: DSRModel) {
        val id = dsr.id.ifBlank { dsrCollection.document().id }
        dsrCollection.document(id)
            .set(dsr.copy(id = id))
            .await()
    }
}
