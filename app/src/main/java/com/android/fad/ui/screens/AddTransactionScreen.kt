package com.android.fad.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
    var selectedCategory by remember { mutableStateOf(TransactionCategory.OTHER_EXPENSE) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var isLoadingSuggestion by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    // Update category when type changes
    LaunchedEffect(selectedType) {
        selectedCategory = if (selectedType == TransactionType.INCOME) {
            TransactionCategory.OTHER_INCOME
        } else {
            TransactionCategory.OTHER_EXPENSE
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Transaction",
                        fontWeight = FontWeight.Bold
                    )
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
                modifier = Modifier.fillMaxWidth()
            )
            
            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
                trailingIcon = {
                    if (description.isNotBlank() && amount.isNotBlank() && selectedType == TransactionType.EXPENSE) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isLoadingSuggestion = true
                                    try {
                                        val suggestedCategory = viewModel.suggestCategory(
                                            description, 
                                            amount.toDoubleOrNull() ?: 0.0
                                        )
                                        selectedCategory = suggestedCategory
                                    } finally {
                                        isLoadingSuggestion = false
                                    }
                                }
                            },
                            enabled = !isLoadingSuggestion
                        ) {
                            if (isLoadingSuggestion) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Suggest Category",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            )
            
            // AI Suggestion Helper Text
            if (selectedType == TransactionType.EXPENSE && description.isNotBlank() && amount.isNotBlank()) {
                Text(
                    text = "💡 Tap the AI icon to auto-suggest category",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
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
            
            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            OutlinedButton(
                onClick = { showCategoryDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedCategory.displayName)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Add Transaction Button
            Button(
                onClick = {
                    if (amount.isNotBlank() && description.isNotBlank()) {
                        val transaction = Transaction(
                            id = UUID.randomUUID().toString(),
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            category = selectedCategory,
                            type = selectedType,
                            description = description
                        )
                        viewModel.addTransaction(transaction)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotBlank() && description.isNotBlank()
            ) {
                Text("Add Transaction")
            }
        }
    }
    
    // Category Selection Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                Column {
                    val categories = if (selectedType == TransactionType.INCOME) {
                        listOf(
                            TransactionCategory.SALARY,
                            TransactionCategory.FREELANCE,
                            TransactionCategory.INVESTMENT,
                            TransactionCategory.OTHER_INCOME
                        )
                    } else {
                        listOf(
                            TransactionCategory.FOOD,
                            TransactionCategory.TRANSPORT,
                            TransactionCategory.ENTERTAINMENT,
                            TransactionCategory.SHOPPING,
                            TransactionCategory.BILLS,
                            TransactionCategory.EDUCATION,
                            TransactionCategory.HEALTH,
                            TransactionCategory.OTHER_EXPENSE
                        )
                    }
                    
                    categories.forEach { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedCategory == category,
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryDialog = false
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = category
                                    showCategoryDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category.displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}