package com.shahsurveyors.myapplication.models

import com.google.firebase.Timestamp

data class AttendanceCorrectionRequest(
    val id: String = "",
    val uid: String = "",
    val employeeName: String = "",
    val attendanceDate: String = "",
    val issueType: String = "LATE",
    val requestedPunchInTime: Timestamp? = null,
    val requestedPunchOutTime: Timestamp? = null,
    val reason: String = "",
    val status: String = "PENDING",
    val adminRemark: String = "",
    val reviewedBy: String = "",
    val reviewedAt: Timestamp? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)
