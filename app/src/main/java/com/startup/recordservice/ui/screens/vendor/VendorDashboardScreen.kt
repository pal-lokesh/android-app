package com.startup.recordservice.ui.screens.vendor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import coil.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.startup.recordservice.data.model.InventoryCreateRequest
import com.startup.recordservice.data.model.ThemeRequest
import com.startup.recordservice.data.model.InventoryResponse
import com.startup.recordservice.data.model.ThemeResponse
import com.startup.recordservice.data.util.UrlResolver
import com.startup.recordservice.ui.viewmodel.VendorViewModel

enum class VendorTab {
    DASHBOARD,
    ORDERS,
    THEME,
    INVENTORY,
    AVAILABILITY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    onLogout: () -> Unit,
    viewModel: VendorViewModel = hiltViewModel(),
    onCreateBusinessClick: () -> Unit = {},
    onAddInventoryClick: () -> Unit = {},
    onAddThemeClick: () -> Unit = {},
    onNotificationsClick: (String) -> Unit = {},
    onDashboardClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onInventoryTabClick: () -> Unit = {},
    onThemeTabClick: () -> Unit = {},
    onAvailabilityClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBusinessClick: (String) -> Unit = {},
    currentTab: VendorTab = VendorTab.DASHBOARD
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val businesses by viewModel.businesses.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val inventoryByBusiness by viewModel.inventoryByBusiness.collectAsStateWithLifecycle()
    val themesByBusiness by viewModel.themesByBusiness.collectAsStateWithLifecycle()
    val themeImageUrls by viewModel.themeImageUrls.collectAsStateWithLifecycle()
    val inventoryImageUrls by viewModel.inventoryImageUrls.collectAsStateWithLifecycle()

    var statusDialogOrder by remember { mutableStateOf<com.startup.recordservice.data.model.OrderResponse?>(null) }
    var statusDialogStatus by remember { mutableStateOf("PENDING") }
    var statusDialogError by remember { mutableStateOf<String?>(null) }
    var statusDialogLoading by remember { mutableStateOf(false) }

    var showInvDialog by remember { mutableStateOf(false) }
    var invDialogMode by remember { mutableStateOf("VIEW") } // VIEW | EDIT | DELETE
    var selectedInv by remember { mutableStateOf<InventoryResponse?>(null) }

    var showThemeDialog by remember { mutableStateOf(false) }
    var themeDialogMode by remember { mutableStateOf("VIEW") } // VIEW | EDIT | DELETE
    var selectedTheme by remember { mutableStateOf<ThemeResponse?>(null) }

    var showBusinessDialog by remember { mutableStateOf(false) }
    var businessDialogMode by remember { mutableStateOf("VIEW") } // VIEW | EDIT | DELETE
    var selectedBusiness by remember { mutableStateOf<com.startup.recordservice.data.model.BusinessResponse?>(null) }
    
    // Load data when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Vendor Dashboard") },
                actions = {
                    // Show notifications for first business (can be enhanced to show all or selected)
                    businesses.firstOrNull()?.businessId?.let { businessId ->
                        IconButton(onClick = { onNotificationsClick(businessId) }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }
                    IconButton(onClick = onAvailabilityClick) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Availability")
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        },
        bottomBar = {
            VendorBottomNav(
                currentTab = currentTab,
                onDashboardClick = onDashboardClick,
                onOrdersClick = onOrdersClick,
                onThemeTabClick = onThemeTabClick,
                onInventoryTabClick = onInventoryTabClick,
                onAvailabilityClick = onAvailabilityClick
            )
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
        // Image pickers for editing
        val inventoryImagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents()
        ) { uris ->
            val invId = selectedInv?.inventoryId.orEmpty()
            if (invId.isNotBlank() && uris.isNotEmpty()) {
                viewModel.updateInventoryImages(invId, uris)
            }
        }

        val themeImagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents()
        ) { uris ->
            val themeId = selectedTheme?.themeId.orEmpty()
            if (themeId.isNotBlank() && uris.isNotEmpty()) {
                viewModel.updateThemeImages(themeId, uris)
            }
        }

