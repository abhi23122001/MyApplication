package com.shahsurveyors.myapplication.utils

import com.shahsurveyors.myapplication.models.AttendanceRecord

/**
 * Pure attendance summary calculation shared by UI/reporting/payroll.
 * Keeps counting rules in one place and avoids duplicate calculations.
 */
data class AttendanceSummary(
    val totalRecords: Int = 0,
    val presentDays: Int = 0,
    val approvedLeaveDays: Int = 0,
    val absentDays: Int = 0,
    val overtimeMinutes: Int = 0,
    val lateMinutes: Int = 0,
    val earlyOutMinutes: Int = 0,
    val totalWorkingMinutes: Int = 0,
    val missingPunchOutDays: Int = 0
)

object AttendanceSummaryCalculator {
    fun calculate(records: List<AttendanceRecord>, expectedWorkingDays: Int? = null): AttendanceSummary {
        val present = records.count { it.status != "APPROVED_LEAVE" && it.status != "ABSENT" }
        val leave = records.count { it.status == "APPROVED_LEAVE" }
        val absent = records.count { it.status == "ABSENT" }
        val missing = records.count { it.punchOutMissing || (it.punchInTime != null && it.punchOutTime == null && it.status != "APPROVED_LEAVE") }
        val expectedAbsent = expectedWorkingDays?.minus(records.count { it.status != "APPROVED_LEAVE" })?.coerceAtLeast(0) ?: 0

        return AttendanceSummary(
            totalRecords = records.size,
            presentDays = present,
            approvedLeaveDays = leave,
            absentDays = maxOf(absent, expectedAbsent),
            overtimeMinutes = records.sumOf { it.overtimeMinutes.coerceAtLeast(0) },
            lateMinutes = records.sumOf { it.lateMinutes.coerceAtLeast(0) },
            earlyOutMinutes = records.sumOf { it.earlyOutMinutes.coerceAtLeast(0) },
            totalWorkingMinutes = records.sumOf { it.workingMinutes.coerceAtLeast(0) },
            missingPunchOutDays = missing
        )
    }
}
