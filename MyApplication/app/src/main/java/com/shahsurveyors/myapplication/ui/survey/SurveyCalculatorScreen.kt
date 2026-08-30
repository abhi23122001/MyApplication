package com.shahsurveyors.myapplication.ui.survey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.ui.theme.*
import com.shahsurveyors.myapplication.utils.SurveyGridEngine
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyCalculatorScreen(
    onBack: () -> Unit = {}
) {

    var selectedTool by remember {
        mutableIntStateOf(0)
    }

    val tools =
        listOf(
            "WGS84 → UTM",
            "AREA CALC",
            "BASE-SHIFT"
        )

    Scaffold(

        topBar = {

            Column {

                TopAppBar(

                    title = {
                        Text(
                            text = "Survey Grid Engine",
                            fontWeight = FontWeight.Bold,
                            color = ShahWhite
                        )
                    },

                    navigationIcon = {

                        IconButton(
                            onClick = onBack
                        ) {

                            Icon(
                                imageVector =
                                    Icons.AutoMirrored.Filled.ArrowBack,

                                contentDescription =
                                    "Back",

                                tint =
                                    ShahWhite
                            )
                        }
                    },

                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor =
                                ShahDarkGreen
                        )
                )


                TabRow(

                    selectedTabIndex =
                        selectedTool,

                    containerColor =
                        ShahWhite,

                    contentColor =
                        ShahGreen,

                    indicator = { tabPositions ->

                        if (
                            selectedTool <
                            tabPositions.size
                        ) {

                            TabRowDefaults
                                .SecondaryIndicator(

                                    Modifier.tabIndicatorOffset(
                                        tabPositions[
                                            selectedTool
                                        ]
                                    ),

                                    color =
                                        ShahGreen
                                )
                        }
                    }
                ) {

                    tools.forEachIndexed { index, title ->

                        Tab(

                            selected =
                                selectedTool == index,

                            onClick = {
                                selectedTool = index
                            },

                            text = {

                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        )
                    }
                }
            }
        }

    ) { paddingValues ->

        Column(

            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(ShahGrey)
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(16.dp)
        ) {

            when (selectedTool) {

                0 -> {
                    UtmConverterTool()
                }

                1 -> {
                    AreaCalculatorTool()
                }

                2 -> {
                    BaseShiftTool()
                }
            }
        }
    }
}


// ============================================================
// WGS84 → UTM
// ============================================================

