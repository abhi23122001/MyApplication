package com.shahsurveyors.myapplication.models

import com.google.firebase.Timestamp

data class SalaryAdvanceRequest(
    val id: String = "",
    val uid: String = "",
    val employeeName: String = "",
    val amount: Double = 0.0,
    val reason: String = "",
    val requestedDate: String = "",
    val status: String = "PENDING",
    val adminRemark: String = "",
    val approvedBy: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)

enum class ApprovalType {
    ATTENDANCE,
    LEAVE,
    EXPENSE,
    SALARY_ADVANCE
}

data class ApprovalRequestItem(
    val id: String,
    val type: ApprovalType,
    val employeeName: String,
    val title: String,
    val subtitle: String,
    val amount: Double? = null,
    val date: String = "",
    val reason: String = "",
    val createdAt: Timestamp? = null
)
