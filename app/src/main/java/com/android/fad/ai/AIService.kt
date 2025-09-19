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
}