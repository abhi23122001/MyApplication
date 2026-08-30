package com.shahsurveyors.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shahsurveyors.myapplication.models.DocType

@Entity(tableName = "company_profile")
data class CompanyProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "SHAH SURVEYORS AND CONSULTANCY",
    val address: String = "Waidhan, Singrauli, MP - 486889",
    val email: String = "info@shahsurveyors.com",
    val phone: String = "+91 9999999999",
    val logoUri: String? = null,
    val sealUri: String? = null,
    val signatureUri: String? = null,
    val footerText: String = "This is Computer Generated File."
)

@Entity(tableName = "bank_details")
data class BankDetails(
    @PrimaryKey val id: Int = 1,
    val bankName: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val gstin: String = "",
    val branchAddress: String = ""
)

@Entity(tableName = "billing_documents")
data class BillingDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docType: DocType,
    val docNumber: String,
    val clientName: String,
    val clientAddress: String,
    val clientGstin: String? = null,
    val date: Long = System.currentTimeMillis(),
    val inquiryDate: Long? = null,
    val validUntil: Long? = null,
    val gstType: String = "CGST_SGST",
    val gstPercentage: Double = 18.0,
    val subTotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val status: String = "DRAFT",
    val paymentStatus: String = "UNPAID",
    val paidAmount: Double = 0.0,
    val termsAndConditions: String = "",
    val referenceDocNumber: String? = null
)

@Entity(tableName = "billing_items")
data class BillingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val description: String,
    val unit: String,
    val qty: Double,
    val rate: Double,
    val amount: Double,
    val orderIndex: Int = 0
)

@Entity(tableName = "terms_conditions")
data class TermConditionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val isDefault: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "numbering_configs")
data class DocNumberingConfig(
    @PrimaryKey val docType: DocType,
    val prefix: String,
    val startingNumber: Int = 1,
    val financialYear: String = "26-27"
)
