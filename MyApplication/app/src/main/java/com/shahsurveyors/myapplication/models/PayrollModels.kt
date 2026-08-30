package com.shahsurveyors.myapplication.models

/**
 * Historical & effective-date based salary profile.
 * Every increment or rate change creates a NEW period and closes the previous active period.
 */
data class SalaryProfileModel(
    val id: String = "",
    val employeeUid: String = "",
    val employeeName: String = "",
    val employeeId: String = "",
    val department: String = "SURVEY",
    val payType: String = "MONTHLY", // MONTHLY, DAILY, HOURLY
    val monthlySalary: Double = 0.0,
    val dailyRate: Double = 0.0,
    val overtimeRatePerHour: Double = 0.0,
    val effectiveFrom: String = "", // YYYY-MM-DD
    val effectiveTo: String? = null, // YYYY-MM-DD or null if currently active
    val note: String = "",
    val setByUid: String = "",
    val setByName: String = "",
    val setAt: Long = System.currentTimeMillis(),
    val active: Boolean = true
)

/**
 * Advance Salary Request submitted by staff and reviewed by Admin.
 */
data class AdvanceSalaryRequest(
    val id: String = "",
    val employeeUid: String = "",
    val employeeName: String = "",
    val employeeId: String = "",
    val department: String = "",
    val requestedAmount: Double = 0.0,
    val approvedAmount: Double = 0.0,
    val installments: Int = 1, // Number of monthly installments (EMI)
    val requestedMonth: String = "", // YYYY-MM (e.g. 2026-08)
    val reason: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val appliedAt: Long = System.currentTimeMillis(),
    val decidedAt: Long? = null,
    val decidedByUid: String? = null,
    val decidedByName: String? = null,
    val note: String = ""
)

/**
 * Leave Request submitted by staff and approved by Admin.
 */
data class LeaveRequest(
    val id: String = "",
    val employeeUid: String = "",
    val employeeName: String = "",
    val employeeId: String = "",
    val department: String = "",
    val startDate: String = "", // YYYY-MM-DD
    val endDate: String = "", // YYYY-MM-DD
    val totalDays: Int = 1,
    val leaveType: String = "CASUAL", // CASUAL, SICK, PAID, UNPAID
    val reason: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, CANCELLED
    val appliedAt: Long = System.currentTimeMillis(),
    val decidedAt: Long? = null,
    val decidedByUid: String? = null,
    val decidedByName: String? = null,
    val note: String = ""
)

/**
 * Task Assignment created by Admin and executed by Employee.
 */
data class TaskModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val projectSite: String = "",
    val assignedToUid: String = "",
    val assignedToName: String = "",
    val assignedToDept: String = "",
    val assignedByUid: String = "",
    val assignedByName: String = "",
    val deadline: String = "", // YYYY-MM-DD
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, URGENT
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val completionNotes: String = ""
)

/**
 * Real-time In-App Notification alert.
 */
data class AppNotification(
    val id: String = "",
    val targetUid: String = "", // Empty for ALL (Broadcast), or specific employee UID
    val title: String = "",
    val message: String = "",
    val type: String = "BROADCAST", // ATTENDANCE_ALERT, LEAVE, ADVANCE, TASK, BROADCAST
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val deepLinkRoute: String? = null
)

/**
 * Final calculated monthly payroll breakdown for an employee.
 * Single source of truth across UI and PDF Salary Slip.
 */
data class PayrollRecord(
    val id: String = "",
    val employeeUid: String = "",
    val name: String = "",
    val employeeId: String = "",
    val dept: String = "",
    val role: String = "",
    val salaryMonth: String = "", // YYYY-MM
    val year: Int = 0,
    val monthName: String = "",

    // Base salary rates
    val baseMonthlySalary: Double = 0.0,
    val dailyRate: Double = 0.0,
    val overtimeRatePerHour: Double = 0.0,
    val effectiveSalaryPeriod: String = "",

    // Days breakdown
    val totalDaysInMonth: Int = 30,
    val workingDaysInMonth: Int = 26,
    val presentDays: Int = 0,
    val approvedLeaveDays: Int = 0,
    val absentDays: Int = 0,
    val overtimeHours: Double = 0.0,

    // Financial breakdown
    val grossSalaryEarned: Double = 0.0,
    val absenceDeduction: Double = 0.0,
    val overtimePay: Double = 0.0,
    val advanceDeduction: Double = 0.0,
    val otherDeductions: Double = 0.0,
    val totalDeductions: Double = 0.0,
    val netSalary: Double = 0.0,

    val status: String = "CALCULATED", // CALCULATED, APPROVED, PAID
    val paymentDate: Long? = null,
    val remarks: String = "",
    val generatedAt: Long = System.currentTimeMillis()
)
