package com.shahsurveyors.myapplication.ui.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.ui.components.GlassCard
import com.shahsurveyors.myapplication.ui.theme.ShahBlack
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahWhite

@Composable
fun DashboardBroadcastNoticeCard(
    notice: String,
    isAdmin: Boolean = false,
    onNoticeUpdated: ((String) -> Unit)? = null
) {

    var showDialog by remember {
        mutableStateOf(false)
    }

    var currentNotice by remember(notice) {
        mutableStateOf(notice)
    }

    var tempNotice by remember {
        mutableStateOf("")
    }

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "broadcast_pulse"
        )

    val scale by
    infiniteTransition.animateFloat(

        initialValue = 1f,

        targetValue = 1.15f,

        animationSpec =
            infiniteRepeatable(

                animation =
                    tween(
                        durationMillis = 1000,
                        easing =
                            FastOutSlowInEasing
                    ),

                repeatMode =
                    RepeatMode.Reverse
            ),

        label = "broadcast_scale"
    )


    GlassCard(

        modifier =
            Modifier.fillMaxWidth(),

        onClick =
            if (isAdmin) {

                {
                    tempNotice =
                        currentNotice

                    showDialog =
                        true
                }

            } else {
                null
            }

    ) {

        Row(

            verticalAlignment =
                Alignment.CenterVertically,

            modifier =
                Modifier.padding(8.dp)
        ) {

            Icon(

                imageVector =
                    Icons.Default.NotificationsActive,

                contentDescription =
                    "Admin Broadcast",

                tint =
                    ShahGreen,

                modifier =
                    Modifier
                        .size(28.dp)
                        .scale(scale)
            )


            Spacer(
                modifier =
                    Modifier.width(16.dp)
            )


            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(

                    text =
                        "ADMIN BROADCAST",

                    style =
                        MaterialTheme.typography
                            .labelSmall,

                    color =
                        ShahGreen,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )


                Text(

                    text =
                        currentNotice.ifBlank {
                            "No new announcement"
                        },

                    style =
                        MaterialTheme.typography
                            .bodyMedium,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        ShahBlack
                )
            }
        }
    }


    // ========================================================
    // ADMIN NOTICE EDIT DIALOG
    // ========================================================

    if (showDialog) {

        AlertDialog(

            onDismissRequest = {
                showDialog =
                    false
            },

            title = {

                Text(
                    text = "Update Notice",
                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                OutlinedTextField(

                    value =
                        tempNotice,

                    onValueChange = {
                        tempNotice = it
                    },

                    label = {
                        Text("Notice Text")
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    minLines = 3,

                    maxLines = 6
                )
            },

            confirmButton = {

                Button(

                    onClick = {

                        val updatedNotice =
                            tempNotice.trim()

                        if (
                            updatedNotice.isNotEmpty()
                        ) {

                            currentNotice =
                                updatedNotice

                            onNoticeUpdated?.invoke(
                                updatedNotice
                            )
                        }

                        showDialog =
                            false
                    },

                    colors =
                        ButtonDefaults
                            .buttonColors(
                                containerColor =
                                    ShahGreen,

                                contentColor =
                                    ShahWhite
                            )
                ) {

                    Text(
                        "UPDATE",
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {
                        showDialog =
                            false
                    }
                ) {

                    Text(
                        "CANCEL",
                        color =
                            ShahGreen
                    )
                }
            }
        )
    }
}