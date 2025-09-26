package com.android.fad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.fad.ui.components.ModernFinancialCard
import com.android.fad.ui.components.ExpenseBarChart
import com.android.fad.ui.components.TransactionItem
import com.android.fad.ui.components.AIInsightCard
import com.android.fad.ui.components.AISpendingAnalysisCard
import com.android.fad.ui.components.SmartNotificationsList
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
    val spendingAnalysis by viewModel.spendingAnalysis.collectAsState()
    val isLoadingInsight by viewModel.isLoadingInsight.collectAsState()
    val isLoadingAnalysis by viewModel.isLoadingAnalysis.collectAsState()
    val smartNotifications by viewModel.smartNotifications.collectAsState()
    val expensesByCategory by viewModel.expensesByCategory.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Financial Dashboard",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AI-Powered Insights",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                actions = {
                    IconButton(onClick = { 
                        viewModel.generateAIInsights()
                        viewModel.generateSpendingAnalysis()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh All"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Transaction")
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
            
            // Smart Notifications
            item {
                SmartNotificationsList(
                    notifications = smartNotifications,
                    onDismiss = viewModel::dismissNotification
                )
            }
            
            // Financial Overview Cards
            item {
                Text(
                    text = "Financial Overview",
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
                    ModernFinancialCard(
                        title = "Income",
                        amount = totalIncome,
                        icon = Icons.Default.TrendingUp,
                        colors = listOf(
                            Color(0xFF4CAF50),
                            Color(0xFF66BB6A)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    ModernFinancialCard(
                        title = "Expenses",
                        amount = totalExpenses,
                        icon = Icons.Default.TrendingDown,
                        colors = listOf(
                            Color(0xFFF44336),
                            Color(0xFFEF5350)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                ModernFinancialCard(
                    title = "Balance",
                    amount = balance,
                    icon = Icons.Default.AccountBalance,
                    colors = if (balance >= 0) listOf(
                        Color(0xFF2196F3),
                        Color(0xFF42A5F5)
                    ) else listOf(
                        Color(0xFFFF9800),
                        Color(0xFFFFB74D)
                    )
                )
            }
            
            // Expense Bar Chart
            item {
                ExpenseBarChart(
                    expensesByCategory = expensesByCategory
                )
            }
            
            // AI Insight Card
            item {
                AIInsightCard(
                    insight = aiInsight,
                    isLoading = isLoadingInsight,
                    onRefresh = { viewModel.generateAIInsights() }
                )
            }
            
            // AI Spending Analysis Card
            item {
                AISpendingAnalysisCard(
                    analysis = spendingAnalysis,
                    isLoading = isLoadingAnalysis,
                    topCategories = expensesByCategory.entries
                        .sortedByDescending { it.value }
                        .map { it.key to it.value },
                    onRefresh = { viewModel.generateSpendingAnalysis() }
                )
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