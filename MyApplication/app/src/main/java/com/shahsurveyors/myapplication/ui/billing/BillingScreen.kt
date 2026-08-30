package com.shahsurveyors.myapplication.ui.billing

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.shahsurveyors.myapplication.data.local.BankDetails
import com.shahsurveyors.myapplication.data.local.BillingDocumentEntity
import com.shahsurveyors.myapplication.data.local.BillingItemEntity
import com.shahsurveyors.myapplication.data.local.CompanyProfile
import com.shahsurveyors.myapplication.models.DocType
import com.shahsurveyors.myapplication.ui.components.GlobalAsyncLoader
import com.shahsurveyors.myapplication.ui.theme.*
import com.shahsurveyors.myapplication.utils.BillingDocumentGenerator
import java.io.File
import java.util.Locale
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    viewModel: BillingViewModel,
    onBack: () -> Unit = {}
) {

    var selectedMainTab by remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Billing & Invoices",
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
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(ShahGrey)
        ) {

            TabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = ShahWhite,
                contentColor = ShahGreen,
                indicator = { positions ->

                    if (positions.isNotEmpty()) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(
                                positions[selectedMainTab]
                            ),
                            color = ShahGreen
                        )
                    }
                }
            ) {

                Tab(
                    selected = selectedMainTab == 0,
                    onClick = {
                        selectedMainTab = 0
                    },
                    text = {
                        Text(
                            text = "New Document",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )

                Tab(
                    selected = selectedMainTab == 1,
                    onClick = {
                        selectedMainTab = 1
                    },
                    text = {
                        Text(
                            text = "History",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            if (selectedMainTab == 0) {

                BillingCreateContent(
                    viewModel = viewModel
                )

            } else {

                BillingHistoryContent(
                    viewModel = viewModel
                )
            }
        }

        GlobalAsyncLoader(
            isLoading = viewModel.isLoading,
            statusText = viewModel.statusMessage ?: "Processing..."
        )
    }
}


// ============================================================
// CREATE BILLING DOCUMENT
// ============================================================

@Composable
fun BillingCreateContent(
    viewModel: BillingViewModel
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val companyProfile by viewModel.companyProfile.collectAsState()
    val bankDetails by viewModel.bankDetails.collectAsState()
    val availableTerms by viewModel.allTerms.collectAsState()

    var clientName by remember {
        mutableStateOf("")
    }

    var clientAddress by remember {
        mutableStateOf("")
    }

    var clientGstin by remember {
        mutableStateOf("")
    }

    var docType by remember {
        mutableStateOf(DocType.TAX_INVOICE)
    }

    var gstType by remember {
        mutableStateOf("CGST_SGST")
    }

    val items = remember {
        mutableStateListOf<BillingItemEntity>()
    }

    var showAddItemDialog by remember {
        mutableStateOf(false)
    }

    var latestFile by remember {
        mutableStateOf<File?>(null)
    }

    var showShareOptions by remember {
        mutableStateOf(false)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp)
    ) {

        // ====================================================
        // CLIENT INFORMATION
        // ====================================================

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

                Text(
                    text = "Client Information",
                    color = ShahGreen,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                BillingTextField(
                    value = clientName,
                    onValueChange = {
                        clientName = it
                    },
                    label = "Client / Company Name"
                )

                BillingTextField(
                    value = clientAddress,
                    onValueChange = {
                        clientAddress = it
                    },
                    label = "Address"
                )

                BillingTextField(
                    value = clientGstin,
                    onValueChange = {
                        clientGstin = it
                    },
                    label = "GSTIN (Optional)"
                )
            }
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ====================================================
        // LINE ITEMS
        // ====================================================

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Line Items",
                        color = ShahGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            showAddItemDialog = true
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add Item",
                            tint = ShahGreen
                        )
                    }
                }


                if (items.isEmpty()) {

                    Text(
                        text = "No items added yet.",
                        color = ShahMediumGrey,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(
                            vertical = 12.dp
                        )
                    )
                }


                items.forEachIndexed { index, item ->

                    ItemRow(
                        index = index + 1,
                        item = item,
                        onDelete = {
                            items.removeAt(index)
                        }
                    )
                }


                if (items.isNotEmpty()) {

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = 12.dp
                        )
                    )

                    val subTotal = items.sumOf {
                        it.amount
                    }

                    SummaryRow(
                        label = "Sub Total",
                        value = subTotal
                    )


                    if (docType != DocType.NON_GST_BILL) {

                        if (gstType == "IGST") {

                            SummaryRow(
                                label = "IGST (18%)",
                                value = subTotal * 0.18
                            )

                        } else {

                            SummaryRow(
                                label = "CGST (9%)",
                                value = subTotal * 0.09
                            )

                            SummaryRow(
                                label = "SGST (9%)",
                                value = subTotal * 0.09
                            )
                        }
                    }


                    val gstAmount =
                        if (docType == DocType.NON_GST_BILL) {
                            0.0
                        } else {
                            subTotal * 0.18
                        }

                    val total =
                        subTotal + gstAmount

                    SummaryRow(
                        label = "Grand Total",
                        value = total,
                        isBold = true
                    )
                }
            }
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        // ====================================================
        // DOCUMENT TYPE
        // ====================================================

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

                Text(
                    text = "Document Type",
                    color = ShahGreen,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    DocTypeChip(
                        type = DocType.TAX_INVOICE,
                        isSelected =
                            docType == DocType.TAX_INVOICE,
                        modifier = Modifier.weight(1f)
                    ) {
                        docType = it
                    }

                    DocTypeChip(
                        type = DocType.QUOTATION,
                        isSelected =
                            docType == DocType.QUOTATION,
                        modifier = Modifier.weight(1f)
                    ) {
                        docType = it
                    }

                    DocTypeChip(
                        type = DocType.NON_GST_BILL,
                        isSelected =
                            docType == DocType.NON_GST_BILL,
                        modifier = Modifier.weight(1f)
                    ) {
                        docType = it
                    }
                }


                if (docType != DocType.NON_GST_BILL) {

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        RadioButton(
                            selected =
                                gstType == "CGST_SGST",
                            onClick = {
                                gstType = "CGST_SGST"
                            },
                            colors =
                                RadioButtonDefaults.colors(
                                    selectedColor =
                                        ShahGreen
                                )
                        )

                        Text(
                            text = "CGST + SGST",
                            style =
                                MaterialTheme.typography
                                    .bodySmall
                        )

                        Spacer(
                            modifier = Modifier.width(16.dp)
                        )

                        RadioButton(
                            selected =
                                gstType == "IGST",
                            onClick = {
                                gstType = "IGST"
                            },
                            colors =
                                RadioButtonDefaults.colors(
                                    selectedColor =
                                        ShahGreen
                                )
                        )

                        Text(
                            text = "IGST",
                            style =
                                MaterialTheme.typography
                                    .bodySmall
                        )
                    }
                }
            }
        }


        Spacer(
            modifier = Modifier.height(24.dp)
        )


        // ====================================================
        // GENERATE PDF
        // ====================================================

        Button(
            onClick = {

                /*
                 * Document number repository se suspend function
                 * ke through milega.
                 */
                scope.launch {

                    try {

                        val docNumber =
                            viewModel.getNextDocumentNumber(
                                docType
                            )

                        val subTotal =
                            items.sumOf {
                                it.amount
                            }

                        val tax =
                            if (
                                docType ==
                                DocType.NON_GST_BILL
                            ) {
                                0.0
                            } else {
                                subTotal * 0.18
                            }

                        val grandTotal =
                            subTotal + tax


                        val document =
                            BillingDocumentGenerator.DocumentData(

                                docNumber =
                                    docNumber,

                                company =
                                    companyProfile
                                        ?: CompanyProfile(),

                                bank =
                                    bankDetails
                                        ?: BankDetails(),

                                clientName =
                                    clientName.trim(),

                                clientAddress =
                                    clientAddress.trim(),

                                clientGstin =
                                    clientGstin
                                        .trim()
                                        .ifBlank {
                                            null
                                        },

                                docType =
                                    docType,

                                gstType =
                                    gstType,

                                items =
                                    items.toList(),

                                terms =
                                    availableTerms.map {
                                        it.content
                                    }
                            )


                        latestFile =
                            BillingDocumentGenerator
                                .generatePdf(
                                    context,
                                    document
                                )


                        latestFile?.let { file ->

                            /*
                             * IMPORTANT:
                             * Current BillingViewModel me context
                             * parameter nahi hai.
                             */
                            viewModel.uploadAndSyncDoc(
                                file = file,
                                clientName =
                                    clientName.trim(),
                                docType =
                                    docType.name
                            )


                            viewPdf(
                                context,
                                file
                            )

                            showShareOptions = true
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }
            },

            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                clientName
                    .trim()
                    .isNotEmpty() &&
                        items.isNotEmpty() &&
                        !viewModel.isLoading,

            shape =
                RoundedCornerShape(12.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        ShahGreen,
                    contentColor =
                        ShahWhite
                )
        ) {

            Icon(
                imageVector =
                    Icons.Default.PictureAsPdf,
                contentDescription = null
            )

            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )

            Text(
                text =
                    "GENERATE OFFICIAL PDF",
                fontWeight =
                    FontWeight.Bold
            )
        }


        // ====================================================
        // SHARE OPTIONS
        // ====================================================

        if (
            showShareOptions &&
            latestFile != null
        ) {

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            ShahGreen.copy(
                                alpha = 0.1f
                            )
                    ),
                shape =
                    RoundedCornerShape(12.dp)
            ) {

                Row(
                    modifier =
                        Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceAround
                ) {

                    TextButton(
                        onClick = {
                            latestFile?.let {
                                sharePdf(
                                    context,
                                    it
                                )
                            }
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Share,
                            contentDescription =
                                null,
                            tint =
                                SuccessGreen
                        )

                        Spacer(
                            modifier =
                                Modifier.width(4.dp)
                        )

                        Text(
                            text = "Share",
                            color =
                                SuccessGreen
                        )
                    }


                    TextButton(
                        onClick = {
                            latestFile?.let {
                                viewPdf(
                                    context,
                                    it
                                )
                            }
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Visibility,
                            contentDescription =
                                null,
                            tint =
                                ShahGreen
                        )

                        Spacer(
                            modifier =
                                Modifier.width(4.dp)
                        )

                        Text(
                            text = "View PDF",
                            color =
                                ShahGreen
                        )
                    }
                }
            }
        }


        Spacer(
            modifier =
                Modifier.height(24.dp)
        )
    }


    // ========================================================
    // ADD ITEM DIALOG
    // ========================================================

    if (showAddItemDialog) {

        AddItemDialog(

            onDismiss = {
                showAddItemDialog = false
            },

            onAdd = { item ->

                items.add(
                    item.copy(
                        orderIndex = items.size
                    )
                )

                showAddItemDialog = false
            }
        )
    }
}


