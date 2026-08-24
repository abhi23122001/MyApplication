package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.ProjectModel
import kotlinx.coroutines.tasks.await

class ProjectRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val projectsCollection = firestore.collection(FirebaseConstants.COLLECTION_PROJECTS)

    suspend fun getAllProjects(): List<ProjectModel> {
        return try {
            val snapshot = projectsCollection.get().await()
            snapshot.toObjects(ProjectModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getProject(id: String): ProjectModel? {
        return try {
            val doc = projectsCollection.document(id).get().await()
            doc.toObject(ProjectModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveProject(project: ProjectModel) {
        val id = project.id.ifBlank { projectsCollection.document().id }
        projectsCollection.document(id)
            .set(project.copy(id = id))
            .await()
    }

    suspend fun deleteProject(id: String) {
        projectsCollection.document(id).delete().await()
    }
}
