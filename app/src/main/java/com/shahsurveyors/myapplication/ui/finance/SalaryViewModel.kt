package com.shahsurveyors.myapplication.ui.finance

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.SalaryRepository
import com.shahsurveyors.myapplication.data.UserRepository
import com.shahsurveyors.myapplication.models.SalaryProfileModel
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class SalaryViewModel(
    private val userRepository: UserRepository,
    private val salaryRepository: SalaryRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    val salaryRecords = mutableStateListOf<SalaryData>()
    var selectedMonth by mutableStateOf(YearMonth.now())
        private set

    fun previousMonth() { selectedMonth = selectedMonth.minusMonths(1); fetchSalaries() }
    fun nextMonth() { selectedMonth = selectedMonth.plusMonths(1); fetchSalaries() }

    fun fetchSalaries() {
        viewModelScope.launch {
            isLoading = true
            try {
                val users = userRepository.getAllEmployees()
                val result = mutableListOf<SalaryData>()
                users.forEach { user ->
                    val profile = salaryRepository.getHistory(user.uid)
                        .filter { it.effectiveFrom.isNotBlank() }
                        .firstOrNull { it.effectiveFrom <= selectedMonth.atEndOfMonth().toString() && (it.effectiveTo == null || it.effectiveTo!! > selectedMonth.atDay(1).toString()) }
                    if (profile != null) {
                        result.add(
                            SalaryData(
                                id = user.uid.take(6),
                                name = user.name,
                                dept = user.department,
                                basicSalary = if (profile.payType == "DAILY") profile.dailyRate else profile.monthlySalary,
                                netSalary = if (profile.payType == "DAILY") profile.dailyRate else profile.monthlySalary,
                                month = selectedMonth.toString(),
                                year = selectedMonth.year,
                                status = "PENDING"
                            )
                        )
                    }
                }
                salaryRecords.clear()
                salaryRecords.addAll(result)
            } finally {
                isLoading = false
            }
        }
    }
}
