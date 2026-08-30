package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TaskRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createTask(task: TaskModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val docRef = if (task.id.isNotBlank()) {
                firestore.collection("tasks").document(task.id)
            } else {
                firestore.collection("tasks").document()
            }

            val toSave = task.copy(
                id = docRef.id,
                createdAt = System.currentTimeMillis(),
                status = "PENDING"
            )

            docRef.set(toSave).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getTasksForEmployee(employeeUid: String): List<TaskModel> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("tasks")
                .whereEqualTo("assignedToUid", employeeUid)
                .get()
                .await()
            snapshot.toObjects(TaskModel::class.java).sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAllTasks(): List<TaskModel> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("tasks")
                .get()
                .await()
            snapshot.toObjects(TaskModel::class.java).sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateTaskStatus(
        taskId: String,
        status: String,
        notes: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val updates = hashMapOf<String, Any>(
                "status" to status,
                "completionNotes" to notes
            )
            if (status == "COMPLETED") {
                updates["completedAt"] = System.currentTimeMillis()
            }

            firestore.collection("tasks").document(taskId).update(updates).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
