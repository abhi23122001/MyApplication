package com.shahsurveyors.myapplication.ui.finance

import com.shahsurveyors.myapplication.data.AdvanceSalaryRepository
import com.shahsurveyors.myapplication.data.AttendanceRepository
import com.shahsurveyors.myapplication.data.LeaveRepository
import com.shahsurveyors.myapplication.data.SalaryRepository
import com.shahsurveyors.myapplication.data.UserRepository
import com.shahsurveyors.myapplication.models.LeaveRequestModel
import com.shahsurveyors.myapplication.models.SalaryProfileModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * Single payroll calculation point. Salary changes are effective-date based,
 * so a change in the middle of a month is prorated by working day instead of
 * incorrectly applying the new salary to the entire month.
 */
class PayrollCalculator(
    private val userRepository: UserRepository = UserRepository(),
    private val salaryRepository: SalaryRepository = SalaryRepository(),
    private val attendanceRepository: AttendanceRepository = AttendanceRepository(),
    private val leaveRepository: LeaveRepository = LeaveRepository(),
    private val advanceSalaryRepository: AdvanceSalaryRepository = AdvanceSalaryRepository()
) {
    suspend fun calculate(month: YearMonth): List<SalaryData> {
        val monthStart = month.atDay(1)
        val monthEnd = month.atEndOfMonth()
        val start = monthStart.toString()
        val end = monthEnd.toString()
        val scheduledWorkingDays = countWorkingDays(monthStart, monthEnd)
        val result = mutableListOf<SalaryData>()

        userRepository.getAllEmployees().forEach { user ->
            val history = salaryRepository.getHistory(user.uid)
                .filter { it.effectiveFrom.isNotBlank() }
                .sortedByDescending { it.effectiveFrom }
            if (history.isEmpty()) return@forEach

            fun profileFor(date: LocalDate): SalaryProfileModel? {
                val value = date.toString()
                return history.firstOrNull { it.effectiveFrom <= value && (it.effectiveTo == null || it.effectiveTo!! > value) }
            }

            val attendance = attendanceRepository.getAttendanceForMonth(user.uid, start, end)
            val approvedLeaveDates = expandApprovedLeaveDates(leaveRepository.getRequestsForUser(user.uid), monthStart, monthEnd)
            val attendanceLeaveDates = attendance
                .filter { it.status == "APPROVED_LEAVE" }
                .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                .filter(::isWorkingDay)
                .toSet()
            val paidLeaveDates = approvedLeaveDates + attendanceLeaveDates
            val presentDates = attendance
                .filter { it.status != "APPROVED_LEAVE" && it.punchInTime != null }
                .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                .filter(::isWorkingDay)
                .toSet()

            val presentDays = presentDates.size
            val leaveDays = (paidLeaveDates - presentDates).size
            var absentDays = 0
            var monthlyGross = 0.0
            var absenceDeduction = 0.0
            var dailyGross = 0.0

            var date = monthStart
            while (!date.isAfter(monthEnd)) {
                if (isWorkingDay(date)) {
                    val profile = profileFor(date)
                    if (profile != null) {
                        val payable = presentDates.contains(date) || paidLeaveDates.contains(date)
                        if (profile.payType == "MONTHLY") {
                            val daySalary = profile.monthlySalary / scheduledWorkingDays.coerceAtLeast(1)
                            monthlyGross += daySalary
                            if (!payable) {
                                absentDays++
                                absenceDeduction += daySalary
                            }
                        } else {
                            if (presentDates.contains(date)) dailyGross += profile.dailyRate
                            else if (!paidLeaveDates.contains(date)) absentDays++
                        }
                    }
                }
                date = date.plusDays(1)
            }

            val overtimeMinutes = attendance.sumOf { it.overtimeMinutes.coerceAtLeast(0) }
            val overtimePay = attendance.sumOf { record ->
                val recordDate = runCatching { LocalDate.parse(record.date) }.getOrNull()
                val rate = recordDate?.let { profileFor(it)?.overtimeRatePerHour } ?: 0.0
                record.overtimeMinutes.coerceAtLeast(0) / 60.0 * rate
            }
            val lateCount = attendance.count { it.isLate && it.status != "APPROVED_LEAVE" }
            val earlyOutCount = attendance.count { it.isEarlyOut && it.status != "APPROVED_LEAVE" }
            val missingPunchOutCount = attendance.count { it.punchOutMissing }

            // Only APPROVED advances are deducted. Each approved request starts
            // its installment schedule from the requested salary month.
            val approvedAdvances = advanceSalaryRepository.getForUser(user.uid)
                .filter { it.status == "APPROVED" && it.approvedAmount > 0.0 && it.installments > 0 }
            val advanceDeduction = approvedAdvances.sumOf { advance ->
                val firstMonth = runCatching { YearMonth.parse(advance.salaryMonth) }.getOrNull()
                    ?: return@sumOf 0.0
                val monthIndex = (month.year - firstMonth.year) * 12 + (month.monthValue - firstMonth.monthValue)
                if (monthIndex in 0 until advance.installments) {
                    advance.approvedAmount / advance.installments
                } else 0.0
            }

            val latestProfile = history.firstOrNull { it.effectiveFrom <= end } ?: history.last()
            val basePay = monthlyGross + dailyGross
            val totalDeductions = absenceDeduction + advanceDeduction
            val net = (basePay - totalDeductions + overtimePay).coerceAtLeast(0.0)
            val displayPayType = if (history.filter { it.effectiveFrom <= end }.map { it.payType }.distinct().size > 1) "MIXED" else latestProfile.payType

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
                advances = advanceDeduction,
                deductions = totalDeductions,
                basicSalary = basePay,
                netSalary = net,
                month = month.toString(),
                year = month.year,
                payType = displayPayType,
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

    private fun expandApprovedLeaveDates(requests: List<LeaveRequestModel>, monthStart: LocalDate, monthEnd: LocalDate): Set<LocalDate> {
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
