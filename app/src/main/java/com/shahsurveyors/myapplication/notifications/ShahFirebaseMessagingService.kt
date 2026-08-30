package com.shahsurveyors.myapplication.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.shahsurveyors.myapplication.MainActivity
import com.shahsurveyors.myapplication.R

class ShahFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val PREFS = "fcm"
        private const val PREF_TOKEN = "token"
        private const val CHANNEL_ID = "shah_erp_notifications"
        private const val CHANNEL_NAME = "Shah ERP Notifications"
    }

    private fun saveToken(token: String) {
        if (token.isBlank()) return
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_TOKEN, token).apply()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .set(
                mapOf(
                    "fcmToken" to token,
                    "fcmTokenUpdatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Shah ERP"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "New notification"
        val notificationId = message.data["notificationId"].orEmpty()
        val route = message.data["route"].orEmpty()

        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (notificationId.isNotBlank()) putExtra("notificationId", notificationId)
            if (route.isNotBlank()) putExtra("notificationRoute", route)
        }

        val pending = PendingIntent.getActivity(
            this,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pending)
            .build()

        val canPost = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (canPost) {
            val id = if (notificationId.isNotBlank()) notificationId.hashCode() else (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            NotificationManagerCompat.from(this).notify(id, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Shah ERP approvals, requests and work updates"
                    enableVibration(true)
                }
            )
        }
    }
}
