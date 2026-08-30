package com.shahsurveyors.myapplication.ui.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shahsurveyors.myapplication.data.AttendanceRepository
import com.shahsurveyors.myapplication.data.StorageRepository

class AttendanceViewModelFactory(
    private val attendanceRepository: AttendanceRepository,
    private val storageRepository: StorageRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                AttendanceViewModel::class.java
            )
        ) {

            @Suppress("UNCHECKED_CAST")

            return AttendanceViewModel(
                attendanceRepository = attendanceRepository,
                storageRepository = storageRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}