package com.shahsurveyors.myapplication.data

import com.shahsurveyors.myapplication.data.local.AppDao
import com.shahsurveyors.myapplication.data.local.BankDetails
import com.shahsurveyors.myapplication.data.local.BillingDocumentEntity
import com.shahsurveyors.myapplication.data.local.BillingItemEntity
import com.shahsurveyors.myapplication.data.local.CompanyProfile
import com.shahsurveyors.myapplication.data.local.DocNumberingConfig
import com.shahsurveyors.myapplication.data.local.TermConditionEntity
import com.shahsurveyors.myapplication.models.DocType
import kotlinx.coroutines.flow.Flow

class BillingRepository(
    private val dao: AppDao
) {
    val companyProfile: Flow<CompanyProfile?> = dao.getCompanyProfile()
    suspend fun updateCompanyProfile(profile: CompanyProfile) = dao.updateCompanyProfile(profile)

    val bankDetails: Flow<BankDetails?> = dao.getBankDetails()
    suspend fun updateBankDetails(details: BankDetails) = dao.updateBankDetails(details)

    val allDocuments: Flow<List<BillingDocumentEntity>> = dao.getAllDocuments()
    suspend fun getDocumentById(id: Long): BillingDocumentEntity? = dao.getDocumentById(id)

    suspend fun saveDocument(document: BillingDocumentEntity, items: List<BillingItemEntity>): Long {
        val documentId = dao.insertDocument(document)
        if (items.isNotEmpty()) {
            dao.insertItems(items.mapIndexed { index, item ->
                item.copy(id = 0, documentId = documentId, orderIndex = index)
            })
        }
        return documentId
    }

    suspend fun updateDocument(document: BillingDocumentEntity, items: List<BillingItemEntity>) {
        dao.updateDocument(document)
        dao.deleteItemsForDocument(document.id)
        if (items.isNotEmpty()) {
            dao.insertItems(items.mapIndexed { index, item ->
                item.copy(id = 0, documentId = document.id, orderIndex = index)
            })
        }
    }

    suspend fun updatePaymentStatus(
        documentId: Long,
        paymentStatus: String,
        paidAmount: Double
    ) {
        dao.updatePaymentStatus(documentId, paymentStatus, paidAmount)
    }

    suspend fun deleteDocument(document: BillingDocumentEntity) {
        dao.deleteItemsForDocument(document.id)
        dao.deleteDocument(document)
    }

    fun getItemsForDocument(documentId: Long): Flow<List<BillingItemEntity>> = dao.getItemsForDocument(documentId)

    val allTerms: Flow<List<TermConditionEntity>> = dao.getAllTerms()
    suspend fun saveTerm(term: TermConditionEntity) = dao.insertTerm(term)
    suspend fun deleteTerm(term: TermConditionEntity) = dao.deleteTerm(term)

    suspend fun getNextDocNumber(docType: DocType): String {
        val config = dao.getNumberingConfig(docType)
        if (config == null) return "SSC/${docType.name.take(1)}/001"
        return buildDocumentNumber(config)
    }

    private fun buildDocumentNumber(config: DocNumberingConfig): String {
        val prefix = config.prefix.trim()
        val number = config.startingNumber.coerceAtLeast(1).toString().padStart(3, '0')
        val financialYear = config.financialYear.trim()
        return if (financialYear.isBlank()) "$prefix/$number" else "$prefix/$financialYear/$number"
    }

    suspend fun updateNumberingConfig(config: DocNumberingConfig) = dao.updateNumberingConfig(config)
}
