package com.shahsurveyors.myapplication.utils

import com.shahsurveyors.myapplication.models.AdvanceSalaryRequest
import com.shahsurveyors.myapplication.models.PayrollRecord
import com.shahsurveyors.myapplication.models.SalaryProfileModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

object PayrollCalculator {

    /**
     * Standard working days in an Indian ERP operational month.
     */
    const val STANDARD_WORKING_DAYS = 26

    /**
     * Calculates full PayrollRecord for a given employee and month.
     *
     * @param employeeUid Target employee UID
     * @param employeeName Employee display name
     * @param employeeId Employee ID (e.g. EMP001)
     * @param department Department (e.g. SURVEY, FINANCE)
     * @param role Role (e.g. STAFF, SURVEYOR)
     * @param yearMonth Selected salary month in "YYYY-MM" format (e.g. "2026-08")
     * @param salaryProfiles List of salary profiles for this employee (historical & active)
     * @param presentDays Verified present punch days in this month
     * @param approvedLeaveDays Verified approved paid leave days in this month
     * @param overtimeHours Verified overtime hours worked in this month
     * @param approvedAdvances List of approved advance salary requests for this employee
     * @param otherDeductions Any miscellaneous penalties or deductions
     * @return Fully calculated PayrollRecord or null if no valid salary profile covers this month
     */
    fun calculateMonthlyPayroll(
        employeeUid: String,
        employeeName: String,
        employeeId: String,
        department: String,
        role: String,
        yearMonth: String, // "YYYY-MM"
        salaryProfiles: List<SalaryProfileModel>,
        presentDays: Int = 0,
        approvedLeaveDays: Int = 0,
        overtimeHours: Double = 0.0,
        approvedAdvances: List<AdvanceSalaryRequest> = emptyList(),
        otherDeductions: Double = 0.0
    ): PayrollRecord? {

        val profile = findApplicableSalaryProfile(salaryProfiles, yearMonth) ?: return null

        val parsedYearMonth = try {
            YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-MM"))
        } catch (e: Exception) {
            YearMonth.now()
        }

        val totalDaysInMonth = parsedYearMonth.lengthOfMonth()
        val workingDaysInMonth = STANDARD_WORKING_DAYS

        // Base rates
        val monthlySalary = profile.monthlySalary
        val dailyRate = if (profile.dailyRate > 0) {
            profile.dailyRate
        } else if (monthlySalary > 0) {
            (monthlySalary / workingDaysInMonth * 100.0).roundToInt() / 100.0
        } else {
            0.0
        }

        val overtimeRate = if (profile.overtimeRatePerHour > 0) {
            profile.overtimeRatePerHour
        } else if (dailyRate > 0) {
            // Default 8-hour shift overtime rate
            ((dailyRate / 8.0) * 1.5 * 100.0).roundToInt() / 100.0
        } else {
            0.0
        }

        // Calculate Effective Period Proration if effective date started mid-month
        val (effectiveBaseSalary, effectivePeriodText) = calculateEffectiveBaseSalary(
            profile = profile,
            yearMonth = yearMonth,
            totalDaysInMonth = totalDaysInMonth
        )

        // Attendance & Absence
        // Approved leave is paid leave (does not create absence deduction)
        val nonAbsenceDays = presentDays + approvedLeaveDays
        val absentDays = maxOf(0, workingDaysInMonth - nonAbsenceDays)
        val absenceDeduction = ((absentDays * dailyRate) * 100.0).roundToInt() / 100.0

        // Overtime Earnings
        val overtimePay = ((overtimeHours * overtimeRate) * 100.0).roundToInt() / 100.0

        // Gross Salary Earned
        val grossSalaryEarned = maxOf(0.0, effectiveBaseSalary - absenceDeduction + overtimePay)

        // Advance Salary Deductions calculation
        val advanceDeduction = calculateAdvanceDeductionForMonth(
            approvedAdvances = approvedAdvances,
            targetYearMonth = yearMonth
        )

        val totalDeductions = ((absenceDeduction + advanceDeduction + otherDeductions) * 100.0).roundToInt() / 100.0
        val netSalary = maxOf(
            0.0,
            ((effectiveBaseSalary + overtimePay - totalDeductions) * 100.0).roundToInt() / 100.0
        )

        val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
        val monthName = parsedYearMonth.format(monthFormatter)

        return PayrollRecord(
            id = "${employeeUid}_${yearMonth}",
            employeeUid = employeeUid,
            name = employeeName,
            employeeId = employeeId,
            dept = department,
            role = role,
            salaryMonth = yearMonth,
            year = parsedYearMonth.year,
            monthName = monthName,
            baseMonthlySalary = monthlySalary,
            dailyRate = dailyRate,
            overtimeRatePerHour = overtimeRate,
            effectiveSalaryPeriod = effectivePeriodText,
            totalDaysInMonth = totalDaysInMonth,
            workingDaysInMonth = workingDaysInMonth,
            presentDays = presentDays,
            approvedLeaveDays = approvedLeaveDays,
            absentDays = absentDays,
            overtimeHours = overtimeHours,
            grossSalaryEarned = grossSalaryEarned,
            absenceDeduction = absenceDeduction,
            overtimePay = overtimePay,
            advanceDeduction = advanceDeduction,
            otherDeductions = otherDeductions,
            totalDeductions = totalDeductions,
            netSalary = netSalary,
            status = "CALCULATED"
        )
    }

