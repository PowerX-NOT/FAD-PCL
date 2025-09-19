package com.android.fad.ai

import com.azure.ai.inference.ChatCompletionsClient
import com.azure.ai.inference.ChatCompletionsClientBuilder
import com.azure.ai.inference.models.ChatCompletions
import com.azure.ai.inference.models.ChatCompletionsOptions
import com.azure.ai.inference.models.ChatRequestMessage
import com.azure.ai.inference.models.ChatRequestSystemMessage
import com.azure.ai.inference.models.ChatRequestUserMessage
import com.azure.core.credential.AzureKeyCredential
import com.android.fad.data.Transaction
import com.android.fad.data.TransactionCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class AIService {
    private val client: ChatCompletionsClient by lazy {
        val key = getGitHubToken()
        val endpoint = "https://models.github.ai/inference"
        
        ChatCompletionsClientBuilder()
            .credential(AzureKeyCredential(key))
            .endpoint(endpoint)
            .buildClient()
    }
    
    private val model = "openai/gpt-4o-mini"
    
    private fun getGitHubToken(): String {
        // In a real app, store this securely (e.g., in encrypted SharedPreferences or BuildConfig)
        // For demo purposes, you would set this as a build config field
        return BuildConfig.GITHUB_TOKEN.ifEmpty { 
            "your-github-token-here" // Replace with your actual token
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
                ChatRequestSystemMessage("""
                    You are a helpful financial advisor AI assistant specializing in student finances. 
                    Provide practical, actionable advice for managing money as a student. 
                    Keep responses concise (2-3 sentences) and focus on the most important insights.
                    Use a friendly, encouraging tone.
                """.trimIndent()),
                ChatRequestUserMessage(prompt)
            )
            
            val options = ChatCompletionsOptions(messages).apply {
                setModel(model)
                setMaxTokens(150)
                setTemperature(0.7)
            }
            
            val completions = client.complete(options)
            completions.choices.firstOrNull()?.message?.content ?: "Unable to generate insight at this time."
            
        } catch (e: Exception) {
            "Financial insight temporarily unavailable. Please check your connection."
        }
    }
    
    suspend fun suggestCategory(description: String, amount: Double): TransactionCategory = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                Based on this transaction description: "$description" (Amount: ₹$amount)
                
                Suggest the most appropriate category from these options:
                FOOD, TRANSPORT, ENTERTAINMENT, SHOPPING, BILLS, EDUCATION, HEALTH, OTHER_EXPENSE
                
                Respond with only the category name, nothing else.
            """.trimIndent()
            
            val messages = listOf(
                ChatRequestSystemMessage("You are a transaction categorization assistant. Respond with only the category name."),
                ChatRequestUserMessage(prompt)
            )
            
            val options = ChatCompletionsOptions(messages).apply {
                setModel(model)
                setMaxTokens(20)
                setTemperature(0.3)
            }
            
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
            val prompt = """
                Student Financial Analysis:
                - Monthly Income: ₹$monthlyIncome
                - Monthly Expenses: ₹$monthlyExpenses
                - Savings Rate: ${((monthlyIncome - monthlyExpenses) / monthlyIncome * 100).toInt()}%
                
                Top Expense Categories:
                ${expensesByCategory.entries.sortedByDescending { it.value }.take(3)
                    .joinToString("\n") { "- ${it.key.displayName}: ₹${String.format("%.2f", it.value)}" }}
                
                Provide specific budgeting advice for a student. Focus on practical tips for the highest expense categories.
            """.trimIndent()
            
            val messages = listOf(
                ChatRequestSystemMessage("""
                    You are a financial advisor for students. Provide practical budgeting advice.
                    Keep it concise (3-4 sentences) and actionable. Focus on student-specific money-saving tips.
                """.trimIndent()),
                ChatRequestUserMessage(prompt)
            )
            
            val options = ChatCompletionsOptions(messages).apply {
                setModel(model)
                setMaxTokens(200)
                setTemperature(0.7)
            }
            
            val completions = client.complete(options)
            completions.choices.firstOrNull()?.message?.content ?: "Budget advice temporarily unavailable."
            
        } catch (e: Exception) {
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
        
        return """
            Student Financial Summary:
            - Total Income: ₹$totalIncome
            - Total Expenses: ₹$totalExpenses
            - Current Balance: ₹$balance
            
            Recent Transactions:
            ${recentTransactions.joinToString("\n") { 
                "- ${it.description}: ₹${it.amount} (${it.category.displayName})" 
            }}
            
            Top Spending Categories:
            ${topCategories.joinToString("\n") { 
                "- ${it.key.displayName}: ₹${String.format("%.2f", it.value)}" 
            }}
            
            Provide a brief financial insight and one actionable tip for this student.
        """.trimIndent()
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