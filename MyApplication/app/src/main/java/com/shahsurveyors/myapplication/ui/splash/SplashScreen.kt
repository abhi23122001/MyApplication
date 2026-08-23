package com.shahsurveyors.myapplication.ui.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.R
import com.shahsurveyors.myapplication.ui.theme.Navy
import com.shahsurveyors.myapplication.ui.theme.ElectricGold
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.8f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing)
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1500)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(3000)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Navy, Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(150.dp)
                    .scale(scale)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SHAH SURVEYORS & CONSULTANCY",
                color = ElectricGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.animateContentSize()
            )
        }
    }
}
