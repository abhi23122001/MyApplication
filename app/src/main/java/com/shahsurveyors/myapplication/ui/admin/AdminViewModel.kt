package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val advanceSalaryRepository = AdvanceSalaryRepository(firestore)

    var isLoading by mutableStateOf(false); private set
    var errorMessage by mutableStateOf<String?>(null); private set
    var pendingUsers by mutableStateOf<List<UserProfile>>(emptyList()); private set
    var allEmployees by mutableStateOf<List<UserProfile>>(emptyList()); private set
    var pendingExpenses by mutableStateOf<List<ExpenseRecord>>(emptyList()); private set
    var allExpenses by mutableStateOf<List<ExpenseRecord>>(emptyList()); private set
    var pendingLeaves by mutableStateOf<List<LeaveRequestModel>>(emptyList()); private set
    var pendingAttendanceCorrections by mutableStateOf<List<AttendanceCorrectionRequest>>(emptyList()); private set
    var pendingSalaryAdvances by mutableStateOf<List<AdvanceSalaryRequest>>(emptyList()); private set
    var attendanceSummary by mutableStateOf<List<AttendanceRecord>>(emptyList()); private set
    var presentCount by mutableIntStateOf(0); private set
    var absentCount by mutableIntStateOf(0); private set

    val companyProfile: StateFlow<CompanyProfile?> = billingRepository.companyProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val bankDetails: StateFlow<BankDetails?> = billingRepository.bankDetails.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allTerms: StateFlow<List<TermConditionEntity>> = billingRepository.allTerms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingApprovalCount: Int
        get() = pendingExpenses.size + pendingLeaves.size + pendingAttendanceCorrections.size + pendingSalaryAdvances.size

    fun fetchAdminData() {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true; errorMessage = null
            try {
                val users = userRepository.getAllEmployees()
                pendingUsers = users.filter { !it.approved }; allEmployees = users
                allExpenses = expenseRepository.getAllExpenses(); pendingExpenses = allExpenses.filter { it.status == "PENDING" }
                pendingLeaves = leaveRepository.getAllRequests().filter { it.status == "PENDING" }
                pendingAttendanceCorrections = attendanceCorrectionRepository.getPendingRequests()
                pendingSalaryAdvances = advanceSalaryRepository.getPending(); loadTodayAttendance()
            } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to load admin data" }
            finally { isLoading = false }
        }
    }

    private suspend fun loadTodayAttendance() {
        val attendanceList = try { attendanceRepository.getTodayAllAttendance() } catch (_: Exception) { emptyList() }
        val sortedAttendance = attendanceList.sortedByDescending { it.punchInTime?.seconds ?: 0L }
        attendanceSummary = sortedAttendance; presentCount = sortedAttendance.size
        val existingUids = allEmployees.map { it.uid }.toHashSet()
        val missingProfiles = sortedAttendance.filter { it.uid.isNotBlank() && it.uid !in existingUids }.map { UserProfile(uid = it.uid, name = it.userName, role = "employee", approved = true, active = true) }
        if (missingProfiles.isNotEmpty()) allEmployees = allEmployees + missingProfiles
        absentCount = (allEmployees.count { it.active } - presentCount).coerceAtLeast(0)
    }

    fun refreshAttendance() { if (isLoading) return; viewModelScope.launch { isLoading = true; try { allEmployees = userRepository.getAllEmployees(); loadTodayAttendance() } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to refresh attendance" } finally { isLoading = false } } }
    fun approveUser(uid: String, access: String) { viewModelScope.launch { try { userRepository.updateUserStatus(uid, true, true); if (access.isNotBlank()) userRepository.updateUserAccess(uid, access); fetchAdminData() } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to approve user" } } }
    fun setEmployeeActive(uid: String, active: Boolean) { viewModelScope.launch { try { userRepository.updateUserStatus(uid, true, active); fetchAdminData() } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to update employee status" } } }
    fun updateEmployeeAccess(uid: String, access: String) { viewModelScope.launch { try { userRepository.updateUserAccess(uid, access); fetchAdminData() } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to update employee access" } } }

    fun approveExpense(id: String, status: String, remark: String = "") {
        viewModelScope.launch { try {
            val adminUid = FirebaseAuth.getInstance().currentUser?.uid ?: "ADMIN"
            val adminName = allEmployees.firstOrNull { it.uid == adminUid }?.name ?: "Admin"
            expenseRepository.updateExpenseReview(id, status, remark, adminUid, adminName); fetchAdminData()
        } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to update expense" } }
    }
    fun markExpensePaid(id: String) { viewModelScope.launch { try { expenseRepository.markExpensePaid(id); fetchAdminData() } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to mark expense paid" } } }
    fun markExpenseUnpaid(id: String) { viewModelScope.launch { try { expenseRepository.markExpenseUnpaid(id); fetchAdminData() } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to mark expense unpaid" } } }
    fun reviewLeave(id: String, approved: Boolean, remark: String = "") { viewModelScope.launch { try { val adminUid = FirebaseAuth.getInstance().currentUser?.uid ?: "ADMIN"; leaveRepository.updateStatus(id, if (approved) "APPROVED" else "REJECTED", adminUid); if (remark.isNotBlank()) firestore.collection("leaveRequests").document(id).update("adminRemark", remark); fetchAdminData() } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to review leave" } } }
    fun reviewAttendanceCorrection(id: String, approved: Boolean, remark: String = "") { viewModelScope.launch { try { val adminUid = FirebaseAuth.getInstance().currentUser?.uid ?: "ADMIN"; attendanceCorrectionRepository.reviewRequest(id, approved, adminUid, remark); fetchAdminData() } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to review attendance correction" } } }
    fun reviewSalaryAdvance(id: String, approved: Boolean, remark: String = "") { viewModelScope.launch { try { val adminUid = FirebaseAuth.getInstance().currentUser?.uid ?: "ADMIN"; val adminName = allEmployees.firstOrNull { it.uid == adminUid }?.name ?: "Admin"; val request = pendingSalaryAdvances.firstOrNull { it.id == id }; advanceSalaryRepository.updateDecision(id, if (approved) "APPROVED" else "REJECTED", if (approved) (request?.amount ?: 0.0) else 0.0, adminUid, if (remark.isBlank()) adminName else "$adminName - $remark"); fetchAdminData() } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to review salary advance" } } }
    fun updateCompanyProfile(profile: CompanyProfile) { viewModelScope.launch { try { billingRepository.updateCompanyProfile(profile) } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to update company profile" } } }
    fun updateBankDetails(details: BankDetails) { viewModelScope.launch { try { billingRepository.updateBankDetails(details) } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to update bank details" } } }
    fun saveTerm(term: TermConditionEntity) { viewModelScope.launch { try { billingRepository.saveTerm(term) } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to save term" } } }
    fun deleteTerm(term: TermConditionEntity) { viewModelScope.launch { try { billingRepository.deleteTerm(term) } catch (e: Exception) { errorMessage = e.localizedMessage ?: "Unable to delete term" } } }
    fun clearError() { errorMessage = null }
}