    /**
     * Finds the salary profile applicable for the selected YearMonth (YYYY-MM).
     */
    fun findApplicableSalaryProfile(
        profiles: List<SalaryProfileModel>,
        yearMonth: String
    ): SalaryProfileModel? {
        if (profiles.isEmpty()) return null

        val monthStart = "${yearMonth}-01"
        val monthEnd = "${yearMonth}-31"

        // 1. Find profile explicitly matching effective period
        val matchingProfile = profiles
            .filter { profile ->
                val fromOk = profile.effectiveFrom.isBlank() || profile.effectiveFrom <= monthEnd
                val toOk = profile.effectiveTo.isNullOrBlank() || profile.effectiveTo >= monthStart
                fromOk && toOk
            }
            .maxByOrNull { it.effectiveFrom }

        if (matchingProfile != null) {
            return matchingProfile
        }

        // 2. Fallback to active profile if starting date is <= selected month
        return profiles.find { it.active && (it.effectiveFrom.isBlank() || it.effectiveFrom <= monthEnd) }
            ?: profiles.maxByOrNull { it.effectiveFrom }
    }

    /**
     * Calculate proration if salary started mid-month.
     */
    private fun calculateEffectiveBaseSalary(
        profile: SalaryProfileModel,
        yearMonth: String,
        totalDaysInMonth: Int
    ): Pair<Double, String> {
        val baseMonthly = profile.monthlySalary
        if (profile.effectiveFrom.isBlank()) {
            return Pair(baseMonthly, "Full Month")
        }

        val effectiveMonth = if (profile.effectiveFrom.length >= 7) {
            profile.effectiveFrom.substring(0, 7)
        } else {
            ""
        }

        if (effectiveMonth == yearMonth && profile.effectiveFrom.length >= 10) {
            val startDay = profile.effectiveFrom.substring(8, 10).toIntOrNull() ?: 1
            if (startDay > 1) {
                val activeDays = totalDaysInMonth - startDay + 1
                val proratedSalary = ((baseMonthly * activeDays / totalDaysInMonth) * 100.0).roundToInt() / 100.0
                return Pair(proratedSalary, "From ${profile.effectiveFrom} ($activeDays/$totalDaysInMonth days)")
            }
        }

        return Pair(baseMonthly, "From ${profile.effectiveFrom}")
    }

    /**
     * Calculates the total advance installment deduction for the selected target month.
     *
     * Rule:
     * - Only APPROVED advances.
     * - Monthly EMI = approvedAmount / installments.
     * - Begins in requestedMonth (e.g. 2026-08) and applies for exactly 'installments' months.
     */
    fun calculateAdvanceDeductionForMonth(
        approvedAdvances: List<AdvanceSalaryRequest>,
        targetYearMonth: String // YYYY-MM
    ): Double {
        var totalDeduction = 0.0

        val targetYm = try {
            YearMonth.parse(targetYearMonth, DateTimeFormatter.ofPattern("yyyy-MM"))
        } catch (e: Exception) {
            return 0.0
        }

        for (advance in approvedAdvances) {
            // Strictly only consider APPROVED advances
            if (advance.status != "APPROVED") continue
            if (advance.approvedAmount <= 0 || advance.installments <= 0) continue

            val startYm = try {
                if (advance.requestedMonth.isNotBlank()) {
                    YearMonth.parse(advance.requestedMonth, DateTimeFormatter.ofPattern("yyyy-MM"))
                } else {
                    targetYm
                }
            } catch (e: Exception) {
                targetYm
            }

            val monthlyEmi = (advance.approvedAmount / advance.installments * 100.0).roundToInt() / 100.0

            // Check if targetYm falls within [startYm, startYm + installments - 1]
            val monthsDiff = (targetYm.year - startYm.year) * 12 + (targetYm.monthValue - startYm.monthValue)

            if (monthsDiff in 0 until advance.installments) {
                totalDeduction += monthlyEmi
            }
        }

        return (totalDeduction * 100.0).roundToInt() / 100.0
    }
}
