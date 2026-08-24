package com.shahsurveyors.myapplication.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shahsurveyors.myapplication.data.local.TermConditionEntity
import com.shahsurveyors.myapplication.ui.theme.ErrorRed
import com.shahsurveyors.myapplication.ui.theme.ShahBlack
import com.shahsurveyors.myapplication.ui.theme.ShahDarkGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGreen
import com.shahsurveyors.myapplication.ui.theme.ShahGrey
import com.shahsurveyors.myapplication.ui.theme.ShahMediumGrey
import com.shahsurveyors.myapplication.ui.theme.ShahWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val terms by viewModel.allTerms.collectAsState()

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var selectedTerm by remember {
        mutableStateOf<TermConditionEntity?>(null)
    }

    var newTermContent by remember {
        mutableStateOf("")
    }

    var inputError by remember {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {

            TopAppBar(

                title = {
                    Text(
                        text = "Terms & Conditions",
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
                    newTermContent = ""
                    inputError = false
                    showAddDialog = true
                },

                containerColor = ShahGreen,
                contentColor = ShahWhite
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Term"
                )
            }
        },

        containerColor = ShahGrey

    ) { paddingValues ->

        if (terms.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(ShahGrey),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "No terms added yet",
                        color = ShahMediumGrey,
                        fontSize = 15.sp
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = "Tap + to add a term or condition.",
                        color = ShahMediumGrey,
                        fontSize = 12.sp
                    )
                }
            }

        } else {

            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(ShahGrey),

                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 90.dp
                )
            ) {

                items(
                    items = terms,
                    key = { it.id }
                ) { term ->

                    Card(

                        modifier = Modifier
                            .padding(vertical = 5.dp)
                            .fillMaxWidth(),

                        shape = RoundedCornerShape(12.dp),

                        colors = CardDefaults.cardColors(
                            containerColor = ShahWhite
                        ),

                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        )
                    ) {

                        Row(

                            modifier = Modifier.padding(16.dp),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = term.content,
                                    color = ShahBlack,
                                    fontSize = 14.sp
                                )

                                if (term.isDefault) {

                                    Spacer(
                                        modifier =
                                            Modifier.height(4.dp)
                                    )

                                    Text(
                                        text = "DEFAULT",
                                        color = ShahGreen,
                                        fontSize = 9.sp,
                                        fontWeight =
                                            FontWeight.Bold
                                    )
                                }
                            }

                            IconButton(

                                onClick = {

                                    selectedTerm = term
                                    showDeleteDialog = true
                                }
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Delete,

                                    contentDescription =
                                        "Delete term",

                                    tint = ErrorRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * ADD TERM DIALOG
     */

    if (showAddDialog) {

        AlertDialog(

            onDismissRequest = {
                showAddDialog = false
                inputError = false
            },

            title = {

                Text(
                    text = "Add Term / Condition",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Column {

                    OutlinedTextField(

                        value = newTermContent,

                        onValueChange = {
                            newTermContent = it
                            inputError = false
                        },

                        label = {
                            Text("Condition Text")
                        },

                        placeholder = {
                            Text(
                                "Enter terms and conditions..."
                            )
                        },

                        modifier = Modifier.fillMaxWidth(),

                        minLines = 4,

                        maxLines = 8,

                        shape = RoundedCornerShape(12.dp),

                        isError = inputError
                    )

                    if (inputError) {

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text =
                                "Please enter a term or condition.",

                            color =
                                MaterialTheme.colorScheme.error,

                            fontSize = 12.sp
                        )
                    }
                }
            },

            confirmButton = {

                Button(

                    onClick = {

                        val content =
                            newTermContent.trim()

                        if (content.isEmpty()) {

                            inputError = true

                        } else {

                            viewModel.saveTerm(
                                TermConditionEntity(
                                    content = content,
                                    isDefault = false,
                                    orderIndex = terms.size
                                )
                            )

                            newTermContent = ""
                            inputError = false
                            showAddDialog = false
                        }
                    },

                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShahGreen,
                        contentColor = ShahWhite
                    )
                ) {

                    Text(
                        text = "ADD",
                        fontWeight = FontWeight.Bold
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {
                        showAddDialog = false
                        inputError = false
                    }
                ) {

                    Text(
                        text = "CANCEL",
                        color = ShahGreen
                    )
                }
            }
        )
    }

    /*
     * DELETE CONFIRMATION
     */

    if (showDeleteDialog) {

        AlertDialog(

            onDismissRequest = {
                showDeleteDialog = false
                selectedTerm = null
            },

            title = {

                Text(
                    text = "Delete Term?",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {

                Text(
                    text =
                        "Are you sure you want to delete this term or condition?"
                )
            },

            confirmButton = {

                Button(

                    onClick = {

                        selectedTerm?.let {
                            viewModel.deleteTerm(it)
                        }

                        selectedTerm = null
                        showDeleteDialog = false
                    },

                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = ShahWhite
                    )
                ) {

                    Text(
                        text = "DELETE",
                        fontWeight = FontWeight.Bold
                    )
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        selectedTerm = null
                        showDeleteDialog = false
                    }
                ) {

                    Text(
                        text = "CANCEL",
                        color = ShahGreen
                    )
                }
            }
        )
    }
}