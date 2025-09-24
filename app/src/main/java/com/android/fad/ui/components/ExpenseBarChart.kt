package com.android.fad.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.fad.data.TransactionCategory
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseBarChart(
    expensesByCategory: Map<TransactionCategory, Double>,
    modifier: Modifier = Modifier
) {
    val sortedExpenses = expensesByCategory.entries
        .filter { it.value > 0 }
        .sortedByDescending { it.value }
        .take(6) // Show top 6 categories
    
    if (sortedExpenses.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No expense data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Expense Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val maxAmount = sortedExpenses.maxOfOrNull { it.value } ?: 1.0
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val barWidth = size.width / sortedExpenses.size * 0.7f
                val spacing = size.width / sortedExpenses.size * 0.3f
                val maxBarHeight = size.height * 0.8f
                
                sortedExpenses.forEachIndexed { index, (category, amount) ->
                    val barHeight = (amount / maxAmount * maxBarHeight).toFloat()
                    val x = index * (barWidth + spacing) + spacing / 2
                    val y = size.height - barHeight
                    
                    // Draw bar with gradient effect
                    drawRoundedBar(
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        color = category.color,
                        cornerRadius = 8.dp.toPx()
                    )
                    
                    // Draw amount text on top of bar
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 12.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                        drawText(
                            "₹${amount.toInt()}",
                            x + barWidth / 2,
                            y - 8.dp.toPx(),
                            paint
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sortedExpenses.size) { index ->
                    val (category, amount) = sortedExpenses[index]
                    LegendItem(
                        category = category,
                        amount = amount
                    )
                }
            }
        }
    }
}

@Composable
private fun LazyRow(
    horizontalArrangement: Arrangement.Horizontal,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Row(
        horizontalArrangement = horizontalArrangement,
        modifier = modifier
    ) {
        content()
    }
}

@Composable
private fun LegendItem(
    category: TransactionCategory,
    amount: Double
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(category.color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.displayName.split(" ").first(),
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontSize = 10.sp
        )
        Text(
            text = "₹${amount.toInt()}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun DrawScope.drawRoundedBar(
    topLeft: Offset,
    size: Size,
    color: Color,
    cornerRadius: Float
) {
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = size,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
    )
    
    // Add subtle gradient effect
    drawRoundRect(
        color = color.copy(alpha = 0.7f),
        topLeft = topLeft,
        size = Size(size.width, size.height * 0.3f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
    )
}

private fun items(size: Int, content: @Composable (Int) -> Unit) {
    repeat(size) { index ->
        content(index)
    }
}