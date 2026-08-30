package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.EquipmentModel
import com.shahsurveyors.myapplication.models.EquipmentHandoverRecord
import com.shahsurveyors.myapplication.models.ProjectModel
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.tasks.await

class EquipmentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val equipmentCollection = firestore.collection(FirebaseConstants.COLLECTION_EQUIPMENT)
    private val historyCollection = firestore.collection("equipment_handover_history")
    private val usersCollection = firestore.collection(FirebaseConstants.COLLECTION_USERS)

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

    suspend fun getAllEquipment(): List<EquipmentModel> {
        requireAdminUid()
        return equipmentCollection.get().await().toObjects(EquipmentModel::class.java)
    }

    suspend fun saveEquipment(equipment: EquipmentModel) {
        requireAdminUid()
        val id = equipment.id.ifBlank { equipmentCollection.document().id }
        require(id.isNotBlank()) { "Equipment ID is required" }
        require(equipment.name.isNotBlank()) { "Equipment name is required" }
        equipmentCollection.document(id).set(equipment.copy(id = id)).await()
    }

    suspend fun updateEquipmentStatus(id: String, status: String, uid: String?, name: String?) {
        requireAdminUid()
        require(id.isNotBlank()) { "Equipment ID is required" }
        require(status.isNotBlank()) { "Equipment status is required" }
        equipmentCollection.document(id).update(
            mapOf("status" to status.trim(), "assignedToUid" to uid, "assignedToName" to name)
        ).await()
    }

    suspend fun getActiveEmployees(): List<UserProfile> = try {
        requireAdminUid()
        usersCollection.whereEqualTo("active", true).whereEqualTo("approved", true).get().await().documents.mapNotNull { d ->
            val profile = d.toObject(UserProfile::class.java) ?: return@mapNotNull null
            profile.copy(uid = profile.uid.ifBlank { d.id })
        }.filter { it.role.lowercase() != "admin" }.sortedBy { it.name.lowercase() }
    } catch (e: IllegalStateException) { throw e }

    suspend fun getActiveProjects(): List<ProjectModel> = try {
        requireAdminUid()
        firestore.collection(FirebaseConstants.COLLECTION_PROJECTS).get().await().documents.mapNotNull { d ->
            val project = d.toObject(ProjectModel::class.java) ?: return@mapNotNull null
            project.copy(id = project.id.ifBlank { d.id })
        }.filter { statusIsActive(it.status) }.sortedBy { it.name.lowercase() }
    } catch (e: IllegalStateException) { throw e }

    private fun statusIsActive(status: String): Boolean = when (status.trim().uppercase()) {
        "ACTIVE", "STARTED", "IN_PROGRESS", "ONGOING" -> true
        else -> false
    }

    suspend fun getHandoverHistory(equipmentId: String): List<EquipmentHandoverRecord> = try {
        requireAdminUid()
        require(equipmentId.isNotBlank()) { "Equipment ID is required" }
        historyCollection.whereEqualTo("equipmentId", equipmentId).get().await().documents.map { d ->
            EquipmentHandoverRecord(
                id = d.id, equipmentId = d.getString("equipmentId") ?: "", equipmentName = d.getString("equipmentName") ?: "",
                action = d.getString("action") ?: "", employeeUid = d.getString("employeeUid") ?: "", employeeName = d.getString("employeeName") ?: "",
                projectId = d.getString("projectId") ?: "", projectName = d.getString("projectName") ?: "", location = d.getString("location") ?: "",
                accessories = (d.get("accessories") as? List<*>)?.filterIsInstance<String>() ?: emptyList(), remarks = d.getString("remarks") ?: "",
                actionAt = d.getTimestamp("actionAt") ?: d.getTimestamp("timestamp"), actionByUid = d.getString("actionByUid") ?: d.getString("performedByUid") ?: "",
                actionByName = d.getString("actionByName") ?: d.getString("performedByName") ?: ""
            )
        }.sortedByDescending { it.actionAt?.toDate()?.time ?: 0L }
    } catch (e: IllegalStateException) { throw e }

    suspend fun saveHandover(id: String, employee: UserProfile, project: ProjectModel, location: String, accessories: List<String>, handoverByUid: String, handoverByName: String) {
        val currentAdminUid = requireAdminUid()
        require(id.isNotBlank()) { "Equipment ID is required" }
        require(employee.uid.isNotBlank()) { "Please select an employee" }
        require(project.id.isNotBlank()) { "Please select a project" }
        require(location.isNotBlank()) { "Please enter handover location" }
        val current = equipmentCollection.document(id).get().await().toObject(EquipmentModel::class.java) ?: throw IllegalArgumentException("Equipment not found")
        require(current.status.trim().uppercase() == "AVAILABLE") { "Equipment is no longer available" }
        val now = Timestamp.now()
        val cleanAccessories = accessories.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        val equipmentRef = equipmentCollection.document(id)
        val historyRef = historyCollection.document()
        val updates = mapOf<String, Any>(
            "status" to "IN_USE", "assignedToUid" to employee.uid, "assignedToName" to employee.name,
            "assignedProjectId" to project.id, "assignedProjectName" to project.name, "handoverLocation" to location.trim(),
            "handoverAt" to now, "handoverByUid" to currentAdminUid, "handoverByName" to handoverByName.trim(), "handoverAccessories" to cleanAccessories
        )
        val history = mapOf<String, Any>(
            "id" to historyRef.id, "equipmentId" to id, "equipmentName" to current.name, "action" to "HANDOVER", "employeeUid" to employee.uid,
            "employeeName" to employee.name, "projectId" to project.id, "projectName" to project.name, "location" to location.trim(),
            "accessories" to cleanAccessories, "remarks" to "", "actionByUid" to currentAdminUid, "actionByName" to handoverByName.trim(), "actionAt" to now
        )
        firestore.runBatch { batch -> batch.update(equipmentRef, updates); batch.set(historyRef, history) }.await()
    }

    suspend fun returnEquipment(id: String, location: String, remarks: String) {
        val currentAdminUid = requireAdminUid()
        require(id.isNotBlank()) { "Equipment ID is required" }
        require(location.isNotBlank()) { "Please enter return location" }
        val now = Timestamp.now()
        val current = equipmentCollection.document(id).get().await().toObject(EquipmentModel::class.java) ?: throw IllegalArgumentException("Equipment not found")
        require(current.status.trim().uppercase() == "IN_USE") { "Equipment is not currently assigned" }
        val historyRef = historyCollection.document()
        val history = mapOf<String, Any>(
            "id" to historyRef.id, "equipmentId" to id, "equipmentName" to current.name, "action" to "RETURN", "employeeUid" to (current.assignedToUid ?: ""),
            "employeeName" to (current.assignedToName ?: ""), "projectId" to (current.assignedProjectId ?: ""), "projectName" to (current.assignedProjectName ?: ""),
            "location" to location.trim(), "accessories" to current.handoverAccessories, "remarks" to remarks.trim(),
            "actionByUid" to currentAdminUid, "actionByName" to "Admin", "actionAt" to now
        )
        val updates = mapOf<String, Any?>(
            "status" to "AVAILABLE", "assignedToUid" to null, "assignedToName" to null, "assignedProjectId" to null,
            "assignedProjectName" to null, "handoverLocation" to null, "lastReturnAt" to now, "lastReturnByUid" to currentAdminUid,
            "lastReturnByName" to "Admin", "lastReturnLocation" to location.trim(), "returnRemarks" to remarks.trim()
        )
        firestore.runBatch { batch -> batch.set(historyRef, history); batch.update(equipmentCollection.document(id), updates) }.await()
    }
}
