package com.android.fad.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernFinancialCard(
    title: String,
    amount: Double,
    previousAmount: Double = 0.0,
    icon: ImageVector,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val animatedAmount by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "amount_animation"
    )
    
    val change = amount - previousAmount
    val changePercentage = if (previousAmount != 0.0) {
        ((change / previousAmount) * 100).toInt()
    } else 0
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = colors
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "₹${String.format("%.2f", animatedAmount)}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
                
                if (previousAmount != 0.0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (change >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = if (change >= 0) "Increase" else "Decrease",
                            tint = if (change >= 0) Color.Green.copy(alpha = 0.8f) else Color.Red.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = "${if (change >= 0) "+" else ""}${changePercentage}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "vs last period",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}