        // Inventory dialog
        if (showInvDialog && selectedInv != null) {
            val inv = selectedInv!!
            when (invDialogMode) {
                "DELETE" -> {
                    AlertDialog(
                        onDismissRequest = { showInvDialog = false },
                        title = { Text("Delete Inventory Item") },
                        text = { Text("Are you sure you want to delete '${inv.itemName ?: "this item"}'?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val invId = inv.inventoryId.orEmpty()
                                    if (invId.isNotBlank()) {
                                        viewModel.deleteInventory(invId) { showInvDialog = false }
                                    } else {
                                        showInvDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) { Text("Delete") }
                        },
                        dismissButton = { TextButton(onClick = { showInvDialog = false }) { Text("Cancel") } }
                    )
                }
                "EDIT" -> {
                    var name by remember(inv.inventoryId) { mutableStateOf(inv.itemName.orEmpty()) }
                    var desc by remember(inv.inventoryId) { mutableStateOf(inv.description.orEmpty()) }
                    var category by remember(inv.inventoryId) { mutableStateOf(inv.category.orEmpty()) }
                    var price by remember(inv.inventoryId) { mutableStateOf(inv.price.toString()) }
                    var qty by remember(inv.inventoryId) { mutableStateOf(inv.quantity.toString()) }

                    AlertDialog(
                        onDismissRequest = { showInvDialog = false },
                        title = { Text("Edit Inventory Item") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") })
                                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") })
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Change Images (optional)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                TextButton(
                                    onClick = { inventoryImagePicker.launch("image/*") }
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Select New Images")
                                }
                                if (desc.isBlank()) {
                                    Text("Description is required", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val invId = inv.inventoryId.orEmpty()
                                    val businessId = inv.businessId.orEmpty()
                                    if (invId.isBlank() || businessId.isBlank()) return@Button

                                    val request = InventoryCreateRequest(
                                        businessId = businessId,
                                        inventoryName = name,
                                        inventoryDescription = desc,
                                        inventoryCategory = category,
                                        price = price.toDoubleOrNull() ?: 0.0,
                                        quantity = qty.toIntOrNull() ?: 0,
                                        isActive = inv.isActive
                                    )
                                    viewModel.updateInventory(invId, request) { showInvDialog = false }
                                },
                                enabled = name.isNotBlank() && desc.isNotBlank() && category.isNotBlank()
                            ) { Text("Save") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showInvDialog = false }) { Text("Cancel") }
                        }
                    )
                }
                else -> {
                    AlertDialog(
                        onDismissRequest = { showInvDialog = false },
                        title = { Text(inv.itemName ?: "Inventory Item") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Category: ${inv.category.orEmpty()}")
                                Text("Price: ₹${"%.2f".format(inv.price)}")
                                Text("Quantity: ${inv.quantity}")
                                if (!inv.description.isNullOrBlank()) Text(inv.description!!)
                            }
                        },
                        confirmButton = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { invDialogMode = "EDIT" }) { Text("Edit") }
                                TextButton(onClick = { invDialogMode = "DELETE" }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                            }
                        },
                        dismissButton = { TextButton(onClick = { showInvDialog = false }) { Text("Close") } }
                    )
                }
            }
        }

        // Theme dialog
        if (showThemeDialog && selectedTheme != null) {
            val theme = selectedTheme!!
            when (themeDialogMode) {
                "DELETE" -> {
                    AlertDialog(
                        onDismissRequest = { showThemeDialog = false },
                        title = { Text("Delete Theme") },
                        text = { Text("Are you sure you want to delete '${theme.themeName ?: "this theme"}'?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val themeId = theme.themeId.orEmpty()
                                    if (themeId.isNotBlank()) {
                                        viewModel.deleteTheme(themeId) { showThemeDialog = false }
                                    } else {
                                        showThemeDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) { Text("Delete") }
                        },
                        dismissButton = { TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") } }
                    )
                }
                "EDIT" -> {
                    var name by remember(theme.themeId) { mutableStateOf(theme.themeName.orEmpty()) }
                    var desc by remember(theme.themeId) { mutableStateOf(theme.themeDescription.orEmpty()) }
                    var category by remember(theme.themeId) { mutableStateOf(theme.themeCategory.orEmpty()) }
                    var priceRange by remember(theme.themeId) { mutableStateOf(theme.priceRange.orEmpty()) }
                    var qty by remember(theme.themeId) { mutableStateOf(theme.quantity.toString()) }

                    AlertDialog(
                        onDismissRequest = { showThemeDialog = false },
                        title = { Text("Edit Theme") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                                OutlinedTextField(value = priceRange, onValueChange = { priceRange = it }, label = { Text("Price Range") })
                                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") })
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Change Images (optional)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                TextButton(
                                    onClick = { themeImagePicker.launch("image/*") }
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Select New Images")
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val themeId = theme.themeId.orEmpty()
                                    val businessId = theme.businessId.orEmpty()
                                    if (themeId.isBlank() || businessId.isBlank()) return@Button

                                    val request = ThemeRequest(
                                        businessId = businessId,
                                        themeName = name,
                                        themeDescription = desc,
                                        themeCategory = category,
                                        priceRange = priceRange,
                                        quantity = qty.toIntOrNull() ?: 0
                                    )
                                    viewModel.updateTheme(themeId, request) { showThemeDialog = false }
                                },
                                enabled = name.isNotBlank() && category.isNotBlank()
                            ) { Text("Save") }
                        },
                        dismissButton = { TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") } }
                    )
                }
                else -> {
                    AlertDialog(
                        onDismissRequest = { showThemeDialog = false },
                        title = { Text(theme.themeName ?: "Theme") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Category: ${theme.themeCategory.orEmpty()}")
                                if (!theme.priceRange.isNullOrBlank()) Text("Price: ${theme.priceRange}")
                                Text("Quantity: ${theme.quantity}")
                                if (!theme.themeDescription.isNullOrBlank()) Text(theme.themeDescription!!)
                            }
                        },
                        confirmButton = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { themeDialogMode = "EDIT" }) { Text("Edit") }
                                TextButton(onClick = { themeDialogMode = "DELETE" }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                            }
                        },
                        dismissButton = { TextButton(onClick = { showThemeDialog = false }) { Text("Close") } }
                    )
                }
            }
        }

        // Business dialog (view / edit / delete)
        if (showBusinessDialog && selectedBusiness != null) {
            val business = selectedBusiness!!
            when (businessDialogMode) {
                "DELETE" -> {
                    AlertDialog(
                        onDismissRequest = { showBusinessDialog = false },
                        title = { Text("Delete Business") },
                        text = { Text("Are you sure you want to delete '${business.businessName ?: "this business"}'? This cannot be undone.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    val id = business.businessId.orEmpty()
                                    if (id.isNotBlank()) {
                                        viewModel.deleteBusiness(id) { result ->
                                            result.onSuccess { showBusinessDialog = false }
                                            // errors already exposed via uiState
                                        }
                                    } else {
                                        showBusinessDialog = false
                                    }
                                }
                            ) { Text("Delete") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showBusinessDialog = false }) { Text("Cancel") }
                        }
                    )
                }
                "EDIT" -> {
                    var name by remember(business.businessId) { mutableStateOf(business.businessName.orEmpty()) }
                    var desc by remember(business.businessId) { mutableStateOf(business.description.orEmpty()) }
                    var category by remember(business.businessId) { mutableStateOf(business.category.orEmpty()) }
                    var address by remember(business.businessId) { mutableStateOf(business.address.orEmpty()) }
                    var phone by remember(business.businessId) { mutableStateOf(business.phoneNumber.orEmpty()) }
                    var email by remember(business.businessId) { mutableStateOf(business.email.orEmpty()) }

                    AlertDialog(
                        onDismissRequest = { showBusinessDialog = false },
                        title = { Text("Edit Business") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
                                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.updateBusiness(
                                        business = business,
                                        updatedName = name,
                                        updatedDescription = desc,
                                        updatedCategory = category,
                                        updatedAddress = address,
                                        updatedPhone = phone,
                                        updatedEmail = email
                                    ) { result ->
                                        result.onSuccess { showBusinessDialog = false }
                                        // errors handled via uiState
                                    }
                                },
                                enabled = name.isNotBlank() && email.isNotBlank()
                            ) { Text("Save") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showBusinessDialog = false }) { Text("Cancel") }
                        }
                    )
                }
                else -> {
                    AlertDialog(
                        onDismissRequest = { showBusinessDialog = false },
                        title = { Text(business.businessName ?: "Business Details") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (!business.category.isNullOrBlank()) {
                                    Text("Category: ${business.category}")
                                }
                                if (!business.description.isNullOrBlank()) {
                                    Text("Description: ${business.description}")
                                }
                                if (!business.address.isNullOrBlank()) {
                                    Text("Address: ${business.address}")
                                }
                                if (!business.phoneNumber.isNullOrBlank()) {
                                    Text("Phone: ${business.phoneNumber}")
                                }
                                if (!business.email.isNullOrBlank()) {
                                    Text("Email: ${business.email}")
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showBusinessDialog = false }) { Text("Close") }
                        }
                    )
                }
            }
        }

        when (uiState) {
            is com.startup.recordservice.ui.viewmodel.VendorUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is com.startup.recordservice.ui.viewmodel.VendorUiState.Error -> {
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
                        text = (uiState as com.startup.recordservice.ui.viewmodel.VendorUiState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Retry")
                    }
                }
            }
            is com.startup.recordservice.ui.viewmodel.VendorUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    item {
                        Text(
                            text = when (currentTab) {
                                VendorTab.DASHBOARD -> "Vendor Dashboard"
                                VendorTab.ORDERS -> "Orders"
                                VendorTab.THEME -> "Themes"
                                VendorTab.INVENTORY -> "Inventory"
                                VendorTab.AVAILABILITY -> "Availability"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // DASHBOARD tab: show businesses + summary
                    if (currentTab == VendorTab.DASHBOARD) {
                        // My Businesses Section
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "My Businesses (${businesses.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                )
                                Button(onClick = onCreateBusinessClick) {
                                    Icon(Icons.Default.AddBusiness, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Business")
                                }
                            }
                        }
                    }

                    if (currentTab == VendorTab.DASHBOARD && businesses.isEmpty()) {
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
                                        imageVector = Icons.Default.Business,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No businesses registered",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Create your first business profile to start showcasing your services.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(onClick = onCreateBusinessClick) {
                                        Icon(
                                            imageVector = Icons.Default.AddBusiness,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Create Your First Business")
                                    }
                                }
                            }
                        }
                    } else if (currentTab == VendorTab.DASHBOARD && businesses.isNotEmpty()) {
                        items(businesses) { business ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        business.businessId?.let { businessId ->
                                            onBusinessClick(businessId)
                                        }
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
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
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(onClick = {
                                                selectedBusiness = business
                                                businessDialogMode = "VIEW"
                                                showBusinessDialog = true
                                            }) {
                                                Icon(Icons.Default.Visibility, contentDescription = "View business")
                                            }
                                            IconButton(onClick = {
                                                selectedBusiness = business
                                                businessDialogMode = "EDIT"
                                                showBusinessDialog = true
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit business")
                                            }
                                            IconButton(onClick = {
                                                selectedBusiness = business
                                                businessDialogMode = "DELETE"
                                                showBusinessDialog = true
                                            }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete business")
                                            }
                                        }
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

                    // ORDERS tab: show only orders
                    if (currentTab == VendorTab.ORDERS && orders.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recent Orders (${orders.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(orders) { order ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        statusDialogOrder = order
                                        statusDialogStatus = (order.status ?: "PENDING").uppercase()
                                        statusDialogError = null
                                    }
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
                    } else if (currentTab == VendorTab.ORDERS && orders.isEmpty()) {
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
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No orders yet",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // THEME tab: show only theme management
                    if (currentTab == VendorTab.THEME) {
                        item {
                            Text(
                                text = "Theme Management",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }

                        item {
                            Button(
                                onClick = onAddThemeClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Add Theme")
                            }
                        }

                        if (businesses.isEmpty()) {
                            item {
                                Text(
                                    text = "Create a business first to manage themes.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            businesses.forEach { business ->
                                val businessId = business.businessId ?: return@forEach
                                val themeItems = themesByBusiness[businessId].orEmpty()

                                item {
                                    Text(
                                        text = business.businessName ?: "Business",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }

                                if (themeItems.isEmpty()) {
                                    item {
                                        Text(
                                            text = "No themes yet for this business.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    items(
                                        themeItems,
                                        key = { it.themeId ?: it.hashCode().toString() }
                                    ) { theme ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        val rawUrl = theme.themeId?.let { themeImageUrls[it] }
                                                        val imageUrl = UrlResolver.resolve(rawUrl)
                                                        if (imageUrl != null) {
                                                            AsyncImage(
                                                                model = imageUrl,
                                                                contentDescription = theme.themeName,
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(80.dp),
                                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                            )
                                                        }
                                                        Text(
                                                            text = theme.themeName ?: "Unnamed theme",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            selectedTheme = theme
                                                            themeDialogMode = "VIEW"
                                                            showThemeDialog = true
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.MoreVert, contentDescription = "Theme actions")
                                                    }
                                                }
                                                if (!theme.themeCategory.isNullOrBlank()) {
                                                    Text(
                                                        text = theme.themeCategory!!,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                if (!theme.themeDescription.isNullOrBlank()) {
                                                    Text(
                                                        text = theme.themeDescription!!,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                }
                                                if (!theme.priceRange.isNullOrBlank()) {
                                                    Text(
                                                        text = "Price: ${theme.priceRange} | Qty: ${theme.quantity}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // INVENTORY tab: show only inventory management
                    if (currentTab == VendorTab.INVENTORY) {
                        item {
                            Text(
                                text = "Inventory Management",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }

                        item {
                            Button(
                                onClick = onAddInventoryClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Add Inventory Item")
                            }
                        }

                        if (businesses.isEmpty()) {
                            item {
                                Text(
                                    text = "Create a business first to manage inventory items.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            businesses.forEach { business ->
                                val businessId = business.businessId ?: return@forEach
                                val items = inventoryByBusiness[businessId].orEmpty()

                                item {
                                    Text(
                                        text = business.businessName ?: "Business",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }

                                if (items.isEmpty()) {
                                    item {
                                        Text(
                                            text = "No inventory items yet for this business.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    items(
                                        items,
                                        key = { it.inventoryId ?: it.hashCode().toString() }
                                    ) { inv ->
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
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        // Inventory image
                                                        val rawUrl = inv.inventoryId?.let { inventoryImageUrls[it] }
                                                        val imageUrl = UrlResolver.resolve(rawUrl)
                                                        if (imageUrl != null) {
                                                            AsyncImage(
                                                                model = imageUrl,
                                                                contentDescription = inv.itemName,
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(70.dp),
                                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                            )
                                                        }
                                                        Text(
                                                            text = inv.itemName ?: "Unnamed item",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            selectedInv = inv
                                                            invDialogMode = "VIEW"
                                                            showInvDialog = true
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.MoreVert, contentDescription = "Inventory actions")
                                                    }
                                                }
                                                if (!inv.category.isNullOrBlank()) {
                                                    Text(
                                                        text = inv.category!!,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                if (!inv.description.isNullOrBlank()) {
                                                    Text(
                                                        text = inv.description!!,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Price: ₹${"%.2f".format(inv.price)} | Qty: ${inv.quantity}",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is com.startup.recordservice.ui.viewmodel.VendorUiState.Idle -> {
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

        // Order status update dialog
        val orderForDialog = statusDialogOrder
        if (orderForDialog != null) {
            AlertDialog(
                onDismissRequest = { if (!statusDialogLoading) statusDialogOrder = null },
                title = { Text("Update Order Status") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Order #${orderForDialog.orderId?.toString()?.take(8) ?: "N/A"}")
                        Text("Current: ${orderForDialog.status ?: "UNKNOWN"}")
                        Spacer(Modifier.height(4.dp))
                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = { /* simple text field below instead of dropdown for now */ }
                        ) {
                            // Simple buttons list instead of true dropdown to avoid extra state
                        }
                        val statuses = listOf("PENDING", "CONFIRMED", "PREPARING", "READY", "DELIVERED", "CANCELLED")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            statuses.chunked((statuses.size + 1) / 2).forEach { chunk ->
                                Column {
                                    chunk.forEach { s ->
                                        FilterChip(
                                            selected = statusDialogStatus == s,
                                            onClick = { statusDialogStatus = s },
                                            label = { Text(s) }
                                        )
                                    }
                                }
                            }
                        }
                        if (statusDialogError != null) {
                            Text(statusDialogError!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !statusDialogLoading,
                        onClick = {
                            val id = orderForDialog.orderId
                            if (id == null) {
                                statusDialogError = "Order ID missing"
                                return@TextButton
                            }
                            statusDialogLoading = true
                            statusDialogError = null
                            viewModel.updateOrderStatus(id, statusDialogStatus) { result ->
                                statusDialogLoading = false
                                result.onSuccess {
                                    statusDialogOrder = null
                                }.onFailure { e ->
                                    statusDialogError = e.message ?: "Failed to update status"
                                }
                            }
                        }
                    ) {
                        Text(if (statusDialogLoading) "Updating…" else "Update")
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !statusDialogLoading,
                        onClick = { statusDialogOrder = null }
                    ) { Text("Cancel") }
                }
            )
        }
    }
}
