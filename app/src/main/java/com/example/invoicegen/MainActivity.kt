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

        // These should be handled in your dependency graph and inject by it.
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = SettingsRepository(database.settingsDao())
        val pdfGenerator = InvoicePdfGenerator(applicationContext)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // To further separate concerns everything in here should be in an app composable
                    // The app composable conceptually will be your display powered by your nav system.

                    val navController = rememberNavController()

                    // I would avoid this pattern in favor of using a dependency injection library
                    // such as Hilt or Koin.
                    val viewModel: InvoiceViewModel = remember {
                        ViewModelProvider(this, object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return InvoiceViewModel(repository) as T
                            }
                        })[InvoiceViewModel::class.java]
                    }

                    // I'd recommend updating to Jetpack Navigation 3 (low priority)
                    // Navigation logic should likely be its own Composable and simply called here.
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
