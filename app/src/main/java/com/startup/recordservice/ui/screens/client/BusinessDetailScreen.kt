package com.startup.recordservice.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.startup.recordservice.ui.viewmodel.BusinessDetailViewModel
import com.startup.recordservice.ui.viewmodel.BusinessCartItem
import com.startup.recordservice.data.model.PlateResponse
import com.startup.recordservice.data.model.DishResponse
import com.startup.recordservice.data.model.InventoryResponse
import com.startup.recordservice.data.util.UrlResolver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailScreen(
    businessId: String,
    onNavigateBack: () -> Unit,
    onChatClick: (String, String?) -> Unit = { _, _ -> },
    viewModel: BusinessDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(businessId) {
        if (businessId.isNotBlank()) {
            viewModel.loadBusiness(businessId)
        } else {
            android.util.Log.e("BusinessDetailScreen", "Business ID is blank")
        }
    }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val business by viewModel.business.collectAsStateWithLifecycle()
    val plates by viewModel.plates.collectAsStateWithLifecycle()
    val inventory by viewModel.inventory.collectAsStateWithLifecycle()
    val plateDishes by viewModel.plateDishes.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    
    var selectedTabIndex by remember { mutableStateOf(0) }
    val businessCategory = business?.category?.lowercase().orEmpty()
    val isCateringBusiness = remember(businessCategory) {
        businessCategory == "caters" ||
            businessCategory == "catering" ||
            businessCategory.contains("cater") ||
            businessCategory.contains("food") ||
            businessCategory.contains("dish") ||
            businessCategory.contains("restaurant")
    }
    val tabs = remember(isCateringBusiness) {
        if (isCateringBusiness) {
            listOf("Info", "Plates", "Inventory")
        } else {
            listOf("Info", "Inventory")
        }
    }

    LaunchedEffect(tabs.size) {
        if (selectedTabIndex >= tabs.size) {
            selectedTabIndex = 0
        }
    }
    
    var showCart by remember { mutableStateOf(false) }
    var showCheckout by remember { mutableStateOf(false) }
    var placingOrder by remember { mutableStateOf(false) }
    var orderError by remember { mutableStateOf<String?>(null) }
    var orderSuccess by remember { mutableStateOf<String?>(null) }
    
    var customerName by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var deliveryDateText by remember { mutableStateOf<String?>(null) }
    var specialNotes by remember { mutableStateOf("") }
    var showDeliveryDateDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Business Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            business?.let { biz ->
                                onChatClick(businessId, biz.businessName)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat")
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = 16.dp)
            ) {
                if (cartItems.isNotEmpty()) {
                    BadgedBox(
                        badge = {
                            Badge {
                                Text(cartItems.size.toString())
                            }
                        }
                    ) {
                        FloatingActionButton(
                            onClick = { showCart = true },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                }
                business?.let { biz ->
                    ExtendedFloatingActionButton(
                        onClick = { onChatClick(businessId, biz.businessName) },
                        icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                        text = { Text("Chat") }
                    )
                }
            }
        }
    ) { padding ->
        when (uiState) {
            is com.startup.recordservice.ui.viewmodel.BusinessDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is com.startup.recordservice.ui.viewmodel.BusinessDetailUiState.Error -> {
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
                        text = (uiState as com.startup.recordservice.ui.viewmodel.BusinessDetailUiState.Error).message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadBusiness(businessId) }) {
                        Text("Retry")
                    }
                }
            }
            is com.startup.recordservice.ui.viewmodel.BusinessDetailUiState.Success -> {
                val biz = (uiState as com.startup.recordservice.ui.viewmodel.BusinessDetailUiState.Success).business
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Business Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = biz.businessName ?: "Unknown Business",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!biz.category.isNullOrBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = biz.category ?: "",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    
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
                    
                    // Tab Content
                    when (tabs.getOrElse(selectedTabIndex) { "Info" }) {
                        "Info" -> BusinessInfoTab(business = biz)
                        "Plates" -> PlatesTab(
                            plates = plates, 
                            plateDishes = plateDishes,
                            businessId = businessId,
                            businessName = business?.businessName,
                            onAddPlateToCart = { plate ->
                                viewModel.addPlateToCart(plate, businessId, business?.businessName)
                            },
                            onAddDishToCart = { dish ->
                                viewModel.addDishToCart(dish, businessId, business?.businessName)
                            }
                        )
                        "Inventory" -> InventoryTab(
                            inventory = inventory,
                            onAddToCart = { inv ->
                                viewModel.addInventoryToCart(inv, businessId, business?.businessName)
                            }
                        )
                        else -> BusinessInfoTab(business = biz)
                    }
                }
            }
            is com.startup.recordservice.ui.viewmodel.BusinessDetailUiState.Idle -> {
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
    
    // Cart Dialog
    if (showCart) {
        CartDialog(
            cartItems = cartItems,
            onDismiss = { showCart = false },
            onRemoveItem = { index: Int -> viewModel.removeCartItem(index) },
            onCheckout = {
                showCart = false
                showCheckout = true
            }
        )
    }
    
    // Checkout Dialog
    if (showCheckout) {
        CheckoutDialog(
            cartItems = cartItems,
            customerName = customerName,
            customerEmail = customerEmail,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            deliveryDateText = deliveryDateText,
            specialNotes = specialNotes,
            placingOrder = placingOrder,
            orderError = orderError,
            orderSuccess = orderSuccess,
            onCustomerNameChange = { value: String -> customerName = value },
            onCustomerEmailChange = { value: String -> customerEmail = value },
            onCustomerPhoneChange = { value: String -> customerPhone = value },
            onDeliveryAddressChange = { value: String -> deliveryAddress = value },
            onSpecialNotesChange = { value: String -> specialNotes = value },
            onDeliveryDateClick = { showDeliveryDateDialog = true },
            onDismiss = { if (!placingOrder) showCheckout = false },
            onPlaceOrder = {
                val date = deliveryDateText
                if (customerName.isBlank() || customerEmail.isBlank() || customerPhone.isBlank() || deliveryAddress.isBlank() || date.isNullOrBlank()) {
                    orderError = "Please fill all required fields (including delivery date)."
                    return@CheckoutDialog
                }
                placingOrder = true
                orderError = null
                orderSuccess = null
                scope.launch {
                    val result = viewModel.placeOrder(
                        customerName = customerName.trim(),
                        customerEmail = customerEmail.trim(),
                        customerPhone = customerPhone.trim(),
                        deliveryAddress = deliveryAddress.trim(),
                        deliveryDate = date,
                        specialNotes = specialNotes.trim().ifBlank { null }
                    )
                    placingOrder = false
                    result.onSuccess {
                        orderSuccess = "Order placed successfully!"
                        showCheckout = false
                        customerName = ""
                        customerEmail = ""
                        customerPhone = ""
                        deliveryAddress = ""
                        deliveryDateText = null
                        specialNotes = ""
                    }.onFailure { e ->
                        orderError = e.message ?: "Failed to place order"
                    }
                }
            }
        )
    }
    
    // Date Picker Dialog
    if (showDeliveryDateDialog) {
        DatePickerDialog(
            onDismissRequest = { showDeliveryDateDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            deliveryDateText = dateFormat.format(Date(millis))
                        }
                        showDeliveryDateDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeliveryDateDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun BusinessInfoTab(business: com.startup.recordservice.data.model.BusinessResponse) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            if (!business.description.isNullOrEmpty()) {
                Column {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = business.description ?: "",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        item {
            if (!business.address.isNullOrEmpty()) {
                Column {
                    Text(
                        text = "Address",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = business.address ?: "",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        
        item {
            if (!business.phoneNumber.isNullOrBlank()) {
                Column {
                    Text(
                        text = "Contact",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = business.phoneNumber ?: "",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        
        item {
            if (!business.email.isNullOrEmpty()) {
                Column {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = business.email ?: "",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (business.isActive) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (business.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (business.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PlatesTab(
    plates: List<PlateResponse>, 
    plateDishes: Map<String, List<DishResponse>>,
    businessId: String,
    businessName: String?,
    onAddPlateToCart: (PlateResponse) -> Unit = {},
    onAddDishToCart: (DishResponse) -> Unit = {}
) {
    if (plates.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "No plates available",
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
            items(plates) { plate ->
                PlateCard(
                    plate = plate, 
                    dishes = plateDishes[plate.plateId] ?: emptyList(),
                    onAddPlateToCart = { onAddPlateToCart(plate) },
                    onAddDishToCart = onAddDishToCart
                )
            }
        }
    }
}

@Composable
fun PlateCard(
    plate: PlateResponse, 
    dishes: List<DishResponse>,
    onAddPlateToCart: () -> Unit = {},
    onAddDishToCart: (DishResponse) -> Unit = {}
) {
    val plateImageUrl = remember(plate) { platePrimaryImageUrl(plate) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (plateImageUrl != null) {
                AsyncImage(
                    model = plateImageUrl,
                    contentDescription = plate.plateName ?: "Plate image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plate.plateName?.takeIf { it.isNotBlank() } ?: "Unknown Plate",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (!plate.category.isNullOrBlank()) {
                        Text(
                            text = plate.category ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "₹${String.format("%.2f", plate.price)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (!plate.description.isNullOrBlank()) {
                Text(
                    text = plate.description ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (dishes.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Available Dishes:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                dishes.forEach { dish ->
                    val dishImageUrl = remember(dish) { dishPrimaryImageUrl(dish) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dishImageUrl != null) {
                            AsyncImage(
                                model = dishImageUrl,
                                contentDescription = dish.dishName ?: "Dish image",
                                modifier = Modifier
                                    .size(56.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dish.dishName?.takeIf { it.isNotBlank() } ?: "Unknown Dish",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "₹${String.format("%.2f", dish.price)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        IconButton(onClick = { onAddDishToCart(dish) }) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = "Add Dish to Cart")
                        }
                    }
                }
            }
            
            if (plate.hasOptionalDishes) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Has Optional Dishes",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onAddPlateToCart) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add to Cart")
                }
            }
        }
    }
}

private fun platePrimaryImageUrl(plate: PlateResponse): String? {
    val candidate = plate.images?.firstOrNull { !it.isNullOrBlank() }
        ?: plate.plateImage
    return UrlResolver.resolve(candidate)
}

private fun dishPrimaryImageUrl(dish: DishResponse): String? {
    val candidate = dish.images?.firstOrNull { !it.isNullOrBlank() }
        ?: dish.dishImage
    return UrlResolver.resolve(candidate)
}

@Composable
fun InventoryTab(
    inventory: List<InventoryResponse>,
    onAddToCart: (InventoryResponse) -> Unit = {}
) {
    if (inventory.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "No inventory items available",
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
            items(inventory) { item ->
                InventoryCard(item = item, onAddToCart = { onAddToCart(item) })
            }
        }
    }
}

@Composable
fun InventoryCard(
    item: InventoryResponse,
    onAddToCart: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val name = item.itemName.orEmpty().ifBlank { "Unknown Item" }
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    val category = item.category.orEmpty()
                    if (category.isNotBlank()) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = "₹${String.format("%.2f", item.price)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            val description = item.description.orEmpty()
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quantity: ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.isActive) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Available",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            
            if (item.isActive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onAddToCart) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add to Cart")
                    }
                }
            }
        }
    }
}

@Composable
fun CartDialog(
    cartItems: List<BusinessCartItem>,
    onDismiss: () -> Unit,
    onRemoveItem: (Int) -> Unit,
    onCheckout: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Your Cart") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (cartItems.isEmpty()) {
                    Text("Your cart is empty.")
                } else {
                    cartItems.forEachIndexed { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "₹${String.format("%.2f", item.price)} x ${item.quantity}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { onRemoveItem(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    val total = cartItems.sumOf { it.price * it.quantity }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹${String.format("%.2f", total)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCheckout,
                enabled = cartItems.isNotEmpty()
            ) {
                Text("Checkout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutDialog(
    cartItems: List<BusinessCartItem>,
    customerName: String,
    customerEmail: String,
    customerPhone: String,
    deliveryAddress: String,
    deliveryDateText: String?,
    specialNotes: String,
    placingOrder: Boolean,
    orderError: String?,
    orderSuccess: String?,
    onCustomerNameChange: (String) -> Unit,
    onCustomerEmailChange: (String) -> Unit,
    onCustomerPhoneChange: (String) -> Unit,
    onDeliveryAddressChange: (String) -> Unit,
    onSpecialNotesChange: (String) -> Unit,
    onDeliveryDateClick: () -> Unit,
    onDismiss: () -> Unit,
    onPlaceOrder: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!placingOrder) onDismiss() },
        title = { Text("Checkout") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (orderError != null) {
                    Text(orderError, color = MaterialTheme.colorScheme.error)
                }
                if (orderSuccess != null) {
                    Text(orderSuccess, color = MaterialTheme.colorScheme.primary)
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = onCustomerNameChange,
                    label = { Text("Customer Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customerEmail,
                    onValueChange = onCustomerEmailChange,
                    label = { Text("Customer Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = onCustomerPhoneChange,
                    label = { Text("Customer Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = onDeliveryAddressChange,
                    label = { Text("Delivery Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Delivery Date",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = onDeliveryDateClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(deliveryDateText ?: "Select date")
                }

                OutlinedTextField(
                    value = specialNotes,
                    onValueChange = onSpecialNotesChange,
                    label = { Text("Special Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !placingOrder,
                onClick = onPlaceOrder
            ) {
                Text(if (placingOrder) "Placing…" else "Place Order")
            }
        },
        dismissButton = {
            TextButton(
                enabled = !placingOrder,
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
