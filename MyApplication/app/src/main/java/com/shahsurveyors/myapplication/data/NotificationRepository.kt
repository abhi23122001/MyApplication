package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.models.AppNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun sendNotification(notification: AppNotification): Boolean = withContext(Dispatchers.IO) {
        try {
            val docRef = if (notification.id.isNotBlank()) {
                firestore.collection("notifications").document(notification.id)
            } else {
                firestore.collection("notifications").document()
            }

            val toSave = notification.copy(
                id = docRef.id,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            docRef.set(toSave).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Real-time stream of notifications for a user (broadcasts or user-specific).
     */
    fun listenToNotifications(userUid: String, isAdmin: Boolean): Flow<List<AppNotification>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val all = snapshot.toObjects(AppNotification::class.java)
                    val filtered = if (isAdmin) {
                        all
                    } else {
                        all.filter { it.targetUid.isBlank() || it.targetUid == userUid }
                    }
                    trySend(filtered)
                }
            }

        awaitClose {
            listener.remove()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun markAsRead(notificationId: String) = withContext(Dispatchers.IO) {
        try {
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
