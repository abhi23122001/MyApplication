package com.shahsurveyors.myapplication.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.ui.theme.ShahBlack
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahWhite

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {

    if (onClick != null) {

        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = ShahWhite
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }

    } else {

        Card(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = ShahWhite
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}


@Composable
fun BroadcastNoticeCard(
    notice: String,
    isAdmin: Boolean = false,
    onClick: () -> Unit = {}
) {

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "notice_animation"
        )

    val scale by
    infiniteTransition.animateFloat(

        initialValue = 1.0f,

        targetValue = 1.08f,

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

        label = "notice_scale"
    )


    Surface(

        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(12.dp)
                )
                .border(

                    BorderStroke(
                        1.dp,
                        ShahGreen.copy(
                            alpha = 0.3f
                        )
                    ),

                    RoundedCornerShape(12.dp)
                ),

        color =
            ShahGreen.copy(
                alpha = 0.05f
            ),

        onClick = onClick
    ) {

        Row(

            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(

                imageVector =
                    Icons.Default.Campaign,

                contentDescription =
                    "Admin Notice",

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
                        if (isAdmin) {
                            "ADMIN BROADCAST"
                        } else {
                            "ADMIN NOTICE"
                        },

                    color =
                        ShahGreen,

                    fontSize =
                        11.sp,

                    fontWeight =
                        FontWeight.Bold,

                    letterSpacing =
                        0.5.sp
                )


                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )


                Text(

                    text = notice,

                    color =
                        ShahBlack,

                    fontSize =
                        14.sp,

                    fontWeight =
                        FontWeight.Medium
                )
            }
        }
    }
}