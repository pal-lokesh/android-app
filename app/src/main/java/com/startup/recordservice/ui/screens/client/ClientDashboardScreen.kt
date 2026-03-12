package com.startup.recordservice.ui.screens.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.startup.recordservice.ui.viewmodel.ClientViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDashboardScreen(
    onLogout: () -> Unit,
    onNavigateToExplore: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onBusinessClick: (String) -> Unit = {},
    viewModel: ClientViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val businesses by viewModel.businesses.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    
    // Load data when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { /* TODO: Navigate to cart */ }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                    IconButton(onClick = { /* TODO: Navigate to notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToExplore,
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Explore") },
                    label = { Text("Explore") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToOrders,
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders") },
                    label = { Text("Orders") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* TODO: Navigate to profile */ },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.refresh() },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    ) { padding ->
        when (uiState) {
            is com.startup.recordservice.ui.viewmodel.ClientUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is com.startup.recordservice.ui.viewmodel.ClientUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (uiState as com.startup.recordservice.ui.viewmodel.ClientUiState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Retry")
                    }
                }
            }
            is com.startup.recordservice.ui.viewmodel.ClientUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Welcome!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    // Orders Section
                    if (orders.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recent Orders (${orders.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                            )
                        }
                        items(orders.take(5)) { order ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Order #${order.orderId?.toString()?.take(8) ?: "N/A"}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = when (order.status?.uppercase()) {
                                                "PENDING" -> MaterialTheme.colorScheme.tertiaryContainer
                                                "CONFIRMED" -> MaterialTheme.colorScheme.primaryContainer
                                                "DELIVERED" -> MaterialTheme.colorScheme.secondaryContainer
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Text(
                                                text = order.status ?: "UNKNOWN",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Date: ${order.orderDate ?: "N/A"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Total: ₹${String.format("%.2f", order.totalAmount ?: 0.0)}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Text(
                                        text = "${order.items?.size ?: 0} item(s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    // Businesses Section
                    item {
                        Text(
                            text = "Available Businesses (${businesses.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    
                    if (businesses.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No businesses available",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(businesses) { business ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val id = business.businessId
                                        if (!id.isNullOrBlank()) {
                                            try {
                                                onBusinessClick(id)
                                            } catch (e: Exception) {
                                                android.util.Log.e("ClientDashboard", "Error navigating to business: ${e.message}", e)
                                            }
                                        } else {
                                            android.util.Log.w("ClientDashboard", "Business ID is null or blank")
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = business.businessName ?: "Unknown Business",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!business.category.isNullOrEmpty()) {
                                        Text(
                                            text = business.category ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                    if (!business.description.isNullOrEmpty()) {
                                        Text(
                                            text = business.description ?: "",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                    if (!business.address.isNullOrEmpty()) {
                                        Row(
                                            modifier = Modifier.padding(top = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = business.address ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is com.startup.recordservice.ui.viewmodel.ClientUiState.Idle -> {
                // Show loading while initializing
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