@Composable
fun UtmConverterTool() {

    var latitude by remember {
        mutableStateOf("")
    }

    var longitude by remember {
        mutableStateOf("")
    }

    var result by remember {
        mutableStateOf("")
    }


    SurveyToolCard {

        Text(
            text = "WGS84 ↔ UTM Zone 44N",
            color = ShahGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        CalcTextField(
            value = latitude,
            onValueChange = {
                latitude = it
            },
            label = "Latitude (Decimal)"
        )


        CalcTextField(
            value = longitude,
            onValueChange = {
                longitude = it
            },
            label = "Longitude (Decimal)"
        )


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        Button(

            onClick = {

                val lat =
                    latitude.toDoubleOrNull()

                val lon =
                    longitude.toDoubleOrNull()


                result =
                    when {

                        lat == null ||
                                lon == null -> {

                            "Invalid coordinates"
                        }

                        lat !in -90.0..90.0 -> {

                            "Latitude must be between -90 and 90"
                        }

                        lon !in -180.0..180.0 -> {

                            "Longitude must be between -180 and 180"
                        }

                        else -> {

                            try {

                                val utm =
                                    SurveyGridEngine
                                        .wgs84ToUtm44N(
                                            lat,
                                            lon
                                        )

                                String.format(
                                    Locale.US,
                                    "EASTING (E): %.3f\nNORTHING (N): %.3f",
                                    utm.first,
                                    utm.second
                                )

                            } catch (e: Exception) {

                                "Unable to transform coordinates"
                            }
                        }
                    }
            },

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(8.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        ShahGreen,

                    contentColor =
                        ShahWhite
                )
        ) {

            Text(
                text =
                    "TRANSFORM TO UTM",

                fontWeight =
                    FontWeight.Bold
            )
        }


        ResultBox(
            result = result
        )
    }
}


// ============================================================
// AREA CALCULATOR
// ============================================================

@Composable
fun AreaCalculatorTool() {

    var pointsText by remember {
        mutableStateOf("")
    }

    var result by remember {
        mutableStateOf("")
    }


    SurveyToolCard {

        Text(
            text = "Polygon Area Calculation",
            color = ShahGreen,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )


        Text(
            text =
                "Enter E,N coordinates — one point per line",

            color =
                ShahMediumGrey,

            fontSize =
                11.sp
        )


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        OutlinedTextField(

            value =
                pointsText,

            onValueChange = {
                pointsText = it
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp),

            placeholder = {

                Text(
                    text =
                        "500000.123, 2600000.456\n500100.123, 2600000.456\n500100.123, 2600100.456",

                    color =
                        ShahLightGrey
                )
            },

            minLines =
                5,

            shape =
                RoundedCornerShape(12.dp)
        )


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        Button(

            onClick = {

                try {

                    val lines =
                        pointsText
                            .lines()
                            .filter {
                                it.isNotBlank()
                            }


                    if (lines.size < 3) {

                        result =
                            "At least 3 points are required"

                    } else {

                        val coordinates =
                            lines.map { line ->

                                val parts =
                                    line
                                        .split(",")

                                if (
                                    parts.size != 2
                                ) {
                                    throw IllegalArgumentException()
                                }

                                val easting =
                                    parts[0]
                                        .trim()
                                        .toDouble()

                                val northing =
                                    parts[1]
                                        .trim()
                                        .toDouble()

                                Pair(
                                    easting,
                                    northing
                                )
                            }


                        val areaSqM =
                            SurveyGridEngine
                                .calculateArea(
                                    coordinates
                                )


                        val units =
                            SurveyGridEngine
                                .convertAreaToUnits(
                                    areaSqM
                                )


                        val acres =
                            units["Acres"] ?: 0.0

                        val hectares =
                            units["Hectares"] ?: 0.0


                        result =
                            String.format(
                                Locale.US,

                                "Area: %.2f Sq.m\nAcres: %.4f\nHectares: %.4f",

                                areaSqM,
                                acres,
                                hectares
                            )
                    }

                } catch (e: Exception) {

                    result =
                        "Format Error\nUse: EASTING, NORTHING"
                }
            },

            modifier =
                Modifier.fillMaxWidth(),

            shape =
                RoundedCornerShape(8.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        ShahGreen,

                    contentColor =
                        ShahWhite
                )
        ) {

            Text(
                text =
                    "CALCULATE AREA",

                fontWeight =
                    FontWeight.Bold
            )
        }


        ResultBox(
            result = result
        )
    }
}


// ============================================================
// BASE SHIFT
// ============================================================

@Composable
fun BaseShiftTool() {

    SurveyToolCard {

        Text(
            text =
                "2D Base-Shift Local Grid",

            color =
                ShahGreen,

            fontWeight =
                FontWeight.Bold,

            fontSize =
                16.sp
        )


        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        Text(
            text =
                "Shift local site coordinates based on Base point corrections.",

            color =
                ShahBlack,

            fontSize =
                13.sp
        )


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )


        Surface(

            color =
                ShahGrey,

            shape =
                RoundedCornerShape(8.dp),

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    "Base-shift calculator is currently under field calibration.",

                color =
                    ShahMediumGrey,

                fontSize =
                    12.sp,

                modifier =
                    Modifier.padding(12.dp)
            )
        }
    }
}


// ============================================================
// COMMON CARD
// ============================================================

@Composable
private fun SurveyToolCard(
    content: @Composable ColumnScope.() -> Unit
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(12.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    ShahWhite
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(

            modifier =
                Modifier.padding(16.dp),

            content =
                content
        )
    }
}


// ============================================================
// RESULT BOX
// ============================================================

@Composable
private fun ResultBox(
    result: String
) {

    if (result.isNotBlank()) {

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )


        Surface(

            color =
                ShahGrey,

            shape =
                RoundedCornerShape(8.dp),

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text =
                    result,

                color =
                    ShahBlack,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    15.sp,

                modifier =
                    Modifier.padding(12.dp)
            )
        }
    }
}


// ============================================================
// COMMON TEXT FIELD
// ============================================================

@Composable
fun CalcTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {

    OutlinedTextField(

        value =
            value,

        onValueChange =
            onValueChange,

        label = {
            Text(label)
        },

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),

        singleLine =
            true,

        shape =
            RoundedCornerShape(12.dp)
    )
}