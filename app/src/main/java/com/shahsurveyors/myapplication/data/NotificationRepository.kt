package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val notifications = firestore.collection(FirebaseConstants.COLLECTION_NOTIFICATIONS)

    suspend fun createForAdmins(
        type: String,
        title: String,
        message: String,
        actorUid: String = "",
        actorName: String = "",
        referenceId: String = "",
        route: String = ""
    ) {
        notifications.add(
            mapOf(
                "type" to type,
                "title" to title,
                "message" to message,
                "actorUid" to actorUid,
                "actorName" to actorName,
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
        notifications.add(
            mapOf(
                "type" to type,
                "title" to title,
                "message" to message,
                "referenceId" to referenceId,
                "route" to route,
                "recipientUid" to recipientUid,
                "read" to false,
                "createdAt" to Timestamp.now()
            )
        ).await()
    }
}
