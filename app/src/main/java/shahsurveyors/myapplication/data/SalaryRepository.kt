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

    suspend fun getHistory(employeeUid: String): List<SalaryProfileModel> =
        collection.whereEqualTo("employeeUid", employeeUid)
            .get().await()
            .toObjects(SalaryProfileModel::class.java)
            .sortedByDescending { it.effectiveFrom }

    suspend fun getCurrent(employeeUid: String): SalaryProfileModel? =
        getHistory(employeeUid).firstOrNull { it.active }

    suspend fun saveSalaryProfile(profile: SalaryProfileModel): String {
        require(profile.employeeUid.isNotBlank()) { "Employee is required" }
        require(profile.payType == "MONTHLY" || profile.payType == "DAILY") { "Invalid payment basis" }
        require(profile.effectiveFrom.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) { "Effective date must be yyyy-MM-dd" }
        if (profile.payType == "MONTHLY") require(profile.monthlySalary > 0) { "Monthly salary must be greater than zero" }
        if (profile.payType == "DAILY") require(profile.dailyRate > 0) { "Daily rate must be greater than zero" }

        val previous = getCurrent(profile.employeeUid)
        if (previous != null && previous.id.isNotBlank()) {
            collection.document(previous.id).update(
                mapOf("active" to false, "effectiveTo" to profile.effectiveFrom)
            ).await()
        }

        val ref = if (profile.id.isBlank()) collection.document() else collection.document(profile.id)
        ref.set(profile.copy(id = ref.id, setAt = profile.setAt ?: Timestamp.now(), active = true)).await()
        return ref.id
    }

    /**
     * Payroll rules used by the app:
     * - Sunday is the default weekly off.
     * - Monthly employees earn monthlySalary / working-days for each payable day.
     * - Daily employees earn dailyRate for each payable day.
     * - Approved non-UNPAID leave is paid; approved UNPAID leave is not.
     * - Absent/unpaid days are deducted automatically.
     * - Late + early-out minutes are deducted using an 8-hour daily rate.
     * - Overtime uses the employee's admin-defined overtimeRatePerHour.
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
        val attendance = getAttendanceForUser(employeeUid).associateBy { it.date }
        val leaveRequests = getLeaveRequestsForUser(employeeUid)

        val paidLeaveDates = mutableSetOf<LocalDate>()
        val unpaidLeaveDates = mutableSetOf<LocalDate>()
        leaveRequests.filter { it.status == "APPROVED" }.forEach { request ->
            val from = runCatching { LocalDate.parse(request.fromDate.trim(), dateFormatter) }.getOrNull() ?: return@forEach
            val to = runCatching { LocalDate.parse(request.toDate.trim(), dateFormatter) }.getOrNull() ?: return@forEach
            var date = maxOf(from, firstDay)
            val end = minOf(to, lastDay)
            while (!date.isAfter(end)) {
                if (request.leaveType.equals("UNPAID", ignoreCase = true)) unpaidLeaveDates += date
                else paidLeaveDates += date
                date = date.plusDays(1)
            }
        }

        val workingDates = generateSequence(firstDay) { current ->
            if (current.isBefore(lastDay)) current.plusDays(1) else null
        }.filter { it.dayOfWeek.value != 7 }.toList()
        val workingDayCount = workingDates.size.coerceAtLeast(1)

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

        val payableStatuses = setOf("PRESENT", "LATE", "EARLY_OUT", "MISSING_PUNCH_OUT")

        workingDates.forEach { date ->
            val dateString = date.format(dateFormatter)
            val record = attendance[dateString]
            val profile = history.firstOrNull { p ->
                p.effectiveFrom.isNotBlank() &&
                    p.effectiveFrom <= dateString &&
                    (p.effectiveTo.isNullOrBlank() || dateString < p.effectiveTo!!)
            }
            if (profile == null) return@forEach

            val dailyEquivalent = if (profile.payType == "DAILY") profile.dailyRate
            else profile.monthlySalary / workingDayCount

            val isPaidLeave = paidLeaveDates.contains(date)
            val isUnpaidLeave = unpaidLeaveDates.contains(date)
            val isPresent = record?.status?.let { it in payableStatuses } == true

            when {
                isPaidLeave -> {
                    paidLeaveDays++
                    baseSalary += dailyEquivalent
                }
                isUnpaidLeave -> {
                    unpaidLeaveDays++
                    attendanceDeduction += dailyEquivalent
                }
                isPresent -> {
                    presentDays++
                    baseSalary += dailyEquivalent
                }
                else -> {
                    absentDays++
                    attendanceDeduction += dailyEquivalent
                }
            }

            if (record != null) {
                val late = record.lateMinutes.coerceAtLeast(0)
                val early = record.earlyOutMinutes.coerceAtLeast(0)
                val overtime = record.overtimeMinutes.coerceAtLeast(0)
                lateMinutes += late
                earlyOutMinutes += early
                overtimeMinutes += overtime
                lateEarlyDeduction += (dailyEquivalent / 480.0) * (late + early)
                overtimePay += profile.overtimeRatePerHour.coerceAtLeast(0.0) * (overtime / 60.0)
            }
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

    private suspend fun getAttendanceForUser(employeeUid: String): List<AttendanceRecord> =
        attendanceCollection.whereEqualTo("uid", employeeUid).get().await()
            .toObjects(AttendanceRecord::class.java)
            .filter { it.date.isNotBlank() }

    private suspend fun getLeaveRequestsForUser(employeeUid: String): List<LeaveRequestModel> =
        leaveCollection.whereEqualTo("userUid", employeeUid).get().await()
            .toObjects(LeaveRequestModel::class.java)
}
