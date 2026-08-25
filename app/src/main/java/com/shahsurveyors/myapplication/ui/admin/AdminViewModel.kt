package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.data.*
import com.shahsurveyors.myapplication.data.local.BankDetails
import com.shahsurveyors.myapplication.data.local.CompanyProfile
import com.shahsurveyors.myapplication.data.local.TermConditionEntity
import com.shahsurveyors.myapplication.models.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminViewModel(
    private val billingRepository: BillingRepository,
    private val userRepository: UserRepository,
    private val expenseRepository: ExpenseRepository,
    private val attendanceRepository: AttendanceRepository,
    private val projectRepository: ProjectRepository,
    private val equipmentRepository: EquipmentRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val leaveRepository = LeaveRepository(firestore)
    private val attendanceCorrectionRepository = AttendanceCorrectionRepository(firestore)

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
    var pendingLeaves by mutableStateOf<List<LeaveRequestModel>>(emptyList())
        private set
    var pendingAttendanceCorrections by mutableStateOf<List<AttendanceCorrectionRequest>>(emptyList())
        private set
    var pendingSalaryAdvances by mutableStateOf<List<SalaryAdvanceRequest>>(emptyList())
        private set
    var attendanceSummary by mutableStateOf<List<AttendanceRecord>>(emptyList())
        private set
    var presentCount by mutableIntStateOf(0)
        private set
    var absentCount by mutableIntStateOf(0)
        private set

    val companyProfile: StateFlow<CompanyProfile?> = billingRepository.companyProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val bankDetails: StateFlow<BankDetails?> = billingRepository.bankDetails.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allTerms: StateFlow<List<TermConditionEntity>> = billingRepository.allTerms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingApprovalCount: Int
        get() = pendingExpenses.size + pendingLeaves.size + pendingAttendanceCorrections.size + pendingSalaryAdvances.size

    fun fetchAdminData() {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val users = userRepository.getAllEmployees()
                pendingUsers = users.filter { !it.approved }
                allEmployees = users
                pendingExpenses = expenseRepository.getAllExpenses().filter { it.status == "PENDING" }
                pendingLeaves = leaveRepository.getAllRequests().filter { it.status == "PENDING" }
                pendingAttendanceCorrections = attendanceCorrectionRepository.getPendingRequests()
                pendingSalaryAdvances = firestore.collection("salaryAdvanceRequests")
                    .whereEqualTo("status", "PENDING")
                    .get().await().toObjects(SalaryAdvanceRequest::class.java)
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
        val attendanceList = try { attendanceRepository.getTodayAllAttendance() } catch (e: Exception) { emptyList() }
        val sortedAttendance = attendanceList.sortedByDescending { it.punchInTime?.seconds ?: 0L }
        attendanceSummary = sortedAttendance
        presentCount = sortedAttendance.size
        val existingUids = allEmployees.map { it.uid }.toHashSet()
        val missingProfiles = sortedAttendance.filter { it.uid.isNotBlank() && it.uid !in existingUids }.map {
            UserProfile(uid = it.uid, name = it.userName, role = "employee", approved = true, active = true)
        }
        if (missingProfiles.isNotEmpty()) allEmployees = allEmployees + missingProfiles
        absentCount = (allEmployees.count { it.active } - presentCount).coerceAtLeast(0)
    }

    fun refreshAttendance() {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            try {
                allEmployees = userRepository.getAllEmployees()
                loadTodayAttendance()
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Unable to refresh attendance"
            } finally { isLoading = false }
        }
    }

    fun approveUser(uid: String, access: String) {
        viewModelScope.launch {
            try { userRepository.updateUserStatus(uid, true, true); fetchAdminData() }
            catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to approve user" }
        }
    }

    fun setEmployeeActive(uid: String, active: Boolean) {
        viewModelScope.launch {
            try { userRepository.updateUserStatus(uid, true, active); fetchAdminData() }
            catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to update employee status" }
        }
    }

    fun approveExpense(id: String, status: String) {
        viewModelScope.launch {
            try { expenseRepository.updateExpenseStatus(id, status); fetchAdminData() }
            catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to update expense" }
        }
    }

    fun reviewLeave(id: String, approved: Boolean, remark: String = "") {
        viewModelScope.launch {
            try {
                val adminUid = FirebaseAuth.getInstance().currentUser?.uid ?: "ADMIN"
                leaveRepository.updateStatus(id, if (approved) "APPROVED" else "REJECTED", adminUid)
                if (remark.isNotBlank()) firestore.collection("leaveRequests").document(id).update("adminRemark", remark).await()
                fetchAdminData()
            } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to review leave" }
        }
    }

    fun reviewAttendanceCorrection(id: String, approved: Boolean, remark: String = "") {
        viewModelScope.launch {
            try {
                val adminUid = FirebaseAuth.getInstance().currentUser?.uid ?: "ADMIN"
                attendanceCorrectionRepository.reviewRequest(id, approved, adminUid, remark)
                fetchAdminData()
            } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to review attendance correction" }
        }
    }

    fun reviewSalaryAdvance(id: String, approved: Boolean, remark: String = "") {
        viewModelScope.launch {
            try {
                val adminUid = FirebaseAuth.getInstance().currentUser?.uid ?: "ADMIN"
                firestore.collection("salaryAdvanceRequests").document(id).update(
                    mapOf("status" to if (approved) "APPROVED" else "REJECTED", "approvedBy" to adminUid, "adminRemark" to remark, "updatedAt" to Timestamp.now())
                ).await()
                fetchAdminData()
            } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to review salary advance" }
        }
    }

    fun updateCompanyProfile(profile: CompanyProfile) { viewModelScope.launch { try { billingRepository.updateCompanyProfile(profile) } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to update company profile" } } }
    fun updateBankDetails(details: BankDetails) { viewModelScope.launch { try { billingRepository.updateBankDetails(details) } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to update bank details" } } }
    fun saveTerm(term: TermConditionEntity) { viewModelScope.launch { try { billingRepository.saveTerm(term) } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to save term" } } }
    fun deleteTerm(term: TermConditionEntity) { viewModelScope.launch { try { billingRepository.deleteTerm(term) } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to delete term" } } }
    fun clearError() { errorMessage = null }
}
