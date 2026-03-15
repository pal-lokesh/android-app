package com.startup.recordservice.ui.screens.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.startup.recordservice.data.model.ClientNotificationResponse
import com.startup.recordservice.ui.viewmodel.ClientNotificationsViewModel
import com.startup.recordservice.ui.viewmodel.ClientNotificationsUiState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientNotificationsScreen(
    onNavigateBack: () -> Unit,
    onNotificationClick: (ClientNotificationResponse) -> Unit = {},
    viewModel: ClientNotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications")
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text(unreadCount.toString())
                                    }
                                }
                            ) {
                                // Empty - badge is decorative
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text("Mark All Read")
                        }
                    }
                    IconButton(onClick = { viewModel.loadNotifications() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is ClientNotificationsUiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ClientNotificationsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ClientNotificationsUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (uiState as ClientNotificationsUiState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadNotifications() }) {
                        Text("Retry")
                    }
                }
            }
            is ClientNotificationsUiState.Success -> {
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No notifications",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "You'll see order updates and stock alerts here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications, key = { it.notificationId }) { notification ->
                            NotificationCard(
                                notification = notification,
                                onClick = {
                                    if (!notification.isRead) {
                                        viewModel.markAsRead(notification.notificationId)
                                    }
                                    onNotificationClick(notification)
                                }
                            )
                        }
                    }
                }
            }
            is ClientNotificationsUiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    notification: ClientNotificationResponse,
    onClick: () -> Unit
) {
    val backgroundColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon based on notification type
            val icon = when (notification.notificationType?.uppercase()) {
                "ORDER_CONFIRMED", "ORDER_STATUS_CHANGED" -> Icons.Default.ShoppingCart
                "STOCK_AVAILABLE" -> Icons.Default.Inventory
                "ORDER_DELIVERED" -> Icons.Default.LocalShipping
                else -> Icons.Default.Notifications
            }
            
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.businessName ?: "Business",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (!notification.isRead) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text("New")
                                }
                            }
                        ) {
                            // Empty - badge is decorative
                        }
                    }
                }
                
                notification.message?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                notification.notificationType?.let { type ->
                    Text(
                        text = formatNotificationType(type),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                notification.createdAt?.let { date ->
                    Text(
                        text = formatNotificationDate(date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun formatNotificationType(type: String): String {
    return when (type.uppercase()) {
        "ORDER_CONFIRMED" -> "Order Confirmed"
        "ORDER_STATUS_CHANGED" -> "Order Update"
        "STOCK_AVAILABLE" -> "Stock Available"
        "ORDER_DELIVERED" -> "Order Delivered"
        else -> type.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
    }
}

private fun formatNotificationDate(dateString: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val dateTime = LocalDateTime.parse(dateString, formatter)
        val now = LocalDateTime.now()
        val diff = java.time.Duration.between(dateTime, now)
        
        when {
            diff.toMinutes() < 1 -> "Just now"
            diff.toMinutes() < 60 -> "${diff.toMinutes()}m ago"
            diff.toHours() < 24 -> "${diff.toHours()}h ago"
            diff.toDays() < 7 -> "${diff.toDays()}d ago"
            else -> {
                val displayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                dateTime.format(displayFormatter)
            }
        }
    } catch (e: Exception) {
        dateString
    }
}
