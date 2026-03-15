package com.startup.recordservice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.startup.recordservice.ui.viewmodel.StockNotificationViewModel

/**
 * Reusable Stock Notification Subscription Button Component
 * Shows subscribe/unsubscribe button for items that are out of stock
 */
@Composable
fun StockNotificationButton(
    itemId: String,
    itemType: String,
    itemName: String,
    businessId: String,
    requestedDate: String? = null,
    modifier: Modifier = Modifier,
    viewModel: StockNotificationViewModel = hiltViewModel()
) {
    val isSubscribed by viewModel.isSubscribed.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    LaunchedEffect(itemId, itemType, requestedDate) {
        viewModel.checkSubscription(itemId, itemType, requestedDate)
    }
    
    OutlinedButton(
        onClick = {
            if (isSubscribed) {
                viewModel.unsubscribe(itemId, itemType, requestedDate)
            } else {
                viewModel.subscribe(itemId, itemType, itemName, businessId, requestedDate)
            }
        },
        enabled = !isLoading,
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                if (isSubscribed) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isSubscribed) "Subscribed" else "Notify Me When Available")
        }
    }
}

