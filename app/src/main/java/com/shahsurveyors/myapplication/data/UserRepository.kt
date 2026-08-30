package com.shahsurveyors.myapplication.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
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

    suspend fun getUserProfile(uid: String): UserProfile? {
        val currentUid = requireCurrentUid()
        require(uid == currentUid) { "Users may only access their own profile" }
        return try {
            val document = usersCollection.document(currentUid).get().await()
            if (document.exists()) document.toObject(UserProfile::class.java) else null
        } catch (e: Exception) { null }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val currentUid = requireCurrentUid()
        require(profile.uid == currentUid) { "Users may only save their own profile" }
        usersCollection.document(currentUid).set(profile.copy(uid = currentUid), SetOptions.merge()).await()
    }

    /**
     * Creates the Firestore profile for an employee whose Firebase Auth account
     * was created by the authorized admin flow. The caller must still be signed
     * in as an active ADMIN; the employee UID is never treated as the caller UID.
     */
    suspend fun saveEmployeeProfileAsAdmin(profile: UserProfile) {
        requireAdminUid()
        require(profile.uid.isNotBlank()) { "Employee UID is required" }
        require(!profile.role.equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true)) {
            "Employee profile cannot be created as ADMIN"
        }
        usersCollection.document(profile.uid).set(
            profile.copy(
                role = "employee",
                approved = true,
                active = true
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun getAllEmployees(): List<UserProfile> {
        requireAdminUid()
        return usersCollection.get().await().toObjects(UserProfile::class.java)
            .filter { it.uid.isNotBlank() && it.active }
            .filter { !it.role.equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true) }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun getAllEmployeesForReports(): List<UserProfile> {
        requireAdminUid()
        return usersCollection.get().await().toObjects(UserProfile::class.java)
            .filter { it.uid.isNotBlank() }
            .filter { !it.role.equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true) }
            .sortedBy { it.name.lowercase() }
    }

    suspend fun updateUserStatus(uid: String, approved: Boolean, active: Boolean) {
        requireAdminUid()
        require(uid.isNotBlank()) { "Employee ID is required" }
        usersCollection.document(uid).update(
            mapOf(
                "approved" to approved,
                "active" to active,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
        ).await()
    }

    suspend fun updateUserAccess(uid: String, access: String) {
        requireAdminUid()
        require(uid.isNotBlank()) { "Employee ID is required" }
        val normalizedAccess = access.split(",")
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(",")
        usersCollection.document(uid).update(
            mapOf(
                "access" to normalizedAccess,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
        ).await()
    }
}
