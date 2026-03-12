package com.startup.recordservice.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.startup.recordservice.data.model.OrderResponse
import com.startup.recordservice.ui.viewmodel.ClientOrdersUiState
import com.startup.recordservice.ui.viewmodel.ClientOrdersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientOrderHistoryScreen(
    onNavigateBack: () -> Unit,
    onViewDetails: (String) -> Unit,
    viewModel: ClientOrdersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Order History") },
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
        when (uiState) {
            is ClientOrdersUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is ClientOrdersUiState.Error -> {
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

            else -> {
                if (orders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No orders yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(orders, key = { it.orderId ?: it.hashCode().toLong() }) { order ->
                            OrderCard(
                                order = order,
                                onViewDetails = {
                                    val id = order.orderId?.toString() ?: return@OrderCard
                                    onViewDetails(id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderResponse,
    onViewDetails: () -> Unit
) {
    val status = (order.status ?: "UNKNOWN").uppercase()
    val badgeColor = when (status) {
        "PENDING" -> MaterialTheme.colorScheme.tertiaryContainer
        "CONFIRMED" -> MaterialTheme.colorScheme.primaryContainer
        "PREPARING" -> MaterialTheme.colorScheme.primaryContainer
        "READY" -> MaterialTheme.colorScheme.secondaryContainer
        "DELIVERED" -> MaterialTheme.colorScheme.secondaryContainer
        "CANCELLED" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val vendorName = order.items?.firstOrNull()?.businessName ?: "Vendor"
                    Text(vendorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Order Date: ${order.orderDate ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(color = badgeColor, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ₹${String.format("%.2f", order.totalAmount ?: 0.0)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${order.items?.size ?: 0} item(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("View Details")
            }
        }
    }
}

