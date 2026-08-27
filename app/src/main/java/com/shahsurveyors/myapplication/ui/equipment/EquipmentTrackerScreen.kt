package com.shahsurveyors.myapplication.ui.equipment

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.models.EquipmentModel
import com.shahsurveyors.myapplication.models.ProjectModel
import com.shahsurveyors.myapplication.models.UserProfile
import com.shahsurveyors.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentTrackerScreen(viewModel: EquipmentViewModel, onBack: () -> Unit = {}) {
    var showAddDialog by remember { mutableStateOf(false) }
    var handoverItem by remember { mutableStateOf<EquipmentModel?>(null) }
    LaunchedEffect(Unit) { viewModel.fetchEquipment() }
    val total = viewModel.equipmentList.size
    val available = viewModel.equipmentList.count { it.status == "AVAILABLE" }
    val inUse = viewModel.equipmentList.count { it.status == "IN_USE" }

    Scaffold(
        topBar = { TopAppBar(title = { Column { Text("Leica Fleet Custody", fontWeight = FontWeight.Bold, color = ShahWhite); Text("Equipment & handover tracking", fontSize = 10.sp, color = ShahWhite.copy(.72f)) } }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ShahWhite) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = ShahDarkGreen)) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = ShahGreen, contentColor = ShahWhite, shape = RoundedCornerShape(16.dp)) { Icon(Icons.Default.Add, "Add Equipment") } }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().background(ShahGrey)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { EquipmentSummary("Total", total.toString(), Icons.Default.Inventory2, ShahGreen, Modifier.weight(1f)); EquipmentSummary("Available", available.toString(), Icons.Default.CheckCircle, SuccessGreen, Modifier.weight(1f)); EquipmentSummary("In Use", inUse.toString(), Icons.Default.PersonPin, WarningAmber, Modifier.weight(1f)) }
            if (viewModel.isLoading && viewModel.equipmentList.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = ShahGreen) }
            else if (viewModel.equipmentList.isEmpty()) Box(Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Inventory2, null, tint = ShahMediumGrey, modifier = Modifier.size(42.dp)); Spacer(Modifier.height(10.dp)); Text("No equipment found", fontWeight = FontWeight.Bold); Text("Tap + to add your first survey instrument.", fontSize = 11.sp, color = ShahMediumGrey) } }
            else LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { items(viewModel.equipmentList, key = { it.id }) { EquipmentCard(it, onHandover = { handoverItem = it }, onReturn = { viewModel.returnEquipment(it.id) }) } }
        }
    }
    if (showAddDialog) AddEquipmentDialog(viewModel, { showAddDialog = false })
    handoverItem?.let { item -> HandoverDialog(item, viewModel, { handoverItem = null }) }
}

