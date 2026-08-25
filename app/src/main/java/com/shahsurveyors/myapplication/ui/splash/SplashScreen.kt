package com.shahsurveyors.myapplication.ui.splash

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
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
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (startAnimation) 1f else .82f, tween(900, easing = LinearOutSlowInEasing), label = "splash_scale")
    val alpha by animateFloatAsState(if (startAnimation) 1f else 0f, tween(800), label = "splash_alpha")
    LaunchedEffect(Unit) { startAnimation = true; delay(2200); onAnimationFinished() }
    Box(Modifier.fillMaxSize().background(ShahDarkGreen),contentAlignment=Alignment.Center){
        Column(Modifier.fillMaxWidth().padding(horizontal=28.dp).alpha(alpha).scale(scale),horizontalAlignment=Alignment.CenterHorizontally){
            Surface(shape=RoundedCornerShape(30.dp),color=ShahWhite.copy(.10f),shadowElevation=8.dp){Image(painterResource(R.drawable.app_logo),"SHAH Logo",Modifier.padding(18.dp).size(145.dp))}
            Spacer(Modifier.height(26.dp));Text("SHAH ERP",color=ShahWhite,fontSize=34.sp,fontWeight=FontWeight.ExtraBold,letterSpacing=1.5.sp);Spacer(Modifier.height(6.dp));Text("Survey • Projects • People • Finance",color=ShahWhite.copy(.72f),fontSize=14.sp,fontWeight=FontWeight.Medium);Spacer(Modifier.height(38.dp));CircularProgressIndicator(color=ShahWhite,strokeWidth=2.dp,modifier=Modifier.size(24.dp));Spacer(Modifier.height(12.dp));Text("Preparing your workspace...",color=ShahWhite.copy(.65f),fontSize=11.sp);Spacer(Modifier.height(28.dp));Text("v1.0.0",color=ShahWhite.copy(.45f),fontSize=10.sp)
        }
    }
}
