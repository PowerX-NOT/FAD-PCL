package com.android.fad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.fad.data.Transaction
import com.android.fad.data.TransactionCategory
import com.android.fad.data.TransactionType
import com.android.fad.viewmodel.FinanceViewModel
import java.util.UUID
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: FinanceViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var suggestedCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    var isLoadingSuggestion by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Auto-suggest category when description and amount change
    LaunchedEffect(description, amount, selectedType) {
        if (description.isNotBlank() && amount.isNotBlank() && selectedType == TransactionType.EXPENSE) {
            isLoadingSuggestion = true
            categoryError = null
            try {
                val category = viewModel.suggestCategory(
                    description, 
                    amount.toDoubleOrNull() ?: 0.0
                )
                suggestedCategory = category
            } catch (e: Exception) {
                categoryError = "Failed to get AI suggestion"
                suggestedCategory = TransactionCategory.OTHER_EXPENSE
            } finally {
                isLoadingSuggestion = false
            }
        } else if (selectedType == TransactionType.INCOME) {
            // For income, use simple keyword matching or default
            suggestedCategory = when {
                description.lowercase().contains("salary") -> TransactionCategory.SALARY
                description.lowercase().contains("freelance") || 
                description.lowercase().contains("project") -> TransactionCategory.FREELANCE
                description.lowercase().contains("investment") || 
                description.lowercase().contains("dividend") -> TransactionCategory.INVESTMENT
                else -> TransactionCategory.OTHER_INCOME
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Add Transaction",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AI will auto-categorize",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text("Enter the transaction amount")
                }
            )
            
            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text("Describe your transaction (AI will categorize automatically)")
                },
                trailingIcon = {
                    if (isLoadingSuggestion) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (suggestedCategory != null) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Categorized",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
            
            // Transaction Type Selection
            Text(
                text = "Transaction Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransactionType.values().forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // AI Category Suggestion Display
            if (suggestedCategory != null || isLoadingSuggestion || categoryError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Category",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Category Suggestion",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        when {
                            isLoadingSuggestion -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI is analyzing your transaction...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            categoryError != null -> {
                                Text(
                                    text = "⚠️ $categoryError. Using default category.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            suggestedCategory != null -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = suggestedCategory!!.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = suggestedCategory!!.color
                                        )
                                        Text(
                                            text = "Automatically selected by AI",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // Category color indicator
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                suggestedCategory!!.color,
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Help text
            if (description.isBlank() && amount.isBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "💡 How AI Categorization Works",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Simply enter your transaction description and amount. Our AI will automatically analyze and categorize your transaction based on keywords, context, and spending patterns.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Add Transaction Button
            Button(
                onClick = {
                    if (amount.isNotBlank() && description.isNotBlank() && suggestedCategory != null) {
                        val transaction = Transaction(
                            id = UUID.randomUUID().toString(),
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            category = suggestedCategory!!,
                            type = selectedType,
                            description = description
                        )
                        viewModel.addTransaction(transaction)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotBlank() && 
                         description.isNotBlank() && 
                         suggestedCategory != null && 
                         !isLoadingSuggestion
            ) {
                if (isLoadingSuggestion) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Categorizing...")
                } else {
                    Text("Add Transaction")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}