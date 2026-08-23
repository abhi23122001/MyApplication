package com.shahsurveyors.myapplication.data

import com.shahsurveyors.myapplication.data.local.*
import com.shahsurveyors.myapplication.models.DocType
import kotlinx.coroutines.flow.Flow

class BillingRepository(private val dao: AppDao) {

    // Company & Bank
    val companyProfile: Flow<CompanyProfile?> = dao.getCompanyProfile()
    val bankDetails: Flow<BankDetails?> = dao.getBankDetails()

    suspend fun updateCompanyProfile(profile: CompanyProfile) = dao.updateCompanyProfile(profile)
    suspend fun updateBankDetails(details: BankDetails) = dao.updateBankDetails(details)

    // Documents
    val allDocuments: Flow<List<BillingDocumentEntity>> = dao.getAllDocuments()
    
    suspend fun getDocumentById(id: Long) = dao.getDocumentById(id)
    
    suspend fun saveDocument(document: BillingDocumentEntity, items: List<BillingItemEntity>): Long {
        val docId = dao.insertDocument(document)
        val itemsWithId = items.map { it.copy(documentId = docId) }
        dao.insertItems(itemsWithId)
        return docId
    }

    suspend fun updateDocument(document: BillingDocumentEntity, items: List<BillingItemEntity>) {
        dao.updateDocument(document)
        dao.deleteItemsForDocument(document.id)
        val itemsWithId = items.map { it.copy(documentId = document.id) }
        dao.insertItems(itemsWithId)
    }

    suspend fun deleteDocument(document: BillingDocumentEntity) {
        dao.deleteItemsForDocument(document.id)
        dao.deleteDocument(document)
    }

    fun getItemsForDocument(documentId: Long): Flow<List<BillingItemEntity>> = dao.getItemsForDocument(documentId)

    // Terms
    val allTerms: Flow<List<TermConditionEntity>> = dao.getAllTerms()
    suspend fun saveTerm(term: TermConditionEntity) = dao.insertTerm(term)
    suspend fun deleteTerm(term: TermConditionEntity) = dao.deleteTerm(term)

    // Numbering
    suspend fun getNextDocNumber(docType: DocType): String {
        val config = dao.getNumberingConfig(docType) ?: return "SSC/${docType.name.take(1)}/001"
        // Simple generation logic, can be improved
        return "${config.prefix}/${config.startingNumber}"
    }

    suspend fun updateNumberingConfig(config: DocNumberingConfig) = dao.updateNumberingConfig(config)
}
