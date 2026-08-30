package com.shahsurveyors.myapplication.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val usersCollection = firestore.collection(FirebaseConstants.COLLECTION_USERS)
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()

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
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val currentUid = requireCurrentUid()
        if (profile.uid == currentUid) {
            usersCollection.document(currentUid)
                .set(profile.copy(uid = currentUid), SetOptions.merge())
                .await()
            return
        }
        requireAdminUid()
        saveEmployeeProfileAsAdmin(profile)
    }

    /** Secure admin-only employee profile writer for an already-created Auth UID. */
    suspend fun saveEmployeeProfileAsAdmin(profile: UserProfile) {
        requireAdminUid()
        require(profile.uid.isNotBlank()) { "Employee UID is required" }
        require(!profile.role.equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true)) {
            "Employee profile cannot be created as ADMIN"
        }

        val data = hashMapOf<String, Any>(
            "uid" to profile.uid,
            "name" to profile.name.trim(),
            "email" to profile.email.trim(),
            "department" to profile.department.trim().uppercase(),
            "access" to normalizeAccess(profile.access)
        )

        functions.getHttpsCallable("saveEmployeeProfileAsAdmin")
            .call(data)
            .await()
    }

    /**
     * Creates both the Firebase Authentication account and the Firestore
     * employee profile on the trusted backend. The admin's Android Auth
     * session is never replaced by the new employee account.
     */
    suspend fun createEmployeeAccountAsAdmin(
        name: String,
        email: String,
        password: String,
        department: String,
        access: String
    ): String {
        requireAdminUid()
        require(name.isNotBlank()) { "Employee name is required" }
        require(email.trim().contains("@")) { "Valid employee email is required" }
        require(password.length >= 6) { "Password must be at least 6 characters" }
        require(department.isNotBlank()) { "Department is required" }

        val data = hashMapOf<String, Any>(
            "name" to name.trim(),
            "email" to email.trim(),
            "password" to password,
            "department" to department.trim().uppercase(),
            "access" to normalizeAccess(access)
        )
        val result = functions.getHttpsCallable("createEmployeeAccountAsAdmin")
            .call(data)
            .await()
        val uid = (result.data as? Map<*, *>)?.get("uid")?.toString().orEmpty()
        require(uid.isNotBlank()) { "Employee account was created without a UID" }
        return uid
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
        usersCollection.document(uid).update(
            mapOf(
                "access" to normalizeAccess(access),
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
        ).await()
    }

    private fun normalizeAccess(access: String): String =
        access.split(",", ";", "|")
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() }
            .distinct()
            .filter { it != "ALL" || access.contains("ALL", ignoreCase = true) }
            .joinToString(",")
}
