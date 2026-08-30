package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shahsurveyors.myapplication.ui.theme.ShahDarkGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGrey
import com.shahsurveyors.myapplication.ui.theme.ShahMediumGrey
import com.shahsurveyors.myapplication.ui.theme.ShahWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoFenceSettingsScreen(
    onBack: () -> Unit
) {
    var radius by remember {
        mutableStateOf("200")
    }

    var enableGeoFence by remember {
        mutableStateOf(true)
    }

    var allowRemoteAttendance by remember {
        mutableStateOf(false)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var savedMessage by remember {
        mutableStateOf(false)
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "Geo-Fence Settings",
                        fontWeight = FontWeight.Bold,
                        color = ShahWhite
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ShahWhite
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShahDarkGreen
                )
            )
        },

        floatingActionButton = {

            FloatingActionButton(

                onClick = {

                    val radiusValue =
                        radius.toIntOrNull()

                    when {

                        !enableGeoFence -> {
                            errorMessage = null
                            savedMessage = true
                        }

                        radiusValue == null -> {
                            errorMessage =
                                "Please enter a valid radius."
                            savedMessage = false
                        }

                        radiusValue <= 0 -> {
                            errorMessage =
                                "Radius must be greater than 0 meters."
                            savedMessage = false
                        }

                        radiusValue > 10000 -> {
                            errorMessage =
                                "Radius cannot be greater than 10,000 meters."
                            savedMessage = false
                        }

                        else -> {
                            errorMessage = null
                            savedMessage = true
                        }
                    }
                },

                containerColor = ShahGreen,
                contentColor = ShahWhite
            ) {

                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save"
                )
            }
        },

        containerColor = ShahGrey

    ) { paddingValues ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ShahGrey)
                .padding(16.dp)
        ) {

            Card(

                modifier = Modifier.fillMaxWidth(),

                shape = RoundedCornerShape(12.dp),

                colors = CardDefaults.cardColors(
                    containerColor = ShahWhite
                ),

                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )

            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    /*
                     * GEO-FENCE ENABLE/DISABLE
                     */

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween

                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = "Enable Geo-Fencing",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text =
                                    "Attendance will be restricted to assigned sites.",
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color = ShahMediumGrey
                            )
                        }

                        Switch(

                            checked = enableGeoFence,

                            onCheckedChange = {
                                enableGeoFence = it
                                errorMessage = null
                                savedMessage = false
                            },

                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ShahGreen,
                                checkedTrackColor =
                                    ShahGreen.copy(alpha = 0.35f)
                            )
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    /*
                     * RADIUS
                     */

                    OutlinedTextField(

                        value = radius,

                        onValueChange = { value ->

                            if (
                                value.isEmpty() ||
                                value.all { it.isDigit() }
                            ) {
                                radius = value
                                errorMessage = null
                                savedMessage = false
                            }
                        },

                        enabled = enableGeoFence,

                        label = {
                            Text("Allowed Radius (meters)")
                        },

                        placeholder = {
                            Text("Example: 200")
                        },

                        supportingText = {
                            Text(
                                text = "Allowed range: 1–10,000 meters"
                            )
                        },

                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),

                        singleLine = true,

                        modifier = Modifier.fillMaxWidth(),

                        shape = RoundedCornerShape(12.dp),

                        isError = errorMessage != null
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    /*
                     * REMOTE ATTENDANCE
                     */

                    Row(

                        modifier = Modifier.fillMaxWidth(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween

                    ) {

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = "Allow Remote Attendance",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text =
                                    "Allow employees to punch from any location (e.g. for marketing).",
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color = ShahMediumGrey
                            )
                        }

                        Switch(

                            checked = allowRemoteAttendance,

                            onCheckedChange = {
                                allowRemoteAttendance = it
                                savedMessage = false
                            },

                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ShahGreen,
                                checkedTrackColor =
                                    ShahGreen.copy(alpha = 0.35f)
                            )
                        )
                    }

                    /*
                     * ERROR MESSAGE
                     */

                    if (!errorMessage.isNullOrBlank()) {

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style =
                                MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    /*
                     * SAVE SUCCESS MESSAGE
                     */

                    if (savedMessage) {

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        Text(
                            text = "Settings saved successfully.",
                            color = ShahGreen,
                            style =
                                MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}