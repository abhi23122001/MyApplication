package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.shahsurveyors.myapplication.models.AdvanceSalaryRequest
import com.shahsurveyors.myapplication.models.PayrollRecord
import com.shahsurveyors.myapplication.models.SalaryProfileModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SalaryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ========================================================
    // SALARY PROFILES (HISTORICAL & EFFECTIVE DATED)
    // ========================================================

    /**
     * Saves a new salary profile.
     * Closes the previous active profile without deleting historical records.
     */
    suspend fun saveSalaryProfile(profile: SalaryProfileModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = firestore.collection("salaryProfiles")

            // 1. Find existing active profiles for this employee
            val existingActiveDocs = collection
                .whereEqualTo("employeeUid", profile.employeeUid)
                .whereEqualTo("active", true)
                .get()
                .await()

            val batch = firestore.batch()

            // Close existing active periods
            for (doc in existingActiveDocs.documents) {
                batch.update(
                    doc.reference,
                    mapOf(
                        "active" to false,
                        "effectiveTo" to profile.effectiveFrom
                    )
                )
            }

            // 2. Create new profile document
            val newDocRef = if (profile.id.isNotBlank()) {
                collection.document(profile.id)
            } else {
                collection.document()
            }

            val toSave = profile.copy(
                id = newDocRef.id,
                active = true,
                setAt = System.currentTimeMillis()
            )

            batch.set(newDocRef, toSave)
            batch.commit().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getSalaryProfilesForEmployee(employeeUid: String): List<SalaryProfileModel> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("salaryProfiles")
                    .whereEqualTo("employeeUid", employeeUid)
                    .orderBy("effectiveFrom", Query.Direction.DESCENDING)
                    .get()
                    .await()

                snapshot.toObjects(SalaryProfileModel::class.java)
            } catch (e: Exception) {
                // Fallback without order by if index is building
                try {
                    val snapshot = firestore.collection("salaryProfiles")
                        .whereEqualTo("employeeUid", employeeUid)
                        .get()
                        .await()
                    snapshot.toObjects(SalaryProfileModel::class.java).sortedByDescending { it.effectiveFrom }
                } catch (e2: Exception) {
                    emptyList()
                }
            }
        }

    suspend fun getAllSalaryProfiles(): List<SalaryProfileModel> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("salaryProfiles")
                .get()
                .await()
            snapshot.toObjects(SalaryProfileModel::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ========================================================
    // ADVANCE SALARY REQUESTS
    // ========================================================

    suspend fun submitAdvanceSalaryRequest(request: AdvanceSalaryRequest): Boolean = withContext(Dispatchers.IO) {
        try {
            val docRef = if (request.id.isNotBlank()) {
                firestore.collection("advanceSalaryRequests").document(request.id)
            } else {
                firestore.collection("advanceSalaryRequests").document()
            }

            val toSave = request.copy(
                id = docRef.id,
                status = "PENDING",
                appliedAt = System.currentTimeMillis()
            )

            docRef.set(toSave).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getAdvanceRequestsForEmployee(employeeUid: String): List<AdvanceSalaryRequest> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("advanceSalaryRequests")
                    .whereEqualTo("employeeUid", employeeUid)
                    .get()
                    .await()
                snapshot.toObjects(AdvanceSalaryRequest::class.java).sortedByDescending { it.appliedAt }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }

    suspend fun getAllAdvanceRequests(): List<AdvanceSalaryRequest> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("advanceSalaryRequests")
                .get()
                .await()
            snapshot.toObjects(AdvanceSalaryRequest::class.java).sortedByDescending { it.appliedAt }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun decideAdvanceRequest(
        requestId: String,
        status: String, // APPROVED or REJECTED
        approvedAmount: Double,
        installments: Int,
        adminUid: String,
        adminName: String,
        note: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val updates = hashMapOf<String, Any>(
                "status" to status,
                "approvedAmount" to if (status == "APPROVED") approvedAmount else 0.0,
                "installments" to if (status == "APPROVED") installments else 1,
                "decidedAt" to System.currentTimeMillis(),
                "decidedByUid" to adminUid,
                "decidedByName" to adminName,
                "note" to note
            )

            firestore.collection("advanceSalaryRequests")
                .document(requestId)
                .update(updates)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // ========================================================
    // PAYROLL RECORDS
    // ========================================================

    suspend fun savePayrollRecord(record: PayrollRecord): Boolean = withContext(Dispatchers.IO) {
        try {
            firestore.collection("payrollRecords")
                .document(record.id)
                .set(record, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getPayrollRecordsForMonth(yearMonth: String): List<PayrollRecord> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("payrollRecords")
                .whereEqualTo("salaryMonth", yearMonth)
                .get()
                .await()
            snapshot.toObjects(PayrollRecord::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getPayrollRecordForEmployee(employeeUid: String, yearMonth: String): PayrollRecord? =
        withContext(Dispatchers.IO) {
            try {
                val doc = firestore.collection("payrollRecords")
                    .document("${employeeUid}_${yearMonth}")
                    .get()
                    .await()

                if (doc.exists()) {
                    doc.toObject(PayrollRecord::class.java)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
}
