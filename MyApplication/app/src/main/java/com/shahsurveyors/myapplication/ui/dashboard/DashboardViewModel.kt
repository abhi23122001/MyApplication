package com.shahsurveyors.myapplication.ui.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.DashboardRepository
import com.shahsurveyors.myapplication.data.NotificationRepository
import com.shahsurveyors.myapplication.models.AppNotification
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DashboardRepository = DashboardRepository(),
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var noticeMessage by mutableStateOf("Maintain Leica Equipment logs daily.")
        private set

    var totalEmployees by mutableIntStateOf(0)
        private set

    var presentToday by mutableIntStateOf(0)
        private set

    var absentToday by mutableIntStateOf(0)
        private set

    var onLeaveToday by mutableIntStateOf(0)
        private set

    var activeProjects by mutableIntStateOf(0)
        private set

    var monthlyExpenses by mutableStateOf("₹ 0")
        private set

    var salaryLiability by mutableStateOf("₹ 0")
        private set

    var estimatedProfit by mutableStateOf("₹ 0")
        private set

    val notificationList = mutableStateListOf<AppNotification>()

    init {
        fetchDashboardData()
    }

    fun startListeningToNotifications(userUid: String, isAdmin: Boolean) {
        viewModelScope.launch {
            notificationRepository.listenToNotifications(userUid, isAdmin).collectLatest { list ->
                notificationList.clear()
                notificationList.addAll(list)
            }
        }
    }

    fun fetchDashboardData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val data = repository.getDashboardData()
                noticeMessage = data.noticeMessage.ifBlank { noticeMessage }
                totalEmployees = data.totalEmployees
                presentToday = data.presentToday
                absentToday = data.absentToday
                onLeaveToday = data.onLeaveToday
                activeProjects = data.activeProjects
                monthlyExpenses = formatCurrency(data.monthlyExpenses)
                salaryLiability = formatCurrency(data.salaryLiability)
                estimatedProfit = formatCurrency(data.estimatedProfit)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun updateNotice(newNotice: String) {
        noticeMessage = newNotice
        viewModelScope.launch {
            repository.updateBroadcastNotice(newNotice)
        }
    }

    fun refresh() {
        fetchDashboardData()
    }

    private fun formatCurrency(amount: Double): String {
        return "₹ " + String.format(java.util.Locale.ENGLISH, "%,.0f", amount)
    }
}

data class DashboardData(
    val noticeMessage: String = "",
    val totalEmployees: Int = 0,
    val presentToday: Int = 0,
    val absentToday: Int = 0,
    val onLeaveToday: Int = 0,
    val activeProjects: Int = 0,
    val monthlyExpenses: Double = 0.0,
    val salaryLiability: Double = 0.0,
    val estimatedProfit: Double = 0.0
)