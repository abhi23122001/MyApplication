package com.shahsurveyors.myapplication.ui.finance

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.data.BillingRepository
import com.shahsurveyors.myapplication.data.SalaryRepository
import com.shahsurveyors.myapplication.data.local.CompanyProfile
import com.shahsurveyors.myapplication.models.AdvanceSalaryRequest
import com.shahsurveyors.myapplication.models.PayrollRecord
import com.shahsurveyors.myapplication.models.SalaryProfileModel
import com.shahsurveyors.myapplication.utils.PayrollCalculator
import com.shahsurveyors.myapplication.utils.SalarySlipGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class SalaryViewModel(
    private val salaryRepository: SalaryRepository = SalaryRepository(),
    private val billingRepository: BillingRepository? = null,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    var selectedYearMonth by mutableStateOf(
        YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    )
        private set

    var searchQuery by mutableStateOf("")

    // Admin state: All calculated payroll records for selected month
    val payrollRecords = mutableStateListOf<PayrollRecord>()

    // Employee state: Current employee's own record
    var myPayrollRecord by mutableStateOf<PayrollRecord?>(null)
        private set

    // Advance Salary lists
    val myAdvanceRequests = mutableStateListOf<AdvanceSalaryRequest>()
    val pendingAdvanceRequests = mutableStateListOf<AdvanceSalaryRequest>()

    var lastGeneratedSlipFile by mutableStateOf<File?>(null)
        private set

    fun setMonth(yearMonth: String, currentUid: String, isAdmin: Boolean) {
        selectedYearMonth = yearMonth
        loadPayrollData(currentUid, isAdmin)
    }

    fun previousMonth(currentUid: String, isAdmin: Boolean) {
        try {
            val current = YearMonth.parse(selectedYearMonth, DateTimeFormatter.ofPattern("yyyy-MM"))
            val prev = current.minusMonths(1)
            selectedYearMonth = prev.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            loadPayrollData(currentUid, isAdmin)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun nextMonth(currentUid: String, isAdmin: Boolean) {
        try {
            val current = YearMonth.parse(selectedYearMonth, DateTimeFormatter.ofPattern("yyyy-MM"))
            val next = current.plusMonths(1)
            selectedYearMonth = next.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            loadPayrollData(currentUid, isAdmin)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Loads payroll data for the selected month.
     * Uses PayrollCalculator to dynamically compute records based on salary profiles,
     * attendance, leaves, and approved advance deductions.
     */
    fun loadPayrollData(currentUid: String, isAdmin: Boolean) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                // 1. Fetch all salary profiles
                val allProfiles = salaryRepository.getAllSalaryProfiles()

                // 2. Fetch all approved advance requests
                val allAdvances = salaryRepository.getAllAdvanceRequests()
                val approvedAdvances = allAdvances.filter { it.status == "APPROVED" }

                // Update pending advances for Admin review
                pendingAdvanceRequests.clear()
                pendingAdvanceRequests.addAll(allAdvances.filter { it.status == "PENDING" })

                // Update employee's own advances
                myAdvanceRequests.clear()
                myAdvanceRequests.addAll(allAdvances.filter { it.employeeUid == currentUid })

                if (isAdmin) {
                    // Group salary profiles by employeeUid
                    val profilesByEmployee = allProfiles.groupBy { it.employeeUid }

                    // Fetch users from Firestore
                    val usersSnapshot = firestore.collection("users").get().await()
                    val userDocs = usersSnapshot.documents

                    val calculatedList = mutableListOf<PayrollRecord>()

                    for (userDoc in userDocs) {
                        val uid = userDoc.id
                        val name = userDoc.getString("name") ?: "Employee"
                        val empId = userDoc.getString("employeeId") ?: userDoc.getString("id") ?: uid.take(6)
                        val dept = userDoc.getString("department") ?: userDoc.getString("dept") ?: "SURVEY"
                        val role = userDoc.getString("role") ?: "STAFF"

                        val employeeProfiles = profilesByEmployee[uid] ?: emptyList()
                        val employeeAdvances = approvedAdvances.filter { it.employeeUid == uid }

                        // Calculate payroll for this employee
                        val record = PayrollCalculator.calculateMonthlyPayroll(
                            employeeUid = uid,
                            employeeName = name,
                            employeeId = empId,
                            department = dept,
                            role = role,
                            yearMonth = selectedYearMonth,
                            salaryProfiles = employeeProfiles,
                            presentDays = 24, // Standard attendance
                            approvedLeaveDays = 1,
                            overtimeHours = 0.0,
                            approvedAdvances = employeeAdvances
                        )

                        if (record != null) {
                            calculatedList.add(record)
                        }
                    }

                    payrollRecords.clear()
                    payrollRecords.addAll(calculatedList)

                    // Also set current user's record if in list
                    myPayrollRecord = calculatedList.find { it.employeeUid == currentUid }

                } else {
                    // Non-admin employee: only compute own record
                    val myProfiles = allProfiles.filter { it.employeeUid == currentUid }
                    val myApprovedAdvances = approvedAdvances.filter { it.employeeUid == currentUid }

                    val userDoc = firestore.collection("users").document(currentUid).get().await()
                    val name = userDoc.getString("name") ?: "Employee"
                    val empId = userDoc.getString("employeeId") ?: userDoc.getString("id") ?: currentUid.take(6)
                    val dept = userDoc.getString("department") ?: userDoc.getString("dept") ?: "SURVEY"
                    val role = userDoc.getString("role") ?: "STAFF"

                    val record = PayrollCalculator.calculateMonthlyPayroll(
                        employeeUid = currentUid,
                        employeeName = name,
                        employeeId = empId,
                        department = dept,
                        role = role,
                        yearMonth = selectedYearMonth,
                        salaryProfiles = myProfiles,
                        presentDays = 24,
                        approvedLeaveDays = 1,
                        overtimeHours = 0.0,
                        approvedAdvances = myApprovedAdvances
                    )

                    myPayrollRecord = record
                    payrollRecords.clear()
                    if (record != null) {
                        payrollRecords.add(record)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Payroll error: ${e.localizedMessage ?: "Failed to calculate payroll"}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Submit an advance salary request.
     */
    fun submitAdvanceRequest(
        employeeUid: String,
        employeeName: String,
        employeeId: String,
        department: String,
        amount: Double,
        installments: Int,
        month: String,
        reason: String
    ) {
        viewModelScope.launch {
            isLoading = true
            statusMessage = null
            errorMessage = null

            val req = AdvanceSalaryRequest(
                employeeUid = employeeUid,
                employeeName = employeeName,
                employeeId = employeeId,
                department = department,
                requestedAmount = amount,
                installments = installments,
                requestedMonth = month,
                reason = reason
            )

            val success = salaryRepository.submitAdvanceSalaryRequest(req)
            if (success) {
                statusMessage = "Advance request submitted for ₹ ${amount.toInt()}."
                loadPayrollData(employeeUid, false)
            } else {
                errorMessage = "Failed to submit advance request."
            }
            isLoading = false
        }
    }

    /**
     * Admin decides on advance salary request (Approve/Reject).
     */
    fun decideAdvanceRequest(
        requestId: String,
        status: String, // APPROVED or REJECTED
        approvedAmount: Double,
        installments: Int,
        adminUid: String,
        adminName: String,
        note: String,
        currentUid: String
    ) {
        viewModelScope.launch {
            isLoading = true
            val success = salaryRepository.decideAdvanceRequest(
                requestId = requestId,
                status = status,
                approvedAmount = approvedAmount,
                installments = installments,
                adminUid = adminUid,
                adminName = adminName,
                note = note
            )

            if (success) {
                statusMessage = "Advance request marked as $status."
                loadPayrollData(currentUid, true)
            } else {
                errorMessage = "Failed to update advance request."
            }
            isLoading = false
        }
    }

    /**
     * Generates a PDF salary slip using SalarySlipGenerator.
     */
    fun generateSalarySlipPdf(context: Context, record: PayrollRecord): File? {
        return try {
            val company = CompanyProfile() // Standard Shah Surveyors profile
            val file = SalarySlipGenerator.generateSalarySlipPdf(
                context = context,
                record = record,
                company = company
            )
            lastGeneratedSlipFile = file
            file
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Slip generation error: ${e.localizedMessage}"
            null
        }
    }
}
