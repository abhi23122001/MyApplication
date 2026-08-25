package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.AttendanceRecord
import com.shahsurveyors.myapplication.models.LeaveRequestModel
import com.shahsurveyors.myapplication.models.SalaryPayrollModel
import com.shahsurveyors.myapplication.models.SalaryProfileModel
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class SalaryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection = firestore.collection("salaryProfiles")
    private val attendanceCollection = firestore.collection(FirebaseConstants.COLLECTION_ATTENDANCE)
    private val leaveCollection = firestore.collection("leaveRequests")
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun getHistory(employeeUid: String): List<SalaryProfileModel> {
        return collection
            .whereEqualTo("employeeUid", employeeUid)
            .get()
            .await()
            .toObjects(SalaryProfileModel::class.java)
            .sortedByDescending { it.effectiveFrom }
    }

    suspend fun getCurrent(employeeUid: String): SalaryProfileModel? {
        return getHistory(employeeUid).firstOrNull { it.active }
    }

    suspend fun saveSalaryProfile(profile: SalaryProfileModel): String {
        require(profile.employeeUid.isNotBlank()) { "Employee is required" }
        require(profile.payType == "MONTHLY" || profile.payType == "DAILY") { "Invalid payment basis" }
        require(profile.effectiveFrom.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { "Effective date must be yyyy-MM-dd" }
        if (profile.payType == "MONTHLY") require(profile.monthlySalary > 0) { "Monthly salary must be greater than zero" }
        if (profile.payType == "DAILY") require(profile.dailyRate > 0) { "Daily rate must be greater than zero" }

        val previous = getCurrent(profile.employeeUid)
        if (previous != null && previous.id.isNotBlank()) {
            collection.document(previous.id).update(
                mapOf(
                    "active" to false,
                    "effectiveTo" to profile.effectiveFrom
                )
            ).await()
        }

        val ref = if (profile.id.isBlank()) collection.document() else collection.document(profile.id)
        val saved = profile.copy(
            id = ref.id,
            setAt = profile.setAt ?: Timestamp.now(),
            active = true
        )
        ref.set(saved).await()
        return ref.id
    }

    /**
     * Calculates one employee's payroll for the requested calendar month.
     * Sunday is treated as the default weekly off. This is intentionally kept
     * as a payroll rule rather than hard-coding salary values into the UI.
     */
    suspend fun calculateMonthlyPayroll(
        employeeUid: String,
        employeeName: String,
        department: String,
        month: YearMonth
    ): SalaryPayrollModel {
        val history = getHistory(employeeUid)
        val firstDay = month.atDay(1)
        val lastDay = month.atEndOfMonth()
        val attendance = getAttendanceForUser(employeeUid)
            .filter { it.date.isNotBlank() }
            .associateBy { it.date }
        val leaveRequests = getLeaveRequestsForUser(employeeUid)

        val paidLeaveDates = mutableSetOf<LocalDate>()
        val unpaidLeaveDates = mutableSetOf<LocalDate>()
        leaveRequests.filter { it.status == "APPROVED" }.forEach { request ->
            val from = runCatching { LocalDate.parse(request.fromDate.trim(), dateFormatter) }.getOrNull() ?: return@forEach
            val to = runCatching { LocalDate.parse(request.toDate.trim(), dateFormatter) }.getOrNull() ?: return@forEach
            var date = maxOf(from, firstDay)
            val end = minOf(to, lastDay)
            while (!date.isAfter(end)) {
                if (request.leaveType.equals("UNPAID", ignoreCase = true)) {
                    unpaidLeaveDates += date
                } else {
                    paidLeaveDates += date
                }
                date = date.plusDays(1)
            }
        }

        val workingDates = generateSequence(firstDay) { current ->
            if (current.isBefore(lastDay)) current.plusDays(1) else null
        }.filter { it.dayOfWeek.value != 7 }.toList()

        var presentDays = 0
        var paidLeaveDays = 0
        var unpaidLeaveDays = 0
        var absentDays = 0
        var lateMinutes = 0
        var earlyOutMinutes = 0
        var overtimeMinutes = 0
        var baseSalary = 0.0
        var attendanceDeduction = 0.0
        var lateEarlyDeduction = 0.0
        var overtimePay = 0.0

        workingDates.forEach { date ->
            val dateString = date.format(dateFormatter)
            val record = attendance[dateString]
            val profile = history.firstOrNull { p ->
                p.effectiveFrom.isNotBlank() &&
                    p.effectiveFrom <= dateString &&
                    (p.effectiveTo.isNullOrBlank() || dateString < p.effectiveTo!!)
            }

            if (paidLeaveDates.contains(date)) {
                paidLeaveDays++
            } else if (unpaidLeaveDates.contains(date)) {
                unpaidLeaveDays++
            } else if (record?.status == "PRESENT" || record?.status == "LATE" || record?.status == "EARLY_OUT" || record?.status == "MISSING_PUNCH_OUT") {
                presentDays++
            } else {
                absentDays++
            }

            if (record != null) {
                lateMinutes += record.lateMinutes.coerceAtLeast(0)
                earlyOutMinutes += record.earlyOutMinutes.coerceAtLeast(0)
                overtimeMinutes += record.overtimeMinutes.coerceAtLeast(0)
            }

            if (profile != null) {
                val dailyEquivalent = when (profile.payType) {
                    "DAILY" -> profile.dailyRate
                    else -> profile.monthlySalary / workingDates.size.coerceAtLeast(1)
                }
                val payable = paidLeaveDates.contains(date) ||
                    (!unpaidLeaveDates.contains(date) && record?.status in setOf("PRESENT", "LATE", "EARLY_OUT", "MISSING_PUNCH_OUT"))
                if (payable) baseSalary += dailyEquivalent

                val minuteRate = dailyEquivalent / 480.0 // 8-hour working day
                if (record != null) {
                    lateEarlyDeduction += minuteRate * (record.lateMinutes.coerceAtLeast(0) + record.earlyOutMinutes.coerceAtLeast(0))
                    overtimePay += profile.overtimeRatePerHour.coerceAtLeast(0.0) * (record.overtimeMinutes.coerceAtLeast(0) / 60.0)
                }
            }
        }

        // Absent/unpaid leave is already excluded from baseSalary. Keep this
        // field explicit so the salary slip can show the attendance impact.
        attendanceDeduction = when {
            history.firstOrNull()?.payType == "MONTHLY" -> {
                val currentMonthly = history.firstOrNull { it.effectiveFrom <= firstDay.toString() }?.monthlySalary ?: 0.0
                (currentMonthly - baseSalary).coerceAtLeast(0.0)
            }
            else -> 0.0
        }

        val net = (baseSalary - lateEarlyDeduction + overtimePay).coerceAtLeast(0.0)

        return SalaryPayrollModel(
            employeeUid = employeeUid,
            employeeName = employeeName,
            department = department,
            month = month.toString(),
            daysInMonth = month.lengthOfMonth(),
            presentDays = presentDays,
            paidLeaveDays = paidLeaveDays,
            unpaidLeaveDays = unpaidLeaveDays,
            absentDays = absentDays,
            lateMinutes = lateMinutes,
            earlyOutMinutes = earlyOutMinutes,
            overtimeMinutes = overtimeMinutes,
            baseSalary = baseSalary,
            attendanceDeduction = attendanceDeduction,
            lateEarlyDeduction = lateEarlyDeduction,
            overtimePay = overtimePay,
            netSalary = net,
            status = "CALCULATED"
        )
    }

    private suspend fun getAttendanceForUser(employeeUid: String): List<AttendanceRecord> {
        return attendanceCollection
            .whereEqualTo("uid", employeeUid)
            .get()
            .await()
            .toObjects(AttendanceRecord::class.java)
    }

    private suspend fun getLeaveRequestsForUser(employeeUid: String): List<LeaveRequestModel> {
        return leaveCollection
            .whereEqualTo("userUid", employeeUid)
            .get()
            .await()
            .toObjects(LeaveRequestModel::class.java)
    }
}