@Composable
private fun AddEquipmentDialog(viewModel: EquipmentViewModel, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }; var model by remember { mutableStateOf("") }; var serial by remember { mutableStateOf("") }; var category by remember { mutableStateOf("Survey Instrument") }
    val canSave = name.isNotBlank() && serial.isNotBlank() && !viewModel.isLoading
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add Equipment", fontWeight = FontWeight.Bold) }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(name,{name=it},label={Text("Equipment name *")},singleLine=true,modifier=Modifier.fillMaxWidth()); OutlinedTextField(model,{model=it},label={Text("Model number")},singleLine=true,modifier=Modifier.fillMaxWidth()); OutlinedTextField(serial,{serial=it},label={Text("Serial number *")},singleLine=true,modifier=Modifier.fillMaxWidth()); OutlinedTextField(category,{category=it},label={Text("Category")},singleLine=true,modifier=Modifier.fillMaxWidth()); viewModel.errorMessage?.takeIf{it.isNotBlank()}?.let{Text(it,color=ErrorRed,fontSize=11.sp)} } }, confirmButton = { Button(enabled=canSave,onClick={viewModel.addEquipment(name,model,serial,category,onDismiss)},colors=ButtonDefaults.buttonColors(containerColor=ShahGreen)){if(viewModel.isLoading)CircularProgressIndicator(Modifier.size(18.dp),color=ShahWhite,strokeWidth=2.dp)else Text("SAVE")} }, dismissButton={TextButton(onClick=onDismiss,enabled=!viewModel.isLoading){Text("CANCEL")}})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandoverDialog(item: EquipmentModel, viewModel: EquipmentViewModel, onDismiss: () -> Unit) {
    var selectedEmployee by remember { mutableStateOf<UserProfile?>(null) }
    var selectedProject by remember { mutableStateOf<ProjectModel?>(null) }
    var location by remember { mutableStateOf("") }
    var otherAccessories by remember { mutableStateOf("") }
    var employeeExpanded by remember { mutableStateOf(false) }
    var projectExpanded by remember { mutableStateOf(false) }
    val checkedAccessories = remember { mutableStateMapOf("Tripod" to false, "Prism" to false, "Battery x2" to false, "Charger" to false, "Tribrach" to false) }
    LaunchedEffect(Unit) { viewModel.clearError(); viewModel.loadHandoverOptions() }
    val accessories = checkedAccessories.filterValues { it }.keys.toMutableList().apply { addAll(otherAccessories.split(',').map { it.trim() }.filter { it.isNotBlank() }) }.distinct()
    AlertDialog(onDismissRequest = { if (!viewModel.isLoading) onDismiss() }, title = { Column { Text("Confirm Equipment Handover", fontWeight = FontWeight.Bold); Text(item.name, fontSize = 11.sp, color = ShahMediumGrey) } }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(expanded=employeeExpanded,onExpandedChange={employeeExpanded=!employeeExpanded}) { OutlinedTextField(value=selectedEmployee?.name.orEmpty(),onValueChange={},readOnly=true,label={Text("Handover to *")},placeholder={Text("Select employee")},trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(employeeExpanded)},modifier=Modifier.menuAnchor().fillMaxWidth()); ExposedDropdownMenu(expanded=employeeExpanded,onDismissRequest={employeeExpanded=false}) { if(viewModel.employees.isEmpty()) DropdownMenuItem(text={Text("No active employees found")},onClick={}) else viewModel.employees.forEach{user->DropdownMenuItem(text={Column{Text(user.name);Text(user.email,fontSize=9.sp,color=ShahMediumGrey)}},onClick={selectedEmployee={user};employeeExpanded=false})} } }
            ExposedDropdownMenuBox(expanded=projectExpanded,onExpandedChange={projectExpanded=!projectExpanded}) { OutlinedTextField(value=selectedProject?.name.orEmpty(),onValueChange={},readOnly=true,label={Text("Project *")},placeholder={Text("Select project")},trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(projectExpanded)},modifier=Modifier.menuAnchor().fillMaxWidth()); ExposedDropdownMenu(expanded=projectExpanded,onDismissRequest={projectExpanded=false}) { if(viewModel.projects.isEmpty()) DropdownMenuItem(text={Text("No active projects found")},onClick={}) else viewModel.projects.forEach{project->DropdownMenuItem(text={Column{Text(project.name);Text(project.siteLocation,fontSize=9.sp,color=ShahMediumGrey)}},onClick={selectedProject={project};projectExpanded=false})} } }
            OutlinedTextField(location,{location=it},label={Text("Handover location *")},placeholder={Text("Site / office / store location")},singleLine=true,modifier=Modifier.fillMaxWidth())
            Text("Accessories handed over",fontSize=11.sp,fontWeight=FontWeight.Bold,color=ShahGreen)
            checkedAccessories.keys.forEach{label->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=checkedAccessories[label]==true,onCheckedChange={checkedAccessories[label]=it},colors=CheckboxDefaults.colors(checkedColor=ShahGreen));Text(label,fontSize=11.sp)}}
            OutlinedTextField(otherAccessories,{otherAccessories=it},label={Text("Other accessories (comma separated)")},placeholder={Text("e.g. USB cable, carrying case")},minLines=2,modifier=Modifier.fillMaxWidth())
            viewModel.errorMessage?.takeIf{it.isNotBlank()}?.let{Text(it,color=ErrorRed,fontSize=11.sp)}
        }
    }, confirmButton={Button(enabled=selectedEmployee!=null&&selectedProject!=null&&location.isNotBlank()&&!viewModel.isLoading,onClick={viewModel.handoverEquipment(item.id,selectedEmployee!!,selectedProject!!,location,accessories,onDismiss)},colors=ButtonDefaults.buttonColors(containerColor=ShahGreen)){if(viewModel.isLoading)CircularProgressIndicator(Modifier.size(18.dp),color=ShahWhite,strokeWidth=2.dp)else{Icon(Icons.Default.Verified,null);Spacer(Modifier.width(5.dp));Text("CONFIRM HANDOVER")}}},dismissButton={TextButton(onClick=onDismiss,enabled=!viewModel.isLoading){Text("CANCEL")}})
}

