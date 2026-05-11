package com.example.invoicegen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.invoicegen.data.AppDatabase
import com.example.invoicegen.data.SettingsRepository
import com.example.invoicegen.pdf.InvoicePdfGenerator
import com.example.invoicegen.ui.InvoiceFormScreen
import com.example.invoicegen.ui.InvoiceViewModel
import com.example.invoicegen.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = SettingsRepository(database.settingsDao())
        val pdfGenerator = InvoicePdfGenerator(applicationContext)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: InvoiceViewModel = remember {
                        ViewModelProvider(this, object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return InvoiceViewModel(repository) as T
                            }
                        })[InvoiceViewModel::class.java]
                    }

                    NavHost(navController = navController, startDestination = "invoice_form") {
                        composable("invoice_form") {
                            InvoiceFormScreen(
                                viewModel = viewModel,
                                pdfGenerator = pdfGenerator,
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