// ============================================================
// HISTORY
// ============================================================

@Composable
fun BillingHistoryContent(
    viewModel: BillingViewModel
) {

    val documents by
    viewModel.allDocuments.collectAsState()


    if (documents.isEmpty()) {

        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    "No document history found",
                color =
                    ShahMediumGrey
            )
        }

        return
    }


    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(16.dp)
    ) {

        items(
            items = documents,
            key = {
                it.id
            }
        ) { doc ->

            Card(
                modifier =
                    Modifier
                        .padding(
                            vertical = 6.dp
                        )
                        .fillMaxWidth(),

                shape =
                    RoundedCornerShape(12.dp),

                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            ShahWhite
                    ),

                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    )
            ) {

                Row(
                    modifier =
                        Modifier.padding(16.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text =
                                doc.clientName,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                ShahBlack
                        )

                        Text(
                            text =
                                "${doc.docType} - ${doc.docNumber}",
                            color =
                                ShahGreen,
                            fontSize =
                                12.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "Amount: ₹${
                                    "%.2f".format(
                                        Locale.ENGLISH,
                                        doc.grandTotal
                                    )
                                }",
                            color =
                                ShahMediumGrey,
                            fontSize =
                                11.sp
                        )

                        Text(
                            text =
                                "Status: ${doc.status}",
                            color =
                                ShahMediumGrey,
                            fontSize =
                                10.sp
                        )
                    }


                    if (
                        doc.docType ==
                        DocType.QUOTATION
                    ) {

                        Button(

                            onClick = {
                                viewModel
                                    .convertQuotationToInvoice(
                                        doc
                                    )
                            },

                            enabled =
                                !viewModel.isLoading,

                            shape =
                                RoundedCornerShape(8.dp),

                            colors =
                                ButtonDefaults
                                    .buttonColors(
                                        containerColor =
                                            ShahGreen.copy(
                                                alpha = 0.1f
                                            ),
                                        contentColor =
                                            ShahGreen
                                    ),

                            contentPadding =
                                PaddingValues(
                                    horizontal = 12.dp,
                                    vertical = 0.dp
                                )
                        ) {

                            Text(
                                text = "CONVERT",
                                fontSize = 10.sp,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


// ============================================================
// ITEM ROW
// ============================================================

@Composable
fun ItemRow(
    index: Int,
    item: BillingItemEntity,
    onDelete: () -> Unit
) {

    Row(
        verticalAlignment =
            Alignment.CenterVertically,

        modifier =
            Modifier.padding(
                vertical = 6.dp
            )
    ) {

        Text(
            text = "$index.",
            color =
                ShahMediumGrey,
            modifier =
                Modifier.width(24.dp),
            fontSize = 12.sp
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text =
                    item.description,
                color =
                    ShahBlack,
                fontSize =
                    14.sp,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    "${item.qty} ${item.unit} @ ₹${item.rate}",
                color =
                    ShahMediumGrey,
                fontSize =
                    11.sp
            )
        }

        Text(
            text =
                "₹${
                    "%.2f".format(
                        Locale.ENGLISH,
                        item.amount
                    )
                }",
            color =
                ShahBlack,
            fontWeight =
                FontWeight.Bold,
            fontSize =
                14.sp
        )

        IconButton(
            onClick = onDelete
        ) {

            Icon(
                imageVector =
                    Icons.Default.Delete,
                contentDescription =
                    "Delete",
                tint =
                    ErrorRed.copy(
                        alpha = 0.6f
                    ),
                modifier =
                    Modifier.size(20.dp)
            )
        }
    }
}


// ============================================================
// SUMMARY
// ============================================================

@Composable
fun SummaryRow(
    label: String,
    value: Double,
    isBold: Boolean = false
) {

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 2.dp
                ),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = label,

            color =
                if (isBold)
                    ShahBlack
                else
                    ShahMediumGrey,

            fontWeight =
                if (isBold)
                    FontWeight.Bold
                else
                    FontWeight.Normal,

            fontSize = 13.sp
        )

        Text(
            text =
                "₹${
                    "%.2f".format(
                        Locale.ENGLISH,
                        value
                    )
                }",

            color =
                if (isBold)
                    ShahGreen
                else
                    ShahBlack,

            fontWeight =
                if (isBold)
                    FontWeight.Bold
                else
                    FontWeight.Normal,

            fontSize = 13.sp
        )
    }
}


