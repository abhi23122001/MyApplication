package com.shahsurveyors.myapplication.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shahsurveyors.myapplication.data.BillingRepository
import com.shahsurveyors.myapplication.data.SalaryRepository

class SalaryViewModelFactory(
    private val salaryRepository: SalaryRepository = SalaryRepository(),
    private val billingRepository: BillingRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalaryViewModel::class.java)) {
            return SalaryViewModel(
                salaryRepository = salaryRepository,
                billingRepository = billingRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
