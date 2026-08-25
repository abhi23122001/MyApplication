package com.shahsurveyors.myapplication.ui.ops

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.models.DSRModel
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyStatusReportScreen(viewModel: DSRViewModel,onBack:()->Unit={}){
    var chainage by remember{mutableStateOf("")};var points by remember{mutableStateOf("")};var area by remember{mutableStateOf("")};var instrument by remember{mutableStateOf("Leica TS04")};var remarks by remember{mutableStateOf("")};var selectedFileUri by remember{mutableStateOf<Uri?>(null)};var showValidationError by remember{mutableStateOf(false)}
    val filePicker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){uri->selectedFileUri=uri}
    Scaffold(topBar={TopAppBar(title={Column{Text("Daily Status Report",fontWeight=FontWeight.Bold,color=ShahWhite);Text("Production • field progress • survey data",fontSize=10.sp,color=ShahWhite.copy(.72f))}},navigationIcon={IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back",tint=ShahWhite)}},colors=TopAppBarDefaults.topAppBarColors(containerColor=ShahDarkGreen))}){padding->Column(Modifier.padding(padding).fillMaxSize().background(ShahGrey).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        Surface(Modifier.fillMaxWidth(),RoundedCornerShape(18.dp),color=ShahDarkGreen){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Surface(shape=RoundedCornerShape(12.dp),color=ShahWhite.copy(.12f)){Icon(Icons.Default.AssignmentTurnedIn,null,tint=ShahWhite,modifier=Modifier.padding(10.dp).size(24.dp))};Spacer(Modifier.width(12.dp));Column{Text("Today's Production",color=ShahWhite,fontWeight=FontWeight.Bold,fontSize=16.sp);Text("Record verified field output for the project",fontSize=10.sp,color=ShahWhite.copy(.72f))}}}
        Card(Modifier.fillMaxWidth(),RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=ShahWhite),elevation=CardDefaults.cardElevation(2.dp)){Column(Modifier.padding(17.dp)){Text("Field Logger Details",color=ShahGreen,fontWeight=FontWeight.Bold,fontSize=16.sp);Text("Enter today's measured production",fontSize=10.sp,color=ShahMediumGrey);Spacer(Modifier.height(12.dp));DSRTextField(chainage,{chainage=it},"Total Chainage / Distance (m)");DSRTextField(points,{points=it},"Total Points Collected");DSRTextField(area,{area=it},"Total Area Covered (Acres)" );Spacer(Modifier.height(14.dp));Text("Instrument Used",fontSize=11.sp,color=ShahMediumGrey,fontWeight=FontWeight.Bold);Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth()){RadioButton(instrument=="Leica TS04",{instrument="Leica TS04"},colors=RadioButtonDefaults.colors(selectedColor=ShahGreen));Text("Leica TS04",fontSize=12.sp);Spacer(Modifier.width(7.dp));RadioButton(instrument=="Leica GS16",{instrument="Leica GS16"},colors=RadioButtonDefaults.colors(selectedColor=ShahGreen));Text("Leica GS16 DGPS",fontSize=12.sp)};Spacer(Modifier.height(12.dp));OutlinedTextField(remarks,{remarks=it},label={Text("General Site Remarks")},modifier=Modifier.fillMaxWidth(),minLines=3,maxLines=6,shape=RoundedCornerShape(12.dp));Spacer(Modifier.height(16.dp));Text("Supporting Data",fontSize=11.sp,color=ShahMediumGrey,fontWeight=FontWeight.Bold);Spacer(Modifier.height(7.dp));OutlinedButton(onClick={filePicker.launch("*/*")},modifier=Modifier.fillMaxWidth().height(48.dp),shape=RoundedCornerShape(12.dp),colors=ButtonDefaults.outlinedButtonColors(contentColor=ShahGreen)){Icon(if(selectedFileUri==null)Icons.Default.UploadFile else Icons.Default.AttachFile,null);Spacer(Modifier.width(7.dp));Text(if(selectedFileUri==null)"UPLOAD CAD / KML DATA" else "FILE SELECTED",fontWeight=FontWeight.Bold,fontSize=11.sp)};selectedFileUri?.let{Spacer(Modifier.height(7.dp));Surface(Modifier.fillMaxWidth(),RoundedCornerShape(10.dp),color=ShahGreen.copy(.06f)){Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.CheckCircle,null,tint=SuccessGreen);Spacer(Modifier.width(8.dp));Text("Survey file attached and ready",fontSize=11.sp,color=ShahDarkGreen,fontWeight=FontWeight.Bold)}}};Spacer(Modifier.height(15.dp));Button(onClick={val valid=chainage.isNotBlank()&&points.isNotBlank()&&area.isNotBlank()&&remarks.isNotBlank();showValidationError=!valid;if(valid)viewModel.submitDSR(DSRModel(workDone="Chainage: $chainage, Points: $points, Area: $area",remarks=remarks,equipmentUsed=instrument,date=com.google.firebase.Timestamp.now()))},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=ShahGreen)){Icon(Icons.Default.Send,null);Spacer(Modifier.width(7.dp));Text("SUBMIT PRODUCTION DSR",fontWeight=FontWeight.ExtraBold)};if(showValidationError){Spacer(Modifier.height(7.dp));Surface(Modifier.fillMaxWidth(),RoundedCornerShape(10.dp),color=MaterialTheme.colorScheme.errorContainer){Text("Please fill all required DSR fields.",color=MaterialTheme.colorScheme.onErrorContainer,fontSize=11.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(10.dp))}}}}
        Text("DSR records are stored in Firebase and can be linked with the assigned project/site.",color=ShahMediumGrey,fontSize=10.sp,modifier=Modifier.padding(horizontal=3.dp))
    }}
}

@Composable fun DSRTextField(value:String,onValueChange:(String)->Unit,label:String){OutlinedTextField(value,onValueChange,label={Text(label)},modifier=Modifier.fillMaxWidth().padding(vertical=4.dp),singleLine=true,shape=RoundedCornerShape(12.dp),colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=ShahGreen))}