// ============================================================
// ADD ITEM DIALOG
// ============================================================

@Composable
fun AddItemDialog(
    onDismiss: () -> Unit,
    onAdd: (BillingItemEntity) -> Unit
) {

    var desc by remember {
        mutableStateOf("")
    }

    var qty by remember {
        mutableStateOf("1")
    }

    var unit by remember {
        mutableStateOf("Day")
    }

    var rate by remember {
        mutableStateOf("")
    }


    val quantity =
        qty.toDoubleOrNull() ?: 0.0

    val itemRate =
        rate.toDoubleOrNull() ?: 0.0


    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {
            Text(
                text = "Add Billing Item",
                fontWeight =
                    FontWeight.Bold
            )
        },

        text = {

            Column {

                OutlinedTextField(
                    value = desc,
                    onValueChange = {
                        desc = it
                    },
                    label = {
                        Text("Description")
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    singleLine = false
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Row {

                    OutlinedTextField(
                        value = qty,
                        onValueChange = {
                            qty = it
                        },
                        label = {
                            Text("Qty")
                        },
                        modifier =
                            Modifier.weight(1f),
                        singleLine = true
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = {
                            unit = it
                        },
                        label = {
                            Text("Unit")
                        },
                        modifier =
                            Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = rate,
                    onValueChange = {
                        rate = it
                    },
                    label = {
                        Text("Rate (₹)")
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                if (
                    quantity > 0 &&
                    itemRate > 0
                ) {

                    Text(
                        text =
                            "Amount: ₹${
                                "%.2f".format(
                                    Locale.ENGLISH,
                                    quantity * itemRate
                                )
                            }",
                        color =
                            ShahGreen,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }
        },

        confirmButton = {

            Button(

                onClick = {

                    if (
                        desc.isBlank() ||
                        quantity <= 0 ||
                        itemRate <= 0
                    ) {
                        return@Button
                    }

                    onAdd(
                        BillingItemEntity(

                            documentId = 0,

                            description =
                                desc.trim(),

                            unit =
                                unit.trim()
                                    .ifBlank {
                                        "Unit"
                                    },

                            qty =
                                quantity,

                            rate =
                                itemRate,

                            amount =
                                quantity * itemRate,

                            orderIndex = 0
                        )
                    )
                },

                enabled =
                    desc.isNotBlank() &&
                            quantity > 0 &&
                            itemRate > 0,

                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            ShahGreen
                    )
            ) {

                Text("ADD")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    text = "CANCEL",
                    color =
                        ShahGreen
                )
            }
        }
    )
}


// ============================================================
// BILLING TEXT FIELD
// ============================================================

@Composable
fun BillingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,

        label = {
            Text(label)
        },

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp
                ),

        shape =
            RoundedCornerShape(12.dp),

        singleLine = true
    )
}


