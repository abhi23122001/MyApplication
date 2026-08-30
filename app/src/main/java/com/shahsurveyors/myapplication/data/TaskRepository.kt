package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.ProjectModel
import com.shahsurveyors.myapplication.models.TaskModel
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.tasks.await

class TaskRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val auth = FirebaseAuth.getInstance()
    private val tasksCollection = firestore.collection(FirebaseConstants.COLLECTION_TASKS)
    private val usersCollection = firestore.collection(FirebaseConstants.COLLECTION_USERS)
    private val projectsCollection = firestore.collection(FirebaseConstants.COLLECTION_PROJECTS)
    private val notificationRepository = NotificationRepository(firestore)

    private fun requireUid(): String = auth.currentUser?.uid?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException("Authentication required")

    private suspend fun currentProfile(): UserProfile {
        val uid = requireUid()
        return usersCollection.document(uid).get().await().toObject(UserProfile::class.java)
            ?: throw IllegalStateException("User profile not found")
    }

    private suspend fun requireAdmin(): String {
        val profile = currentProfile()
        require(profile.active && profile.role.equals(FirebaseConstants.ROLE_ADMIN, true)) { "Admin authorization required" }
        return profile.uid
    }

    private fun hasTaskAccess(profile: UserProfile): Boolean =
        profile.role.equals(FirebaseConstants.ROLE_ADMIN, true) ||
            profile.access.split(",").any { it.trim().equals("TASKS", true) }

    suspend fun getTasksForUser(uid: String): List<TaskModel> {
        val currentUid = requireUid()
        require(uid == currentUid) { "You may only access your own task list" }
        val profile = currentProfile()
        require(profile.active && hasTaskAccess(profile)) { "Task access denied" }
        return try {
            tasksCollection.whereEqualTo("assignedToUid", currentUid).get().await().documents
                .mapNotNull { it.toObject(TaskModel::class.java)?.copy(id = it.id) }
                .sortedBy { it.dueDate?.seconds ?: Long.MAX_VALUE }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun getAllTasks(): List<TaskModel> {
        requireAdmin()
        return try {
            tasksCollection.get().await().documents
                .mapNotNull { it.toObject(TaskModel::class.java)?.copy(id = it.id) }
                .sortedBy { it.dueDate?.seconds ?: Long.MAX_VALUE }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun getEmployees(): List<UserProfile> {
        requireAdmin()
        return try {
            usersCollection.whereEqualTo("active", true).get().await().documents
                .mapNotNull { doc -> doc.toObject(UserProfile::class.java)?.let { p -> if (p.uid.isBlank()) p.copy(uid = doc.id) else p } }
                .filter { !it.role.equals(FirebaseConstants.ROLE_ADMIN, true) }
                .sortedBy { it.name.lowercase() }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun getProjects(): List<ProjectModel> {
        requireAdmin()
        return try {
            projectsCollection.get().await().documents
                .mapNotNull { doc -> doc.toObject(ProjectModel::class.java)?.let { p -> if (p.id.isBlank()) p.copy(id = doc.id) else p } }
                .filter { it.status.equals(FirebaseConstants.STATUS_ACTIVE, true) || it.status.equals("STARTED", true) || it.status.equals("IN PROGRESS", true) || it.status.equals("ONGOING", true) }
                .sortedBy { it.name.lowercase() }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun saveTask(task: TaskModel) {
        requireAdmin()
        require(task.title.isNotBlank()) { "Task title is required" }
        require(task.assignedToUid.isNotBlank()) { "Employee is required" }
        require(task.projectId.isNotBlank()) { "Project is required" }
        val employee = usersCollection.document(task.assignedToUid).get().await().toObject(UserProfile::class.java)
            ?: throw IllegalArgumentException("Assigned employee not found")
        require(employee.active && !employee.role.equals(FirebaseConstants.ROLE_ADMIN, true)) { "Invalid assigned employee" }
        val id = task.id.ifBlank { tasksCollection.document().id }
        val safeTask = task.copy(id = id, assignedToUid = employee.uid, assignedToName = employee.name)
        tasksCollection.document(id).set(safeTask).await()
        tasksCollection.document(id).collection("history").add(mapOf("status" to safeTask.status, "action" to "CREATED", "at" to Timestamp.now(), "actorUid" to requireUid())).await()
        notificationRepository.createForUser(safeTask.assignedToUid, "TASK_ASSIGNED", "New Task Assigned", "${safeTask.title} has been assigned to you.", id, "tasks")
    }

    suspend fun updateTaskStatus(id: String, status: String, actorUid: String, isAdmin: Boolean) {
        require(status in setOf("OPEN", "IN_PROGRESS", "BLOCKED", "COMPLETED")) { "Invalid task status" }
        require(id.isNotBlank()) { "Invalid task" }
        val currentUid = requireUid()
        val profile = currentProfile()
        require(profile.active && hasTaskAccess(profile)) { "Task access denied" }
        val actualAdmin = profile.role.equals(FirebaseConstants.ROLE_ADMIN, true)
        require(isAdmin == actualAdmin || !isAdmin) { "Invalid authorization state" }
        val ref = tasksCollection.document(id)
        val snapshot = ref.get().await()
        require(snapshot.exists()) { "Task not found" }
        val task = snapshot.toObject(TaskModel::class.java) ?: throw IllegalStateException("Task data unavailable")
        if (!actualAdmin) require(task.assignedToUid == currentUid) { "You are not allowed to update this task" }
        ref.update(mapOf("status" to status, "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(), "updatedByUid" to currentUid)).await()
        ref.collection("history").add(mapOf("status" to status, "action" to "STATUS_CHANGED", "at" to Timestamp.now(), "actorUid" to currentUid)).await()
        if (actualAdmin) notificationRepository.createForUser(task.assignedToUid, "TASK_STATUS", "Task status updated", "${task.title}: $status", id, "tasks")
        else notificationRepository.createForAdmins("TASK_STATUS", "Task status updated", "${task.assignedToName.ifBlank { currentUid }} changed ${task.title} to $status", currentUid, task.assignedToName, id, "tasks")
    }
}
