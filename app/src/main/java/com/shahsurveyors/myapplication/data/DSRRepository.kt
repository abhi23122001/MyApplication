package com.shahsurveyors.myapplication.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.models.DSRModel
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class DSRRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val dsrCollection = firestore.collection(FirebaseConstants.COLLECTION_DSR)

    private fun requireCurrentUid(): String =
        auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Authentication required")

    private suspend fun requireAdmin() {
        val uid = requireCurrentUid()
        val profile = firestore.collection(FirebaseConstants.COLLECTION_USERS)
            .document(uid).get().await().toObject(com.shahsurveyors.myapplication.models.UserProfile::class.java)
        require(profile?.active == true && profile.role.equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true)) {
            "Admin authorization required"
        }
    }

    suspend fun getDSRForProject(projectId: String): List<DSRModel> {
        require(projectId.isNotBlank()) { "Project is required" }
        val uid = requireCurrentUid()
        return try {
            dsrCollection.whereEqualTo("uid", uid).whereEqualTo("projectId", projectId)
                .orderBy("date", Query.Direction.DESCENDING).get().await().toObjects(DSRModel::class.java)
        } catch (_: Exception) { emptyList() }
    }

    suspend fun getAllDSRForProject(projectId: String): List<DSRModel> {
        requireAdmin()
        require(projectId.isNotBlank()) { "Project is required" }
        return dsrCollection.whereEqualTo("projectId", projectId)
            .orderBy("date", Query.Direction.DESCENDING).get().await().toObjects(DSRModel::class.java)
    }

    suspend fun saveDSR(dsr: DSRModel) {
        val uid = requireCurrentUid()
        require(dsr.projectId.isNotBlank()) { "Project is required" }
        require(dsr.workDone.isNotBlank()) { "Work details are required" }
        val today = dsr.date?.toDate()?.let { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(it) } ?: ""
        require(today.isNotBlank()) { "DSR date is required" }

        val duplicate = dsrCollection.whereEqualTo("uid", uid).whereEqualTo("projectId", dsr.projectId)
            .get().await().toObjects(DSRModel::class.java).any {
                it.date?.toDate()?.let { d -> SimpleDateFormat("yyyy-MM-dd", Locale.US).format(d) } == today
            }
        require(!duplicate) { "A DSR for this project has already been submitted today" }

        val id = dsr.id.ifBlank { dsrCollection.document().id }
        dsrCollection.document(id).set(dsr.copy(id = id, uid = uid)).await()
    }
}
