package com.shahsurveyors.myapplication.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shahsurveyors.myapplication.data.BillingRepository
import com.shahsurveyors.myapplication.data.UserRepository
import com.shahsurveyors.myapplication.data.ExpenseRepository
import com.shahsurveyors.myapplication.data.AttendanceRepository
import com.shahsurveyors.myapplication.data.ProjectRepository
import com.shahsurveyors.myapplication.data.EquipmentRepository
import com.shahsurveyors.myapplication.data.ClientRepository

class AdminViewModelFactory(
    private val billingRepository: BillingRepository,
    private val userRepository: UserRepository,
    private val expenseRepository: ExpenseRepository,
    private val attendanceRepository: AttendanceRepository,
    private val projectRepository: ProjectRepository,
    private val equipmentRepository: EquipmentRepository,
    private val clientRepository: ClientRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(
                billingRepository,
                userRepository,
                expenseRepository,
                attendanceRepository,
                projectRepository,
                equipmentRepository,
                clientRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
