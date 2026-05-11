package com.example.invoicegen.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invoicegen.model.InvoiceItem
import com.example.invoicegen.pdf.InvoicePdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceFormScreen(
    viewModel: InvoiceViewModel,
    pdfGenerator: InvoicePdfGenerator,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Notification permission is required to show download alerts", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val header by viewModel.header.collectAsState()
    val items by viewModel.items.collectAsState()
    val transport by viewModel.transport.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val settingsState by viewModel.companySettings.collectAsState()
    val settings = settingsState ?: com.example.invoicegen.data.CompanySettings()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("InvoiceGen") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Invoice Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = header.invoiceNo,
                    onValueChange = { viewModel.updateHeader(header.copy(invoiceNo = it)) },
                    label = { Text("Invoice No") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = header.date,
                    onValueChange = { viewModel.updateHeader(header.copy(date = it)) },
                    label = { Text("Date") },
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = header.nameOfParty,
                onValueChange = { viewModel.updateHeader(header.copy(nameOfParty = it)) },
                label = { Text("Name of Party") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = header.address,
                onValueChange = { viewModel.updateHeader(header.copy(address = it)) },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = header.gstin,
                    onValueChange = { viewModel.updateHeader(header.copy(gstin = it)) },
                    label = { Text("Party GSTIN") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = header.mobileNo,
                    onValueChange = { viewModel.updateHeader(header.copy(mobileNo = it)) },
                    label = { Text("Party Mobile No") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = header.transport,
                    onValueChange = { viewModel.updateHeader(header.copy(transport = it)) },
                    label = { Text("Transport") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = header.stateCode,
                    onValueChange = { viewModel.updateHeader(header.copy(stateCode = it)) },
                    label = { Text("State Code") },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = header.broker,
                    onValueChange = { viewModel.updateHeader(header.copy(broker = it)) },
                    label = { Text("Broker") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = header.truckNo,
                    onValueChange = { viewModel.updateHeader(header.copy(truckNo = it)) },
                    label = { Text("Truck No") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Items", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
            items.forEachIndexed { index, item ->
                ItemRow(
                    item = item,
                    onUpdate = { viewModel.updateItem(index, it) },
                    onDelete = { viewModel.removeItem(index) }
                )
            }
            
            Button(
                onClick = { viewModel.addItem() },
                modifier = Modifier.align(Alignment.End).padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add Item")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Transport & Summary", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = transport.driverName,
                onValueChange = { viewModel.updateTransport(transport.copy(driverName = it)) },
                label = { Text("Driver Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = transport.licenseNo,
                    onValueChange = { viewModel.updateTransport(transport.copy(licenseNo = it)) },
                    label = { Text("License No") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = transport.mobileNo,
                    onValueChange = { viewModel.updateTransport(transport.copy(mobileNo = it)) },
                    label = { Text("Driver Mobile") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = transport.truckOwner,
                    onValueChange = { viewModel.updateTransport(transport.copy(truckOwner = it)) },
                    label = { Text("Truck Owner") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = transport.engineNo,
                    onValueChange = { viewModel.updateTransport(transport.copy(engineNo = it)) },
                    label = { Text("Engine No") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = transport.chassisNo,
                    onValueChange = { viewModel.updateTransport(transport.copy(chassisNo = it)) },
                    label = { Text("Chassis No") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = summary.advance,
                    onValueChange = { viewModel.updateSummary(summary.copy(advance = it)) },
                    label = { Text("Advance") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = summary.insurance,
                    onValueChange = { viewModel.updateSummary(summary.copy(insurance = it)) },
                    label = { Text("Insurance") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val uris = withContext(Dispatchers.IO) {
                                pdfGenerator.generateInvoices(
                                    header, items, transport, summary, settings,
                                    viewModel.totalAmount, viewModel.grandTotal
                                )
                            }
                            if (uris.isNotEmpty()) {
                                // Show notification for the "Original" copy (first URI)
                                pdfGenerator.showNotification(uris[0], header.invoiceNo)
                                
                                // Increment and update UI
                                viewModel.incrementNextInvoiceNumber()
                                
                                Toast.makeText(context, "PDFs generated successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to generate PDFs", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("GENERATE PDF (3 COPIES)")
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ItemRow(
    item: InvoiceItem,
    onUpdate: (InvoiceItem) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item.particulars,
                    onValueChange = { onUpdate(item.copy(particulars = it)) },
                    label = { Text("Particulars") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = item.qtyBags,
                    onValueChange = { onUpdate(item.copy(qtyBags = it)) },
                    label = { Text("Qty (Bags)") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(4.dp))
                OutlinedTextField(
                    value = item.weightPerBag,
                    onValueChange = { onUpdate(item.copy(weightPerBag = it)) },
                    label = { Text("Wt/Bag (kg)") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(4.dp))
                OutlinedTextField(
                    value = item.ratePerQtl,
                    onValueChange = { onUpdate(item.copy(ratePerQtl = it)) },
                    label = { Text("Rate/Qtl") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Net Wt: %.2f QTL".format(item.netWeightQtl),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    "Amount: %.2f".format(item.amount),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
