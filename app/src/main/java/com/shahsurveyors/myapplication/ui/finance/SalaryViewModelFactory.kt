package com.shahsurveyors.myapplication.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shahsurveyors.myapplication.data.SalaryRepository
import com.shahsurveyors.myapplication.data.UserRepository

class SalaryViewModelFactory(
    private val userRepository: UserRepository,
    private val salaryRepository: SalaryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SalaryViewModel(userRepository, salaryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
