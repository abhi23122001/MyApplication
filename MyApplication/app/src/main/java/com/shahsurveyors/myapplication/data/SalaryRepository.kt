package com.shahsurveyors.myapplication.data

import com.google.firebase.auth.FirebaseAuth
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
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // ========================================================
    // SALARY PROFILES (HISTORICAL & EFFECTIVE DATED)
    // ========================================================

    suspend fun saveSalaryProfile(profile: SalaryProfileModel): Boolean = withContext(Dispatchers.IO) {
        try {
            val collection = firestore.collection("salaryProfiles")
            val currentUid = auth.currentUser?.uid ?: ""

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

            val data = hashMapOf(
                "id" to newDocRef.id,
                "uid" to profile.employeeUid,
                "employeeUid" to profile.employeeUid,
                "employeeName" to profile.employeeName,
                "employeeId" to profile.employeeId,
                "department" to profile.department,
                "payType" to profile.payType,
                "monthlySalary" to profile.monthlySalary,
                "dailyRate" to profile.dailyRate,
                "overtimeRatePerHour" to profile.overtimeRatePerHour,
                "effectiveFrom" to profile.effectiveFrom,
                "effectiveTo" to profile.effectiveTo,
                "note" to profile.note,
                "setByUid" to currentUid,
                "setByName" to profile.setByName,
                "setAt" to System.currentTimeMillis(),
                "active" to true
            )

            batch.set(newDocRef, data)
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
                    .get()
                    .await()
                snapshot.toObjects(SalaryProfileModel::class.java).sortedByDescending { it.effectiveFrom }
            } catch (e: Exception) {
                emptyList()
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
            val currentUid = auth.currentUser?.uid ?: request.employeeUid
            val docRef = if (request.id.isNotBlank()) {
                firestore.collection("advanceSalaryRequests").document(request.id)
            } else {
                firestore.collection("advanceSalaryRequests").document()
            }

            val data = hashMapOf(
                "id" to docRef.id,
                "uid" to currentUid,
                "userUid" to currentUid,
                "employeeUid" to currentUid,
                "employeeName" to request.employeeName,
                "employeeId" to request.employeeId,
                "department" to request.department,
                "requestedAmount" to request.requestedAmount,
                "approvedAmount" to 0.0,
                "installments" to request.installments,
                "requestedMonth" to request.requestedMonth,
                "reason" to request.reason,
                "status" to "PENDING",
                "appliedAt" to System.currentTimeMillis()
            )

            docRef.set(data).await()
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
        status: String,
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
            val currentUid = auth.currentUser?.uid ?: ""
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
