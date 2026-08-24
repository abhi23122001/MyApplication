package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.EquipmentModel
import kotlinx.coroutines.tasks.await

class EquipmentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val equipmentCollection = firestore.collection(FirebaseConstants.COLLECTION_EQUIPMENT)

    suspend fun getAllEquipment(): List<EquipmentModel> {
        return try {
            val snapshot = equipmentCollection.get().await()
            snapshot.toObjects(EquipmentModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveEquipment(equipment: EquipmentModel) {
        val id = equipment.id.ifBlank { equipmentCollection.document().id }
        equipmentCollection.document(id)
            .set(equipment.copy(id = id))
            .await()
    }

    suspend fun updateEquipmentStatus(id: String, status: String, uid: String?, name: String?) {
        equipmentCollection.document(id)
            .update(
                mapOf(
                    "status" to status,
                    "assignedToUid" to uid,
                    "assignedToName" to name
                )
            )
            .await()
    }
}
