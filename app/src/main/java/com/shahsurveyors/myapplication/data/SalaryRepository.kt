package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.SalaryProfileModel
import kotlinx.coroutines.tasks.await

class SalaryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val collection = firestore.collection("salaryProfiles")

    suspend fun getHistory(employeeUid: String): List<SalaryProfileModel> {
        return collection
            .whereEqualTo("employeeUid", employeeUid)
            .get()
            .await()
            .toObjects(SalaryProfileModel::class.java)
            .sortedByDescending { it.effectiveFrom }
    }

    suspend fun getCurrent(employeeUid: String): SalaryProfileModel? {
        return getHistory(employeeUid).firstOrNull { it.active }
    }

    /**
     * Saves a new salary period and closes the previous active period.
     * This preserves increments for future payroll calculations.
     */
    suspend fun saveSalaryProfile(profile: SalaryProfileModel): String {
        val previous = getCurrent(profile.employeeUid)
        if (previous != null && previous.id.isNotBlank()) {
            collection.document(previous.id).update(
                mapOf(
                    "active" to false,
                    "effectiveTo" to profile.effectiveFrom
                )
            ).await()
        }

        val ref = if (profile.id.isBlank()) collection.document() else collection.document(profile.id)
        val saved = profile.copy(
            id = ref.id,
            setAt = profile.setAt ?: Timestamp.now(),
            active = true
        )
        ref.set(saved).await()
        return ref.id
    }
}
