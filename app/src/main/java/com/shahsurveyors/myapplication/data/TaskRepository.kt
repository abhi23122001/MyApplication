package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.models.ProjectModel
import com.shahsurveyors.myapplication.models.TaskModel
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.tasks.await

class TaskRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val tasksCollection = firestore.collection(FirebaseConstants.COLLECTION_TASKS)
    private val usersCollection = firestore.collection(FirebaseConstants.COLLECTION_USERS)
    private val projectsCollection = firestore.collection(FirebaseConstants.COLLECTION_PROJECTS)

    suspend fun getTasksForUser(uid: String): List<TaskModel> {
        return try {
            tasksCollection
                .whereEqualTo("assignedToUid", uid)
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .await()
                .toObjects(TaskModel::class.java)
        } catch (_: Exception) {
            try {
                tasksCollection
                    .whereEqualTo("assignedToUid", uid)
                    .get()
                    .await()
                    .toObjects(TaskModel::class.java)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getAllTasks(): List<TaskModel> {
        return try {
            tasksCollection
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .await()
                .toObjects(TaskModel::class.java)
        } catch (_: Exception) {
            try {
                tasksCollection.get().await().toObjects(TaskModel::class.java)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getEmployees(): List<UserProfile> {
        return try {
            usersCollection
                .whereEqualTo("active", true)
                .get()
                .await()
                .toObjects(UserProfile::class.java)
                .filter { !it.uid.isBlank() && !it.role.equals(FirebaseConstants.ROLE_ADMIN, true) }
                .sortedBy { it.name.lowercase() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getProjects(): List<ProjectModel> {
        return try {
            projectsCollection
                .whereEqualTo("status", FirebaseConstants.STATUS_ACTIVE)
                .get()
                .await()
                .toObjects(ProjectModel::class.java)
                .sortedBy { it.name.lowercase() }
        } catch (_: Exception) {
            try {
                projectsCollection.get().await().toObjects(ProjectModel::class.java)
                    .filter { it.status.equals(FirebaseConstants.STATUS_ACTIVE, true) }
                    .sortedBy { it.name.lowercase() }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveTask(task: TaskModel) {
        val id = task.id.ifBlank { tasksCollection.document().id }
        tasksCollection.document(id).set(task.copy(id = id)).await()
    }

    suspend fun updateTaskStatus(id: String, status: String) {
        tasksCollection.document(id).update("status", status).await()
    }
}
