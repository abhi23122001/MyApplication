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
import com.shahsurveyors.myapplication.models.*
import com.shahsurveyors.myapplication.network.RetrofitClient
import com.shahsurveyors.myapplication.utils.PayrollCalculator
import com.shahsurveyors.myapplication.utils.SalarySlipGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SalaryViewModel(
    private val salaryRepository: SalaryRepository = SalaryRepository(),
    private val billingRepository: BillingRepository? = null,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var is360Loading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    var selectedYearMonth by mutableStateOf(
        SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(Date())
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

    // Selected 360 Employee details
    var selected360Report by mutableStateOf<Employee360Report?>(null)
        private set

    var lastGeneratedSlipFile by mutableStateOf<File?>(null)
        private set

    fun setMonth(yearMonth: String, currentUid: String, isAdmin: Boolean) {
        selectedYearMonth = yearMonth
        loadPayrollData(currentUid, isAdmin)
    }

    fun previousMonth(currentUid: String, isAdmin: Boolean) {
        try {
            val parts = selectedYearMonth.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1 // 0-indexed
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.add(Calendar.MONTH, -1)
            selectedYearMonth = SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(cal.time)
            loadPayrollData(currentUid, isAdmin)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun nextMonth(currentUid: String, isAdmin: Boolean) {
        try {
            val parts = selectedYearMonth.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() - 1
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.add(Calendar.MONTH, 1)
            selectedYearMonth = SimpleDateFormat("yyyy-MM", Locale.ENGLISH).format(cal.time)
            loadPayrollData(currentUid, isAdmin)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Loads 360° Monthly Employee Details when Admin clicks an employee name.
     */
    fun loadEmployee360(employeeUid: String, yearMonth: String = selectedYearMonth) {
        viewModelScope.launch {
            is360Loading = true
            try {
                val report = salaryRepository.getEmployee360Report(employeeUid, yearMonth)
                selected360Report = report
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = "Unable to load employee details: ${e.localizedMessage}"
            } finally {
                is360Loading = false
            }
        }
    }

    fun clear360Report() {
        selected360Report = null
    }

    /**
     * Loads payroll data for the selected month.
     * Computes real attendance, half days, absent days, and approved advance deductions.
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

                // 3. Query all attendance records for this month
                val monthStart = "$selectedYearMonth-01"
                val monthEnd = "$selectedYearMonth-31"
                val attSnapshot = firestore.collection("attendance")
                    .whereGreaterThanOrEqualTo("date", monthStart)
                    .whereLessThanOrEqualTo("date", monthEnd)
                    .get()
                    .await()
                val attDocs = attSnapshot.documents

                // 4. Query all approved leaves for this month
                val leavesSnapshot = firestore.collection("leaves")
                    .whereEqualTo("status", "APPROVED")
                    .get()
                    .await()
                val leavesList = leavesSnapshot.toObjects(LeaveRequest::class.java)

                if (isAdmin) {
                    val profilesByEmployee = allProfiles.groupBy { it.employeeUid }

                    // Fetch all users from Firestore
                    val usersSnapshot = firestore.collection("users").get().await()
                    val userDocs = usersSnapshot.documents

                    val calculatedList = mutableListOf<PayrollRecord>()

                    for (userDoc in userDocs) {
                        val uid = userDoc.id
                        val name = userDoc.getString("name") ?: "Staff User"
                        val empId = userDoc.getString("employeeId") ?: userDoc.getString("id") ?: uid.take(6).uppercase()
                        val dept = userDoc.getString("department") ?: userDoc.getString("dept") ?: "SURVEY"
                        val role = userDoc.getString("role") ?: "STAFF"
                        val salaryFallback = userDoc.getDouble("monthlySalary")
                            ?: userDoc.getDouble("salary")
                            ?: 15000.0

                        val employeeProfiles = profilesByEmployee[uid] ?: emptyList()
                        val employeeAdvances = approvedAdvances.filter { it.employeeUid == uid }

                        // Calculate attendance for this employee
                        val employeePunches = attDocs.filter {
                            val docUid = it.getString("employeeUid") ?: it.getString("uid") ?: it.getString("userUid") ?: ""
                            val docName = it.getString("staffName") ?: it.getString("name") ?: ""
                            docUid == uid || docName.equals(name, ignoreCase = true)
                        }

                        val punchesByDate = employeePunches.groupBy { it.getString("date") ?: "" }
                        var presentCount = 0
                        var halfDayCount = 0

                        for ((_, punches) in punchesByDate) {
                            val inPunch = punches.find {
                                val t = it.getString("type") ?: it.getString("action") ?: ""
                                t.contains("IN", ignoreCase = true)
                            }
                            val outPunch = punches.find {
                                val t = it.getString("type") ?: it.getString("action") ?: ""
                                t.contains("OUT", ignoreCase = true)
                            }
                            if (inPunch != null && outPunch != null) {
                                presentCount++
                            } else {
                                halfDayCount++
                            }
                        }

                        val employeeLeaves = leavesList.filter {
                            it.employeeUid == uid && (it.startDate.startsWith(selectedYearMonth) || it.endDate.startsWith(selectedYearMonth))
                        }
                        val approvedLeaveDays = employeeLeaves.sumOf { it.totalDays }

                        val record = PayrollCalculator.calculateMonthlyPayroll(
                            employeeUid = uid,
                            employeeName = name,
                            employeeId = empId,
                            department = dept,
                            role = role,
                            yearMonth = selectedYearMonth,
                            salaryProfiles = employeeProfiles,
                            fallbackMonthlySalary = salaryFallback,
                            presentDays = presentCount,
                            halfDays = halfDayCount,
                            approvedLeaveDays = approvedLeaveDays,
                            approvedAdvances = employeeAdvances
                        )

                        calculatedList.add(record)
                    }

                    payrollRecords.clear()
                    payrollRecords.addAll(calculatedList)

                    myPayrollRecord = calculatedList.find { it.employeeUid == currentUid }

                } else {
                    // Non-admin employee: compute own record
                    val myProfiles = allProfiles.filter { it.employeeUid == currentUid }
                    val myApprovedAdvances = approvedAdvances.filter { it.employeeUid == currentUid }

                    val userDoc = firestore.collection("users").document(currentUid).get().await()
                    val name = userDoc.getString("name") ?: "Staff User"
                    val empId = userDoc.getString("employeeId") ?: userDoc.getString("id") ?: currentUid.take(6).uppercase()
                    val dept = userDoc.getString("department") ?: userDoc.getString("dept") ?: "SURVEY"
                    val role = userDoc.getString("role") ?: "STAFF"
                    val salaryFallback = userDoc.getDouble("monthlySalary")
                        ?: userDoc.getDouble("salary")
                        ?: 15000.0

                    val myPunches = attDocs.filter {
                        val docUid = it.getString("employeeUid") ?: it.getString("uid") ?: it.getString("userUid") ?: ""
                        val docName = it.getString("staffName") ?: it.getString("name") ?: ""
                        docUid == currentUid || docName.equals(name, ignoreCase = true)
                    }

                    val punchesByDate = myPunches.groupBy { it.getString("date") ?: "" }
                    var presentCount = 0
                    var halfDayCount = 0

                    for ((_, punches) in punchesByDate) {
                        val inPunch = punches.find {
                            val t = it.getString("type") ?: it.getString("action") ?: ""
                            t.contains("IN", ignoreCase = true)
                        }
                        val outPunch = punches.find {
                            val t = it.getString("type") ?: it.getString("action") ?: ""
                            t.contains("OUT", ignoreCase = true)
                        }
                        if (inPunch != null && outPunch != null) {
                            presentCount++
                        } else {
                            halfDayCount++
                        }
                    }

                    val myLeaves = leavesList.filter {
                        it.employeeUid == currentUid && (it.startDate.startsWith(selectedYearMonth) || it.endDate.startsWith(selectedYearMonth))
                    }
                    val approvedLeaveDays = myLeaves.sumOf { it.totalDays }

                    val record = PayrollCalculator.calculateMonthlyPayroll(
                        employeeUid = currentUid,
                        employeeName = name,
                        employeeId = empId,
                        department = dept,
                        role = role,
                        yearMonth = selectedYearMonth,
                        salaryProfiles = myProfiles,
                        fallbackMonthlySalary = salaryFallback,
                        presentDays = presentCount,
                        halfDays = halfDayCount,
                        approvedLeaveDays = approvedLeaveDays,
                        approvedAdvances = myApprovedAdvances
                    )

                    myPayrollRecord = record
                    payrollRecords.clear()
                    payrollRecords.add(record)
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
        status: String,
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
     * Sync payroll to Google Sheets.
     */
    fun syncPayrollToGoogleSheets(record: PayrollRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = mapOf<String, Any>(
                    "action" to "SYNC_PAYROLL",
                    "month" to record.salaryMonth,
                    "monthName" to record.monthName,
                    "employeeUid" to record.employeeUid,
                    "employeeName" to record.name,
                    "employeeId" to record.employeeId,
                    "department" to record.dept,
                    "role" to record.role,
                    "baseMonthlySalary" to record.baseMonthlySalary,
                    "dailyRate" to record.dailyRate,
                    "totalDaysInMonth" to record.totalDaysInMonth,
                    "workingDaysInMonth" to record.workingDaysInMonth,
                    "presentDays" to record.presentDays,
                    "halfDays" to record.halfDays,
                    "approvedLeaveDays" to record.approvedLeaveDays,
                    "absentDays" to record.absentDays,
                    "grossSalaryEarned" to record.grossSalaryEarned,
                    "advanceDeduction" to record.advanceDeduction,
                    "absenceDeduction" to record.absenceDeduction,
                    "netSalary" to record.netSalary,
                    "status" to record.status
                )
                RetrofitClient.api.handleAction(payload)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Generates a PDF salary slip using SalarySlipGenerator.
     */
    fun generateSalarySlipPdf(context: Context, record: PayrollRecord): File? {
        return try {
            val company = CompanyProfile()
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
