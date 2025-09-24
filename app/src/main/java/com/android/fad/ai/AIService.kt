package com.android.fad.ai

import com.azure.ai.inference.ChatCompletionsClient
import com.azure.ai.inference.ChatCompletionsClientBuilder
import com.azure.ai.inference.models.ChatCompletions
import com.azure.ai.inference.models.ChatCompletionsOptions
import com.azure.ai.inference.models.ChatRequestMessage
import com.azure.ai.inference.models.ChatRequestSystemMessage
import com.azure.ai.inference.models.ChatRequestUserMessage
import com.azure.core.credential.AzureKeyCredential
import com.android.fad.BuildConfig
import com.android.fad.data.Transaction
import com.android.fad.data.TransactionCategory
import com.android.fad.ui.components.SmartNotification
import com.android.fad.ui.components.NotificationType
import com.android.fad.ui.components.NotificationPriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class AIService {
    private val client: ChatCompletionsClient by lazy {
        val key = getGitHubToken()
        val endpoint = "https://models.inference.ai.azure.com"
        
        ChatCompletionsClientBuilder()
            .credential(AzureKeyCredential(key))
            .endpoint(endpoint)
            .buildClient()
    }
    
    private val model = "gpt-4o-mini" // GitHub Models uses this format
    
    private fun getGitHubToken(): String {
        // Token is loaded from .env file (preferred) or gradle.properties (fallback)
        // .env file is gitignored for security
        val token = BuildConfig.GITHUB_TOKEN.takeIf { it.isNotBlank() } 
            ?: throw IllegalStateException("GitHub token not configured. Please set GITHUB_TOKEN in .env file or gradle.properties")
        
        // Debug: Log token length (not the actual token for security)
        println("GitHub token loaded: ${token.length} characters")
        return token
    }
    
    // Test method to verify AI service connectivity
    suspend fun testConnection(): String = withContext(Dispatchers.IO) {
        try {
            val messages = listOf(
                ChatRequestSystemMessage("You are a helpful assistant."),
                ChatRequestUserMessage("Say 'Hello, AI service is working!'")
            )
            
            val options = ChatCompletionsOptions(messages)
            options.model = model
            options.maxTokens = 50
            options.temperature = 0.1
            
            val completions = client.complete(options)
            completions.choices.firstOrNull()?.message?.content ?: "No response"
            
        } catch (e: Exception) {
            println("AI Connection Test Error: ${e.message}")
            e.printStackTrace()
            "Connection test failed: ${e.message}"
        }
    }

    suspend fun generateFinancialInsight(
        transactions: List<Transaction>,
        totalIncome: Double,
        totalExpenses: Double,
        balance: Double
    ): String = withContext(Dispatchers.IO) {
        try {
            val prompt = buildFinancialInsightPrompt(transactions, totalIncome, totalExpenses, balance)
            
            val messages = listOf(
                ChatRequestSystemMessage("You are a helpful financial advisor AI assistant specializing in student finances. Provide practical, actionable advice for managing money as a student. Keep responses concise (2-3 sentences) and focus on the most important insights. Use a friendly, encouraging tone."),
                ChatRequestUserMessage(prompt)
            )
            
            val options = ChatCompletionsOptions(messages)
            options.model = model
            options.maxTokens = 150
            options.temperature = 0.7
            
            val completions = client.complete(options)
            completions.choices.firstOrNull()?.message?.content ?: "Unable to generate insight at this time."
            
        } catch (e: Exception) {
            // Log the actual error for debugging
            println("AI Service Error: ${e.message}")
            e.printStackTrace()
            "Financial insight temporarily unavailable. Please check your connection."
        }
    }
    
    suspend fun suggestCategory(description: String, amount: Double): TransactionCategory = withContext(Dispatchers.IO) {
        try {
            val prompt = "Based on this transaction description: \"$description\" (Amount: ₹$amount). Suggest the most appropriate category from these options: FOOD, TRANSPORT, ENTERTAINMENT, SHOPPING, BILLS, EDUCATION, HEALTH, OTHER_EXPENSE. Respond with only the category name, nothing else."
            
            val messages = listOf(
                ChatRequestSystemMessage("You are a transaction categorization assistant. Respond with only the category name."),
                ChatRequestUserMessage(prompt)
            )
            
            val options = ChatCompletionsOptions(messages)
            options.model = model
            options.maxTokens = 20
            options.temperature = 0.3
            
            val completions = client.complete(options)
            val response = completions.choices.firstOrNull()?.message?.content?.trim()?.uppercase()
            
            // Map AI response to our categories
            when (response) {
                "FOOD" -> TransactionCategory.FOOD
                "TRANSPORT" -> TransactionCategory.TRANSPORT
                "ENTERTAINMENT" -> TransactionCategory.ENTERTAINMENT
                "SHOPPING" -> TransactionCategory.SHOPPING
                "BILLS" -> TransactionCategory.BILLS
                "EDUCATION" -> TransactionCategory.EDUCATION
                "HEALTH" -> TransactionCategory.HEALTH
                else -> TransactionCategory.OTHER_EXPENSE
            }
            
        } catch (e: Exception) {
            // Log the actual error for debugging
            println("AI Category Suggestion Error: ${e.message}")
            e.printStackTrace()
            // Fallback to simple keyword matching
            suggestCategoryFallback(description)
        }
    }
    
    suspend fun generateBudgetAdvice(
        monthlyIncome: Double,
        monthlyExpenses: Double,
        expensesByCategory: Map<TransactionCategory, Double>
    ): String = withContext(Dispatchers.IO) {
        try {
            val topExpensesText = expensesByCategory.entries.sortedByDescending { it.value }.take(3)
                .joinToString(", ") { "${it.key.displayName}: ₹${String.format("%.2f", it.value)}" }
            val savingsRate = ((monthlyIncome - monthlyExpenses) / monthlyIncome * 100).toInt()
            
            val prompt = "Student Financial Analysis: Monthly Income: ₹$monthlyIncome, Monthly Expenses: ₹$monthlyExpenses, Savings Rate: $savingsRate%. Top Expense Categories: $topExpensesText. Provide specific budgeting advice for a student. Focus on practical tips for the highest expense categories."
            
            val messages = listOf(
                ChatRequestSystemMessage("You are a financial advisor for students. Provide practical budgeting advice. Keep it concise (3-4 sentences) and actionable. Focus on student-specific money-saving tips."),
                ChatRequestUserMessage(prompt)
            )
            
            val options = ChatCompletionsOptions(messages)
            options.model = model
            options.maxTokens = 200
            options.temperature = 0.7
            
            val completions = client.complete(options)
            completions.choices.firstOrNull()?.message?.content ?: "Budget advice temporarily unavailable."
            
        } catch (e: Exception) {
            // Log the actual error for debugging
            println("AI Budget Advice Error: ${e.message}")
            e.printStackTrace()
            generateFallbackBudgetAdvice(monthlyIncome, monthlyExpenses)
        }
    }
    
    suspend fun generateSpendingAnalysis(
        transactions: List<Transaction>,
        expensesByCategory: Map<TransactionCategory, Double>
    ): String = withContext(Dispatchers.IO) {
        try {
            val topCategories = expensesByCategory.entries.sortedByDescending { it.value }.take(3)
                .joinToString(", ") { "${it.key.displayName}: ₹${String.format("%.0f", it.value)}" }
            
            val totalSpent = expensesByCategory.values.sum()
            val avgTransactionAmount = if (transactions.isNotEmpty()) {
                transactions.filter { it.type == com.android.fad.data.TransactionType.EXPENSE }
                    .map { it.amount }.average()
            } else 0.0
            
            val prompt = "Spending Pattern Analysis: Total spent: ₹$totalSpent, Average transaction: ₹${String.format("%.2f", avgTransactionAmount)}, Top categories: $topCategories. Provide insights on spending patterns, identify potential areas for optimization, and suggest 2-3 actionable improvements for better financial health."
            
            val messages = listOf(
                ChatRequestSystemMessage("You are a financial analyst AI. Analyze spending patterns and provide actionable insights. Keep responses concise (4-5 sentences) and focus on practical recommendations."),
                ChatRequestUserMessage(prompt)
            )
            
            val options = ChatCompletionsOptions(messages)
            options.model = model
            options.maxTokens = 200
            options.temperature = 0.6
            
            val completions = client.complete(options)
            completions.choices.firstOrNull()?.message?.content ?: "Analysis temporarily unavailable."
            
        } catch (e: Exception) {
            println("AI Spending Analysis Error: ${e.message}")
            e.printStackTrace()
            generateFallbackSpendingAnalysis(expensesByCategory)
        }
    }
    
    suspend fun generateSmartNotifications(
        transactions: List<Transaction>,
        monthlyIncome: Double,
        monthlyExpenses: Double,
        expensesByCategory: Map<TransactionCategory, Double>
    ): List<SmartNotification> = withContext(Dispatchers.IO) {
        val notifications = mutableListOf<SmartNotification>()
        
        try {
            // Budget warning if expenses > 80% of income
            val spendingRatio = if (monthlyIncome > 0) monthlyExpenses / monthlyIncome else 0.0
            if (spendingRatio > 0.8) {
                notifications.add(
                    SmartNotification(
                        id = "budget_warning_${System.currentTimeMillis()}",
                        title = "Budget Alert",
                        message = "You've spent ${(spendingRatio * 100).toInt()}% of your income this month. Consider reviewing your expenses.",
                        type = NotificationType.BUDGET_WARNING,
                        priority = NotificationPriority.HIGH
                    )
                )
            }
            
            // High spending category alert
            val topCategory = expensesByCategory.maxByOrNull { it.value }
            if (topCategory != null && topCategory.value > monthlyIncome * 0.3) {
                notifications.add(
                    SmartNotification(
                        id = "spending_alert_${System.currentTimeMillis()}",
                        title = "High Spending Alert",
                        message = "${topCategory.key.displayName} accounts for ${((topCategory.value / monthlyExpenses) * 100).toInt()}% of your expenses. Consider optimizing this category.",
                        type = NotificationType.SPENDING_ALERT,
                        priority = NotificationPriority.MEDIUM
                    )
                )
            }
            
            // Savings tip if spending ratio is good
            if (spendingRatio < 0.7) {
                notifications.add(
                    SmartNotification(
                        id = "savings_tip_${System.currentTimeMillis()}",
                        title = "Great Job!",
                        message = "You're saving ${((1 - spendingRatio) * 100).toInt()}% of your income. Consider investing some of these savings for long-term growth.",
                        type = NotificationType.SAVINGS_TIP,
                        priority = NotificationPriority.LOW
                    )
                )
            }
            
        } catch (e: Exception) {
            println("Smart Notifications Error: ${e.message}")
        }
        
        notifications
    }
    
    private fun buildFinancialInsightPrompt(
        transactions: List<Transaction>,
        totalIncome: Double,
        totalExpenses: Double,
        balance: Double
    ): String {
        val recentTransactions = transactions.take(5)
        val topCategories = transactions
            .groupBy { it.category }
            .mapValues { it.value.sumOf { transaction -> transaction.amount } }
            .entries.sortedByDescending { it.value }
            .take(3)
        
        val recentTxnText = recentTransactions.joinToString(", ") { 
            "${it.description}: ₹${it.amount} (${it.category.displayName})" 
        }
        val topCategoriesText = topCategories.joinToString(", ") { 
            "${it.key.displayName}: ₹${String.format("%.2f", it.value)}" 
        }
        
        return "Student Financial Summary: Total Income: ₹$totalIncome, Total Expenses: ₹$totalExpenses, Current Balance: ₹$balance. Recent Transactions: $recentTxnText. Top Spending Categories: $topCategoriesText. Provide a brief financial insight and one actionable tip for this student."
    }
    
    private fun suggestCategoryFallback(description: String): TransactionCategory {
        val desc = description.lowercase()
        return when {
            desc.contains("food") || desc.contains("restaurant") || desc.contains("cafe") || 
            desc.contains("lunch") || desc.contains("dinner") || desc.contains("breakfast") -> 
                TransactionCategory.FOOD
            desc.contains("bus") || desc.contains("train") || desc.contains("uber") || 
            desc.contains("taxi") || desc.contains("transport") -> 
                TransactionCategory.TRANSPORT
            desc.contains("movie") || desc.contains("game") || desc.contains("entertainment") -> 
                TransactionCategory.ENTERTAINMENT
            desc.contains("book") || desc.contains("course") || desc.contains("education") || 
            desc.contains("tuition") -> 
                TransactionCategory.EDUCATION
            desc.contains("medicine") || desc.contains("doctor") || desc.contains("health") -> 
                TransactionCategory.HEALTH
            desc.contains("electricity") || desc.contains("water") || desc.contains("bill") -> 
                TransactionCategory.BILLS
            else -> TransactionCategory.OTHER_EXPENSE
        }
    }
    
    private fun generateFallbackBudgetAdvice(monthlyIncome: Double, monthlyExpenses: Double): String {
        val savingsRate = ((monthlyIncome - monthlyExpenses) / monthlyIncome * 100).toInt()
        return when {
            savingsRate < 10 -> "Your savings rate is low. Try the 50/30/20 rule: 50% needs, 30% wants, 20% savings. Look for areas to cut back on discretionary spending."
            savingsRate < 20 -> "Good progress! You're saving $savingsRate% of your income. Consider increasing this to 20% by reducing entertainment or dining out expenses."
            else -> "Excellent savings rate of $savingsRate%! You're doing great. Consider investing some of your savings for long-term growth."
        }
    }
    
    private fun generateFallbackSpendingAnalysis(expensesByCategory: Map<TransactionCategory, Double>): String {
        val topCategory = expensesByCategory.maxByOrNull { it.value }
        val totalSpent = expensesByCategory.values.sum()
        
        return if (topCategory != null) {
            val percentage = ((topCategory.value / totalSpent) * 100).toInt()
            "Your highest spending category is ${topCategory.key.displayName} at $percentage% of total expenses (₹${String.format("%.0f", topCategory.value)}). Consider setting a budget limit for this category and look for alternatives to reduce costs. Track your daily expenses to identify patterns and opportunities for savings."
        } else {
            "Start tracking your expenses to get personalized spending insights. Focus on categorizing your transactions to understand where your money goes each month."
        }
    }
}