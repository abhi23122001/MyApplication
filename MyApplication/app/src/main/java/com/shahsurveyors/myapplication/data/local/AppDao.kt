package com.shahsurveyors.myapplication.data.local

import androidx.room.*
import com.shahsurveyors.myapplication.models.DocType
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Company Profile
    @Query("SELECT * FROM company_profile WHERE id = 1")
    fun getCompanyProfile(): Flow<CompanyProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateCompanyProfile(profile: CompanyProfile)

    // Bank Details
    @Query("SELECT * FROM bank_details WHERE id = 1")
    fun getBankDetails(): Flow<BankDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBankDetails(details: BankDetails)

    // Billing Documents
    @Query("SELECT * FROM billing_documents ORDER BY date DESC")
    fun getAllDocuments(): Flow<List<BillingDocumentEntity>>

    @Query("SELECT * FROM billing_documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): BillingDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: BillingDocumentEntity): Long

    @Update
    suspend fun updateDocument(document: BillingDocumentEntity)

    @Delete
    suspend fun deleteDocument(document: BillingDocumentEntity)

    // Billing Items
    @Query("SELECT * FROM billing_items WHERE documentId = :documentId ORDER BY orderIndex")
    fun getItemsForDocument(documentId: Long): Flow<List<BillingItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<BillingItemEntity>)

    @Query("DELETE FROM billing_items WHERE documentId = :documentId")
    suspend fun deleteItemsForDocument(documentId: Long)

    // Terms & Conditions
    @Query("SELECT * FROM terms_conditions ORDER BY orderIndex")
    fun getAllTerms(): Flow<List<TermConditionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerm(term: TermConditionEntity)

    @Delete
    suspend fun deleteTerm(term: TermConditionEntity)

    // Numbering Config
    @Query("SELECT * FROM numbering_configs WHERE docType = :docType")
    suspend fun getNumberingConfig(docType: DocType): DocNumberingConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateNumberingConfig(config: DocNumberingConfig)
}
