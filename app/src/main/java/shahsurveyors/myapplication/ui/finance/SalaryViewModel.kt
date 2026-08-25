package com.shahsurveyors.myapplication.ui.finance

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.SalaryRepository
import com.shahsurveyors.myapplication.data.UserRepository
import com.shahsurveyors.myapplication.models.SalaryPayrollModel
import kotlinx.coroutines.launch
import java.time.YearMonth

class SalaryViewModel(
    private val userRepository: UserRepository,
    private val salaryRepository: SalaryRepository
) : ViewModel() {
    var isLoading by mutableStateOf(false)
    var selectedMonth by mutableStateOf(YearMonth.now())
    val salaryRecords = mutableStateListOf<SalaryData>()

    fun fetchSalaries(month: YearMonth = selectedMonth) {
        selectedMonth = month
        viewModelScope.launch {
            isLoading = true
            try {
                val users = userRepository.getAllEmployees()
                val calculated = users.mapNotNull { user ->
                    runCatching {
                        salaryRepository.calculateMonthlyPayroll(
                            employeeUid = user.uid,
                            employeeName = user.name,
                            department = user.department,
                            month = month
                        )
                    }.getOrNull()
                }
                salaryRecords.clear()
                salaryRecords.addAll(calculated.map { it.toSalaryData() })
            } finally {
                isLoading = false
            }
        }
    }

    fun previousMonth() = fetchSalaries(selectedMonth.minusMonths(1))
    fun nextMonth() = fetchSalaries(selectedMonth.plusMonths(1))
}

private fun SalaryPayrollModel.toSalaryData() = SalaryData(
    id = employeeUid.take(6),
    name = employeeName,
    dept = department,
    presentDays = presentDays,
    paidLeaveDays = paidLeaveDays,
    unpaidLeaveDays = unpaidLeaveDays,
    absentDays = absentDays,
    lateMinutes = lateMinutes,
    earlyOutMinutes = earlyOutMinutes,
    overtimeMinutes = overtimeMinutes,
    advances = advances,
    deductions = attendanceDeduction + lateEarlyDeduction + otherDeductions,
    basicSalary = baseSalary,
    overtimePay = overtimePay,
    netSalary = netSalary,
    month = month,
    status = status
)

data class SalaryData(
    val id: String = "",
    val name: String = "",
    val dept: String = "",
    val presentDays: Int = 0,
    val paidLeaveDays: Int = 0,
    val unpaidLeaveDays: Int = 0,
    val absentDays: Int = 0,
    val lateMinutes: Int = 0,
    val earlyOutMinutes: Int = 0,
    val overtimeMinutes: Int = 0,
    val advances: Double = 0.0,
    val deductions: Double = 0.0,
    val basicSalary: Double = 0.0,
    val overtimePay: Double = 0.0,
    val netSalary: Double = 0.0,
    val month: String = "",
    val status: String = "CALCULATED"
)
