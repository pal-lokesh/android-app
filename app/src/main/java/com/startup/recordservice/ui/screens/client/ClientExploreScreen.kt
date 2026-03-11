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
    var searchText by remember { mutableStateOf("") }
    var showCart by remember { mutableStateOf(false) }
    
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
                onNotificationsClick = { /* TODO: Navigate to notifications */ },
                onCartClick = { showCart = true },
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
            if (showCart) {
                CartScreen(
                    items = cartItems,
                    onClose = { showCart = false },
                    onClearCart = { viewModel.clearCart() },
                    onIncreaseQuantity = { itemId, itemType -> viewModel.increaseItemQuantity(itemId, itemType) },
                    onDecreaseQuantity = { itemId, itemType -> viewModel.decreaseItemQuantity(itemId, itemType) },
                    onRemoveItem = { itemId, itemType -> viewModel.removeItemFromCart(itemId, itemType) }
                )
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
                // Plates & Dishes data can be wired later; for now use empty lists
                val plates: List<PlateResponse> = emptyList()
                val dishes: List<DishResponse> = emptyList()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
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
                                onAddToCart = { /* TODO: implement plate cart */ }
                            )
                        }
                    }

                    if (dishes.isNotEmpty()) {
                        item {
                            DishSection(
                                dishes = dishes,
                                onAddToCart = { /* TODO: implement dish cart */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreTopBar(
    searchText: String,
    onSearchChange: (String) -> Unit,
    cartCount: Int,
    onNotificationsClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search themes, food, vendors") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true
            )

            IconButton(onClick = onNotificationsClick) {
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
                IconButton(onClick = onCartClick) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                }
            }

            IconButton(onClick = onProfileClick) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
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
                        // Image placeholder
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

@Composable
fun CartScreen(
    items: List<ExploreCartItem>,
    onClose: () -> Unit,
    onClearCart: () -> Unit,
    onIncreaseQuantity: (String, String) -> Unit,
    onDecreaseQuantity: (String, String) -> Unit,
    onRemoveItem: (String, String) -> Unit
) {
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
                                if (cartItem.price > 0.0) {
                                    Text(
                                        text = "₹${String.format("%.2f", cartItem.price)} each",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
                                if (cartItem.price > 0.0) {
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
                    onClick = { /* TODO: Navigate to checkout */ },
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
