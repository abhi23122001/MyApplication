package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val notifications = firestore.collection(FirebaseConstants.COLLECTION_NOTIFICATIONS)

    private fun currentUid(): String =
        auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Authentication required")

    suspend fun createForAdmins(
        type: String,
        title: String,
        message: String,
        actorUid: String = "",
        actorName: String = "",
        referenceId: String = "",
        route: String = ""
    ) {
        val senderUid = actorUid.ifBlank { currentUid() }
        notifications.add(
            mapOf(
                "type" to type,
                "title" to title,
                "message" to message,
                "actorUid" to senderUid,
                "actorName" to actorName,
                "uid" to senderUid,
                "referenceId" to referenceId,
                "route" to route,
                "targetRole" to FirebaseConstants.ROLE_ADMIN,
                "read" to false,
                "createdAt" to Timestamp.now()
            )
        ).await()
    }

    suspend fun createForUser(
        recipientUid: String,
        type: String,
        title: String,
        message: String,
        referenceId: String = "",
        route: String = ""
    ) {
        if (recipientUid.isBlank()) return
        val senderUid = currentUid()
        notifications.add(
            mapOf(
                "type" to type,
                "title" to title,
                "message" to message,
                "uid" to senderUid,
                "actorUid" to senderUid,
                "referenceId" to referenceId,
                "route" to route,
                "recipientUid" to recipientUid,
                "read" to false,
                "createdAt" to Timestamp.now()
            )
        ).await()
    }

    suspend fun getUnreadCount(recipientUid: String, isAdmin: Boolean): Int {
        if (recipientUid.isBlank()) return 0
        return try {
            notifications
                .whereEqualTo("recipientUid", recipientUid)
                .whereEqualTo("read", false)
                .get()
                .await()
                .size()
        } catch (_: Exception) {
            0
        }
    }
}
