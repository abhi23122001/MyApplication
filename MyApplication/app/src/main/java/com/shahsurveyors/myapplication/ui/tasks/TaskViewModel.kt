package com.shahsurveyors.myapplication.ui.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.NotificationRepository
import com.shahsurveyors.myapplication.data.TaskRepository
import com.shahsurveyors.myapplication.models.AppNotification
import com.shahsurveyors.myapplication.models.TaskModel
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository = TaskRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    val taskList = mutableStateListOf<TaskModel>()

    fun loadTasks(userUid: String, isAdmin: Boolean) {
        viewModelScope.launch {
            isLoading = true
            try {
                val list = if (isAdmin) {
                    taskRepository.getAllTasks()
                } else {
                    taskRepository.getTasksForEmployee(userUid)
                }
                taskList.clear()
                taskList.addAll(list)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun createTask(
        title: String,
        description: String,
        projectSite: String,
        assignedToUid: String,
        assignedToName: String,
        assignedToDept: String,
        deadline: String,
        priority: String,
        adminUid: String,
        adminName: String
    ) {
        viewModelScope.launch {
            isLoading = true
            val task = TaskModel(
                title = title,
                description = description,
                projectSite = projectSite,
                assignedToUid = assignedToUid,
                assignedToName = assignedToName,
                assignedToDept = assignedToDept,
                assignedByUid = adminUid,
                assignedByName = adminName,
                deadline = deadline,
                priority = priority
            )

            val success = taskRepository.createTask(task)
            if (success) {
                statusMessage = "Task assigned to $assignedToName"
                // Send notification to employee
                notificationRepository.sendNotification(
                    AppNotification(
                        targetUid = assignedToUid,
                        title = "New Task Assigned",
                        message = "You have been assigned: $title (Deadline: $deadline)",
                        type = "TASK"
                    )
                )
                loadTasks(adminUid, true)
            } else {
                statusMessage = "Failed to create task"
            }
            isLoading = false
        }
    }

    fun updateTaskStatus(
        taskId: String,
        status: String,
        notes: String = "",
        userUid: String,
        isAdmin: Boolean
    ) {
        viewModelScope.launch {
            val success = taskRepository.updateTaskStatus(taskId, status, notes)
            if (success) {
                statusMessage = "Task status updated to $status"
                loadTasks(userUid, isAdmin)
            }
        }
    }
}
