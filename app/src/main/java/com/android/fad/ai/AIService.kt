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
            println("Testing AI connection with GitHub Models...")
            val messages = listOf(
                ChatRequestSystemMessage("You are a helpful assistant."),
                ChatRequestUserMessage("Respond with exactly: 'AI_CONNECTION_SUCCESS'")
            )
            
            val options = ChatCompletionsOptions(messages)
            options.model = model
            options.maxTokens = 50
            options.temperature = 0.1
            
            val completions = client.complete(options)
            val response = completions.choices.firstOrNull()?.message?.content ?: "No response"
            println("AI Connection Test Response: '$response'")
            response
            
        } catch (e: Exception) {
            println("AI Connection Test Error: ${e.message}")
            e.printStackTrace()
            throw e // Re-throw to handle in calling code
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
            // Enhanced AI prompt with better context and examples
            val prompt = """
                You are an expert financial AI that categorizes transactions with 95% accuracy.
                
                Transaction Details:
                Description: "$description"
                Amount: ₹$amount
                
                Analyze the context, keywords, and amount to determine the MOST appropriate category:
                
                FOOD: Restaurants, cafes, groceries, food delivery, dining, meals, snacks, beverages, cooking ingredients
                Examples: "lunch at McDonald's", "grocery shopping", "coffee with friends", "food delivery"
                
                TRANSPORT: Public transport, taxis, ride-sharing, fuel, parking, vehicle maintenance, travel
                Examples: "uber ride", "bus ticket", "petrol", "parking fee", "metro card recharge"
                
                ENTERTAINMENT: Movies, games, streaming, concerts, sports, hobbies, recreational activities
                Examples: "Netflix subscription", "movie tickets", "gaming", "concert", "sports event"
                
                SHOPPING: Clothing, electronics, personal items, online purchases, retail shopping
                Examples: "Amazon purchase", "new shirt", "phone accessories", "online shopping"
                
                BILLS: Utilities, rent, phone bills, internet, insurance, recurring payments
                Examples: "electricity bill", "phone recharge", "internet payment", "rent"
                
                EDUCATION: Books, courses, tuition, school supplies, educational materials, learning
                Examples: "textbooks", "course fee", "online course", "school supplies", "tuition"
                
                HEALTH: Medical expenses, medicines, doctor visits, health insurance, fitness
                Examples: "doctor visit", "medicines", "gym membership", "health checkup"
                
                OTHER_EXPENSE: Miscellaneous expenses that don't fit other categories
                
                Consider:
                1. Primary keywords in description
                2. Context clues and common usage patterns
                3. Amount reasonableness for category
                4. Common merchant names and services
                
                Respond with ONLY the exact category name: FOOD, TRANSPORT, ENTERTAINMENT, SHOPPING, BILLS, EDUCATION, HEALTH, or OTHER_EXPENSE
            """.trimIndent()
            
            val messages = listOf(
                ChatRequestSystemMessage("You are an expert financial AI specializing in transaction categorization. You have been trained on millions of transactions and achieve 95% accuracy. Always respond with only the category name, nothing else."),
                ChatRequestUserMessage(prompt)
            )
            
            val options = ChatCompletionsOptions(messages)
            options.model = model
            options.maxTokens = 20
            options.temperature = 0.1 // Low temperature for consistent categorization
            
            val completions = client.complete(options)
            val response = completions.choices.firstOrNull()?.message?.content?.trim()?.uppercase()
            
            println("AI Category Response: '$response' for description: '$description'")
            
            // Map AI response to our categories with better matching
            when (response) {
                "FOOD", "FOOD_DINING" -> TransactionCategory.FOOD
                "TRANSPORT", "TRANSPORTATION" -> TransactionCategory.TRANSPORT
                "ENTERTAINMENT", "RECREATION" -> TransactionCategory.ENTERTAINMENT
                "SHOPPING", "RETAIL" -> TransactionCategory.SHOPPING
                "BILLS", "UTILITIES" -> TransactionCategory.BILLS
                "EDUCATION", "LEARNING" -> TransactionCategory.EDUCATION
                "HEALTH", "MEDICAL", "HEALTHCARE" -> TransactionCategory.HEALTH
                "OTHER_EXPENSE", "OTHER", "MISCELLANEOUS" -> TransactionCategory.OTHER_EXPENSE
                else -> {
                    println("Unexpected AI response: '$response', using enhanced fallback")
                    suggestCategoryFallback(description, amount)
                }
            }
            
        } catch (e: Exception) {
            // Log the actual error for debugging
            println("AI Category Suggestion Error for '$description': ${e.message}")
            e.printStackTrace()
            // Fallback to simple keyword matching
            suggestCategoryFallback(description, amount)
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
    
    private fun suggestCategoryFallback(description: String, amount: Double = 0.0): TransactionCategory {
        val desc = description.lowercase()
        println("Using enhanced fallback categorization for: '$description' (₹$amount)")
        
        // Enhanced keyword matching with more comprehensive patterns
        return when {
            // Food related keywords - expanded with Indian context
            desc.contains("food") || desc.contains("restaurant") || desc.contains("cafe") ||
            desc.contains("lunch") || desc.contains("dinner") || desc.contains("breakfast") ||
            desc.contains("snack") || desc.contains("grocery") || desc.contains("meal") ||
            desc.contains("pizza") || desc.contains("burger") || desc.contains("coffee") ||
            desc.contains("tea") || desc.contains("canteen") || desc.contains("cafeteria") ||
            desc.contains("zomato") || desc.contains("swiggy") || desc.contains("dominos") ||
            desc.contains("mcdonald") || desc.contains("kfc") || desc.contains("subway") ||
            desc.contains("starbucks") || desc.contains("chai") || desc.contains("dosa") ||
            desc.contains("biryani") || desc.contains("thali") || desc.contains("samosa") ||
            desc.contains("delivery") && (desc.contains("food") || amount < 500) ||
            desc.contains("dining") || desc.contains("eat") || desc.contains("drink") ->
                TransactionCategory.FOOD
                
            // Transport related keywords - expanded with Indian context
            desc.contains("bus") || desc.contains("train") || desc.contains("uber") ||
            desc.contains("taxi") || desc.contains("transport") || desc.contains("fuel") ||
            desc.contains("petrol") || desc.contains("diesel") || desc.contains("parking") ||
            desc.contains("metro") || desc.contains("auto") || desc.contains("rickshaw") ||
            desc.contains("cab") || desc.contains("ola") || desc.contains("rapido") ||
            desc.contains("bike") && desc.contains("ride") || desc.contains("travel") ||
            desc.contains("flight") || desc.contains("train") || desc.contains("railway") ||
            desc.contains("irctc") || desc.contains("makemytrip") || desc.contains("goibibo") ||
            desc.contains("redbus") || desc.contains("toll") || desc.contains("highway") ->
                TransactionCategory.TRANSPORT
                
            // Entertainment related keywords - expanded
            desc.contains("movie") || desc.contains("game") || desc.contains("entertainment") ||
            desc.contains("cinema") || desc.contains("theater") || desc.contains("concert") ||
            desc.contains("netflix") || desc.contains("spotify") || desc.contains("youtube") ||
            desc.contains("gaming") || desc.contains("party") || desc.contains("club") ||
            desc.contains("hotstar") || desc.contains("prime") || desc.contains("subscription") ||
            desc.contains("music") || desc.contains("stream") || desc.contains("fun") ||
            desc.contains("recreation") || desc.contains("hobby") || desc.contains("sports") ||
            desc.contains("gym") && !desc.contains("membership") ->
                TransactionCategory.ENTERTAINMENT
                
            // Education related keywords - expanded
            desc.contains("book") || desc.contains("course") || desc.contains("education") ||
            desc.contains("tuition") || desc.contains("school") || desc.contains("college") ||
            desc.contains("university") || desc.contains("study") || desc.contains("exam") ||
            desc.contains("textbook") || desc.contains("notebook") || desc.contains("pen") ||
            desc.contains("pencil") || desc.contains("stationery") || desc.contains("fees") ||
            desc.contains("admission") || desc.contains("library") || desc.contains("research") ||
            desc.contains("thesis") || desc.contains("project") && desc.contains("academic") ||
            desc.contains("udemy") || desc.contains("coursera") || desc.contains("byju") ||
            desc.contains("unacademy") || desc.contains("learning") ->
                TransactionCategory.EDUCATION
                
            // Health related keywords - expanded
            desc.contains("medicine") || desc.contains("doctor") || desc.contains("health") ||
            desc.contains("hospital") || desc.contains("pharmacy") || desc.contains("medical") ||
            desc.contains("clinic") || desc.contains("checkup") || desc.contains("treatment") ||
            desc.contains("tablet") || desc.contains("syrup") || desc.contains("injection") ||
            desc.contains("apollo") || desc.contains("fortis") || desc.contains("max") ||
            desc.contains("dental") || desc.contains("eye") || desc.contains("surgery") ||
            desc.contains("physiotherapy") || desc.contains("lab") || desc.contains("test") ||
            desc.contains("scan") || desc.contains("xray") || desc.contains("fitness") ||
            desc.contains("gym") && desc.contains("membership") ->
                TransactionCategory.HEALTH
                
            // Bills related keywords - expanded with Indian context
            desc.contains("electricity") || desc.contains("water") || desc.contains("bill") ||
            desc.contains("phone") || desc.contains("internet") || desc.contains("wifi") ||
            desc.contains("rent") || desc.contains("utility") || desc.contains("recharge") ||
            desc.contains("mobile") || desc.contains("broadband") || desc.contains("airtel") ||
            desc.contains("jio") || desc.contains("vodafone") || desc.contains("bsnl") ||
            desc.contains("tata") && desc.contains("sky") || desc.contains("dish") ||
            desc.contains("insurance") || desc.contains("emi") || desc.contains("loan") ||
            desc.contains("credit") && desc.contains("card") || desc.contains("maintenance") ->
                TransactionCategory.BILLS
                
            // Shopping related keywords - expanded with Indian context
            desc.contains("shopping") || desc.contains("clothes") || desc.contains("shirt") ||
            desc.contains("shoes") || desc.contains("electronics") || desc.contains("amazon") ||
            desc.contains("flipkart") || desc.contains("online") || desc.contains("purchase") ||
            desc.contains("buy") || desc.contains("store") || desc.contains("mall") ||
            desc.contains("myntra") || desc.contains("ajio") || desc.contains("nykaa") ||
            desc.contains("meesho") || desc.contains("paytm") && desc.contains("mall") ||
            desc.contains("clothing") || desc.contains("fashion") || desc.contains("accessories") ||
            desc.contains("gadget") || desc.contains("mobile") && !desc.contains("recharge") ||
            desc.contains("laptop") || desc.contains("headphone") || desc.contains("watch") ||
            // Amount-based heuristics for shopping
            (amount > 500 && (desc.contains("purchase") || desc.contains("order"))) ->
                TransactionCategory.SHOPPING
                
            // Amount-based categorization for ambiguous cases
            amount < 50 && (desc.contains("small") || desc.contains("quick")) -> TransactionCategory.FOOD
            amount > 5000 && desc.contains("payment") -> TransactionCategory.BILLS
            amount > 1000 && desc.contains("service") -> TransactionCategory.OTHER_EXPENSE
            
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