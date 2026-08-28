package com.shahsurveyors.myapplication.ui.tasks

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.TaskRepository
import com.shahsurveyors.myapplication.models.ProjectModel
import com.shahsurveyors.myapplication.models.TaskModel
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {
    var isLoading by mutableStateOf(false)
    var isSaving by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    val tasks = mutableStateListOf<TaskModel>()
    val employees = mutableStateListOf<UserProfile>()
    val projects = mutableStateListOf<ProjectModel>()

    fun fetchTasks(uid: String, isAdmin: Boolean = false) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val list = if (isAdmin) repository.getAllTasks() else repository.getTasksForUser(uid)
            tasks.clear()
            tasks.addAll(list)
            isLoading = false
        }
    }

    fun loadAssignmentData() {
        viewModelScope.launch {
            isLoading = true
            employees.clear()
            projects.clear()
            employees.addAll(repository.getEmployees())
            projects.addAll(repository.getProjects())
            isLoading = false
        }
    }

    fun saveTask(task: TaskModel, uid: String, isAdmin: Boolean) {
        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            try {
                repository.saveTask(task)
                fetchTasks(uid, isAdmin)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unable to save task"
            } finally {
                isSaving = false
            }
        }
    }

    fun updateStatus(id: String, status: String, uid: String, isAdmin: Boolean = false) {
        viewModelScope.launch {
            try {
                repository.updateTaskStatus(id, status)
                fetchTasks(uid, isAdmin)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unable to update task"
            }
        }
    }
}
