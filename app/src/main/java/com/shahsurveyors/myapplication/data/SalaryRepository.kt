package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.SalaryProfileModel
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.tasks.await

class SalaryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val collection = firestore.collection("salaryProfiles")
    private val usersCollection = firestore.collection(FirebaseConstants.COLLECTION_USERS)

    private fun requireCurrentUid(): String =
        auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Authentication required")

    private suspend fun requireAdminUid(): String {
        val uid = requireCurrentUid()
        val profile = usersCollection.document(uid).get().await().toObject(UserProfile::class.java)
        require(profile?.active == true && profile.role.equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true)) {
            "Admin authorization required"
        }
        return uid
    }

    private suspend fun requireSelfOrAdmin(targetUid: String): String {
        require(targetUid.isNotBlank()) { "Employee ID is required" }
        val currentUid = requireCurrentUid()
        if (targetUid == currentUid) return currentUid
        requireAdminUid()
        return currentUid
    }

    suspend fun getHistory(employeeUid: String): List<SalaryProfileModel> {
        requireSelfOrAdmin(employeeUid)
        return collection
            .whereEqualTo("employeeUid", employeeUid)
            .get()
            .await()
            .toObjects(SalaryProfileModel::class.java)
            .sortedByDescending { it.effectiveFrom }
    }

    suspend fun getAllProfiles(): List<SalaryProfileModel> {
        requireAdminUid()
        return collection
            .get()
            .await()
            .toObjects(SalaryProfileModel::class.java)
            .filter { it.employeeUid.isNotBlank() }
            .sortedByDescending { it.effectiveFrom }
    }

    suspend fun getCurrent(employeeUid: String): SalaryProfileModel? {
        return getHistory(employeeUid).firstOrNull { it.active }
    }

    suspend fun saveSalaryProfile(profile: SalaryProfileModel): String {
        requireAdminUid()
        require(profile.employeeUid.isNotBlank()) { "Employee ID is required" }
        require(profile.monthlySalary >= 0.0 && profile.dailyRate >= 0.0 && profile.overtimeRatePerHour >= 0.0) {
            "Salary values cannot be negative"
        }
        require(profile.effectiveFrom.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            "Invalid effective date"
        }

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
            setByUid = requireCurrentUid(),
            setAt = profile.setAt ?: Timestamp.now(),
            active = true
        )
        ref.set(saved).await()
        return ref.id
    }
}
