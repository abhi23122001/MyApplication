package com.shahsurveyors.myapplication.ui.finance

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.YearMonth

class SalaryViewModel(
    private val payrollCalculator: PayrollCalculator
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    val salaryRecords = mutableStateListOf<SalaryData>()
    var selectedMonth by mutableStateOf(YearMonth.now())
        private set

    fun previousMonth() {
        selectedMonth = selectedMonth.minusMonths(1)
        fetchSalaries()
    }

    fun nextMonth() {
        selectedMonth = selectedMonth.plusMonths(1)
        fetchSalaries()
    }

    fun fetchSalaries() {
        viewModelScope.launch {
            isLoading = true
            try {
                val result = payrollCalculator.calculate(selectedMonth)
                salaryRecords.clear()
                salaryRecords.addAll(result)
            } catch (_: Exception) {
                salaryRecords.clear()
            } finally {
                isLoading = false
            }
        }
    }
}
