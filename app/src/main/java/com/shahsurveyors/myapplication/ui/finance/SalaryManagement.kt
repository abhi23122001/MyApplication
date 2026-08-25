package com.shahsurveyors.myapplication.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryManagementScreen(viewModel: SalaryViewModel,onBack:()->Unit,onGenerateSalarySlip:(SalaryData)->Unit={}) {
    var searchQuery by remember{mutableStateOf("")}
    LaunchedEffect(Unit){viewModel.fetchSalaries()}
    val filtered=remember(viewModel.salaryRecords,searchQuery){if(searchQuery.isBlank())viewModel.salaryRecords else viewModel.salaryRecords.filter{it.name.contains(searchQuery,true)||it.id.contains(searchQuery,true)||it.dept.contains(searchQuery,true)}}
    val totalNet=filtered.sumOf{it.netSalary}
    Scaffold(topBar={TopAppBar(title={Column{Text("Salary & Payroll",fontWeight=FontWeight.Bold,color=ShahWhite);Text("Monthly salary & employee payroll",fontSize=10.sp,color=ShahWhite.copy(.72f))}},navigationIcon={IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back",tint=ShahWhite)}},colors=TopAppBarDefaults.topAppBarColors(containerColor=ShahDarkGreen))}){padding->
        Column(Modifier.padding(padding).fillMaxSize().background(ShahGrey)){
            Surface(Modifier.fillMaxWidth().padding(16.dp),RoundedCornerShape(18.dp),color=ShahDarkGreen){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Surface(shape=RoundedCornerShape(12.dp),color=ShahWhite.copy(.12f)){Icon(Icons.Default.Payments,null,tint=ShahWhite,modifier=Modifier.padding(10.dp).size(24.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("Payroll overview",color=ShahWhite,fontWeight=FontWeight.Bold);Text("${filtered.size} employee records",fontSize=10.sp,color=ShahWhite.copy(.72f))};Column(horizontalAlignment=Alignment.End){Text(formatIndianCurrency(totalNet),color=ShahWhite,fontWeight=FontWeight.ExtraBold,fontSize=17.sp);Text("Net payroll",fontSize=9.sp,color=ShahWhite.copy(.72f))}}}
            OutlinedTextField(value=searchQuery,onValueChange={searchQuery=it},modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp),placeholder={Text("Search employee, ID or department...",fontSize=12.sp)},leadingIcon={Icon(Icons.Default.Search,null)},trailingIcon={if(searchQuery.isNotBlank())IconButton(onClick={searchQuery=""}){Icon(Icons.Default.Clear,"Clear")}},singleLine=true,shape=RoundedCornerShape(14.dp),colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=ShahGreen,focusedContainerColor=ShahWhite,unfocusedContainerColor=ShahWhite))
            Spacer(Modifier.height(4.dp))
            if(viewModel.isLoading&&filtered.isEmpty())Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){CircularProgressIndicator(color=ShahGreen)}
            else if(filtered.isEmpty())Box(Modifier.fillMaxSize().padding(30.dp),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.Payments,null,tint=ShahMediumGrey,modifier=Modifier.size(42.dp));Spacer(Modifier.height(10.dp));Text("No salary records found",fontWeight=FontWeight.Bold);Text(if(searchQuery.isBlank())"Salary records will appear here." else "Try a different employee name or ID.",fontSize=11.sp,color=ShahMediumGrey)}}
            else LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){items(filtered,key={it.id}){salary->SalaryCard(salary){onGenerateSalarySlip(salary)}}}
        }
    }
}

@Composable
fun SalaryCard(data:SalaryData,onGenerateSalarySlip:()->Unit){Card(Modifier.fillMaxWidth(),RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=ShahWhite),elevation=CardDefaults.cardElevation(1.dp)){Column(Modifier.padding(15.dp)){Row(verticalAlignment=Alignment.CenterVertically){Surface(shape=RoundedCornerShape(12.dp),color=ShahGreen.copy(.10f)){Icon(Icons.Default.Person,null,tint=ShahGreen,modifier=Modifier.padding(9.dp).size(22.dp))};Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text(data.name,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold,color=ShahBlack);Text("${data.dept}  •  ID ${data.id}",fontSize=10.sp,color=ShahMediumGrey)};Column(horizontalAlignment=Alignment.End){Text(formatIndianCurrency(data.netSalary),fontWeight=FontWeight.ExtraBold,color=ShahGreen,fontSize=16.sp);Text("NET SALARY",fontSize=8.sp,color=ShahMediumGrey)}};HorizontalDivider(Modifier.padding(vertical=12.dp),color=ShahLightGrey);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){InfoItem("Attendance","${data.presentDays} Days");InfoItem("Advances",formatIndianCurrency(data.advances));InfoItem("Deductions",formatIndianCurrency(data.deductions))};Spacer(Modifier.height(13.dp));Button(onClick=onGenerateSalarySlip,Modifier.fillMaxWidth().height(48.dp),shape=RoundedCornerShape(12.dp),colors=ButtonDefaults.buttonColors(containerColor=ShahGreen)){Icon(Icons.Default.PictureAsPdf,null,Modifier.size(18.dp));Spacer(Modifier.width(7.dp));Text("GENERATE SALARY SLIP",fontWeight=FontWeight.Bold,fontSize=11.sp)}}}}

@Composable
fun InfoItem(label:String,value:String){Column{Text(label,fontSize=9.sp,color=ShahMediumGrey);Text(value,fontSize=11.sp,fontWeight=FontWeight.Bold,color=ShahBlack)}}

data class SalaryData(val id:String="",val name:String="",val dept:String="",val presentDays:Int=0,val advances:Double=0.0,val deductions:Double=0.0,val basicSalary:Double=0.0,val netSalary:Double=0.0,val month:String="",val year:Int=0,val status:String="PENDING")
private fun formatIndianCurrency(amount:Double)="₹ "+String.format(java.util.Locale.ENGLISH,"%,.0f",amount)
