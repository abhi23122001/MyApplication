package com.shahsurveyors.myapplication.ui.survey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Landscape
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
fun SurveyCalculatorScreen(onBack: () -> Unit = {}) {
    var selectedTool by remember { mutableIntStateOf(0) }
    val tools = listOf("WGS84 → UTM", "AREA CALC", "BASE-SHIFT")
    Scaffold(topBar = {
        Column {
            TopAppBar(title = { Column { Text("Survey Grid Engine", fontWeight = FontWeight.Bold, color = ShahWhite); Text("Professional field calculation tools", fontSize = 10.sp, color = ShahWhite.copy(.72f)) } }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen))
            TabRow(selectedTabIndex = selectedTool, containerColor = ShahWhite, contentColor = ShahGreen, indicator = { positions -> if (selectedTool < positions.size) TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(positions[selectedTool]), color = ShahGreen) }) {
                tools.forEachIndexed { index, title -> Tab(selected = selectedTool == index, onClick = { selectedTool = index }, text = { Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold) }) }
            }
        }
    }) { paddingValues ->
        Column(Modifier.padding(paddingValues).fillMaxSize().background(ShahGrey).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(Modifier.fillMaxWidth(), RoundedCornerShape(18.dp), color = ShahGreen.copy(.07f)) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(11.dp), color = ShahGreen.copy(.12f)) { Icon(if (selectedTool == 0) Icons.Default.GpsFixed else if (selectedTool == 1) Icons.Default.Landscape else Icons.Default.Calculate, null, tint = ShahGreen, modifier = Modifier.padding(9.dp).size(23.dp)) }
                    Spacer(Modifier.width(11.dp)); Column { Text(tools[selectedTool], fontWeight = FontWeight.Bold, color = ShahDarkGreen); Text(when(selectedTool){0->"Coordinate transformation";1->"Calculate polygon area & land units";else->"Local grid correction"},fontSize=10.sp,color=ShahMediumGrey) }
                }
            }
            when (selectedTool) { 0 -> UtmConverterTool(); 1 -> AreaCalculatorTool(); 2 -> BaseShiftTool() }
        }
    }
}

@Composable
fun UtmConverterTool() {
    var latitude by remember { mutableStateOf("") }; var longitude by remember { mutableStateOf("") }; var result by remember { mutableStateOf("") }
    SurveyToolCard("Coordinate Transformation") {
        CalcTextField(latitude, { latitude = it }, "Latitude (Decimal)"); CalcTextField(longitude, { longitude = it }, "Longitude (Decimal)")
        Spacer(Modifier.height(8.dp)); Button(onClick = { val lat=latitude.toDoubleOrNull(); val lon=longitude.toDoubleOrNull(); result=when{lat==null||lon==null->"Enter valid latitude and longitude";lat !in -90.0..90.0->"Latitude must be between -90 and 90";lon !in -180.0..180.0->"Longitude must be between -180 and 180";else->try{val u=SurveyGridEngine.wgs84ToUtm44N(lat,lon);String.format(Locale.US,"EASTING (E): %.3f\nNORTHING (N): %.3f",u.first,u.second)}catch(_:Exception){"Unable to transform coordinates"}}},Modifier.fillMaxWidth().height(50.dp),shape=RoundedCornerShape(12.dp),colors=ButtonDefaults.buttonColors(containerColor=ShahGreen)){Text("TRANSFORM TO UTM",fontWeight=FontWeight.Bold)}
        ResultBox(result)
    }
}

@Composable
fun AreaCalculatorTool() {
    var pointsText by remember { mutableStateOf("") }; var result by remember { mutableStateOf("") }
    SurveyToolCard("Polygon Area Calculation") {
        Text("Enter E,N coordinates — one point per line",fontSize=11.sp,color=ShahMediumGrey); Spacer(Modifier.height(8.dp))
        OutlinedTextField(pointsText,{pointsText=it},Modifier.fillMaxWidth().height(155.dp),placeholder={Text("500000.123, 2600000.456\n500100.123, 2600000.456\n500100.123, 2600100.456",color=ShahLightGrey)},minLines=5,shape=RoundedCornerShape(12.dp))
        Spacer(Modifier.height(10.dp)); Button(onClick={try{val lines=pointsText.lines().filter{it.isNotBlank()};if(lines.size<3)result="At least 3 points are required" else{val coords=lines.map{p->val x=p.split(",");if(x.size!=2)throw IllegalArgumentException();Pair(x[0].trim().toDouble(),x[1].trim().toDouble())};val area=SurveyGridEngine.calculateArea(coords);val units=SurveyGridEngine.convertAreaToUnits(area);result=String.format(Locale.US,"Area: %.2f Sq.m\nAcres: %.4f\nHectares: %.4f",area,units["Acres"]?:0.0,units["Hectares"]?:0.0)}}catch(_:Exception){result="Format error — use EASTING, NORTHING"}},Modifier.fillMaxWidth().height(50.dp),shape=RoundedCornerShape(12.dp),colors=ButtonDefaults.buttonColors(containerColor=ShahGreen)){Text("CALCULATE AREA",fontWeight=FontWeight.Bold)}; ResultBox(result)
    }
}

@Composable
fun BaseShiftTool() { SurveyToolCard("2D Base-Shift Local Grid") { Text("Shift local site coordinates based on Base point corrections.",fontSize=13.sp,color=ShahBlack); Spacer(Modifier.height(18.dp)); Surface(Modifier.fillMaxWidth(),RoundedCornerShape(12.dp),color=ShahGrey){Text("Base-shift calculator is currently under field calibration.",fontSize=12.sp,color=ShahMediumGrey,modifier=Modifier.padding(13.dp))} } }

@Composable
private fun SurveyToolCard(title:String,content:@Composable ColumnScope.()->Unit){Card(Modifier.fillMaxWidth(),RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=ShahWhite),elevation=CardDefaults.cardElevation(2.dp)){Column(Modifier.padding(17.dp)){Text(title,color=ShahGreen,fontWeight=FontWeight.Bold,fontSize=16.sp);Spacer(Modifier.height(14.dp));content()}}}

@Composable private fun ResultBox(result:String){if(result.isNotBlank()){Spacer(Modifier.height(12.dp));Surface(Modifier.fillMaxWidth(),RoundedCornerShape(12.dp),color=ShahGreen.copy(.06f)){Text(result,color=ShahDarkGreen,fontWeight=FontWeight.Bold,fontSize=14.sp,modifier=Modifier.padding(13.dp))}}}

@Composable fun CalcTextField(value:String,onValueChange:(String)->Unit,label:String){OutlinedTextField(value,onValueChange,label={Text(label)},modifier=Modifier.fillMaxWidth().padding(vertical=4.dp),singleLine=true,shape=RoundedCornerShape(12.dp),colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=ShahGreen))}
