package com.shahsurveyors.myapplication.models

/**
 * Single billing item.
 *
 * taxableAmount = quantity × rate
 */
data class BillingItem(
    val description: String,
    val qty: Double,
    val unit: String,
    val rate: Double
) {

    val taxableAmount: Double
        get() {
            val safeQty = qty.coerceAtLeast(0.0)
            val safeRate = rate.coerceAtLeast(0.0)

            return safeQty * safeRate
        }
}


/**
 * Billing document used by quotation/bill/invoice modules.
 */
data class BillingDocument(
    val docType: DocType,
    val clientName: String,
    val clientAddress: String,
    val clientGstin: String? = null,
    val items: List<BillingItem>,
    val isInterState: Boolean = false
) {

    /**
     * Total taxable value before GST.
     */
    val subTotal: Double
        get() = items.sumOf { it.taxableAmount }
}


/**
 * Supported document types.
 */
enum class DocType {

    /**
     * GST Tax Invoice.
     */
    TAX_INVOICE,

    /**
     * Non-GST bill.
     */
    NON_GST_BILL,

    /**
     * Quotation / estimate.
     */
    QUOTATION
}