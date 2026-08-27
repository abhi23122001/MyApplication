package com.shahsurveyors.myapplication.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.data.FirebaseConstants
import com.shahsurveyors.myapplication.ui.theme.*
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

private data class LiveNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val type: String,
    val read: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenter(viewModel: DashboardViewModel, onDismiss: () -> Unit) {
    val firestore = remember { FirebaseFirestore.getInstance() }
    val uid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    var notifications by remember { mutableStateOf<List<LiveNotification>>(emptyList()) }

    LaunchedEffect(uid) {
        if (uid.isBlank()) return@LaunchedEffect
        val registration = firestore.collection(FirebaseConstants.COLLECTION_NOTIFICATIONS)
            .whereEqualTo("recipientUid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener
                notifications = snapshot.documents.map { doc ->
                    val timestamp = doc.getTimestamp("createdAt") ?: Timestamp.now()
                    LiveNotification(
                        id = doc.id,
                        title = doc.getString("title") ?: "Shah ERP",
                        message = doc.getString("message") ?: "New notification",
                        time = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(timestamp.toDate()),
                        type = doc.getString("type") ?: "GENERAL",
                        read = doc.getBoolean("read") ?: false
                    )
                }
            }
        awaitDispose { registration.remove() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ShahGrey,
        dragHandle = {
            Surface(Modifier.padding(top = 8.dp), RoundedCornerShape(50), color = ShahMediumGrey.copy(alpha = .35f)) {
                Spacer(Modifier.width(38.dp).height(4.dp))
            }
        }
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(13.dp), color = ShahGreen.copy(alpha = .10f)) {
                    Icon(Icons.Default.NotificationsActive, null, tint = ShahGreen, modifier = Modifier.padding(10.dp).size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Approval requests and ERP updates", color = ShahMediumGrey, fontSize = 11.sp)
                }
                TextButton(onClick = onDismiss) { Text("Done", color = ShahGreen) }
            }
            Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${notifications.size} notifications", color = ShahMediumGrey, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(8.dp), color = ShahGreen.copy(alpha = .08f)) {
                    Text("LIVE", color = ShahGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                }
            }
            if (notifications.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Text("No new notifications", color = ShahMediumGrey)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications, key = { it.id }) { item ->
                        val tint = if (item.type.startsWith("EXPENSE")) WarningAmber else ShahGreen
                        Card(
                            onClick = {
                                firestore.collection(FirebaseConstants.COLLECTION_NOTIFICATIONS).document(item.id).update("read", true)
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ShahWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
                                Box {
                                    Surface(shape = RoundedCornerShape(12.dp), color = tint.copy(alpha = .11f)) {
                                        Icon(
                                            if (item.type.startsWith("ATTENDANCE")) Icons.Default.PunchClock else Icons.Default.ReceiptLong,
                                            null,
                                            tint = tint,
                                            modifier = Modifier.padding(10.dp).size(22.dp)
                                        )
                                    }
                                    if (!item.read) Box(Modifier.size(7.dp).clip(CircleShape).background(tint).align(Alignment.TopEnd))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.title, fontWeight = FontWeight.Bold, color = ShahBlack, modifier = Modifier.weight(1f))
                                        Text(item.time, color = ShahMediumGrey, fontSize = 9.sp)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(item.message, color = ShahMediumGrey, fontSize = 12.sp, lineHeight = 17.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
