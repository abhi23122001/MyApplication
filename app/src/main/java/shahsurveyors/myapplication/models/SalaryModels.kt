package com.shahsurveyors.myapplication.models

import com.google.firebase.Timestamp

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

data class SalaryPayrollModel(
    val employeeUid: String = "",
    val employeeName: String = "",
    val department: String = "",
    val month: String = "", // yyyy-MM
    val daysInMonth: Int = 0,
    val presentDays: Int = 0,
    val paidLeaveDays: Int = 0,
    val unpaidLeaveDays: Int = 0,
    val absentDays: Int = 0,
    val lateMinutes: Int = 0,
    val earlyOutMinutes: Int = 0,
    val overtimeMinutes: Int = 0,
    val baseSalary: Double = 0.0,
    val attendanceDeduction: Double = 0.0,
    val lateEarlyDeduction: Double = 0.0,
    val overtimePay: Double = 0.0,
    val advances: Double = 0.0,
    val otherDeductions: Double = 0.0,
    val netSalary: Double = 0.0,
    val status: String = "CALCULATED"
)
