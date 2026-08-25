package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.*
import com.shahsurveyors.myapplication.data.local.BankDetails
import com.shahsurveyors.myapplication.data.local.CompanyProfile
import com.shahsurveyors.myapplication.data.local.TermConditionEntity
import com.shahsurveyors.myapplication.models.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val billingRepository: BillingRepository,
    private val userRepository: UserRepository,
    private val expenseRepository: ExpenseRepository,
    private val attendanceRepository: AttendanceRepository,
    private val projectRepository: ProjectRepository,
    private val equipmentRepository: EquipmentRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var pendingUsers by mutableStateOf<List<UserProfile>>(emptyList())
        private set

    var allEmployees by mutableStateOf<List<UserProfile>>(emptyList())
        private set

    var pendingExpenses by mutableStateOf<List<ExpenseRecord>>(emptyList())
        private set

    var attendanceSummary by mutableStateOf<List<AttendanceRecord>>(emptyList())
        private set

    var presentCount by mutableIntStateOf(0)
        private set

    var absentCount by mutableIntStateOf(0)
        private set

    val companyProfile: StateFlow<CompanyProfile?> =
        billingRepository.companyProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val bankDetails: StateFlow<BankDetails?> =
        billingRepository.bankDetails.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allTerms: StateFlow<List<TermConditionEntity>> =
        billingRepository.allTerms.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun fetchAdminData() {
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                val users = userRepository.getAllEmployees()

                pendingUsers = users.filter { !it.approved }
                allEmployees = users

                val expenses = expenseRepository.getAllExpenses()
                pendingExpenses = expenses.filter { it.status == "PENDING" }

                loadTodayAttendance()
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.localizedMessage ?: "Unable to load admin data"
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun loadTodayAttendance() {
        val attendanceList = try {
            attendanceRepository.getTodayAllAttendance()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

        val sortedAttendance = attendanceList.sortedByDescending { record ->
            record.punchInTime?.seconds ?: 0L
        }

        attendanceSummary = sortedAttendance
        presentCount = sortedAttendance.size

        val existingUids = allEmployees.map { it.uid }.toHashSet()
        val missingAttendanceProfiles = sortedAttendance
            .filter { it.uid.isNotBlank() && it.uid !in existingUids }
            .map { record ->
                UserProfile(
                    uid = record.uid,
                    name = record.userName,
                    role = "employee",
                    approved = true,
                    active = true
                )
            }

        if (missingAttendanceProfiles.isNotEmpty()) {
            allEmployees = allEmployees + missingAttendanceProfiles
        }

        absentCount = (allEmployees.count { it.active } - presentCount)
            .coerceAtLeast(0)
    }

    fun refreshAttendance() {
        if (isLoading) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                allEmployees = userRepository.getAllEmployees()
                loadTodayAttendance()
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.localizedMessage ?: "Unable to refresh attendance"
            } finally {
                isLoading = false
            }
        }
    }

    fun approveUser(uid: String, access: String) {
        viewModelScope.launch {
            try {
                userRepository.updateUserStatus(uid = uid, approved = true, active = true)
                fetchAdminData()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unable to approve user"
            }
        }
    }

    fun setEmployeeActive(uid: String, active: Boolean) {
        viewModelScope.launch {
            try {
                userRepository.updateUserStatus(uid = uid, approved = true, active = active)
                fetchAdminData()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unable to update employee status"
            }
        }
    }

    fun approveExpense(id: String, status: String) {
        viewModelScope.launch {
            try {
                expenseRepository.updateExpenseStatus(id, status)
                fetchAdminData()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unable to update expense"
            }
        }
    }

    fun updateCompanyProfile(profile: CompanyProfile) {
        viewModelScope.launch {
            try {
                billingRepository.updateCompanyProfile(profile)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unable to update company profile"
            }
        }
    }

    fun updateBankDetails(details: BankDetails) {
        viewModelScope.launch {
            try {
                billingRepository.updateBankDetails(details)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unable to update bank details"
            }
        }
    }

    fun saveTerm(term: TermConditionEntity) {
        viewModelScope.launch {
            try {
                billingRepository.saveTerm(term)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unable to save term"
            }
        }
    }

    fun deleteTerm(term: TermConditionEntity) {
        viewModelScope.launch {
            try {
                billingRepository.deleteTerm(term)
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unable to delete term"
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}
