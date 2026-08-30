package com.shahsurveyors.myapplication.models

import com.google.firebase.Timestamp

/**
 * Admin-controlled employee compensation. Every change creates a new record,
 * so salary increments are preserved historically instead of overwriting the
 * previous salary.
 */
data class SalaryProfileModel(
    val id: String = "",
    val employeeUid: String = "",
    val employeeName: String = "",
    val payType: String = "MONTHLY", // MONTHLY or DAILY
    val monthlySalary: Double = 0.0,
    val dailyRate: Double = 0.0,
    val overtimeRatePerHour: Double = 0.0,
    val effectiveFrom: String = "", // yyyy-MM-dd
    val effectiveTo: String? = null,
    val note: String = "",
    val setByUid: String = "",
    val setAt: Timestamp? = null,
    val active: Boolean = true
)
