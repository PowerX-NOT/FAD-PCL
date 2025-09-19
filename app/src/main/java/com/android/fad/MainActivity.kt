package com.android.fad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.fad.ui.theme.FADTheme
import com.android.fad.ui.screens.HomeScreen
import com.android.fad.ui.screens.TransactionsScreen
import com.android.fad.ui.screens.AddTransactionScreen
import com.android.fad.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FADTheme {
                FinancialDashboardApp()
            }
        }
    }
}

@Composable
fun FinancialDashboardApp() {
    val navController = rememberNavController()
    val viewModel: FinanceViewModel = viewModel()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToTransactions = {
                    navController.navigate("transactions")
                },
                onAddTransaction = {
                    navController.navigate("add_transaction")
                }
            )
        }
        
        composable("transactions") {
            TransactionsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("add_transaction") {
            AddTransactionScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FinancialDashboardPreview() {
    FADTheme {
        FinancialDashboardApp()
    }
}