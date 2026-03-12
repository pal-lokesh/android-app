package com.startup.recordservice.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.startup.recordservice.data.model.OrderItemResponse
import com.startup.recordservice.data.model.OrderResponse
import com.startup.recordservice.ui.viewmodel.ClientOrdersUiState
import com.startup.recordservice.ui.viewmodel.ClientOrdersViewModel
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: String,
    onNavigateBack: () -> Unit,
    viewModel: ClientOrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    var showCancelConfirm by remember { mutableStateOf(false) }
    var ratingStars by remember { mutableStateOf(0) }
    var ratingComment by remember { mutableStateOf("") }
    var ratingError by remember { mutableStateOf<String?>(null) }
    var ratingSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Ensure orders are loaded (web refreshes often; we do the same)
        if (orders.isEmpty()) viewModel.loadOrders()
    }

    val order = orders.firstOrNull { it.orderId?.toString() == orderId }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState is ClientOrdersUiState.Loading && order == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            uiState is ClientOrdersUiState.Error && order == null -> {
                val msg = (uiState as ClientOrdersUiState.Error).message
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(msg, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                }
            }

            order == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Order not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            else -> {
                val status = (order.status ?: "UNKNOWN").uppercase()
                val canCancel = status != "READY" && status != "DELIVERED" && status != "CANCELLED"
                val canRate = status == "DELIVERED"
                val businessIdForRating = order.items?.firstOrNull()?.businessId

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Text(
                            text = order.items?.firstOrNull()?.businessName ?: "Vendor",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Order Date: ${order.orderDate ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Status: $status",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        OrderStatusTracker(status = status, orderDate = order.orderDate)
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Delivery Address", fontWeight = FontWeight.Bold)
                                Text(order.deliveryAddress ?: "N/A")
                                Text("Delivery Date", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                                Text(order.deliveryDate ?: "N/A")
                                if (!order.specialNotes.isNullOrBlank()) {
                                    Text("Special Notes", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                                    Text(order.specialNotes ?: "")
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Total: ₹${String.format("%.2f", order.totalAmount ?: 0.0)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Items (${order.items?.size ?: 0})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(order.items.orEmpty(), key = { it.itemId ?: it.hashCode().toString() }) { item ->
                        OrderItemCard(item)
                    }

                    if (canCancel) {
                        item {
                            Button(
                                onClick = { showCancelConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Cancel Order")
                            }
                        }
                    }

                    if (canRate && businessIdForRating != null) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Rate this Order",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (1..5).forEach { starIndex ->
                                    IconButton(onClick = { ratingStars = starIndex }) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Star $starIndex",
                                            tint = if (ratingStars >= starIndex)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = ratingComment,
                                onValueChange = { ratingComment = it },
                                label = { Text("Comment (optional)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                            if (ratingError != null) {
                                Text(ratingError!!, color = MaterialTheme.colorScheme.error)
                            }
                            Button(
                                onClick = {
                                    if (ratingStars <= 0) {
                                        ratingError = "Please select a rating"
                                        return@Button
                                    }
                                    ratingSubmitting = true
                                    ratingError = null
                                    viewModel.submitRating(
                                        businessId = businessIdForRating,
                                        stars = ratingStars,
                                        comment = ratingComment.trim().ifBlank { null }
                                    ) { result ->
                                        ratingSubmitting = false
                                        result.onSuccess {
                                            ratingError = null
                                        }.onFailure { e ->
                                            ratingError = e.message ?: "Failed to submit rating"
                                        }
                                    }
                                },
                                enabled = !ratingSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            ) {
                                Text(if (ratingSubmitting) "Submitting…" else "Submit Rating")
                            }
                        }
                    }
                }

                if (showCancelConfirm) {
                    AlertDialog(
                        onDismissRequest = { showCancelConfirm = false },
                        title = { Text("Cancel order?") },
                        text = { Text("Are you sure you want to cancel this order? This action cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showCancelConfirm = false
                                    viewModel.cancelOrder(orderId) { /* refresh happens inside */ }
                                }
                            ) { Text("Yes, Cancel") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCancelConfirm = false }) { Text("No") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderItemCard(item: OrderItemResponse) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.itemName ?: "Item", fontWeight = FontWeight.Bold)
            Text("${item.itemType ?: ""} • Qty: ${item.quantity}")
            if (!item.bookingDate.isNullOrBlank()) {
                Text("Booking Date: ${item.bookingDate}", color = MaterialTheme.colorScheme.primary)
            }
            Text("Unit Price: ₹${String.format("%.2f", item.price)}")
            Text("Total: ₹${String.format("%.2f", item.price * item.quantity)}", fontWeight = FontWeight.Bold)

            // Plates: selectedDishes is a JSON string; web parses & shows dish list
            val selected = item.selectedDishes
            val dishLines = remember(selected) { parseSelectedDishes(selected) }
            if (dishLines.isNotEmpty()) {
                Divider(modifier = Modifier.padding(top = 6.dp))
                Text("Selected Dishes:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                dishLines.forEach { line ->
                    Text("• $line", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

private fun parseSelectedDishes(selectedDishesJson: String?): List<String> {
    if (selectedDishesJson.isNullOrBlank()) return emptyList()
    return try {
        val arr = JSONArray(selectedDishesJson)
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val name = obj.optString("dishName")
                val qty = obj.optInt("quantity", 0)
                val price = obj.optDouble("dishPrice", 0.0)
                add("$name (Qty: $qty) - ₹${String.format("%.2f", price * qty)}")
            }
        }
    } catch (_: Exception) {
        emptyList()
    }
}

@Composable
private fun OrderStatusTracker(
    status: String,
    orderDate: String?
) {
    val steps = listOf("PENDING", "CONFIRMED", "PREPARING", "READY", "DELIVERED")

    val completedCount = when (status) {
        "PENDING" -> 1
        "CONFIRMED" -> 2
        "PREPARING" -> 3
        "READY" -> 4
        "DELIVERED" -> 5
        "CANCELLED" -> 0
        else -> 0
    }
    val progress = if (status == "CANCELLED") 0f else (completedCount.toFloat() / steps.size.toFloat()) * 100f

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Order Timeline", fontWeight = FontWeight.Bold)
            LinearProgressIndicator(progress = progress / 100f)
            Text("${progress.toInt()}% Complete", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!orderDate.isNullOrBlank()) {
                Text("Order placed on $orderDate", style = MaterialTheme.typography.bodySmall)
            }

            steps.forEachIndexed { idx, s ->
                val completed = idx < completedCount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s, fontWeight = if (completed) FontWeight.Bold else FontWeight.Normal)
                    if (status == "CANCELLED") {
                        Text("Cancelled", color = MaterialTheme.colorScheme.error)
                    } else if (completed) {
                        Text("✓", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

