package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.ui.dashboard.DashboardData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class DashboardRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getDashboardData(): DashboardData = withContext(Dispatchers.IO) {
        try {
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

            DashboardData(
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
        } catch (e: Exception) {
            e.printStackTrace()
            DashboardData(noticeMessage = "Unable to sync dashboard data. Pull to refresh.")
        }
    }
}
