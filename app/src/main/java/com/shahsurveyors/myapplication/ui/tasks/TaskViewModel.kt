package com.shahsurveyors.myapplication.ui.tasks

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.TaskRepository
import com.shahsurveyors.myapplication.models.TaskModel
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    val tasks = mutableStateListOf<TaskModel>()

    fun fetchTasks(uid: String) {
        viewModelScope.launch {
            isLoading = true
            val list = repository.getTasksForUser(uid)
            tasks.clear()
            tasks.addAll(list)
            isLoading = false
        }
    }

    fun updateStatus(id: String, status: String, uid: String) {
        viewModelScope.launch {
            repository.updateTaskStatus(id, status)
            fetchTasks(uid)
        }
    }
}
