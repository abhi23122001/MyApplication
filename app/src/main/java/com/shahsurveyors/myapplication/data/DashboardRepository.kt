package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.shahsurveyors.myapplication.ui.dashboard.DashboardData

class DashboardRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getDashboardData(): DashboardData =
        withContext(Dispatchers.IO) {

            try {
                // Fetch stats from Firestore
                val totalEmployees = firestore.collection(FirebaseConstants.COLLECTION_USERS)
                    .whereEqualTo("role", FirebaseConstants.ROLE_EMPLOYEE)
                    .get().await().size()

                val activeProjects = firestore.collection(FirebaseConstants.COLLECTION_PROJECTS)
                    .whereEqualTo("status", "ACTIVE")
                    .get().await().size()

                DashboardData(
                    noticeMessage = "Maintain Leica Equipment logs daily.",
                    totalEmployees = totalEmployees,
                    presentToday = 0, 
                    absentToday = 0,
                    onLeaveToday = 0,
                    activeProjects = activeProjects,
                    monthlyExpenses = 0.0,
                    salaryLiability = 0.0,
                    estimatedProfit = 0.0
                )
            } catch (e: Exception) {
                e.printStackTrace()
                DashboardData(noticeMessage = "Error syncing dashboard data.")
            }
        }
}
