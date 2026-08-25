package com.shahsurveyors.myapplication.ui.finance

import com.shahsurveyors.myapplication.data.AttendanceRepository
import com.shahsurveyors.myapplication.data.LeaveRepository
import com.shahsurveyors.myapplication.data.SalaryRepository
import com.shahsurveyors.myapplication.data.UserRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * Single payroll calculation point.
 * Monthly employees are paid their configured monthly salary less absence
 * deductions, while daily employees are paid only for worked days. Approved
 * leave is paid for monthly employees. Overtime is added for both types.
 * Late/early/missing-punch metrics remain visible but are not silently
 * deducted because no company deduction rule has been configured yet.
 */
class PayrollCalculator(
    private val userRepository: UserRepository = UserRepository(),
    private val salaryRepository: SalaryRepository = SalaryRepository(),
    private val attendanceRepository: AttendanceRepository = AttendanceRepository(),
    private val leaveRepository: LeaveRepository = LeaveRepository()
) {

    suspend fun calculate(month: YearMonth): List<SalaryData> {
        val monthStart = month.atDay(1)
        val monthEnd = month.atEndOfMonth()
        val start = monthStart.toString()
        val end = monthEnd.toString()
        val scheduledWorkingDays = countWorkingDays(monthStart, monthEnd)
        val result = mutableListOf<SalaryData>()

        userRepository.getAllEmployees().forEach { user ->
            val profile = salaryRepository.getHistory(user.uid)
                .filter { it.effectiveFrom.isNotBlank() }
                .firstOrNull { it.effectiveFrom <= end && (it.effectiveTo == null || it.effectiveTo!! > start) }
                ?: return@forEach

            val attendance = attendanceRepository.getAttendanceForMonth(user.uid, start, end)
            val approvedLeaveDates = expandApprovedLeaveDates(leaveRepository.getRequestsForUser(user.uid), monthStart, monthEnd)
            val attendanceLeaveDates = attendance
                .filter { it.status == "APPROVED_LEAVE" }
                .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                .filter(::isWorkingDay)
                .toSet()

            val presentDates = attendance
                .filter { it.status != "APPROVED_LEAVE" && it.punchInTime != null }
                .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                .filter(::isWorkingDay)
                .toSet()

            // A date can never be both present and paid leave for payroll purposes.
            val paidLeaveDates = (approvedLeaveDates + attendanceLeaveDates) - presentDates
            val presentDays = presentDates.size
            val leaveDays = paidLeaveDates.size
            val absentDays = (scheduledWorkingDays - presentDays - leaveDays).coerceAtLeast(0)
            val lateCount = attendance.count { it.isLate && it.status != "APPROVED_LEAVE" }
            val earlyOutCount = attendance.count { it.isEarlyOut && it.status != "APPROVED_LEAVE" }
            val missingPunchOutCount = attendance.count { it.punchOutMissing }
            val overtimeMinutes = attendance.sumOf { it.overtimeMinutes.coerceAtLeast(0) }
            val overtimePay = overtimeMinutes / 60.0 * profile.overtimeRatePerHour

            val basePay = if (profile.payType == "DAILY") presentDays * profile.dailyRate else profile.monthlySalary
            val absenceDeduction = if (profile.payType == "MONTHLY" && scheduledWorkingDays > 0) {
                profile.monthlySalary / scheduledWorkingDays * absentDays
            } else 0.0
            val net = (basePay - absenceDeduction + overtimePay).coerceAtLeast(0.0)

            result += SalaryData(
                id = user.uid.take(6),
                name = user.name,
                dept = user.department,
                presentDays = presentDays,
                approvedLeaveDays = leaveDays,
                absentDays = absentDays,
                lateCount = lateCount,
                earlyOutCount = earlyOutCount,
                missingPunchOutCount = missingPunchOutCount,
                overtimeMinutes = overtimeMinutes,
                overtimePay = overtimePay,
                advances = 0.0,
                deductions = absenceDeduction,
                basicSalary = basePay,
                netSalary = net,
                month = month.toString(),
                year = month.year,
                payType = profile.payType,
                status = "PENDING"
            )
        }
        return result.sortedBy { it.name.lowercase() }
    }

    private fun countWorkingDays(start: LocalDate, end: LocalDate): Int {
        var count = 0
        var date = start
        while (!date.isAfter(end)) {
            if (isWorkingDay(date)) count++
            date = date.plusDays(1)
        }
        return count
    }

    private fun isWorkingDay(date: LocalDate): Boolean = date.dayOfWeek != DayOfWeek.SUNDAY

    private fun expandApprovedLeaveDates(
        requests: List<com.shahsurveyors.myapplication.models.LeaveRequestModel>,
        monthStart: LocalDate,
        monthEnd: LocalDate
    ): Set<LocalDate> {
        val result = mutableSetOf<LocalDate>()
        requests.filter { it.status == "APPROVED" }.forEach { request ->
            val from = runCatching { LocalDate.parse(request.fromDate) }.getOrNull() ?: return@forEach
            val to = runCatching { LocalDate.parse(request.toDate) }.getOrNull() ?: return@forEach
            var date = maxOf(from, monthStart)
            val last = minOf(to, monthEnd)
            while (!date.isAfter(last)) {
                if (isWorkingDay(date)) result.add(date)
                date = date.plusDays(1)
            }
        }
        return result
    }
}
