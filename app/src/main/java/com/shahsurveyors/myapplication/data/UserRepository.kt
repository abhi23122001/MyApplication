package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val usersCollection = firestore.collection(FirebaseConstants.COLLECTION_USERS)

    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val document = usersCollection.document(uid).get().await()
            if (document.exists()) {
                document.toObject(UserProfile::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        usersCollection.document(profile.uid)
            .set(profile, SetOptions.merge())
            .await()
    }

    /**
     * Returns active non-admin staff for admin operations such as recording
     * leave on behalf of an employee. Do not rely on a single role value here:
     * staff can be employee, surveyor, site_manager, marketing, etc.
     */
    suspend fun getAllEmployees(): List<UserProfile> {
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.toObjects(UserProfile::class.java)
                .filter { it.uid.isNotBlank() }
                .filter { it.active }
                .filter { !it.role.equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true) }
                .sortedBy { it.name.lowercase() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateUserStatus(uid: String, approved: Boolean, active: Boolean) {
        usersCollection.document(uid)
            .update(
                mapOf(
                    "approved" to approved,
                    "active" to active,
                    "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            )
            .await()
    }
}
