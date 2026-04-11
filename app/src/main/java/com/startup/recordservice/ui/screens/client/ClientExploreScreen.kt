package com.startup.recordservice.ui.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.startup.recordservice.data.model.BusinessResponse
import com.startup.recordservice.data.model.DishResponse
import com.startup.recordservice.data.model.InventoryResponse
import com.startup.recordservice.data.model.PlateResponse
import com.startup.recordservice.data.model.ThemeResponse
import com.startup.recordservice.data.util.UrlResolver
import com.startup.recordservice.ui.viewmodel.ExploreViewModel
import com.startup.recordservice.ui.viewmodel.ExploreCartItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.DialogProperties

@Composable
fun ClientExploreScreen(
    onNavigateBack: () -> Unit, // currently unused, kept for API compatibility
    onBusinessClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: ExploreViewModel = hiltViewModel()
) {
    ExploreScreen(
        onBusinessClick = onBusinessClick,
        onProfileClick = onProfileClick,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onBusinessClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val inventoryImageUrls by viewModel.inventoryImageUrls.collectAsStateWithLifecycle()
    val themeImageUrls by viewModel.themeImageUrls.collectAsStateWithLifecycle()
    val filterOptions by viewModel.filterOptions.collectAsStateWithLifecycle()
    val activeFilterCount = viewModel.getActiveFilterCount()
    var searchText by remember { mutableStateOf("") }
    var showCart by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    LaunchedEffect(searchText) {
        viewModel.setSearchQuery(searchText)
    }
    
    Scaffold(
        topBar = {
            ExploreTopBar(
                searchText = searchText,
                onSearchChange = { searchText = it },
                cartCount = cartCount,
                filterCount = activeFilterCount,
                onNotificationsClick = { /* TODO: Navigate to notifications */ },
                onCartClick = { showCart = true },
                onFilterClick = { showFilterDialog = true },
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
            if (showCart) {
                // Respect Scaffold padding so the cart UI doesn't render under the top bar
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    CartScreen(
                        items = cartItems,
                        onClose = { showCart = false },
                        onClearCart = { viewModel.clearCart() },
                        onIncreaseQuantity = { itemId, itemType -> viewModel.increaseItemQuantity(itemId, itemType) },
                        onDecreaseQuantity = { itemId, itemType -> viewModel.decreaseItemQuantity(itemId, itemType) },
                        onRemoveItem = { itemId, itemType -> viewModel.removeItemFromCart(itemId, itemType) }
                    )
                }
            } else when (uiState) {
                is com.startup.recordservice.ui.viewmodel.ExploreUiState.Loading -> {
                    Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is com.startup.recordservice.ui.viewmodel.ExploreUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
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
                            text = (uiState as com.startup.recordservice.ui.viewmodel.ExploreUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }

                is com.startup.recordservice.ui.viewmodel.ExploreUiState.Success -> {
                    val vendors = viewModel.getFilteredBusinesses()
                    val themes = viewModel.getFilteredThemes()
                    val inventory = viewModel.getFilteredInventory()
                    val plates = viewModel.getFilteredPlates()
                    val dishes = viewModel.getFilteredDishes()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        // Active Filters Chips
                        if (activeFilterCount > 0) {
                            com.startup.recordservice.ui.components.ActiveFiltersChips(
                                filters = filterOptions,
                                onRemoveFilter = { key ->
                                    val updatedFilters = when (key) {
                                        "eventType" -> filterOptions.copy(eventType = "all")
                                        "category" -> filterOptions.copy(category = "all")
                                        "location" -> filterOptions.copy(location = "all")
                                        "budget" -> filterOptions.copy(budget = "all")
                                        "sortBy" -> filterOptions.copy(sortBy = "default")
                                        "minRating" -> filterOptions.copy(minRating = 0.0)
                                        else -> filterOptions
                                    }
                                    viewModel.applyFilters(updatedFilters)
                                },
                                onClearAll = {
                                    viewModel.resetFilters()
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            item {
                                EventCategorySection()
                            }

                            if (vendors.isNotEmpty()) {
                                item {
                                    VendorSection(
                                        vendors = vendors,
                                        onBusinessClick = onBusinessClick
                                    )
                                }
                            }

                            if (themes.isNotEmpty()) {
                                item {
                                    ThemeSection(
                                        themes = themes,
                                        imageUrls = themeImageUrls,
                                        onAddToCart = { viewModel.addThemeToCart(it) }
                                    )
                                }
                            }

                            if (inventory.isNotEmpty()) {
                                item {
                                    InventorySection(
                                        inventory = inventory,
                                        imageUrls = inventoryImageUrls,
                                        onAddToCart = { viewModel.addInventoryToCart(it) }
                                    )
                                }
                            }

                            if (plates.isNotEmpty()) {
                                item {
                                    PlateSection(
                                        plates = plates,
                                        onAddToCart = { viewModel.addPlateToCart(it) }
                                    )
                                }
                            }

                            if (dishes.isNotEmpty()) {
                                item {
                                    DishSection(
                                        dishes = dishes,
                                        onAddToCart = { viewModel.addDishToCart(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        
        // Filter Dialog
        com.startup.recordservice.ui.components.FilterDialog(
            open = showFilterDialog,
            onDismiss = { showFilterDialog = false },
            onApply = { filters ->
                viewModel.applyFilters(filters)
                showFilterDialog = false
            },
            onReset = {
                viewModel.resetFilters()
            },
            initialFilters = filterOptions,
            activeFilterCount = activeFilterCount
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreTopBar(
    searchText: String,
    onSearchChange: (String) -> Unit,
    cartCount: Int,
    filterCount: Int = 0,
    onNotificationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onFilterClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val topBarHeight = 60.dp
    val actionButtonSize = 48.dp
    val itemSpacing = 8.dp
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val desiredSearchWidth = maxWidth * 0.5f
            val actionsWidth = actionButtonSize * 4
            val totalSpacing = itemSpacing * 4
            val maxAllowedSearchWidth = maxWidth - actionsWidth - totalSpacing
            val searchWidth = minOf(desiredSearchWidth, maxAllowedSearchWidth)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarHeight),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .width(searchWidth)
                        .fillMaxHeight(),
                    placeholder = { Text("Search themes, food, vendors") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    singleLine = true
                )

                BadgedBox(
                    badge = {
                        if (filterCount > 0) {
                            Badge { Text(filterCount.toString()) }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onFilterClick,
                        modifier = Modifier.size(actionButtonSize)
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                }

                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.size(actionButtonSize)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                }

                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge {
                                Text(cartCount.toString())
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.size(actionButtonSize)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    }
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(actionButtonSize)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                }
            }
        }
    }
}

// REGION: Sections

@Composable
fun EventCategorySection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
            text = "Event Categories",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

        val categories = listOf(
            "Decoration & Themes" to "Create stunning event experiences",
            "Catering & Food" to "Delicious menus for every occasion",
            "Hotels & Farmhouses" to "Perfect venues for your events",
            "Everything Together" to "All-in-one event packages"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { (title, description) ->
                Card(
                    modifier = Modifier
                        .width(200.dp)
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(
                                onClick = { /* TODO: filter by category */ }
                ) {
                    Text(
                                    text = "Browse Services",
                                    style = MaterialTheme.typography.labelLarge
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
fun VendorSection(
    vendors: List<BusinessResponse>,
    onBusinessClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Vendors",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(vendors) { business ->
                Card(
                    modifier = Modifier
                        .width(220.dp)
                        .height(100.dp)
                        .clickable {
                            val id = business.businessId
                            if (!id.isNullOrBlank()) {
                                onBusinessClick(id)
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = business.businessName ?: "Unknown Vendor",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (!business.category.isNullOrBlank()) {
                            Text(
                                text = business.category ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (!business.description.isNullOrBlank()) {
                Text(
                                text = business.description ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeSection(
    themes: List<ThemeResponse>,
    imageUrls: Map<String, String>,
    onAddToCart: (ThemeResponse) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Themes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(themes) { theme ->
    Card(
                    modifier = Modifier.width(240.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
                        modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                        // Image: use preloaded theme imageUrl map (from /images/theme/{id})
                        val rawUrl = theme.themeId?.let { imageUrls[it] }
                        val imageUrl = UrlResolver.resolve(rawUrl)
                        
                        android.util.Log.d("ClientExploreScreen", "Theme: ${theme.themeName}, rawUrl: $rawUrl, resolvedUrl: $imageUrl")
                        
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = theme.themeName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                    Text(
                        text = theme.themeName?.takeIf { it.isNotBlank() } ?: "Unknown Theme",
                            style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (!theme.themeCategory.isNullOrBlank()) {
                        Text(
                            text = theme.themeCategory ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                }
                        if (!theme.priceRange.isNullOrBlank()) {
                            Text(
                                text = theme.priceRange ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (theme.isActive)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (theme.isActive) "Available" else "Unavailable",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Button(
                                onClick = { onAddToCart(theme) },
                                enabled = theme.isActive
                            ) {
                                Text("Add to Cart")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventorySection(
    inventory: List<InventoryResponse>,
    imageUrls: Map<String, String>,
    onAddToCart: (InventoryResponse) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Inventory",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(inventory) { item ->
                Card(
                    modifier = Modifier.width(220.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Image: use preloaded inventory imageUrl map (from /inventory/images)
                        val rawUrl = item.inventoryId?.let { imageUrls[it] }
                        val imageUrl = UrlResolver.resolve(rawUrl)
                        
                        android.util.Log.d("ClientExploreScreen", "Inventory: ${item.itemName}, rawUrl: $rawUrl, resolvedUrl: $imageUrl")
                        
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = item.itemName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = item.itemName ?: "Unknown Item",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        if (!item.category.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                Text(
                                    text = item.category ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹${String.format("%.2f", item.price)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { onAddToCart(item) },
                                enabled = item.isActive
                            ) {
                                Text("Add to Cart")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlateSection(
    plates: List<PlateResponse>,
    onAddToCart: (PlateResponse) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Plates",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(plates) { plate ->
                Card(
                    modifier = Modifier.width(240.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
                        modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                        // Plate image (if provided by backend)
                        val plateImageUrl = platePrimaryImageUrl(plate)
                        if (plateImageUrl != null) {
                            AsyncImage(
                                model = plateImageUrl,
                                contentDescription = plate.plateName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Veg / Non-veg badge (placeholder - no field in model yet)
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Veg / Non-Veg",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                    Text(
                            text = plate.plateName ?: "Unknown Plate",
                            style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                        Text(
                            text = "₹${String.format("%.2f", plate.price)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { onAddToCart(plate) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add to Cart")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DishSection(
    dishes: List<DishResponse>,
    onAddToCart: (DishResponse) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
            text = "Dishes",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dishes) { dish ->
                Card(
                    modifier = Modifier.width(240.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dish image (if provided by backend)
                        val dishImageUrl = dishPrimaryImageUrl(dish)
                        if (dishImageUrl != null) {
                            AsyncImage(
                                model = dishImageUrl,
                                contentDescription = dish.dishName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.RamenDining,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = dish.dishName ?: "Unknown Dish",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = if (dish.isActive)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (dish.isActive) "Available" else "Unavailable",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "₹${String.format("%.2f", dish.price)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onAddToCart(dish) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = dish.isActive
                        ) {
                            Text("Add to Cart")
                        }
                    }
                }
            }
        }
    }
}

private fun platePrimaryImageUrl(plate: PlateResponse): String? {
    val candidate = plate.images?.firstOrNull { !it.isNullOrBlank() } ?: plate.plateImage
    return UrlResolver.resolve(candidate)
}

private fun dishPrimaryImageUrl(dish: DishResponse): String? {
    val candidate = dish.images?.firstOrNull { !it.isNullOrBlank() } ?: dish.dishImage
    return UrlResolver.resolve(candidate)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    items: List<ExploreCartItem>,
    onClose: () -> Unit,
    onClearCart: () -> Unit,
    onIncreaseQuantity: (String, String) -> Unit,
    onDecreaseQuantity: (String, String) -> Unit,
    onRemoveItem: (String, String) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    var showCheckout by remember { mutableStateOf(false) }
    var placingOrder by remember { mutableStateOf(false) }
    var orderError by remember { mutableStateOf<String?>(null) }
    var orderSuccess by remember { mutableStateOf<String?>(null) }

    var customerName by remember { mutableStateOf("") }
    var customerEmail by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var specialNotes by remember { mutableStateOf("") }

    val dateFormatter = remember { DateTimeFormatter.ISO_LOCAL_DATE }
    val datePickerState = rememberDatePickerState()
    var deliveryDateText by remember { mutableStateOf<String?>(null) }
    var showDeliveryDateDialog by remember { mutableStateOf(false) }

    // Per-item booking date dialog (matches web DatePickerDialog behavior)
    var showBookingDateDialog by remember { mutableStateOf(false) }
    var bookingTarget by remember { mutableStateOf<ExploreCartItem?>(null) }
    val bookingDatePickerState = rememberDatePickerState()
    var bookingAvailableQty by remember { mutableStateOf<Int?>(null) }
    var bookingChecking by remember { mutableStateOf(false) }
    var bookingError by remember { mutableStateOf<String?>(null) }
    var bookingSubscribed by remember { mutableStateOf(false) }
    var bookingCheckingSub by remember { mutableStateOf(false) }
    var showNotifyDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Cart",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close cart")
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Your cart is empty.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val total = items.sumOf { it.price * it.quantity }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { "${it.id}_${it.type}" }) { cartItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Item details section
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = cartItem.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = cartItem.type.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = "₹${String.format("%.2f", cartItem.price)} each",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Booking date row (web: per item booking date is editable)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = cartItem.bookingDate?.let { "Date: $it" } ?: "Date: Not set",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    TextButton(
                                        onClick = {
                                            bookingTarget = cartItem
                                            bookingAvailableQty = null
                                            bookingError = null
                                            bookingSubscribed = false
                                            showNotifyDialog = false
                                            showBookingDateDialog = true
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Edit")
                                    }
                                    if (cartItem.bookingDate != null) {
                                        TextButton(
                                            onClick = {
                                                viewModel.setBookingDate(cartItem.id, cartItem.type, null)
                                            },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("Clear")
                                        }
                                    }
                                }
                            }
                            
                            // Quantity controls section
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Remove button
                                IconButton(
                                    onClick = { onRemoveItem(cartItem.id, cartItem.type) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove item",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                // Quantity controls
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { onDecreaseQuantity(cartItem.id, cartItem.type) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Decrease",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${cartItem.quantity}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(24.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(
                                        onClick = { onIncreaseQuantity(cartItem.id, cartItem.type) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Increase",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                // Subtotal
                                Text(
                                    text = "₹${String.format("%.2f", cartItem.price * cartItem.quantity)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
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
                        Text(
                            text = "Total items:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${items.sumOf { it.quantity }}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (total > 0.0) {
                        Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimated total:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val formattedTotal = String.format("%.2f", total)
                            Text(
                                text = "₹$formattedTotal",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        onClearCart()
                        onClose()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Cart")
                }
                Button(
                    onClick = { showCheckout = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = items.isNotEmpty()
                ) {
                    Text("Proceed to Checkout")
                }
            }
        }
    }

    if (showBookingDateDialog && bookingTarget != null) {
        val target = bookingTarget!!

        // On open: init selected date
        LaunchedEffect(target.id, target.type, target.bookingDate) {
            val initial = target.bookingDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
            bookingDatePickerState.setSelection(
                initial.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            bookingAvailableQty = null
            bookingError = null
            bookingSubscribed = false
        }

        // Debounced availability check (500ms) just like web
        LaunchedEffect(target.id, target.type, bookingDatePickerState.selectedDateMillis) {
            val millis = bookingDatePickerState.selectedDateMillis ?: return@LaunchedEffect
            bookingChecking = true
            bookingCheckingSub = false
            bookingError = null
            bookingAvailableQty = null
            bookingSubscribed = false

            delay(500)
            val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
            val itemTypeLower = when (target.type.uppercase()) {
                "THEME" -> "theme"
                "INVENTORY" -> "inventory"
                else -> target.type.lowercase()
            }
            viewModel.getAvailableQuantity(target.id, itemTypeLower, date)
                .onSuccess { qty ->
                    bookingAvailableQty = qty
                    if (qty == 0) {
                        val userId = viewModel.getCurrentUserId()
                        val businessId = target.businessId
                        if (!userId.isNullOrBlank() && !businessId.isNullOrBlank()) {
                            bookingCheckingSub = true
                            viewModel.isSubscribedForDate(
                                userId = userId,
                                itemId = target.id,
                                itemTypeUpper = itemTypeLower.uppercase(),
                                date = date
                            ).onSuccess { sub ->
                                bookingSubscribed = sub
                            }
                            bookingCheckingSub = false
                        }
                    }
                }
                .onFailure {
                    bookingAvailableQty = 0
                    bookingError = "Failed to check availability. Please try again."
                }
            bookingChecking = false
        }

        AlertDialog(
            onDismissRequest = { showBookingDateDialog = false; bookingTarget = null },
            title = { Text("Select Booking Date") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DatePicker(state = bookingDatePickerState, showModeToggle = false)
                    if (bookingChecking) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text("Checking availability…", style = MaterialTheme.typography.bodySmall)
                        }
                    } else if (bookingAvailableQty != null) {
                        val qty = bookingAvailableQty!!
                        if (qty > 0) {
                            Text(
                                text = "Available: $qty item(s) on ${bookingDatePickerState.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter) }}",
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Not available on the selected date.",
                                color = MaterialTheme.colorScheme.error
                            )
                            if (bookingCheckingSub) {
                                Text("Checking subscription…", style = MaterialTheme.typography.bodySmall)
                            } else if (bookingSubscribed) {
                                Text(
                                    "You are already subscribed for notifications on this date.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    "You can still keep this item in your cart. You can also subscribe to be notified when it becomes available on this date.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        val userId = viewModel.getCurrentUserId()
                                        val businessId = target.businessId
                                        val itemName = target.name
                                        val millis = bookingDatePickerState.selectedDateMillis
                                        val date = millis?.let {
                                            Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
                                        }
                                        val itemTypeUpper = target.type.uppercase()

                                        if (!userId.isNullOrBlank() && !businessId.isNullOrBlank() && !itemName.isNullOrBlank() && !date.isNullOrBlank()) {
                                            bookingCheckingSub = true
                                            bookingError = null
                                            scope.launch {
                                                viewModel.subscribeForDate(
                                                    userId = userId,
                                                    itemId = target.id,
                                                    itemTypeUpper = itemTypeUpper,
                                                    itemName = itemName,
                                                    businessId = businessId,
                                                    date = date
                                                ).onSuccess {
                                                    bookingSubscribed = true
                                                    bookingError = "You will be notified if this item becomes available on $date."
                                                }.onFailure { e ->
                                                    bookingError = e.message ?: "Failed to subscribe for notifications."
                                                }
                                                bookingCheckingSub = false
                                            }
                                        } else {
                                            bookingError = "Missing information to subscribe for notifications."
                                        }
                                    },
                                    enabled = !bookingCheckingSub,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (bookingCheckingSub) "Subscribing…" else "Notify Me")
                                }
                            }
                        }
                    }
                    if (bookingError != null) {
                        Text(bookingError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = bookingDatePickerState.selectedDateMillis
                        if (millis == null) {
                            bookingError = "Please select a booking date."
                            return@TextButton
                        }
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(dateFormatter)
                        viewModel.setBookingDate(target.id, target.type, date)
                        showBookingDateDialog = false
                        bookingTarget = null
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showBookingDateDialog = false; bookingTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (showCheckout) {
        AlertDialog(
            onDismissRequest = { if (!placingOrder) showCheckout = false },
            title = { Text("Checkout") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    if (orderError != null) {
                        Text(orderError!!, color = MaterialTheme.colorScheme.error)
                    }
                    if (orderSuccess != null) {
                        Text(orderSuccess!!, color = MaterialTheme.colorScheme.primary)
                    }

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerEmail,
                        onValueChange = { customerEmail = it },
                        label = { Text("Customer Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Customer Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Delivery Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Delivery Date",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { showDeliveryDateDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(deliveryDateText ?: "Select date")
                    }

                    OutlinedTextField(
                        value = specialNotes,
                        onValueChange = { specialNotes = it },
                        label = { Text("Special Notes (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !placingOrder,
                    onClick = {
                        val date = deliveryDateText

                        if (customerName.isBlank() || customerEmail.isBlank() || customerPhone.isBlank() || deliveryAddress.isBlank() || date.isNullOrBlank()) {
                            orderError = "Please fill all required fields (including delivery date)."
                            return@TextButton
                        }

                        placingOrder = true
                        orderError = null
                        orderSuccess = null

                        // Place orders (one per business) with availability enforcement
                        scope.launch {
                            val res = viewModel.placeOrders(
                                customerName = customerName.trim(),
                                customerEmail = customerEmail.trim(),
                                customerPhone = customerPhone.trim(),
                                deliveryAddress = deliveryAddress.trim(),
                                deliveryDate = date,
                                specialNotes = specialNotes.trim().ifBlank { null }
                            )
                            placingOrder = false
                            res.onSuccess { orders ->
                                orderSuccess = "Order placed (${orders.size})"
                                onClose()
                            }.onFailure { e ->
                                orderError = e.message ?: "Failed to place order"
                            }
                        }
                    }
                ) { Text(if (placingOrder) "Placing…" else "Place Order") }
            },
            dismissButton = {
                TextButton(
                    enabled = !placingOrder,
                    onClick = { showCheckout = false }
                ) { Text("Cancel") }
            }
        )
    }

    if (showDeliveryDateDialog) {
        DatePickerDialog(
            onDismissRequest = { showDeliveryDateDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            deliveryDateText = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .format(dateFormatter)
                        }
                        showDeliveryDateDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showDeliveryDateDialog = false }) { Text("Cancel") }
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            // Give the calendar enough width so day numbers don't get cramped/cut off on small screens.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// A simple reusable item card that can be used for list-based content if needed.
@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    badge: (@Composable (() -> Unit))? = null,
    price: String? = null,
    trailingContent: (@Composable (() -> Unit))? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (badge != null) {
                badge()
            }
            if (price != null) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            if (trailingContent != null) {
                Spacer(modifier = Modifier.height(4.dp))
                trailingContent()
            }
        }
    }
}
