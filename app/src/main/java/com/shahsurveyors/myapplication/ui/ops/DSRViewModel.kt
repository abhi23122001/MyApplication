package com.shahsurveyors.myapplication.ui.ops

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.data.DSRRepository
import com.shahsurveyors.myapplication.data.StorageRepository
import com.shahsurveyors.myapplication.models.DSRModel
import com.shahsurveyors.myapplication.models.ProjectModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DSRViewModel(private val dsrRepository: DSRRepository, private val storageRepository: StorageRepository) : ViewModel() {
    var isLoading by mutableStateOf(false); private set
    var errorMessage by mutableStateOf<String?>(null); private set
    val dsrList = mutableStateListOf<DSRModel>()
    val projects = mutableStateListOf<ProjectModel>()

    fun loadProjects() {
        viewModelScope.launch {
            isLoading = true; errorMessage = null
            try {
                val loaded = FirebaseFirestore.getInstance().collection("projects").get().await().documents
                    .mapNotNull { doc -> doc.toObject(ProjectModel::class.java)?.let { project -> if (project.id.isBlank()) project.copy(id = doc.id) else project } }
                    .filter { status -> status.status.equals("ACTIVE", true) || status.status.equals("STARTED", true) || status.status.equals("IN PROGRESS", true) || status.status.equals("ONGOING", true) }
                    .sortedBy { it.name.lowercase() }
                projects.clear(); projects.addAll(loaded)
            } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to load projects" }
            finally { isLoading = false }
        }
    }

    fun fetchDSR(projectId: String) {
        if (projectId.isBlank()) return
        viewModelScope.launch {
            isLoading = true; errorMessage = null
            try { dsrList.clear(); dsrList.addAll(dsrRepository.getDSRForProject(projectId)) }
            catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to load DSR" }
            finally { isLoading = false }
        }
    }

    fun submitDSR(dsr: DSRModel, onDone: () -> Unit = {}) {
        if (dsr.projectId.isBlank()) { errorMessage = "Please select a project"; return }
        viewModelScope.launch {
            isLoading = true; errorMessage = null
            try { dsrRepository.saveDSR(dsr); fetchDSR(dsr.projectId); onDone() }
            catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to submit DSR" }
            finally { isLoading = false }
        }
    }

    fun clearError() { errorMessage = null }
}