@Composable
private fun EquipmentSummary(title:String,value:String,icon:androidx.compose.ui.graphics.vector.ImageVector,tint:Color,modifier:Modifier){Card(modifier,RoundedCornerShape(14.dp),colors=CardDefaults.cardColors(containerColor=ShahWhite),elevation=CardDefaults.cardElevation(1.dp)){Column(Modifier.padding(10.dp)){Icon(icon,null,tint=tint,modifier=Modifier.size(19.dp));Spacer(Modifier.height(5.dp));Text(value,fontWeight=FontWeight.Bold,fontSize=17.sp);Text(title,fontSize=9.sp,color=ShahMediumGrey)}}}

@Composable
fun EquipmentCard(item: EquipmentModel, onHandover: () -> Unit = {}, onReturn: () -> Unit = {}) {
    var expanded by remember { mutableStateOf(false) }
    val statusColor=when(item.status){"AVAILABLE"->SuccessGreen;"IN_USE"->WarningAmber;"MAINTENANCE"->ErrorRed;else->ShahMediumGrey}
    Card(Modifier.fillMaxWidth(),RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=ShahWhite),elevation=CardDefaults.cardElevation(1.dp)){Column(Modifier.padding(15.dp)){Row(verticalAlignment=Alignment.CenterVertically){Surface(shape=RoundedCornerShape(12.dp),color=ShahGreen.copy(.10f)){Icon(Icons.Default.PrecisionManufacturing,null,tint=ShahGreen,modifier=Modifier.padding(10.dp).size(25.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(item.name,color=ShahBlack,fontWeight=FontWeight.Bold,fontSize=14.sp);Text("S/N: ${item.serialNumber}",color=ShahMediumGrey,fontSize=10.sp);Spacer(Modifier.height(4.dp));Surface(shape=RoundedCornerShape(8.dp),color=statusColor.copy(.10f)){Text(item.status,color=statusColor,fontSize=9.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(horizontal=7.dp,vertical=4.dp))};item.assignedToName?.takeIf{it.isNotBlank()}?.let{Text("Assigned: $it",color=ShahDarkGrey,fontSize=10.sp,modifier=Modifier.padding(top=4.dp))};item.assignedProjectName?.takeIf{it.isNotBlank()}?.let{Text("Project: $it",color=ShahDarkGrey,fontSize=10.sp)}};IconButton(onClick={expanded=!expanded}){Icon(if(expanded)Icons.Default.ExpandLess else Icons.Default.ExpandMore,if(expanded)"Collapse" else "Expand",tint=ShahMediumGrey)}}
        if(expanded){Spacer(Modifier.height(12.dp));HorizontalDivider(color=ShahLightGrey);Spacer(Modifier.height(10.dp));item.handoverLocation?.takeIf{it.isNotBlank()}?.let{Text("Handover location: $it",fontSize=10.sp,color=ShahDarkGrey)};item.handoverAt?.let{Text("Handover time: ${it.toDate()}",fontSize=10.sp,color=ShahDarkGrey)};if(item.handoverAccessories.isNotEmpty())Text("Accessories: ${item.handoverAccessories.joinToString(", ")}",fontSize=10.sp,color=ShahDarkGrey,modifier=Modifier.padding(top=4.dp));Spacer(Modifier.height(8.dp));if(item.status=="AVAILABLE")Button(onClick=onHandover,modifier=Modifier.fillMaxWidth().height(46.dp),shape=RoundedCornerShape(12.dp),colors=ButtonDefaults.buttonColors(containerColor=ShahGreen)){Icon(Icons.Default.Handshake,null);Spacer(Modifier.width(7.dp));Text("HANDOVER",fontWeight=FontWeight.Bold)}else if(item.status=="IN_USE")OutlinedButton(onClick=onReturn,modifier=Modifier.fillMaxWidth().height(46.dp),shape=RoundedCornerShape(12.dp)){Icon(Icons.Default.KeyboardReturn,null);Spacer(Modifier.width(7.dp));Text("RETURN EQUIPMENT",fontWeight=FontWeight.Bold)}}
    }}
}
