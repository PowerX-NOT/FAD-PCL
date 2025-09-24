package com.android.fad.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.fad.data.Transaction
import com.android.fad.data.TransactionCategory
import com.android.fad.data.TransactionType

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    showDetailedInfo: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(transaction.category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(transaction.category),
                    contentDescription = transaction.category.displayName,
                    tint = transaction.category.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Transaction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                if (showDetailedInfo) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = transaction.category.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${transaction.getFormattedDate()} • ${transaction.getFormattedTime()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Amount
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}₹${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == TransactionType.INCOME) 
                        Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                
                if (showDetailedInfo) {
                    Text(
                        text = transaction.getFormattedTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun getCategoryIcon(category: TransactionCategory): ImageVector {
    return when (category) {
        TransactionCategory.SALARY -> Icons.Default.Work
        TransactionCategory.FREELANCE -> Icons.Default.Computer
        TransactionCategory.INVESTMENT -> Icons.Default.TrendingUp
        TransactionCategory.OTHER_INCOME -> Icons.Default.AccountBalance
        TransactionCategory.FOOD -> Icons.Default.Restaurant
        TransactionCategory.TRANSPORT -> Icons.Default.DirectionsBus
        TransactionCategory.ENTERTAINMENT -> Icons.Default.Movie
        TransactionCategory.SHOPPING -> Icons.Default.ShoppingBag
        TransactionCategory.BILLS -> Icons.Default.Receipt
        TransactionCategory.EDUCATION -> Icons.Default.School
        TransactionCategory.HEALTH -> Icons.Default.LocalHospital
        TransactionCategory.OTHER_EXPENSE -> Icons.Default.Category
    }
}