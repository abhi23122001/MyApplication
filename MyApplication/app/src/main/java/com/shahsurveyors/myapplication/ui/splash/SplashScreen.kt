package com.shahsurveyors.myapplication.ui.splash

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.R
import com.shahsurveyors.myapplication.ui.theme.ShahDarkGreen
import com.shahsurveyors.myapplication.ui.theme.ShahWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onAnimationFinished: () -> Unit
) {

    var startAnimation by remember {
        mutableStateOf(false)
    }

    val scale by animateFloatAsState(
        targetValue =
            if (startAnimation) {
                1.0f
            } else {
                0.8f
            },

        animationSpec =
            tween(
                durationMillis = 1000,
                easing = LinearOutSlowInEasing
            ),

        label = "splash_scale"
    )


    val alpha by animateFloatAsState(
        targetValue =
            if (startAnimation) {
                1f
            } else {
                0f
            },

        animationSpec =
            tween(
                durationMillis = 1000
            ),

        label = "splash_alpha"
    )


    LaunchedEffect(Unit) {

        startAnimation = true

        delay(2500)

        onAnimationFinished()
    }


    Box(

        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    ShahDarkGreen
                ),

        contentAlignment =
            Alignment.Center
    ) {

        Column(

            horizontalAlignment =
                Alignment.CenterHorizontally,

            modifier =
                Modifier
                    .alpha(alpha)
                    .scale(scale)
        ) {

            // ====================================================
            // LOGO
            // ====================================================

            Image(

                painter =
                    painterResource(
                        id = R.drawable.app_logo
                    ),

                contentDescription =
                    "SHAH Logo",

                modifier =
                    Modifier.size(180.dp)
            )


            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )


            // ====================================================
            // APP NAME
            // ====================================================

            Text(

                text =
                    "SHAH ERP",

                color =
                    ShahWhite,

                fontSize =
                    32.sp,

                fontWeight =
                    FontWeight.Bold,

                letterSpacing =
                    1.sp
            )


            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )


            Text(

                text =
                    "All-in-One Business Management",

                color =
                    ShahWhite.copy(
                        alpha = 0.7f
                    ),

                fontSize =
                    16.sp,

                fontWeight =
                    FontWeight.Medium
            )


            Spacer(
                modifier =
                    Modifier.height(48.dp)
            )


            // ====================================================
            // LOADING INDICATOR
            // ====================================================

            CircularProgressIndicator(

                color =
                    ShahWhite,

                strokeWidth =
                    2.dp,

                modifier =
                    Modifier.size(24.dp)
            )


            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )


            // ====================================================
            // VERSION
            // ====================================================

            Text(

                text =
                    "v1.0.0",

                color =
                    ShahWhite.copy(
                        alpha = 0.5f
                    ),

                fontSize =
                    12.sp
            )
        }
    }
}