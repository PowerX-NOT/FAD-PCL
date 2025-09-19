package com.android.fad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.fad.data.Transaction
import com.android.fad.data.TransactionCategory
import com.android.fad.data.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID

class FinanceViewModel : ViewModel() {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()
    
    private val _totalIncome = MutableStateFlow(0.0)
    val totalIncome: StateFlow<Double> = _totalIncome.asStateFlow()
    
    private val _totalExpenses = MutableStateFlow(0.0)
    val totalExpenses: StateFlow<Double> = _totalExpenses.asStateFlow()
    
    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()
    
    init {
        // Add some sample data
        loadSampleData()
    }
    
    private fun loadSampleData() {
        val sampleTransactions = listOf(
            Transaction(
                id = UUID.randomUUID().toString(),
                amount = 3500.0,
                category = TransactionCategory.SALARY,
                type = TransactionType.INCOME,
                description = "Monthly Salary",
                date = LocalDateTime.now().minusDays(1)
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                amount = 45.50,
                category = TransactionCategory.FOOD,
                type = TransactionType.EXPENSE,
                description = "Lunch at campus cafeteria",
                date = LocalDateTime.now().minusHours(3)
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                amount = 25.00,
                category = TransactionCategory.TRANSPORT,
                type = TransactionType.EXPENSE,
                description = "Bus pass monthly",
                date = LocalDateTime.now().minusHours(5)
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                amount = 120.00,
                category = TransactionCategory.EDUCATION,
                type = TransactionType.EXPENSE,
                description = "Textbooks",
                date = LocalDateTime.now().minusDays(2)
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                amount = 500.0,
                category = TransactionCategory.FREELANCE,
                type = TransactionType.INCOME,
                description = "Web design project",
                date = LocalDateTime.now().minusDays(3)
            )
        )
        
        _transactions.value = sampleTransactions.sortedByDescending { it.date }
        calculateTotals()
    }
    
    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val currentTransactions = _transactions.value.toMutableList()
            currentTransactions.add(transaction)
            _transactions.value = currentTransactions.sortedByDescending { it.date }
            calculateTotals()
        }
    }
    
    private fun calculateTotals() {
        val transactions = _transactions.value
        val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        
        _totalIncome.value = income
        _totalExpenses.value = expenses
        _balance.value = income - expenses
    }
    
    fun getTransactionsByCategory(category: TransactionCategory): List<Transaction> {
        return _transactions.value.filter { it.category == category }
    }
    
    fun getRecentTransactions(limit: Int = 5): List<Transaction> {
        return _transactions.value.take(limit)
    }
}