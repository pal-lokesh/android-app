package com.startup.recordservice.ui.screens.vendor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Divider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.startup.recordservice.ui.viewmodel.VendorBusinessDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorBusinessDetailScreen(
    businessId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCreateTheme: (String) -> Unit = {},
    onNavigateToCreateInventory: (String) -> Unit = {},
    onNavigateToCreatePlate: (String) -> Unit = {},
    onNavigateToCreateDish: (String) -> Unit = {},
    onNavigateToConversations: (String) -> Unit = {},
    viewModel: VendorBusinessDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(businessId) {
        if (businessId.isNotBlank()) {
            viewModel.loadBusinessData(businessId)
        }
    }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val business by viewModel.business.collectAsStateWithLifecycle()
    val themes by viewModel.themes.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val availability by viewModel.availability.collectAsStateWithLifecycle()
    val plates by viewModel.plates.collectAsStateWithLifecycle()
    val plateDishes by viewModel.plateDishes.collectAsStateWithLifecycle()
    
    // Helper functions to determine business type
    fun isTentBusiness(business: com.startup.recordservice.data.model.BusinessResponse?): Boolean {
        if (business == null) return false
        val category = business.category?.lowercase() ?: ""
        return category == "tent_house" || category == "tent" || 
               (category.contains("tent") && !category.contains("cater"))
    }
    
    fun isCateringBusiness(business: com.startup.recordservice.data.model.BusinessResponse?): Boolean {
        if (business == null) return false
        val category = business.category?.lowercase() ?: ""
        return category == "caters" || category == "catering" || category.contains("cater")
    }
    
    fun isFarmhouseBusiness(business: com.startup.recordservice.data.model.BusinessResponse?): Boolean {
        if (business == null) return false
        val category = business.category?.lowercase() ?: ""
        return category.contains("farmhouse") || category.contains("farm house")
    }
    
    // Dynamic tabs based on business category (matching web behavior)
    val tabs = remember(business?.category) {
        when {
            business == null -> listOf("Details")
            isTentBusiness(business) -> listOf("Details", "Themes", "Inventory", "Orders", "Availability")
            isCateringBusiness(business) -> listOf("Details", "Plates", "Dishes", "Orders", "Availability")
            isFarmhouseBusiness(business) -> listOf("Details", "Themes", "Orders", "Availability")
            else -> listOf("Details", "Themes", "Inventory", "Orders", "Availability")
        }
    }
    
    // Reset selected tab index if it's out of bounds when tabs change
    var selectedTabIndex by remember { mutableStateOf(0) }
    LaunchedEffect(tabs.size) {
        if (selectedTabIndex >= tabs.size) {
            selectedTabIndex = 0
        }
    }
    
    // Edit/Delete dialogs
    var showEditBusinessDialog by remember { mutableStateOf(false) }
    var showDeleteBusinessDialog by remember { mutableStateOf(false) }
    var showDeleteThemeDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteInventoryDialog by remember { mutableStateOf<String?>(null) }
    var showDeletePlateDialog by remember { mutableStateOf<String?>(null) }
    var showDeleteDishDialog by remember { mutableStateOf<String?>(null) }
    var showAdjustAmountDialog by remember { mutableStateOf<com.startup.recordservice.data.model.OrderResponse?>(null) }
    var showStatusUpdateDialog by remember { mutableStateOf<com.startup.recordservice.data.model.OrderResponse?>(null) }
    
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(business?.businessName ?: "Business Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    business?.let { biz ->
                        biz.businessId?.let { businessId ->
                            IconButton(onClick = { onNavigateToConversations(businessId) }) {
                                Icon(Icons.Default.Chat, contentDescription = "Conversations")
                            }
                        }
                    }
                    business?.let {
                        IconButton(onClick = { showEditBusinessDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Business")
                        }
                        IconButton(onClick = { showDeleteBusinessDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Business")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            val currentTabLabel = tabs.getOrNull(selectedTabIndex) ?: "Details"
            when (currentTabLabel) {
                "Themes" -> {
                    FloatingActionButton(
                        onClick = { 
                            business?.businessId?.let { onNavigateToCreateTheme(it) }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Theme")
                    }
                }
                "Inventory" -> {
                    FloatingActionButton(
                        onClick = { 
                            business?.businessId?.let { onNavigateToCreateInventory(it) }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Inventory")
                    }
                }
                "Plates" -> {
                    FloatingActionButton(
                        onClick = { 
                            business?.businessId?.let { onNavigateToCreatePlate(it) }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Plate")
                    }
                }
                "Dishes" -> {
                    // Show FAB for adding dish to any plate
                    if (plates.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = { 
                                plates.firstOrNull()?.plateId?.let { plateId ->
                                    onNavigateToCreateDish(plateId)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Dish")
                        }
                    }
                }
                else -> null
            }
        }
    ) { padding ->
        when (uiState) {
            is com.startup.recordservice.ui.viewmodel.VendorBusinessDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is com.startup.recordservice.ui.viewmodel.VendorBusinessDetailUiState.Error -> {
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
                        text = (uiState as com.startup.recordservice.ui.viewmodel.VendorBusinessDetailUiState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.refresh(businessId) }) {
                        Text("Retry")
                    }
                }
            }
            is com.startup.recordservice.ui.viewmodel.VendorBusinessDetailUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Tabs
                    TabRow(selectedTabIndex = selectedTabIndex) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title) }
                            )
                        }
                    }
                    
                    // Content based on selected tab label (dynamic based on business category)
                    val currentTabLabel = tabs.getOrNull(selectedTabIndex) ?: "Details"
                    when (currentTabLabel) {
                        "Details" -> BusinessDetailsTab(
                            business = business!!,
                            themes = themes,
                            inventory = inventory,
                            plates = plates,
                            orders = orders
                        )
                        "Themes" -> ThemesTab(
                            themes = themes,
                            businessId = businessId,
                            onDeleteTheme = { themeId ->
                                showDeleteThemeDialog = themeId
                            },
                            onEditTheme = { themeId ->
                                onNavigateToCreateTheme(businessId)
                            }
                        )
                        "Inventory" -> InventoryTab(
                            inventory = inventory,
                            businessId = businessId,
                            onDeleteInventory = { inventoryId ->
                                showDeleteInventoryDialog = inventoryId
                            },
                            onEditInventory = { inventoryId ->
                                onNavigateToCreateInventory(businessId)
                            }
                        )
                        "Plates" -> PlatesTab(
                            plates = plates,
                            plateDishes = plateDishes,
                            businessId = businessId,
                            onDeletePlate = { plateId ->
                                showDeletePlateDialog = plateId
                            },
                            onDeleteDish = { dishId ->
                                showDeleteDishDialog = dishId
                            },
                            onEditPlate = { plateId ->
                                onNavigateToCreatePlate(businessId)
                            },
                            onEditDish = { dishId ->
                                // Navigate to create dish screen
                            },
                            onAddDish = { plateId ->
                                onNavigateToCreateDish(plateId)
                            }
                        )
                        "Dishes" -> {
                            // Show all dishes across all plates
                            val allDishes = plateDishes.values.flatten()
                            DishesTab(
                                dishes = allDishes,
                                plates = plates,
                                plateDishes = plateDishes,
                                businessId = businessId,
                                onDeleteDish = { dishId ->
                                    showDeleteDishDialog = dishId
                                },
                                onEditDish = { dishId ->
                                    // Navigate to create dish screen
                                },
                                onAddDish = { plateId ->
                                    onNavigateToCreateDish(plateId)
                                }
                            )
                        }
                        "Orders" -> OrdersTab(
                            orders = orders,
                            onAdjustAmount = { order ->
                                showAdjustAmountDialog = order
                            },
                            onUpdateStatus = { order ->
                                showStatusUpdateDialog = order
                            }
                        )
                        "Availability" -> AvailabilityTab(availability = availability)
                        else -> BusinessDetailsTab(business = business!!)
                    }
                }
            }
            else -> {
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
    
    // Edit Business Dialog
    if (showEditBusinessDialog && business != null) {
        EditBusinessDialog(
            business = business!!,
            onDismiss = { showEditBusinessDialog = false },
            onSave = { name, description, category, address, phone, email ->
                viewModel.updateBusiness(
                    businessId = businessId,
                    name = name,
                    description = description,
                    category = category,
                    address = address,
                    phone = phone,
                    email = email
                ) { result ->
                    result.onSuccess {
                        showEditBusinessDialog = false
                    }.onFailure {
                        // Error will be shown via UI state
                    }
                }
            }
        )
    }
    
    // Delete Business Dialog
    business?.let { biz ->
        if (showDeleteBusinessDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteBusinessDialog = false },
                title = { Text("Delete Business") },
                text = { Text("Are you sure you want to delete '${biz.businessName}'? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBusiness(businessId) { result ->
                            result.onSuccess {
                                showDeleteBusinessDialog = false
                                onNavigateBack()
                            }.onFailure {
                                showDeleteBusinessDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteBusinessDialog = false }) {
                    Text("Cancel")
                }
            }
        )
        }
    }
    
    // Delete Theme Dialog
    showDeleteThemeDialog?.let { themeId ->
        AlertDialog(
            onDismissRequest = { showDeleteThemeDialog = null },
            title = { Text("Delete Theme") },
            text = { Text("Are you sure you want to delete this theme?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTheme(themeId) { result ->
                            result.onSuccess {
                                showDeleteThemeDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteThemeDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete Inventory Dialog
    showDeleteInventoryDialog?.let { inventoryId ->
        AlertDialog(
            onDismissRequest = { showDeleteInventoryDialog = null },
            title = { Text("Delete Inventory Item") },
            text = { Text("Are you sure you want to delete this inventory item?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInventory(inventoryId) { result ->
                            result.onSuccess {
                                showDeleteInventoryDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteInventoryDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete Plate Dialog
    showDeletePlateDialog?.let { plateId ->
        AlertDialog(
            onDismissRequest = { showDeletePlateDialog = null },
            title = { Text("Delete Plate") },
            text = { Text("Are you sure you want to delete this plate? All associated dishes will also be deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePlate(plateId) { result ->
                            result.onSuccess {
                                showDeletePlateDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePlateDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete Dish Dialog
    showDeleteDishDialog?.let { dishId ->
        AlertDialog(
            onDismissRequest = { showDeleteDishDialog = null },
            title = { Text("Delete Dish") },
            text = { Text("Are you sure you want to delete this dish?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDish(dishId) { result ->
                            result.onSuccess {
                                showDeleteDishDialog = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDishDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Adjust Order Amount Dialog
    showAdjustAmountDialog?.let { order ->
        AdjustOrderAmountDialog(
            order = order,
            onDismiss = { showAdjustAmountDialog = null },
            onConfirm = { newAmount ->
                viewModel.updateOrderAmount(
                    orderId = order.orderId?.toString() ?: "",
                    newAmount = newAmount
                ) { result ->
                    result.onSuccess {
                        showAdjustAmountDialog = null
                    }.onFailure {
                        // Error will be shown via UI state
                    }
                }
            }
        )
    }
    
    // Order Status Update Dialog
    showStatusUpdateDialog?.let { order ->
        OrderStatusUpdateDialog(
            order = order,
            onDismiss = { showStatusUpdateDialog = null },
            onStatusUpdate = { newStatus ->
                viewModel.updateOrderStatus(
                    orderId = order.orderId?.toString() ?: "",
                    status = newStatus
                ) { result ->
                    result.onSuccess {
                        showStatusUpdateDialog = null
                    }.onFailure {
                        // Error will be shown via UI state
                    }
                }
            }
        )
    }
}

@Composable
fun AdjustOrderAmountDialog(
    order: com.startup.recordservice.data.model.OrderResponse,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var adjustedAmount by remember { mutableStateOf(order.totalAmount?.toString() ?: "0.0") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val currentAmount = order.totalAmount ?: 0.0
    val amountValue = adjustedAmount.toDoubleOrNull() ?: 0.0
    val amountDifference = amountValue - currentAmount
    val hasChanged = kotlin.math.abs(amountDifference) > 0.01
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adjust Order Amount")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Order ID: ${order.orderId?.toString()?.take(8) ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Current Order Amount:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "₹${String.format("%.2f", currentAmount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Divider()
                
                OutlinedTextField(
                    value = adjustedAmount,
                    onValueChange = { value ->
                        adjustedAmount = value
                        error = null
                        val numValue = value.toDoubleOrNull()
                        when {
                            value.isBlank() -> error = "Amount cannot be empty"
                            numValue == null -> error = "Please enter a valid number"
                            numValue < 0 -> error = "Amount cannot be negative"
                        }
                    },
                    label = { Text("New Order Amount") },
                    leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyLarge) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (hasChanged && error == null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (amountDifference > 0) 
                                MaterialTheme.colorScheme.errorContainer 
                            else 
                                MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (amountDifference > 0) "Increase" else "Decrease",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${if (amountDifference > 0) "+" else ""}₹${String.format("%.2f", amountDifference)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "New Total Amount:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "₹${String.format("%.2f", amountValue)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val numValue = adjustedAmount.toDoubleOrNull()
                    if (numValue != null && numValue >= 0) {
                        isLoading = true
                        onConfirm(numValue)
                    }
                },
                enabled = !isLoading && error == null && adjustedAmount.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasChanged) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (hasChanged) "Update Amount" else "Continue")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun BusinessDetailsTab(
    business: com.startup.recordservice.data.model.BusinessResponse,
    themes: List<com.startup.recordservice.data.model.ThemeResponse> = emptyList(),
    inventory: List<com.startup.recordservice.data.model.InventoryResponse> = emptyList(),
    plates: List<com.startup.recordservice.data.model.PlateResponse> = emptyList(),
    orders: List<com.startup.recordservice.data.model.OrderResponse> = emptyList()
) {
    val category = business.category?.lowercase() ?: ""
    val isCatering = category == "caters" || category == "catering" || category.contains("cater")
    val isFarmhouse = category.contains("farmhouse") == true || 
                      business.category?.lowercase()?.contains("farm house") == true
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Information Card
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = business.businessName ?: "Unknown Business",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = if (business.isActive) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = if (business.isActive) "Active" else "Inactive",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (business.isActive) 
                                    MaterialTheme.colorScheme.onPrimaryContainer 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    if (!business.category.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = business.category!!,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    if (!business.description.isNullOrBlank()) {
                        Text(
                            text = business.description!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    if (!business.address.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Address",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = business.address!!,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    if (!business.phoneNumber.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Phone",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = business.phoneNumber!!,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    if (!business.email.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Email",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = business.email!!,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Business Statistics Card (matching web Overview tab)
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Business Statistics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isCatering) {
                        // For catering businesses, show plate counts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = plates.size.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Total Plates",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = plates.count { it.isActive == true }.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Active Plates",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // For non-catering businesses, show theme and inventory counts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = themes.size.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Total Themes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = themes.count { it.isActive == true }.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Active Themes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        if (!isFarmhouse) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Inventory,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = inventory.size.toString(),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Total Inventory",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    // Orders count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = orders.size.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total Orders",
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

@Composable
fun ThemesTab(
    themes: List<com.startup.recordservice.data.model.ThemeResponse>,
    businessId: String,
    onDeleteTheme: (String) -> Unit,
    onEditTheme: (String) -> Unit
) {
    if (themes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No themes yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(themes, key = { it.themeId ?: it.hashCode().toString() }) { theme ->
                ThemeCard(
                    theme = theme,
                    onDelete = { theme.themeId?.let { onDeleteTheme(it) } },
                    onEdit = { theme.themeId?.let { onEditTheme(it) } }
                )
            }
        }
    }
}

@Composable
fun ThemeCard(
    theme: com.startup.recordservice.data.model.ThemeResponse,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = theme.themeName ?: "Unnamed Theme",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (!theme.themeCategory.isNullOrBlank()) {
                        Text(
                            text = theme.themeCategory!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (!theme.themeDescription.isNullOrBlank()) {
                Text(
                    text = theme.themeDescription!!,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (!theme.priceRange.isNullOrBlank()) {
                Text(
                    text = "Price: ${theme.priceRange} | Quantity: ${theme.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun InventoryTab(
    inventory: List<com.startup.recordservice.data.model.InventoryResponse>,
    businessId: String,
    onDeleteInventory: (String) -> Unit,
    onEditInventory: (String) -> Unit
) {
    if (inventory.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Inventory,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No inventory items yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(inventory, key = { it.inventoryId ?: it.hashCode().toString() }) { item ->
                InventoryCard(
                    inventory = item,
                    onDelete = { item.inventoryId?.let { onDeleteInventory(it) } },
                    onEdit = { item.inventoryId?.let { onEditInventory(it) } }
                )
            }
        }
    }
}

@Composable
fun InventoryCard(
    inventory: com.startup.recordservice.data.model.InventoryResponse,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = inventory.itemName ?: "Unnamed Item",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (!inventory.category.isNullOrBlank()) {
                        Text(
                            text = inventory.category!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (!inventory.description.isNullOrBlank()) {
                Text(
                    text = inventory.description!!,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Text(
                text = "Price: ₹${"%.2f".format(inventory.price)} | Quantity: ${inventory.quantity}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun DishesTab(
    dishes: List<com.startup.recordservice.data.model.DishResponse>,
    plates: List<com.startup.recordservice.data.model.PlateResponse>,
    plateDishes: Map<String, List<com.startup.recordservice.data.model.DishResponse>>,
    businessId: String,
    onDeleteDish: (String) -> Unit,
    onEditDish: (String) -> Unit,
    onAddDish: (String) -> Unit
) {
    if (dishes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.RestaurantMenu,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No dishes yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add dishes to plates to see them here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Group dishes by plate
            plates.forEach { plate ->
                val plateDishesList = plateDishes[plate.plateId] ?: emptyList()
                if (plateDishesList.isNotEmpty()) {
                    item {
                        Text(
                            text = "Plate: ${plate.plateName ?: "Unknown"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(plateDishesList, key = { it.dishId ?: it.hashCode().toString() }) { dish ->
                        DishCard(
                            dish = dish,
                            plateName = plate.plateName,
                            onDelete = { dish.dishId?.let { onDeleteDish(it) } },
                            onEdit = { dish.dishId?.let { onEditDish(it) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DishCard(
    dish: com.startup.recordservice.data.model.DishResponse,
    plateName: String?,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dish.dishName ?: "Unnamed Dish",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (plateName != null) {
                        Text(
                            text = "Plate: $plateName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            if (!dish.description.isNullOrBlank()) {
                Text(
                    text = dish.description!!,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Text(
                text = "Price: ₹${"%.2f".format(dish.price)}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun OrdersTab(
    orders: List<com.startup.recordservice.data.model.OrderResponse>,
    onAdjustAmount: (com.startup.recordservice.data.model.OrderResponse) -> Unit = {},
    onUpdateStatus: (com.startup.recordservice.data.model.OrderResponse) -> Unit = {}
) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Receipt,
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
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders, key = { it.orderId?.toString() ?: it.hashCode().toString() }) { order ->
                OrderCard(
                    order = order, 
                    onAdjustAmount = onAdjustAmount,
                    onUpdateStatus = onUpdateStatus
                )
            }
        }
    }
}

@Composable
fun OrderCard(
    order: com.startup.recordservice.data.model.OrderResponse,
    onAdjustAmount: (com.startup.recordservice.data.model.OrderResponse) -> Unit = {},
    onUpdateStatus: (com.startup.recordservice.data.model.OrderResponse) -> Unit = {}
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = { onUpdateStatus(order) }) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Update Status")
                }
                TextButton(onClick = { onAdjustAmount(order) }) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adjust Amount")
                }
            }
        }
    }
}

@Composable
fun AvailabilityTab(availability: List<com.startup.recordservice.data.model.AvailabilityResponse>) {
    if (availability.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No availability entries yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(availability, key = { "${it.itemId}_${it.availabilityDate}_${it.itemType}" }) { avail ->
                AvailabilityCard(availability = avail)
            }
        }
    }
}

@Composable
fun PlatesTab(
    plates: List<com.startup.recordservice.data.model.PlateResponse>,
    plateDishes: Map<String, List<com.startup.recordservice.data.model.DishResponse>>,
    businessId: String,
    onDeletePlate: (String) -> Unit,
    onDeleteDish: (String) -> Unit,
    onEditPlate: (String) -> Unit,
    onEditDish: (String) -> Unit,
    onAddDish: (String) -> Unit
) {
    if (plates.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No plates yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(plates, key = { it.plateId ?: it.hashCode().toString() }) { plate ->
                PlateCard(
                    plate = plate,
                    dishes = plateDishes[plate.plateId] ?: emptyList(),
                    onDelete = { plate.plateId?.let { onDeletePlate(it) } },
                    onEdit = { plate.plateId?.let { onEditPlate(it) } },
                    onDeleteDish = onDeleteDish,
                    onEditDish = onEditDish,
                    onAddDish = { plate.plateId?.let { onAddDish(it) } }
                )
            }
        }
    }
}

@Composable
fun PlateCard(
    plate: com.startup.recordservice.data.model.PlateResponse,
    dishes: List<com.startup.recordservice.data.model.DishResponse>,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onDeleteDish: (String) -> Unit,
    onEditDish: (String) -> Unit,
    onAddDish: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plate.plateName ?: "Unnamed Plate",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (!plate.category.isNullOrBlank()) {
                        Text(
                            text = plate.category!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (!plate.description.isNullOrBlank()) {
                        Text(
                            text = plate.description!!,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    Text(
                        text = "Price: ₹${"%.2f".format(plate.price)} | Quantity: ${plate.quantity ?: 0}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Dishes section
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dishes (${dishes.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onAddDish) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Dish")
                }
            }
            
            if (dishes.isNotEmpty()) {
                dishes.forEach { dish ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dish.dishName ?: "Unnamed Dish",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (!dish.description.isNullOrBlank()) {
                                Text(
                                    text = dish.description!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Price: ₹${"%.2f".format(dish.price)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row {
                            IconButton(onClick = { dish.dishId?.let { onEditDish(it) } }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Dish", modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { dish.dishId?.let { onDeleteDish(it) } }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Dish",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvailabilityCard(availability: com.startup.recordservice.data.model.AvailabilityResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Date: ${availability.availabilityDate ?: "N/A"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Item Type: ${availability.itemType ?: "N/A"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Available Quantity: ${availability.availableQuantity ?: 0}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun EditBusinessDialog(
    business: com.startup.recordservice.data.model.BusinessResponse,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(business.businessName.orEmpty()) }
    var desc by remember { mutableStateOf(business.description.orEmpty()) }
    var category by remember { mutableStateOf(business.category.orEmpty()) }
    var address by remember { mutableStateOf(business.address.orEmpty()) }
    var phone by remember { mutableStateOf(business.phoneNumber.orEmpty()) }
    var email by remember { mutableStateOf(business.email.orEmpty()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Business") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, desc, category, address, phone, email)
                },
                enabled = name.isNotBlank() && email.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OrderStatusUpdateDialog(
    order: com.startup.recordservice.data.model.OrderResponse,
    onDismiss: () -> Unit,
    onStatusUpdate: (String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(order.status ?: "PENDING") }
    val orderStatuses = listOf("PENDING", "CONFIRMED", "PREPARING", "READY", "DELIVERED", "CANCELLED")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Order Status")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Order #${order.orderId?.toString()?.take(8) ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Current Status: ${order.status ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Divider()
                
                Text(
                    text = "Select New Status:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                orderStatuses.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStatus = status },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status }
                        )
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStatusUpdate(selectedStatus) },
                enabled = selectedStatus != order.status
            ) {
                Text("Update Status")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
