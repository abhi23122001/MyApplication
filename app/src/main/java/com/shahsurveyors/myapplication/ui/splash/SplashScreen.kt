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

    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(700), label = "splash_alpha")
    val scale by animateFloatAsState(if (visible) 1f else .88f, tween(900, easing = FastOutSlowInEasing), label = "splash_scale")
    val transition = rememberInfiniteTransition(label = "splash_motion")
    val rotation by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(3200, easing = FastOutSlowInEasing), RepeatMode.Restart), label = "rotation")
    val pulse by transition.animateFloat(.96f, 1.04f, infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")

    LaunchedEffect(Unit) {
        visible = true
        delay(550)
        fullText.forEachIndexed { index, _ ->
            typed = fullText.take(index + 1)
            delay(38)
        }
        delay(2100)
        onAnimationFinished()
    }

    Box(Modifier.fillMaxSize().background(ShahWhite), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(ShahLightGreen.copy(alpha = .12f), w * .43f, Offset(w * .5f, h * .40f))
            drawCircle(ShahGreen.copy(alpha = .06f), w * .65f, Offset(w * .5f, h * .40f))
            listOf(Offset(w*.13f,h*.20f), Offset(w*.84f,h*.23f), Offset(w*.18f,h*.59f), Offset(w*.80f,h*.63f)).forEach {
                drawCircle(ShahGreen.copy(alpha = .55f), 3.5f, it)
            }
        }

        Column(
            Modifier.fillMaxWidth().padding(horizontal = 28.dp).alpha(alpha).scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Box(Modifier.size(150.dp).scale(pulse), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().rotate(rotation)) {
                    drawArc(ShahGreen, -65f, 285f, false, style = Stroke(5f, cap = StrokeCap.Round))
                    drawCircle(ShahLightGreen.copy(alpha = .5f), 58.dp.toPx(), style = Stroke(2f))
                }
                Box(Modifier.size(108.dp).background(ShahWhite, CircleShape), contentAlignment = Alignment.Center) {
                    Image(painterResource(R.drawable.app_logo), "Shah Surveyors & Consultancy logo", Modifier.size(94.dp))
                }
            }

            Spacer(Modifier.height(22.dp))
            Text(
                text = typed.take(11),
                color = ShahDarkGreen,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (typed.length > 12) typed.drop(12) else "",
                color = ShahGreen,
                fontSize = 21.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(12.dp))
            Box(Modifier.width(58.dp).height(3.dp).background(ShahGreen, RoundedCornerShape(50)))
            Spacer(Modifier.height(13.dp))
            Text("Smart Solutions.\nStronger Tomorrow.", color = ShahDarkGreen.copy(alpha = .72f), fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(20.dp))

            SurveyIllustration(Modifier.fillMaxWidth().height(120.dp))

            Spacer(Modifier.height(16.dp))
            Text("Survey • Projects • People • Finance", color = ShahDarkGreen.copy(alpha = .55f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                repeat(3) { index ->
                    Box(Modifier.size(if (index == 0) 7.dp else 6.dp).background(if (index == 0) ShahGreen else ShahGreen.copy(alpha = .32f), CircleShape))
                }
            }
        }
    }
}

@Composable
private fun SurveyIllustration(modifier: Modifier) {
    Canvas(modifier) {
        val base = size.height * .82f
        val building = ShahLightGreen.copy(alpha = .55f)
        val line = ShahGreen.copy(alpha = .72f)
        drawLine(line, Offset(0f, base), Offset(size.width, base), 3f)
        drawRect(building, Offset(size.width*.03f, base-46f), Size(size.width*.13f, 46f))
        drawRect(building, Offset(size.width*.18f, base-64f), Size(size.width*.11f, 64f))
        drawRect(building, Offset(size.width*.33f, base-36f), Size(size.width*.09f, 36f))
        drawRect(building, Offset(size.width*.73f, base-52f), Size(size.width*.10f, 52f))
        drawRect(building, Offset(size.width*.87f, base-38f), Size(size.width*.09f, 38f))
        val cx = size.width*.58f
        val top = size.height*.18f
        drawLine(line, Offset(cx,top), Offset(cx-34f,base), 3f)
        drawLine(line, Offset(cx,top), Offset(cx+36f,base), 3f)
        drawLine(line, Offset(cx,top), Offset(cx,base), 3f)
        drawLine(line, Offset(cx-28f,base-12f), Offset(cx+30f,base-12f), 2f)
        drawRect(ShahDarkGreen.copy(alpha=.82f), Offset(cx-13f,top-8f), Size(26f,12f))
        drawCircle(ShahGreen, 4f, Offset(cx,top-2f))
    }
}
