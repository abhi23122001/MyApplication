package com.shahsurveyors.myapplication.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.ui.components.GlassCard

@Composable
fun BroadcastNoticeCard(notice: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                Icons.Default.NotificationsActive,
                contentDescription = "Alert",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp * scale)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "ADMIN BROADCAST",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    notice,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    }
}
