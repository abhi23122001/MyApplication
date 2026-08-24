package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.models.TaskModel
import kotlinx.coroutines.tasks.await

class TaskRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val tasksCollection = firestore.collection(FirebaseConstants.COLLECTION_TASKS)

    suspend fun getTasksForUser(uid: String): List<TaskModel> {
        return try {
            val snapshot = tasksCollection
                .whereEqualTo("assignedToUid", uid)
                .orderBy("dueDate", Query.Direction.ASCENDING)
                .get()
                .await()
            snapshot.toObjects(TaskModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveTask(task: TaskModel) {
        val id = task.id.ifBlank { tasksCollection.document().id }
        tasksCollection.document(id)
            .set(task.copy(id = id))
            .await()
    }

    suspend fun updateTaskStatus(id: String, status: String) {
        tasksCollection.document(id)
            .update("status", status)
            .await()
    }
}
