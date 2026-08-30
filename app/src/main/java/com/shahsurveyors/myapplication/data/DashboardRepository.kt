package com.shahsurveyors.myapplication.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.ui.dashboard.DashboardData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class DashboardRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun getDashboardData(): DashboardData = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUser?.uid ?: throw IllegalStateException("Authentication required")
            val profile = firestore.collection(FirebaseConstants.COLLECTION_USERS).document(uid).get().await()
                .toObject(com.shahsurveyors.myapplication.models.UserProfile::class.java)
                ?: throw IllegalStateException("User profile not found")
            val isAdmin = profile.active && profile.role.equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true)

            if (!isAdmin) {
                return@withContext getEmployeeDashboard(uid)
            }

            getAdminDashboard()
        } catch (e: Exception) {
            e.printStackTrace()
            DashboardData(noticeMessage = "Unable to sync dashboard data. Pull to refresh.")
        }
    }

    private suspend fun getEmployeeDashboard(uid: String): DashboardData {
        val zone = ZoneId.of("Asia/Kolkata")
        val today = LocalDate.now(zone)
        val monthStart = today.withDayOfMonth(1)
        val nextMonthStart = monthStart.plusMonths(1)
        val monthStartMillis = monthStart.atStartOfDay(zone).toInstant().toEpochMilli()
        val nextMonthMillis = nextMonthStart.atStartOfDay(zone).toInstant().toEpochMilli()

        val todayAttendance = firestore.collection(FirebaseConstants.COLLECTION_ATTENDANCE)
            .document("${uid}_${today}").get().await()
        val status = todayAttendance.getString("status") ?: ""
        val presentToday = if (todayAttendance.exists() && !status.equals("APPROVED_LEAVE", true)) 1 else 0
        val onLeaveToday = if (status.equals("APPROVED_LEAVE", true)) 1 else 0

        val ownExpenses = firestore.collection(FirebaseConstants.COLLECTION_EXPENSES)
            .whereEqualTo("uid", uid).get().await().documents
        val monthlyExpenses = ownExpenses.sumOf { doc ->
            val timestamp = doc.getTimestamp("date") ?: return@sumOf 0.0
            val millis = timestamp.toDate().time
            if (millis >= monthStartMillis && millis < nextMonthMillis) doc.getDouble("amount") ?: 0.0 else 0.0
        }

        val ownSalaryProfiles = firestore.collection("salaryProfiles")
            .whereEqualTo("employeeUid", uid)
            .whereEqualTo("active", true)
            .get().await().documents
        val salaryLiability = ownSalaryProfiles.maxByOrNull { it.getTimestamp("createdAt")?.seconds ?: 0L }?.let { doc ->
            val payType = doc.getString("payType") ?: "MONTHLY"
            if (payType.equals("DAILY", ignoreCase = true)) (doc.getDouble("dailyRate") ?: 0.0) * 26.0
            else doc.getDouble("monthlySalary") ?: 0.0
        } ?: 0.0

        return DashboardData(
            noticeMessage = "Your personal attendance, expenses and payroll summary.",
            totalEmployees = 0,
            presentToday = presentToday,
            absentToday = if (presentToday == 0 && onLeaveToday == 0) 1 else 0,
            onLeaveToday = onLeaveToday,
            activeProjects = 0,
            monthlyExpenses = monthlyExpenses,
            salaryLiability = salaryLiability,
            estimatedProfit = 0.0
        )
    }

    private suspend fun getAdminDashboard(): DashboardData {
        val zone = ZoneId.of("Asia/Kolkata")
        val today = LocalDate.now(zone)
        val monthStart = today.withDayOfMonth(1)
        val nextMonthStart = monthStart.plusMonths(1)
        val monthStartMillis = monthStart.atStartOfDay(zone).toInstant().toEpochMilli()
        val nextMonthMillis = nextMonthStart.atStartOfDay(zone).toInstant().toEpochMilli()

        val users = firestore.collection(FirebaseConstants.COLLECTION_USERS).get().await().documents
        val employees = users.filter {
            it.getBoolean("active") != false &&
                !it.getString("role").equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true)
        }
        val totalEmployees = employees.size

        val attendance = firestore.collection(FirebaseConstants.COLLECTION_ATTENDANCE)
            .whereEqualTo("date", today.toString()).get().await().documents
        val presentToday = attendance.count {
            (it.getString("status") ?: "PRESENT").equals("PRESENT", ignoreCase = true)
        }
        val onLeaveToday = attendance.count {
            (it.getString("status") ?: "").equals("APPROVED_LEAVE", ignoreCase = true)
        }
        val absentToday = (totalEmployees - presentToday - onLeaveToday).coerceAtLeast(0)

        val projects = firestore.collection(FirebaseConstants.COLLECTION_PROJECTS).get().await().documents
        val activeProjectDocs = projects.filter {
            (it.getString("status") ?: FirebaseConstants.STATUS_ACTIVE).equals(FirebaseConstants.STATUS_ACTIVE, ignoreCase = true)
        }
        val activeProjects = activeProjectDocs.size
        val activeProjectBudget = activeProjectDocs.sumOf { it.getDouble("budget") ?: 0.0 }

        val expenses = firestore.collection(FirebaseConstants.COLLECTION_EXPENSES).get().await().documents
        val monthlyExpenses = expenses.sumOf { doc ->
            val timestamp = doc.getTimestamp("date") ?: return@sumOf 0.0
            val millis = timestamp.toDate().time
            if (millis >= monthStartMillis && millis < nextMonthMillis) {
                doc.getDouble("amount") ?: 0.0
            } else 0.0
        }

        val salaryProfiles = firestore.collection("salaryProfiles")
            .whereEqualTo("active", true).get().await().documents
        val salaryLiability = salaryProfiles.sumOf { doc ->
            val payType = doc.getString("payType") ?: "MONTHLY"
            if (payType.equals("DAILY", ignoreCase = true)) {
                (doc.getDouble("dailyRate") ?: 0.0) * 26.0
            } else {
                doc.getDouble("monthlySalary") ?: 0.0
            }
        }

        return DashboardData(
            noticeMessage = "Maintain project, expense and attendance records daily.",
            totalEmployees = totalEmployees,
            presentToday = presentToday,
            absentToday = absentToday,
            onLeaveToday = onLeaveToday,
            activeProjects = activeProjects,
            monthlyExpenses = monthlyExpenses,
            salaryLiability = salaryLiability,
            estimatedProfit = activeProjectBudget - monthlyExpenses - salaryLiability
        )
    }
}
