package com.shahsurveyors.myapplication.ui.ops

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.ProjectRepository
import com.shahsurveyors.myapplication.models.ProjectModel
import kotlinx.coroutines.launch

class ProjectViewModel(
    private val repository: ProjectRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    val projects = mutableStateListOf<ProjectModel>()

    fun fetchProjects() {
        viewModelScope.launch {
            isLoading = true
            val list = repository.getAllProjects()
            projects.clear()
            projects.addAll(list)
            isLoading = false
        }
    }

    fun saveProject(project: ProjectModel) {
        viewModelScope.launch {
            repository.saveProject(project)
            fetchProjects()
        }
    }
}
