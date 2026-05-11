package com.example.invoicegen.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invoicegen.data.CompanySettings
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: InvoiceViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.companySettings.collectAsState()
    var localSettings by remember(settings) { mutableStateOf(settings ?: CompanySettings()) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val logoFile = File(context.filesDir, "company_logo.png")
                    val outputStream = FileOutputStream(logoFile)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    localSettings = localSettings.copy(logoPath = logoFile.absolutePath)
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            Text("Company Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = localSettings.companyName,
                onValueChange = { localSettings = localSettings.copy(companyName = it) },
                label = { Text("Company Name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localSettings.subtitle,
                onValueChange = { localSettings = localSettings.copy(subtitle = it) },
                label = { Text("Company Subtitle") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localSettings.address,
                onValueChange = { localSettings = localSettings.copy(address = it) },
                label = { Text("Company Address") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    logoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (localSettings.logoPath.isEmpty()) "Select Company Logo" else "Change Company Logo")
            }
            if (localSettings.logoPath.isNotEmpty()) {
                Text("Logo saved at: .../${File(localSettings.logoPath).name}", fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = localSettings.gstin,
                onValueChange = { localSettings = localSettings.copy(gstin = it) },
                label = { Text("GSTIN") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localSettings.mobileNumbers,
                onValueChange = { localSettings = localSettings.copy(mobileNumbers = it) },
                label = { Text("Mobile Numbers") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localSettings.nextInvoiceNumber.toString(),
                onValueChange = { localSettings = localSettings.copy(nextInvoiceNumber = it.toIntOrNull() ?: 0) },
                label = { Text("Next Invoice Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Bank Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("ICICI Bank", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = localSettings.iciciAccountNo,
                onValueChange = { localSettings = localSettings.copy(iciciAccountNo = it) },
                label = { Text("Account No") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localSettings.iciciIfsc,
                onValueChange = { localSettings = localSettings.copy(iciciIfsc = it) },
                label = { Text("IFSC Code") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("PNB Bank", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = localSettings.pnbAccountNo,
                onValueChange = { localSettings = localSettings.copy(pnbAccountNo = it) },
                label = { Text("Account No") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = localSettings.pnbIfsc,
                onValueChange = { localSettings = localSettings.copy(pnbIfsc = it) },
                label = { Text("IFSC Code") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Terms & Conditions", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = localSettings.termsAndConditions,
                onValueChange = { localSettings = localSettings.copy(termsAndConditions = it) },
                label = { Text("Default Terms") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.saveSettings(localSettings)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("SAVE SETTINGS")
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