// ============================================================
// DOCUMENT TYPE CHIP
// ============================================================

@Composable
fun DocTypeChip(
    type: DocType,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (DocType) -> Unit
) {

    FilterChip(
        selected = isSelected,

        onClick = {
            onClick(type)
        },

        label = {

            Text(
                text =
                    type.name
                        .replace("_", " "),

                fontSize = 9.sp,

                modifier =
                    Modifier.fillMaxWidth(),

                textAlign =
                    TextAlign.Center
            )
        },

        modifier = modifier,

        colors =
            FilterChipDefaults
                .filterChipColors(
                    selectedContainerColor =
                        ShahGreen,

                    selectedLabelColor =
                        ShahWhite,

                    labelColor =
                        ShahMediumGrey
                )
    )
}


// ============================================================
// VIEW PDF
// ============================================================

private fun viewPdf(
    context: Context,
    file: File
) {

    val uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

    val intent =
        Intent(
            Intent.ACTION_VIEW
        ).apply {

            setDataAndType(
                uri,
                "application/pdf"
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        }

    context.startActivity(intent)
}


// ============================================================
// SHARE PDF
// ============================================================

private fun sharePdf(
    context: Context,
    file: File
) {

    val uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

    val intent =
        Intent(
            Intent.ACTION_SEND
        ).apply {

            type =
                "application/pdf"

            putExtra(
                Intent.EXTRA_STREAM,
                uri
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

    context.startActivity(
        Intent.createChooser(
            intent,
            "Share Document"
        )
    )
}