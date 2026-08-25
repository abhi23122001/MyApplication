package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.models.UserProfile
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeManagementScreen(viewModel: AdminViewModel,onBack:()->Unit={}){
    var searchQuery by remember{mutableStateOf("")}
    LaunchedEffect(Unit){viewModel.fetchAdminData()}
    val filteredEmployees=remember(searchQuery,viewModel.allEmployees){if(searchQuery.isBlank())viewModel.allEmployees else viewModel.allEmployees.filter{e->e.name.contains(searchQuery,true)||e.uid.contains(searchQuery,true)||e.department.contains(searchQuery,true)||e.role.contains(searchQuery,true)}}
    val departments=filteredEmployees.map{it.department}.filter{it.isNotBlank()}.distinct().size
    Scaffold(topBar={TopAppBar(title={Column{Text("Employee Management",fontWeight=FontWeight.Bold,color=ShahWhite);Text("Team directory & staff details",fontSize=10.sp,color=ShahWhite.copy(.72f))}},navigationIcon={IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back",tint=ShahWhite)}},colors=TopAppBarDefaults.topAppBarColors(containerColor=ShahDarkGreen))},floatingActionButton={FloatingActionButton(onClick={},containerColor=ShahGreen,contentColor=ShahWhite,shape=RoundedCornerShape(16.dp)){Icon(Icons.Default.Add,"Add Employee")}},containerColor=ShahGrey){padding->
        Column(Modifier.fillMaxSize().padding(padding).background(ShahGrey)){
            Surface(Modifier.fillMaxWidth().padding(16.dp),RoundedCornerShape(18.dp),color=ShahDarkGreen){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Surface(shape=RoundedCornerShape(12.dp),color=ShahWhite.copy(.12f)){Icon(Icons.Default.Groups,null,tint=ShahWhite,modifier=Modifier.padding(10.dp).size(24.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("Team overview",color=ShahWhite,fontWeight=FontWeight.Bold);Text("${filteredEmployees.size} employees • $departments departments",fontSize=10.sp,color=ShahWhite.copy(.72f))}}}
            OutlinedTextField(value=searchQuery,onValueChange={searchQuery=it},modifier=Modifier.fillMaxWidth().padding(horizontal=16.dp),placeholder={Text("Search name, ID, department or role...",fontSize=12.sp)},leadingIcon={Icon(Icons.Default.Search,null)},trailingIcon={if(searchQuery.isNotBlank())IconButton(onClick={searchQuery=""}){Icon(Icons.Default.Clear,"Clear")}},singleLine=true,shape=RoundedCornerShape(14.dp),colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=ShahGreen,unfocusedBorderColor=ShahMediumGrey,focusedContainerColor=ShahWhite,unfocusedContainerColor=ShahWhite))
            Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Text("${filteredEmployees.size} Employee(s)",color=ShahDarkGrey,fontSize=11.sp,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));if(searchQuery.isNotBlank())Text("Filtered results",color=ShahGreen,fontSize=10.sp)}
            if(filteredEmployees.isEmpty())Box(Modifier.fillMaxSize().padding(30.dp),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.PersonSearch,null,tint=ShahMediumGrey,modifier=Modifier.size(42.dp));Spacer(Modifier.height(10.dp));Text("No employees found",fontWeight=FontWeight.Bold);Text(if(searchQuery.isBlank())"Employee records will appear here." else "Try another name, ID, department or role.",fontSize=11.sp,color=ShahMediumGrey)}}
            else LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(horizontal=16.dp,bottom=90.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){items(filteredEmployees,key={it.uid}){EmployeeCard(it)}}
        }
    }
}

@Composable
fun EmployeeCard(employee:UserProfile){Card(Modifier.fillMaxWidth(),RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=ShahWhite),elevation=CardDefaults.cardElevation(1.dp)){Row(Modifier.padding(15.dp),verticalAlignment=Alignment.CenterVertically){Surface(Modifier.size(50.dp).clip(CircleShape),color=ShahGreen.copy(.10f)){Box(contentAlignment=Alignment.Center){Text(employee.name.trim().firstOrNull()?.uppercase()?:"?",fontWeight=FontWeight.Bold,color=ShahGreen,fontSize=20.sp)}};Spacer(Modifier.width(13.dp));Column(Modifier.weight(1f)){Text(employee.name,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium);Spacer(Modifier.height(3.dp));Text("ID: ${employee.uid.take(8)} • ${employee.role}",fontSize=10.sp,color=ShahMediumGrey);Spacer(Modifier.height(4.dp));Surface(shape=RoundedCornerShape(7.dp),color=ShahGreen.copy(.08f)){Text(employee.department,color=ShahGreen,fontWeight=FontWeight.Bold,fontSize=9.sp,modifier=Modifier.padding(horizontal=7.dp,vertical=4.dp))}};IconButton(onClick={}){Icon(Icons.Default.MoreVert,"Employee options",tint=ShahMediumGrey)}}}
