package com.android.fad.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SmartNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val priority: NotificationPriority
)

enum class NotificationType(val icon: ImageVector, val color: Color) {
    BUDGET_WARNING(Icons.Default.Warning, Color(0xFFFF9800)),
    SPENDING_ALERT(Icons.Default.TrendingUp, Color(0xFFF44336)),
    SAVINGS_TIP(Icons.Default.Lightbulb, Color(0xFF4CAF50)),
    BILL_REMINDER(Icons.Default.Schedule, Color(0xFF2196F3))
}

enum class NotificationPriority {
    HIGH, MEDIUM, LOW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartNotificationCard(
    notification: SmartNotification,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = notification.type.color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.priority == NotificationPriority.HIGH) 6.dp else 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = notification.type.icon,
                contentDescription = notification.type.name,
                tint = notification.type.color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = notification.type.color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            IconButton(
                onClick = { onDismiss(notification.id) }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SmartNotificationsList(
    notifications: List<SmartNotification>,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (notifications.isNotEmpty()) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Smart Alerts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            notifications.forEach { notification ->
                SmartNotificationCard(
                    notification = notification,
                    onDismiss = onDismiss
                )
            }
        }
    }
}