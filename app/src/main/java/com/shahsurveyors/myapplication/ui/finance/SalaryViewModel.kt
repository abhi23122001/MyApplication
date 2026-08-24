package com.shahsurveyors.myapplication.ui.finance

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.UserRepository
import kotlinx.coroutines.launch

class SalaryViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    val salaryRecords = mutableStateListOf<SalaryData>()

    fun fetchSalaries() {
        viewModelScope.launch {
            isLoading = true
            val users = userRepository.getAllEmployees()
            salaryRecords.clear()
            // Mocking salary records from user profiles for now
            users.forEach { user ->
                salaryRecords.add(
                    SalaryData(
                        id = user.uid.take(6),
                        name = user.name,
                        dept = user.department,
                        netSalary = 25000.0, // Placeholder
                        presentDays = 26
                    )
                )
            }
            isLoading = false
        }
    }
}
