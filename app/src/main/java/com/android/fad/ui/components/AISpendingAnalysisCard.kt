package com.android.fad.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.fad.data.TransactionCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISpendingAnalysisCard(
    analysis: String?,
    isLoading: Boolean,
    topCategories: List<Pair<TransactionCategory, Double>>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "AI Analysis",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Spending Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Analysis",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analyzing spending patterns...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Top spending categories
                if (topCategories.isNotEmpty()) {
                    Text(
                        text = "Top Spending Categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    topCategories.take(3).forEach { (category, amount) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            category.color,
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Text(
                                text = "₹${String.format("%.0f", amount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // AI Analysis Text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Text(
                        text = analysis ?: "No analysis available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}