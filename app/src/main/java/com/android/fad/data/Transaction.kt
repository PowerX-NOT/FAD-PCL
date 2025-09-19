package com.android.fad.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Transaction(
    val id: String,
    val amount: Double,
    val category: TransactionCategory,
    val type: TransactionType,
    val description: String,
    val date: LocalDateTime = LocalDateTime.now()
) {
    fun getFormattedDate(): String {
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }
    
    fun getFormattedTime(): String {
        return date.format(DateTimeFormatter.ofPattern("HH:mm"))
    }
}

enum class TransactionType {
    INCOME, EXPENSE
}

enum class TransactionCategory(val displayName: String, val color: androidx.compose.ui.graphics.Color) {
    // Income categories
    SALARY("Salary", androidx.compose.ui.graphics.Color(0xFF4CAF50)),
    FREELANCE("Freelance", androidx.compose.ui.graphics.Color(0xFF8BC34A)),
    INVESTMENT("Investment", androidx.compose.ui.graphics.Color(0xFF009688)),
    OTHER_INCOME("Other Income", androidx.compose.ui.graphics.Color(0xFF00BCD4)),
    
    // Expense categories
    FOOD("Food & Dining", androidx.compose.ui.graphics.Color(0xFFFF9800)),
    TRANSPORT("Transport", androidx.compose.ui.graphics.Color(0xFF2196F3)),
    ENTERTAINMENT("Entertainment", androidx.compose.ui.graphics.Color(0xFF9C27B0)),
    SHOPPING("Shopping", androidx.compose.ui.graphics.Color(0xFFE91E63)),
    BILLS("Bills & Utilities", androidx.compose.ui.graphics.Color(0xFFF44336)),
    EDUCATION("Education", androidx.compose.ui.graphics.Color(0xFF3F51B5)),
    HEALTH("Health & Medical", androidx.compose.ui.graphics.Color(0xFF607D8B)),
    OTHER_EXPENSE("Other Expense", androidx.compose.ui.graphics.Color(0xFF795548))
}