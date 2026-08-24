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

    // =========================================================
    // LOADING / ERROR
    // =========================================================

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set


    // =========================================================
    // DATA
    // =========================================================

    var pendingUsers by mutableStateOf<List<UserProfile>>(emptyList())
        private set

    var allEmployees by mutableStateOf<List<UserProfile>>(emptyList())
        private set

    var pendingExpenses by mutableStateOf<List<ExpenseRecord>>(emptyList())
        private set

    var attendanceSummary by mutableStateOf<List<AttendanceRecord>>(emptyList())
        private set


    // =========================================================
    // ATTENDANCE STATS
    // =========================================================

    var presentCount by mutableIntStateOf(0)
        private set

    var absentCount by mutableIntStateOf(0)
        private set


    // =========================================================
    // ROOM FLOWS
    // =========================================================

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


    // =========================================================
    // FETCH ADMIN DATA
    // =========================================================

    fun fetchAdminData() {

        if (isLoading) return

        viewModelScope.launch {

            isLoading = true
            errorMessage = null

            try {

                // -------------------------------------------------
                // USERS
                // -------------------------------------------------

                val users =
                    userRepository.getAllEmployees()

                val newPendingUsers =
                    users.filter {
                        !it.approved
                    }

                // Assign complete list at once
                pendingUsers =
                    newPendingUsers

                allEmployees =
                    users


                // -------------------------------------------------
                // EXPENSES
                // -------------------------------------------------

                val expenses =
                    expenseRepository.getAllExpenses()

                pendingExpenses =
                    expenses.filter {
                        it.status == "PENDING"
                    }


                // -------------------------------------------------
                // ATTENDANCE
                // -------------------------------------------------

                loadTodayAttendance(users)

            } catch (e: Exception) {

                e.printStackTrace()

                errorMessage =
                    e.localizedMessage
                        ?: "Unable to load admin data"

            } finally {

                isLoading = false
            }
        }
    }


    // =========================================================
    // LOAD TODAY ATTENDANCE
    // =========================================================

    private suspend fun loadTodayAttendance(
        users: List<UserProfile>
    ) {

        val attendanceList =
            mutableListOf<AttendanceRecord>()

        /*
         * Simple sequential loading.
         *
         * We intentionally don't use async/awaitAll here.
         * This keeps the ViewModel state update predictable
         * and avoids unnecessary Compose recompositions.
         */

        for (user in users) {

            try {

                val record =
                    attendanceRepository
                        .getTodayAttendance(
                            user.uid
                        )

                if (record != null) {
                    attendanceList.add(record)
                }

            } catch (e: Exception) {

                e.printStackTrace()
            }
        }


        // ---------------------------------------------------------
        // SORT
        // ---------------------------------------------------------

        val sortedAttendance =
            attendanceList
                .sortedByDescending {
                    it.punchInTime?.seconds ?: 0L
                }


        // ---------------------------------------------------------
        // SINGLE STATE UPDATE
        // ---------------------------------------------------------

        attendanceSummary =
            sortedAttendance


        // ---------------------------------------------------------
        // PRESENT
        // ---------------------------------------------------------

        presentCount =
            sortedAttendance.size


        // ---------------------------------------------------------
        // TOTAL
        // ---------------------------------------------------------

        val totalEmployees =
            users.size


        // ---------------------------------------------------------
        // ABSENT
        // ---------------------------------------------------------

        absentCount =
            (
                    totalEmployees -
                            presentCount
                    ).coerceAtLeast(0)
    }


    // =========================================================
    // REFRESH ATTENDANCE
    // =========================================================

    fun refreshAttendance() {

        if (isLoading) return

        viewModelScope.launch {

            isLoading = true
            errorMessage = null

            try {

                val users =
                    userRepository.getAllEmployees()

                allEmployees =
                    users

                loadTodayAttendance(
                    users
                )

            } catch (e: Exception) {

                e.printStackTrace()

                errorMessage =
                    e.localizedMessage
                        ?: "Unable to refresh attendance"

            } finally {

                isLoading = false
            }
        }
    }


    // =========================================================
    // APPROVE USER
    // =========================================================

    fun approveUser(
        uid: String,
        access: String
    ) {

        viewModelScope.launch {

            try {

                userRepository.updateUserStatus(
                    uid = uid,
                    approved = true,
                    active = true
                )

                fetchAdminData()

            } catch (e: Exception) {

                errorMessage =
                    e.localizedMessage
                        ?: "Unable to approve user"
            }
        }
    }


    // =========================================================
    // APPROVE EXPENSE
    // =========================================================

    fun approveExpense(
        id: String,
        status: String
    ) {

        viewModelScope.launch {

            try {

                expenseRepository
                    .updateExpenseStatus(
                        id,
                        status
                    )

                fetchAdminData()

            } catch (e: Exception) {

                errorMessage =
                    e.localizedMessage
                        ?: "Unable to update expense"
            }
        }
    }


    // =========================================================
    // COMPANY PROFILE
    // =========================================================

    fun updateCompanyProfile(
        profile: CompanyProfile
    ) {

        viewModelScope.launch {

            try {

                billingRepository
                    .updateCompanyProfile(
                        profile
                    )

            } catch (e: Exception) {

                errorMessage =
                    e.localizedMessage
                        ?: "Unable to update company profile"
            }
        }
    }


    // =========================================================
    // BANK DETAILS
    // =========================================================

    fun updateBankDetails(
        details: BankDetails
    ) {

        viewModelScope.launch {

            try {

                billingRepository
                    .updateBankDetails(
                        details
                    )

            } catch (e: Exception) {

                errorMessage =
                    e.localizedMessage
                        ?: "Unable to update bank details"
            }
        }
    }


    // =========================================================
    // TERMS
    // =========================================================

    fun saveTerm(
        term: TermConditionEntity
    ) {

        viewModelScope.launch {

            try {

                billingRepository
                    .saveTerm(term)

            } catch (e: Exception) {

                errorMessage =
                    e.localizedMessage
                        ?: "Unable to save term"
            }
        }
    }


    fun deleteTerm(
        term: TermConditionEntity
    ) {

        viewModelScope.launch {

            try {

                billingRepository
                    .deleteTerm(term)

            } catch (e: Exception) {

                errorMessage =
                    e.localizedMessage
                        ?: "Unable to delete term"
            }
        }
    }


    // =========================================================
    // CLEAR ERROR
    // =========================================================

    fun clearError() {
        errorMessage = null
    }
}