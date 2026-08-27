package com.shahsurveyors.myapplication.models

import com.google.firebase.Timestamp

data class ClientPaymentRecord(
    val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val amount: Double = 0.0,
    val paymentDate: Timestamp? = null,
    val paymentMode: String = "BANK TRANSFER",
    val referenceNumber: String = "",
    val remark: String = "",
    val recordedByUid: String = "",
    val recordedByName: String = "",
    val createdAt: Timestamp? = null
)
