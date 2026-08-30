package com.shahsurveyors.myapplication.models

import com.google.firebase.Timestamp

data class LeaveRequestModel(
    val id: String = "",
    val userUid: String = "",
    val employeeName: String = "",
    val leaveType: String = "CASUAL",
    val fromDate: String = "",
    val toDate: String = "",
    val reason: String = "",
    val status: String = "PENDING",
    val approvedBy: String = "",
    val adminRemark: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
