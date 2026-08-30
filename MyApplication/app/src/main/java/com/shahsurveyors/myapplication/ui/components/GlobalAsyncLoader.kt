package com.shahsurveyors.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shahsurveyors.myapplication.ui.theme.ShahBlack
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahWhite

@Composable
fun GlobalAsyncLoader(
    isLoading: Boolean,
    statusText: String = "Syncing data..."
) {

    if (!isLoading) {
        return
    }

    Dialog(

        onDismissRequest = {
            // Loading ke time dialog dismiss nahi hoga
        },

        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
    ) {

        Box(

            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        ShahBlack.copy(
                            alpha = 0.60f
                        )
                    ),

            contentAlignment =
                Alignment.Center
        ) {

            Column(

                modifier =
                    Modifier.padding(32.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.Center
            ) {

                CircularProgressIndicator(

                    color =
                        ShahGreen,

                    modifier =
                        Modifier.size(56.dp),

                    strokeWidth = 4.dp
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )

                Text(

                    text =
                        statusText.ifBlank {
                            "Processing..."
                        },

                    color =
                        ShahWhite,

                    fontSize =
                        16.sp,

                    fontWeight =
                        FontWeight.SemiBold,

                    textAlign =
                        TextAlign.Center
                )
            }
        }
    }
}