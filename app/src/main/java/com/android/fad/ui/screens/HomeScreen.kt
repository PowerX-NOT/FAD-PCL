package com.android.fad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.fad.ui.components.FinancialCard
import com.android.fad.ui.components.TransactionItem
import com.android.fad.ui.components.AIInsightCard
import com.android.fad.ui.components.BudgetAdviceCard
import com.android.fad.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FinanceViewModel = viewModel(),
    onNavigateToTransactions: () -> Unit = {},
    onAddTransaction: () -> Unit = {}
) {
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val aiInsight by viewModel.aiInsight.collectAsState()
    val budgetAdvice by viewModel.budgetAdvice.collectAsState()
    val isLoadingInsight by viewModel.isLoadingInsight.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Financial Dashboard",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // AI Insight Card
            item {
                AIInsightCard(
                    insight = aiInsight,
                    isLoading = isLoadingInsight,
                    onRefresh = { viewModel.generateAIInsights() }
                )
            }
            
            // Financial Overview Cards
            item {
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FinancialCard(
                        title = "Income",
                        amount = totalIncome,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    FinancialCard(
                        title = "Expenses",
                        amount = totalExpenses,
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                FinancialCard(
                    title = "Balance",
                    amount = balance,
                    color = if (balance >= 0) Color(0xFF2196F3) else Color(0xFFFF9800)
                )
            }
            
            // Budget Advice Card
            item {
                BudgetAdviceCard(advice = budgetAdvice)
            }
            
            // Recent Transactions Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToTransactions) {
                        Text("View All")
                    }
                }
            }
            
            // Recent Transactions List
            items(viewModel.getRecentTransactions()) { transaction ->
                TransactionItem(transaction = transaction)
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }
    }
}