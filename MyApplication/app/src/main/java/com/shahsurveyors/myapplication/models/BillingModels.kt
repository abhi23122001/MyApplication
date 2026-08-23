package com.shahsurveyors.myapplication.models

data class BillingItem(
    val description: String,
    val qty: Double,
    val unit: String,
    val rate: Double
) {
    val taxableAmount: Double get() = qty * rate
}

data class BillingDocument(
    val docType: DocType,
    val clientName: String,
    val clientAddress: String,
    val clientGstin: String? = null,
    val items: List<BillingItem>,
    val isInterState: Boolean = false
)

enum class DocType {
    TAX_INVOICE, NON_GST_BILL, QUOTATION
}
