package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.EquipmentModel
import com.shahsurveyors.myapplication.models.ProjectModel
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.tasks.await

class EquipmentRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val equipmentCollection = firestore.collection(FirebaseConstants.COLLECTION_EQUIPMENT)

    suspend fun getAllEquipment(): List<EquipmentModel> = try { equipmentCollection.get().await().toObjects(EquipmentModel::class.java) } catch (_: Exception) { emptyList() }

    suspend fun saveEquipment(equipment: EquipmentModel) {
        val id = equipment.id.ifBlank { equipmentCollection.document().id }
        equipmentCollection.document(id).set(equipment.copy(id = id)).await()
    }

    suspend fun updateEquipmentStatus(id: String, status: String, uid: String?, name: String?) {
        equipmentCollection.document(id).update(mapOf("status" to status,"assignedToUid" to uid,"assignedToName" to name)).await()
    }

    suspend fun getActiveEmployees(): List<UserProfile> = try {
        firestore.collection(FirebaseConstants.COLLECTION_USERS).whereEqualTo("active", true).whereEqualTo("approved", true).get().await().toObjects(UserProfile::class.java).filter { it.role.lowercase() != "admin" }.sortedBy { it.name.lowercase() }
    } catch (_: Exception) { emptyList() }

    suspend fun getActiveProjects(): List<ProjectModel> = try {
        firestore.collection(FirebaseConstants.COLLECTION_PROJECTS).whereEqualTo("status", "ACTIVE").get().await().toObjects(ProjectModel::class.java).sortedBy { it.name.lowercase() }
    } catch (_: Exception) { emptyList() }

    suspend fun saveHandover(id: String, employee: UserProfile, project: ProjectModel, location: String, accessories: List<String>, handoverByUid: String, handoverByName: String) {
        require(employee.uid.isNotBlank()) { "Please select an employee" }
        require(project.id.isNotBlank()) { "Please select a project" }
        require(location.isNotBlank()) { "Please enter handover location" }
        equipmentCollection.document(id).update(mapOf(
            "status" to "IN_USE",
            "assignedToUid" to employee.uid,
            "assignedToName" to employee.name,
            "assignedProjectId" to project.id,
            "assignedProjectName" to project.name,
            "handoverLocation" to location.trim(),
            "handoverAt" to Timestamp.now(),
            "handoverByUid" to handoverByUid,
            "handoverByName" to handoverByName,
            "handoverAccessories" to accessories.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        )).await()
    }

    suspend fun returnEquipment(id: String) {
        equipmentCollection.document(id).update(mapOf("status" to "AVAILABLE","assignedToUid" to null,"assignedToName" to null,"assignedProjectId" to null,"assignedProjectName" to null,"handoverLocation" to null)).await()
    }
}
