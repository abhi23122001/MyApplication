package com.shahsurveyors.myapplication.ui.billing

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.shahsurveyors.myapplication.models.BillingDocument
import com.shahsurveyors.myapplication.models.BillingItem
import com.shahsurveyors.myapplication.models.DocType
import com.shahsurveyors.myapplication.ui.components.GlassCard
import com.shahsurveyors.myapplication.ui.components.LoadingOverlay
import com.shahsurveyors.myapplication.utils.BillingDocumentGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen() {
    val context = LocalContext.current
    var clientName by remember { mutableStateOf("") }
    var clientAddress by remember { mutableStateOf("") }
    var isTaxInvoice by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Billing Suite") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = clientName, onValueChange = { clientName = it }, label = { Text("Client Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = clientAddress, onValueChange = { clientAddress = it }, label = { Text("Client Address") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = isTaxInvoice, onCheckedChange = { isTaxInvoice = it })
                    Text("GST Tax Invoice")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isLoading = true
                        val doc = BillingDocument(
                            docType = if (isTaxInvoice) DocType.TAX_INVOICE else DocType.NON_GST_BILL,
                            clientName = clientName,
                            clientAddress = clientAddress,
                            items = listOf(BillingItem("Surveying Services - Waidhan", 1.0, "Job", 50000.0))
                        )
                        val file = BillingDocumentGenerator.generatePdf(context, doc)
                        isLoading = false
                        
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Invoice"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("GENERATE & SHARE PDF")
                }
            }
        }
        LoadingOverlay(isLoading = isLoading, statusText = "Generating Production PDF...")
    }
}
