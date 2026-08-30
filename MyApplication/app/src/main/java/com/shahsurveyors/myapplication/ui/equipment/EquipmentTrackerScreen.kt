package com.shahsurveyors.myapplication.ui.equipment

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Equipment(
    val id: String = "",
    val name: String,
    val serial: String,
    val status: String,
    val assignedTo: String = "",
    val site: String = "",
    val checklist: List<String> = listOf("Tripod", "Prism", "Battery x2", "Charger", "Tribrach")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentTrackerScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }

    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val equipmentList = remember { mutableStateListOf<Equipment>() }
    var showAddDialog by remember { mutableStateOf(false) }
    var expandedId by remember { mutableStateOf<String?>(null) }

    fun loadEquipment() {
        coroutineScope.launch {
            isLoading = true
            try {
                val snapshot = firestore.collection("equipment").get().await()
                equipmentList.clear()
                if (snapshot.isEmpty) {
                    equipmentList.add(
                        Equipment(
                            id = "1",
                            name = "Leica TS07 Total Station",
                            serial = "SN-982314",
                            status = "IN_FIELD",
                            assignedTo = "Rahul Kumar",
                            site = "Singrauli Block-A"
                        )
                    )
                    equipmentList.add(
                        Equipment(
                            id = "2",
                            name = "Leica GS18 T GNSS RTK",
                            serial = "SN-110294",
                            status = "AVAILABLE",
                            assignedTo = "Store / Main Office",
                            site = "HQ Equipment Lab"
                        )
                    )
                    equipmentList.add(
                        Equipment(
                            id = "3",
                            name = "Auto Level Leica NA730 Plus",
                            serial = "SN-441209",
                            status = "MAINTENANCE",
                            assignedTo = "Calibration Service",
                            site = "Service Center"
                        )
                    )
                } else {
                    for (doc in snapshot.documents) {
                        equipmentList.add(
                            Equipment(
                                id = doc.id,
                                name = doc.getString("name") ?: "Instrument",
                                serial = doc.getString("serial") ?: "",
                                status = doc.getString("status") ?: "AVAILABLE",
                                assignedTo = doc.getString("assignedTo") ?: "",
                                site = doc.getString("site") ?: ""
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadEquipment()
    }

    val filteredEquipment = remember(equipmentList, searchQuery) {
        if (searchQuery.isBlank()) {
            equipmentList.toList()
        } else {
            equipmentList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.serial.contains(searchQuery, ignoreCase = true) ||
                        it.site.contains(searchQuery, ignoreCase = true) ||
                        it.assignedTo.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Survey Equipment Tracker",
                        fontWeight = FontWeight.Bold,
                        color = ShahWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                onClick = { showAddDialog = true },
                containerColor = ShahGreen,
                contentColor = ShahWhite
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Equipment")
            }
        },
        containerColor = ShahGrey
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ShahGrey)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by instrument, serial, surveyor, site...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ShahGreen,
                    unfocusedBorderColor = ShahMediumGrey,
                    focusedContainerColor = ShahWhite,
                    unfocusedContainerColor = ShahWhite
                )
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ShahGreen)
                }
            } else if (filteredEquipment.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No instruments found", color = ShahMediumGrey)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEquipment, key = { it.id }) { item ->
                        val isExpanded = expandedId == item.id
                        val statusColor = when (item.status) {
                            "AVAILABLE" -> SuccessGreen
                            "IN_FIELD" -> ShahGreen
                            else -> WarningAmber
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedId = if (isExpanded) null else item.id },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ShahWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = ShahBlack
                                        )
                                        Text(
                                            text = "Serial: ${item.serial}",
                                            fontSize = 12.sp,
                                            color = ShahMediumGrey
                                        )
                                    }

                                    Surface(
                                        color = statusColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = item.status.replace("_", " "),
                                            color = statusColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Assigned To:", fontSize = 11.sp, color = ShahMediumGrey)
                                        Text(item.assignedTo.ifBlank { "Unassigned" }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ShahBlack)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Current Location / Site:", fontSize = 11.sp, color = ShahMediumGrey)
                                        Text(item.site.ifBlank { "HQ Store" }, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ShahDarkGreen)
                                    }
                                }

                                if (isExpanded) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        color = ShahLightGrey
                                    )
                                    Text("Standard Accessories Checklist:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ShahDarkGrey)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    item.checklist.forEach { accessory ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(accessory, fontSize = 12.sp, color = ShahDarkGrey)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var serial by remember { mutableStateOf("") }
        var status by remember { mutableStateOf("AVAILABLE") }
        var assignedTo by remember { mutableStateOf("") }
        var site by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = ShahWhite,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Register New Equipment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ShahBlack)

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Instrument Name *") }, placeholder = { Text("e.g. Leica TS16") }, singleLine = true)
                    OutlinedTextField(value = serial, onValueChange = { serial = it }, label = { Text("Serial Number *") }, singleLine = true)
                    OutlinedTextField(value = assignedTo, onValueChange = { assignedTo = it }, label = { Text("Assigned Surveyor / Store") }, singleLine = true)
                    OutlinedTextField(value = site, onValueChange = { site = it }, label = { Text("Site / Location") }, singleLine = true)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("AVAILABLE", "IN_FIELD", "MAINTENANCE").forEach { st ->
                            FilterChip(
                                selected = status == st,
                                onClick = { status = st },
                                label = { Text(st, fontSize = 10.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank() && serial.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        val newDoc = firestore.collection("equipment").document()
                                        val data = hashMapOf(
                                            "name" to name.trim(),
                                            "serial" to serial.trim(),
                                            "status" to status,
                                            "assignedTo" to assignedTo.trim(),
                                            "site" to site.trim(),
                                            "createdAt" to System.currentTimeMillis()
                                        )
                                        newDoc.set(data).await()
                                        Toast.makeText(context, "Equipment registered", Toast.LENGTH_SHORT).show()
                                        showAddDialog = false
                                        loadEquipment()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ShahGreen, contentColor = ShahWhite),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SAVE INSTRUMENT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}