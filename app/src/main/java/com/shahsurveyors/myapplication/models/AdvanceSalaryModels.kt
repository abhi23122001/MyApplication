package com.shahsurveyors.myapplication.models

data class AdvanceSalaryRequest(
    val id: String = "",
    val uid: String = "",
    val userName: String = "",
    val amount: Double = 0.0,
    val reason: String = "",
    val salaryMonth: String = "",
    val installments: Int = 1,
    val status: String = "PENDING",
    val approvedAmount: Double = 0.0,
    val adminUid: String = "",
    val adminName: String = "",
    val requestedAt: Long = System.currentTimeMillis(),
    val decidedAt: Long = 0L
)
