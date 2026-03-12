package com.startup.recordservice.ui.screens.vendor

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun VendorBottomNav(
    currentTab: VendorTab,
    onDashboardClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onThemeTabClick: () -> Unit,
    onInventoryTabClick: () -> Unit,
    onAvailabilityClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentTab == VendorTab.DASHBOARD,
            onClick = onDashboardClick,
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Dashboard") }
        )
        NavigationBarItem(
            selected = currentTab == VendorTab.ORDERS,
            onClick = onOrdersClick,
            icon = { Icon(Icons.Default.Receipt, contentDescription = "Orders") },
            label = { Text("Orders") }
        )
        NavigationBarItem(
            selected = currentTab == VendorTab.THEME,
            onClick = onThemeTabClick,
            icon = { Icon(Icons.Default.ColorLens, contentDescription = "Themes") },
            label = { Text("Themes") }
        )
        NavigationBarItem(
            selected = currentTab == VendorTab.INVENTORY,
            onClick = onInventoryTabClick,
            icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventory") },
            label = { Text("Inventory") }
        )
        NavigationBarItem(
            selected = currentTab == VendorTab.AVAILABILITY,
            onClick = onAvailabilityClick,
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Availability") },
            label = { Text("Availability") }
        )
    }
}

