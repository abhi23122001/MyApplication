package com.shahsurveyors.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shahsurveyors.myapplication.ui.theme.ElectricGold
import com.shahsurveyors.myapplication.ui.theme.GlassBlack

@Composable
fun LoadingOverlay(isLoading: Boolean) {
    if (isLoading) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GlassBlack),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ElectricGold,
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp
                )
            }
        }
    }
}
