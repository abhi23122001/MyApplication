package com.shahsurveyors.myapplication.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.R
import com.shahsurveyors.myapplication.ui.theme.ShahDarkGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahLightGreen
import com.shahsurveyors.myapplication.ui.theme.ShahWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var typed by remember { mutableStateOf("") }
    val fullText = "Welcome to Shah Surveyors & Consultancy."

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(650),
        label = "splash_alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.88f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "splash_scale"
    )
    val transition = rememberInfiniteTransition(label = "splash_motion")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "logo_rotation"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(500)
        fullText.forEachIndexed { index, _ ->
            typed = fullText.take(index + 1)
            delay(42)
        }
        delay(2100)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ShahWhite)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft green glow behind the logo, matching the reference.
            drawCircle(
                color = ShahLightGreen.copy(alpha = 0.10f),
                radius = w * 0.43f,
                center = Offset(w * 0.5f, h * 0.32f)
            )
            drawCircle(
                color = ShahGreen.copy(alpha = 0.045f),
                radius = w * 0.64f,
                center = Offset(w * 0.5f, h * 0.32f)
            )

            // Small floating green/yellow particles.
            listOf(
                Offset(w * 0.12f, h * 0.18f),
                Offset(w * 0.83f, h * 0.16f),
                Offset(w * 0.18f, h * 0.35f),
                Offset(w * 0.84f, h * 0.31f),
                Offset(w * 0.10f, h * 0.52f),
                Offset(w * 0.90f, h * 0.48f)
            ).forEachIndexed { index, point ->
                drawCircle(
                    color = if (index == 1 || index == 3) ShahGreen.copy(alpha = 0.80f) else ShahGreen.copy(alpha = 0.42f),
                    radius = if (index % 2 == 0) 2.8f else 3.8f,
                    center = point
                )
            }

            // Gentle ground glow at the bottom.
            drawCircle(
                color = ShahLightGreen.copy(alpha = 0.10f),
                radius = w * 0.75f,
                center = Offset(w * 0.50f, h * 0.92f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 24.dp)
                .alpha(alpha)
                .scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(38.dp))

            // Animated logo with rotating green ring.
            Box(
                modifier = Modifier
                    .size(154.dp)
                    .scale(pulse),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation)
                ) {
                    drawArc(
                        color = ShahGreen,
                        startAngle = -82f,
                        sweepAngle = 286f,
                        useCenter = false,
                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = ShahLightGreen.copy(alpha = 0.35f),
                        startAngle = 115f,
                        sweepAngle = 70f,
                        useCenter = false,
                        style = Stroke(width = 2f, cap = StrokeCap.Round)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(ShahWhite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.app_logo),
                        contentDescription = "Shah Surveyors & Consultancy",
                        modifier = Modifier.size(96.dp)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // Typewriter title.
            val welcomePart = typed.take(11)
            val companyPart = if (typed.length > 12) typed.drop(12) else ""
            Text(
                text = welcomePart,
                color = ShahDarkGreen,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = companyPart,
                color = ShahGreen,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .width(58.dp)
                    .height(3.dp)
                    .background(ShahGreen, RoundedCornerShape(50))
            )
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Smart Solutions.\nStronger Tomorrow.",
                color = ShahDarkGreen.copy(alpha = 0.72f),
                fontSize = 11.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(20.dp))
            SurveyIllustration(Modifier.fillMaxWidth().height(142.dp))

            Spacer(Modifier.height(6.dp))
            Text(
                text = "Survey • Projects • People • Finance",
                color = ShahDarkGreen.copy(alpha = 0.48f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == 0) 7.dp else 6.dp)
                            .background(
                                if (index == 0) ShahGreen else ShahGreen.copy(alpha = 0.30f),
                                CircleShape
                            )
                    )
                }
            }
            Spacer(Modifier.height(26.dp))
        }
    }
}

@Composable
private fun SurveyIllustration(modifier: Modifier) {
    Canvas(modifier) {
        val base = size.height * 0.88f
        val building = ShahLightGreen.copy(alpha = 0.52f)
        val line = ShahGreen.copy(alpha = 0.70f)

        // Minimal skyline.
        drawLine(line, Offset(0f, base), Offset(size.width, base), 2.5f)
        val buildings = listOf(
            Triple(0.02f, 42f, 0.12f),
            Triple(0.17f, 60f, 0.10f),
            Triple(0.30f, 34f, 0.08f),
            Triple(0.69f, 50f, 0.10f),
            Triple(0.84f, 38f, 0.11f)
        )
        buildings.forEach { (x, height, width) ->
            drawRect(
                color = building,
                topLeft = Offset(size.width * x, base - height),
                size = Size(size.width * width, height)
            )
        }

        // Tiny windows.
        listOf(
            Pair(0.055f, 30f), Pair(0.055f, 18f), Pair(0.205f, 43f),
            Pair(0.205f, 28f), Pair(0.335f, 21f), Pair(0.735f, 34f),
            Pair(0.735f, 19f), Pair(0.89f, 25f)
        ).forEach { (x, y) ->
            drawRect(
                color = ShahWhite.copy(alpha = 0.85f),
                topLeft = Offset(size.width * x, base - y),
                size = Size(4f, 5f)
            )
        }

        // Total station tripod.
        val cx = size.width * 0.60f
        val top = size.height * 0.16f
        drawLine(line, Offset(cx, top), Offset(cx - 38f, base), 2.8f)
        drawLine(line, Offset(cx, top), Offset(cx + 40f, base), 2.8f)
        drawLine(line, Offset(cx, top), Offset(cx, base), 2.8f)
        drawLine(line, Offset(cx - 31f, base - 10f), Offset(cx + 32f, base - 10f), 2f)
        drawRect(
            color = ShahDarkGreen.copy(alpha = 0.82f),
            topLeft = Offset(cx - 15f, top - 8f),
            size = Size(30f, 13f)
        )
        drawCircle(ShahGreen, 4f, Offset(cx, top - 2f))
        drawLine(line, Offset(cx - 10f, top + 5f), Offset(cx + 10f, top + 5f), 2f)

        // Soft green landscape.
        drawLine(
            ShahLightGreen.copy(alpha = 0.75f),
            Offset(0f, base + 2f),
            Offset(size.width * 0.28f, base - 10f),
            8f
        )
        drawLine(
            ShahLightGreen.copy(alpha = 0.75f),
            Offset(size.width * 0.28f, base - 10f),
            Offset(size.width * 0.55f, base + 2f),
            8f
        )
    }
}
