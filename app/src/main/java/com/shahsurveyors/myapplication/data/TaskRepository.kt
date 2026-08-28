package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.ProjectModel
import com.shahsurveyors.myapplication.models.TaskModel
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.tasks.await

class TaskRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val tasksCollection = firestore.collection(FirebaseConstants.COLLECTION_TASKS)
    private val usersCollection = firestore.collection(FirebaseConstants.COLLECTION_USERS)
    private val projectsCollection = firestore.collection(FirebaseConstants.COLLECTION_PROJECTS)
    private val notificationRepository = NotificationRepository(firestore)

    suspend fun getTasksForUser(uid: String): List<TaskModel> = try {
        tasksCollection.whereEqualTo("assignedToUid", uid).get().await().documents
            .mapNotNull { it.toObject(TaskModel::class.java)?.copy(id = it.id) }
            .sortedBy { it.dueDate?.seconds ?: Long.MAX_VALUE }
    } catch (_: Exception) { emptyList() }

    suspend fun getAllTasks(): List<TaskModel> = try {
        tasksCollection.get().await().documents
            .mapNotNull { it.toObject(TaskModel::class.java)?.copy(id = it.id) }
            .sortedBy { it.dueDate?.seconds ?: Long.MAX_VALUE }
    } catch (_: Exception) { emptyList() }

    suspend fun getEmployees(): List<UserProfile> = try {
        usersCollection.whereEqualTo("active", true).get().await().documents
            .mapNotNull { doc -> doc.toObject(UserProfile::class.java)?.let { p -> if (p.uid.isBlank()) p.copy(uid = doc.id) else p } }
            .filter { !it.role.equals(FirebaseConstants.ROLE_ADMIN, true) }
            .sortedBy { it.name.lowercase() }
    } catch (_: Exception) { emptyList() }

    suspend fun getProjects(): List<ProjectModel> = try {
        projectsCollection.get().await().documents
            .mapNotNull { doc -> doc.toObject(ProjectModel::class.java)?.let { p -> if (p.id.isBlank()) p.copy(id = doc.id) else p } }
            .filter { it.status.equals(FirebaseConstants.STATUS_ACTIVE, true) || it.status.equals("STARTED", true) || it.status.equals("IN PROGRESS", true) || it.status.equals("ONGOING", true) }
            .sortedBy { it.name.lowercase() }
    } catch (_: Exception) { emptyList() }

    suspend fun saveTask(task: TaskModel) {
        require(task.title.isNotBlank()) { "Task title is required" }
        require(task.assignedToUid.isNotBlank()) { "Employee is required" }
        require(task.projectId.isNotBlank()) { "Project is required" }
        val id = task.id.ifBlank { tasksCollection.document().id }
        tasksCollection.document(id).set(task.copy(id = id)).await()
        tasksCollection.document(id).collection("history").add(mapOf("status" to task.status, "action" to "CREATED", "at" to Timestamp.now(), "actorUid" to task.assignedToUid)).await()
        notificationRepository.createForUser(task.assignedToUid, "TASK_ASSIGNED", "New Task Assigned", "${task.title} has been assigned to you.", id, "tasks")
    }

    suspend fun updateTaskStatus(id: String, status: String, actorUid: String, isAdmin: Boolean) {
        require(status in setOf("OPEN", "IN_PROGRESS", "BLOCKED", "COMPLETED")) { "Invalid task status" }
        require(id.isNotBlank()) { "Invalid task" }
        require(actorUid.isNotBlank()) { "User session unavailable" }
        val ref = tasksCollection.document(id)
        val snapshot = ref.get().await()
        require(snapshot.exists()) { "Task not found" }
        val task = snapshot.toObject(TaskModel::class.java) ?: throw IllegalStateException("Task data unavailable")
        if (!isAdmin) require(task.assignedToUid == actorUid) { "You are not allowed to update this task" }
        ref.update(mapOf("status" to status, "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(), "updatedByUid" to actorUid)).await()
        ref.collection("history").add(mapOf("status" to status, "action" to "STATUS_CHANGED", "at" to Timestamp.now(), "actorUid" to actorUid)).await()
        if (isAdmin) notificationRepository.createForUser(task.assignedToUid, "TASK_STATUS", "Task status updated", "${task.title}: $status", id, "tasks")
        else notificationRepository.createForAdmins("TASK_STATUS", "Task status updated", "${task.assignedToName.ifBlank { actorUid }} changed ${task.title} to $status", actorUid, task.assignedToName, id, "tasks")
    }
}
