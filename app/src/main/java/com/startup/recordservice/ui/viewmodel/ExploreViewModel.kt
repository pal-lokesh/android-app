package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.CartStorage
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.BusinessResponse
import com.startup.recordservice.data.model.ThemeResponse
import com.startup.recordservice.data.model.InventoryResponse
import com.startup.recordservice.data.model.PlateResponse
import com.startup.recordservice.data.model.DishResponse
import com.startup.recordservice.data.model.OrderItemRequest
import com.startup.recordservice.data.model.OrderRequest
import com.startup.recordservice.data.repository.BusinessRepository
import com.startup.recordservice.data.repository.ThemeRepository
import com.startup.recordservice.data.repository.InventoryRepository
import com.startup.recordservice.data.repository.PlateRepository
import com.startup.recordservice.data.repository.DishRepository
import com.startup.recordservice.data.repository.AvailabilityRepository
import com.startup.recordservice.data.repository.InventoryImageRepository
import com.startup.recordservice.data.repository.ImageRepository
import com.startup.recordservice.data.repository.OrderRepository
import com.startup.recordservice.data.repository.StockNotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ExploreUiState {
    data object Loading : ExploreUiState()
    data class Success(
        val businesses: List<BusinessResponse> = emptyList(),
        val themes: List<ThemeResponse> = emptyList(),
        val inventory: List<InventoryResponse> = emptyList()
    ) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

// Simple in-memory cart item used for the Explore cart badge and cart sheet
data class ExploreCartItem(
    val id: String,
    val name: String,
    val price: Double,
    val type: String, // "THEME" or "INVENTORY"
    val businessId: String? = null,
    val businessName: String? = null,
    val imageUrl: String? = null,
    val quantity: Int = 1,
    val bookingDate: String? = null // yyyy-MM-dd (optional; if null, we may use deliveryDate)
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val themeRepository: ThemeRepository,
    private val inventoryRepository: InventoryRepository,
    private val plateRepository: PlateRepository,
    private val dishRepository: DishRepository,
    private val inventoryImageRepository: InventoryImageRepository,
    private val imageRepository: ImageRepository,
    private val orderRepository: OrderRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val stockNotificationRepository: StockNotificationRepository,
    private val tokenManager: TokenManager,
    private val cartStorage: CartStorage
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()
    
    private val _businesses = MutableStateFlow<List<BusinessResponse>>(emptyList())
    val businesses: StateFlow<List<BusinessResponse>> = _businesses.asStateFlow()
    
    private val _themes = MutableStateFlow<List<ThemeResponse>>(emptyList())
    val themes: StateFlow<List<ThemeResponse>> = _themes.asStateFlow()
    
    private val _inventory = MutableStateFlow<List<InventoryResponse>>(emptyList())
    val inventory: StateFlow<List<InventoryResponse>> = _inventory.asStateFlow()

    private val _plates = MutableStateFlow<List<PlateResponse>>(emptyList())
    val plates: StateFlow<List<PlateResponse>> = _plates.asStateFlow()

    private val _dishes = MutableStateFlow<List<DishResponse>>(emptyList())
    val dishes: StateFlow<List<DishResponse>> = _dishes.asStateFlow()

    // Map of inventoryId -> first image URL (from inventory images endpoint)
    private val _inventoryImageUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val inventoryImageUrls: StateFlow<Map<String, String>> = _inventoryImageUrls.asStateFlow()

    // Map of themeId -> first image URL (from /api/images/theme/{id})
    private val _themeImageUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val themeImageUrls: StateFlow<Map<String, String>> = _themeImageUrls.asStateFlow()
    
    // Simple in-memory cart count for Explore screen badge
    private val _cartCount = MutableStateFlow(0)
    val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

    // In-memory cart items for the client Explore cart
    private val _cartItems = MutableStateFlow<List<ExploreCartItem>>(emptyList())
    val cartItems: StateFlow<List<ExploreCartItem>> = _cartItems.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String>("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filterOptions = MutableStateFlow<com.startup.recordservice.ui.components.FilterOptions>(
        com.startup.recordservice.ui.components.FilterOptions()
    )
    val filterOptions: StateFlow<com.startup.recordservice.ui.components.FilterOptions> = _filterOptions.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = ExploreUiState.Loading
                restoreCartFromStorage()
                
                val businessesResult = businessRepository.getAllBusinesses()
                val themesResult = themeRepository.getAllThemes()
                val inventoryResult = inventoryRepository.getAllInventory()
                
                val businessesList = businessesResult.getOrElse { emptyList() }
                val themesList = themesResult.getOrElse { emptyList() }
                val inventoryList = inventoryResult.getOrElse { emptyList() }

                // Plates/dishes are loaded via business/plate endpoints so image URLs are present.
                val platesList = mutableListOf<PlateResponse>()
                val dishesList = mutableListOf<DishResponse>()

                businessesList.forEach { business ->
                    val businessId = business.businessId
                    if (businessId.isNullOrBlank()) return@forEach

                    val businessPlates = plateRepository.getBusinessPlates(businessId).getOrElse { emptyList() }
                    platesList.addAll(businessPlates)

                    businessPlates.forEach { plate ->
                        val plateId = plate.plateId
                        if (plateId.isNullOrBlank()) return@forEach

                        val plateDishes = dishRepository.getPlateDishes(plateId).getOrElse { emptyList() }
                        dishesList.addAll(plateDishes)
                    }
                }

                val distinctPlates = platesList
                    .filter { !it.plateId.isNullOrBlank() }
                    .distinctBy { it.plateId }

                val distinctDishes = dishesList
                    .filter { !it.dishId.isNullOrBlank() }
                    .distinctBy { it.dishId }
                
                _businesses.value = businessesList
                _themes.value = themesList
                _inventory.value = inventoryList
                _plates.value = distinctPlates
                _dishes.value = distinctDishes

                // Load primary image URLs for each inventory item
                val imageMap = mutableMapOf<String, String>()
                for (item in inventoryList) {
                    val invId = item.inventoryId
                    if (!invId.isNullOrBlank()) {
                        inventoryImageRepository.getInventoryImagesByInventoryId(invId)
                            .onSuccess { images ->
                                // Prefer primary image, fallback to first
                                val primary = images.firstOrNull { it.isPrimary == true } ?: images.firstOrNull()
                                val url = primary?.imageUrl
                                if (!url.isNullOrBlank()) {
                                    imageMap[invId] = url
                                }
                            }
                            .onFailure { e ->
                                android.util.Log.e("ExploreViewModel", "Failed to load images for inventory $invId: ${e.message}")
                            }
                    }
                }
                _inventoryImageUrls.value = imageMap

                // Load primary image URLs for each theme
                val themeImageMap = mutableMapOf<String, String>()
                for (theme in themesList) {
                    val themeId = theme.themeId
                    if (!themeId.isNullOrBlank()) {
                        imageRepository.getImagesByThemeId(themeId)
                            .onSuccess { images ->
                                val primary = images.firstOrNull { it.isPrimary == true } ?: images.firstOrNull()
                                val url = primary?.imageUrl
                                if (!url.isNullOrBlank()) {
                                    themeImageMap[themeId] = url
                                }
                            }
                            .onFailure { e ->
                                android.util.Log.e("ExploreViewModel", "Failed to load images for theme $themeId: ${e.message}")
                            }
                    }
                }
                _themeImageUrls.value = themeImageMap
                
                _uiState.value = ExploreUiState.Success(
                    businesses = businessesList,
                    themes = themesList,
                    inventory = inventoryList
                )
            } catch (e: Exception) {
                android.util.Log.e("ExploreViewModel", "Error loading data: ${e.message}", e)
                _uiState.value = ExploreUiState.Error(
                    "Failed to load data: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    private fun restoreCartFromStorage() {
        val userId = tokenManager.getUserPhone() ?: return
        val stored = cartStorage.loadCart(userId)
        _cartItems.value = stored
        _cartCount.value = stored.sumOf { it.quantity }
    }

    private fun persistCartToStorage() {
        val userId = tokenManager.getUserPhone() ?: return
        cartStorage.saveCart(userId, _cartItems.value)
    }
    
    fun setCategory(category: String) {
        _selectedCategory.value = category
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun applyFilters(filters: com.startup.recordservice.ui.components.FilterOptions) {
        _filterOptions.value = filters
        // Update category if changed
        if (filters.category != "all") {
            _selectedCategory.value = filters.category
        }
    }
    
    fun resetFilters() {
        _filterOptions.value = com.startup.recordservice.ui.components.FilterOptions()
        _selectedCategory.value = "all"
    }
    
    fun getActiveFilterCount(): Int {
        val filters = _filterOptions.value
        var count = 0
        if (filters.eventType != "all") count++
        if (filters.category != "all") count++
        if (filters.location != "all") count++
        if (filters.budget != "all") count++
        if (filters.sortBy != "default") count++
        if (filters.minRating > 0) count++
        return count
    }
    
    fun getFilteredThemes(): List<ThemeResponse> {
        val filters = _filterOptions.value
        val category = if (filters.category != "all") filters.category else _selectedCategory.value
        val query = _searchQuery.value.lowercase()
        
        var filtered = _themes.value.filter { theme ->
            val matchesCategory = category == "all" || theme.themeCategory?.lowercase() == category.lowercase()
            val matchesSearch = query.isEmpty() || 
                theme.themeName?.lowercase()?.contains(query) == true ||
                theme.themeDescription?.lowercase()?.contains(query) == true ||
                theme.themeCategory?.lowercase()?.contains(query) == true
            matchesCategory && matchesSearch && theme.isActive
        }
        
        // Apply budget filter
        if (filters.budget == "custom") {
            filtered = filtered.filter { theme ->
                val price = themePriceFromThemeId(theme.themeId ?: "")
                price >= filters.minBudget && price <= filters.maxBudget
            }
        }
        
        // Apply sort
        filtered = when (filters.sortBy) {
            "price-low" -> filtered.sortedBy { themePriceFromThemeId(it.themeId ?: "") }
            "price-high" -> filtered.sortedByDescending { themePriceFromThemeId(it.themeId ?: "") }
            "rating-high", "rating-low" -> {
                // Rating sorting would require rating data - for now keep as is
                filtered
            }
            else -> filtered
        }
        
        return filtered
    }
    
    fun getFilteredInventory(): List<InventoryResponse> {
        val filters = _filterOptions.value
        val category = if (filters.category != "all") filters.category else _selectedCategory.value
        val query = _searchQuery.value.lowercase()
        
        var filtered = _inventory.value.filter { item ->
            val matchesCategory = category == "all" || item.category?.lowercase() == category.lowercase()
            val matchesSearch = query.isEmpty() || 
                item.itemName?.lowercase()?.contains(query) == true ||
                item.description?.lowercase()?.contains(query) == true ||
                item.category?.lowercase()?.contains(query) == true
            matchesCategory && matchesSearch && item.isActive
        }
        
        // Apply budget filter
        if (filters.budget == "custom") {
            filtered = filtered.filter { it.price >= filters.minBudget && it.price <= filters.maxBudget }
        }
        
        // Apply sort
        filtered = when (filters.sortBy) {
            "price-low" -> filtered.sortedBy { it.price }
            "price-high" -> filtered.sortedByDescending { it.price }
            "rating-high", "rating-low" -> filtered // Rating sorting requires rating data
            else -> filtered
        }
        
        return filtered
    }
    
    fun getFilteredBusinesses(): List<BusinessResponse> {
        val filters = _filterOptions.value
        val query = _searchQuery.value.lowercase()
        
        var filtered = _businesses.value.filter { business ->
            val matchesCategory = filters.category == "all" || 
                business.category?.lowercase() == filters.category.lowercase()
            val matchesSearch = query.isEmpty() || 
                business.businessName?.lowercase()?.contains(query) == true ||
                business.category?.lowercase()?.contains(query) == true ||
                business.description?.lowercase()?.contains(query) == true
            matchesCategory && matchesSearch && business.isActive
        }
        
        // Apply location filter (would need user location and business coordinates)
        // For now, location filtering is not implemented as it requires GPS
        
        // Apply sort
        filtered = when (filters.sortBy) {
            "price-low", "price-high" -> {
                // Sort by average price of themes/inventory
                filtered.sortedBy { business ->
                    val themes = _themes.value.filter { it.businessId == business.businessId }
                    val inventory = _inventory.value.filter { it.businessId == business.businessId }
                    val themePrices = themes.map { themePriceFromThemeId(it.themeId ?: "") }
                    val invPrices = inventory.map { it.price }
                    val allPrices = themePrices + invPrices
                    if (allPrices.isEmpty()) Double.MAX_VALUE else allPrices.average()
                }
            }
            "rating-high", "rating-low" -> filtered // Rating sorting requires rating data
            else -> filtered
        }
        
        if (filters.sortBy == "price-high") {
            filtered = filtered.reversed()
        }
        
        return filtered
    }

    fun getFilteredPlates(): List<PlateResponse> {
        val query = _searchQuery.value.lowercase()
        return _plates.value.filter { plate ->
            val matchesSearch = query.isEmpty() ||
                plate.plateName?.lowercase()?.contains(query) == true ||
                plate.description?.lowercase()?.contains(query) == true ||
                plate.category?.lowercase()?.contains(query) == true
            matchesSearch && plate.isActive
        }
    }

    fun getFilteredDishes(): List<DishResponse> {
        val query = _searchQuery.value.lowercase()
        return _dishes.value.filter { dish ->
            val matchesSearch = query.isEmpty() ||
                dish.dishName?.lowercase()?.contains(query) == true ||
                dish.description?.lowercase()?.contains(query) == true ||
                dish.dishType?.lowercase()?.contains(query) == true
            matchesSearch && dish.isActive
        }
    }
    
    fun addInventoryToCart(item: InventoryResponse) {
        if (!item.isActive) return
        val id = item.inventoryId ?: return

        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == id && it.type == "INVENTORY" }
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            val businessName = item.businessId?.let { bid ->
                _businesses.value.firstOrNull { it.businessId == bid }?.businessName
            }
            current += ExploreCartItem(
                id = id,
                name = item.itemName ?: "Inventory Item",
                price = item.price,
                type = "INVENTORY",
                businessId = item.businessId,
                businessName = businessName,
                // imageUrl is resolved later via UrlResolver when displaying
                quantity = 1
            )
        }
        _cartItems.value = current
        _cartCount.value = current.sumOf { it.quantity }
        persistCartToStorage()
    }

    fun addThemeToCart(theme: ThemeResponse) {
        if (!theme.isActive) return
        val id = theme.themeId ?: return

        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == id && it.type == "THEME" }
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            val businessName = theme.businessId?.let { bid ->
                _businesses.value.firstOrNull { it.businessId == bid }?.businessName
            }
            val numericPrice = themePriceFromThemeId(id)
            current += ExploreCartItem(
                id = id,
                name = theme.themeName ?: "Theme",
                // Web parses numeric price from priceRange; do the same so totals/checkout match
                price = numericPrice,
                type = "THEME",
                businessId = theme.businessId,
                businessName = businessName,
                quantity = 1
            )
        }
        _cartItems.value = current
        _cartCount.value = current.sumOf { it.quantity }
        persistCartToStorage()
    }

    fun addPlateToCart(plate: PlateResponse) {
        if (!plate.isActive) return
        val id = plate.plateId ?: return

        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == id && it.type == "PLATE" }
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            val businessName = plate.businessId?.let { bid ->
                _businesses.value.firstOrNull { it.businessId == bid }?.businessName
            }
            current += ExploreCartItem(
                id = id,
                name = plate.plateName ?: "Plate",
                price = plate.price,
                type = "PLATE",
                businessId = plate.businessId,
                businessName = businessName,
                quantity = 1
            )
        }
        _cartItems.value = current
        _cartCount.value = current.sumOf { it.quantity }
        persistCartToStorage()
    }

    fun addDishToCart(dish: DishResponse) {
        if (!dish.isActive) return
        val id = dish.dishId ?: return

        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == id && it.type == "DISH" }
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            current[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            val businessName = dish.businessId?.let { bid ->
                _businesses.value.firstOrNull { it.businessId == bid }?.businessName
            }
            current += ExploreCartItem(
                id = id,
                name = dish.dishName ?: "Dish",
                price = dish.price,
                type = "DISH",
                businessId = dish.businessId,
                businessName = businessName,
                quantity = 1
            )
        }
        _cartItems.value = current
        _cartCount.value = current.sumOf { it.quantity }
        persistCartToStorage()
    }

    fun increaseItemQuantity(itemId: String, itemType: String) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == itemId && it.type == itemType }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(quantity = item.quantity + 1)
            _cartItems.value = current
            _cartCount.value = current.sumOf { it.quantity }
            persistCartToStorage()
        }
    }

    fun decreaseItemQuantity(itemId: String, itemType: String) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == itemId && it.type == itemType }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity - 1
            if (newQty <= 0) current.removeAt(index)
            else current[index] = item.copy(quantity = newQty)
            _cartItems.value = current
            _cartCount.value = current.sumOf { it.quantity }
            persistCartToStorage()
        }
    }

    fun removeItemFromCart(itemId: String, itemType: String) {
        val current = _cartItems.value.toMutableList()
        current.removeAll { it.id == itemId && it.type == itemType }
        _cartItems.value = current
        _cartCount.value = current.sumOf { it.quantity }
        persistCartToStorage()
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cartCount.value = 0
        val userId = tokenManager.getUserPhone()
        if (!userId.isNullOrBlank()) cartStorage.clearCart(userId)
    }

    fun setBookingDate(itemId: String, itemType: String, bookingDate: String?) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.id == itemId && it.type == itemType }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(bookingDate = bookingDate)
            _cartItems.value = current
            persistCartToStorage()
        }
    }

    fun getCurrentUserId(): String? = tokenManager.getUserPhone()

    suspend fun getAvailableQuantity(itemId: String, itemTypeLower: String, date: String): Result<Int> {
        return availabilityRepository.getAvailableQuantity(itemId, itemTypeLower, date)
            .map { it.availableQuantity }
    }

    suspend fun isSubscribedForDate(
        userId: String,
        itemId: String,
        itemTypeUpper: String,
        date: String
    ): Result<Boolean> {
        return stockNotificationRepository.isSubscribed(
            userId = userId,
            itemId = itemId,
            itemType = itemTypeUpper,
            requestedDate = date
        )
    }

    suspend fun subscribeForDate(
        userId: String,
        itemId: String,
        itemTypeUpper: String,
        itemName: String,
        businessId: String,
        date: String
    ): Result<Unit> {
        return stockNotificationRepository.subscribe(
            com.startup.recordservice.data.model.StockSubscribeRequest(
                userId = userId,
                itemId = itemId,
                itemType = itemTypeUpper,
                itemName = itemName,
                businessId = businessId,
                requestedDate = date
            )
        ).map { Unit }
    }

    private fun themePriceFromThemeId(themeId: String): Double {
        val theme = _themes.value.firstOrNull { it.themeId == themeId } ?: return 0.0
        val range = theme.priceRange ?: return 0.0
        // Extract first numeric value (web does similar parse of priceRange)
        val match = Regex("""(\d+(\.\d+)?)""").find(range)
        return match?.value?.toDoubleOrNull() ?: 0.0
    }

    suspend fun placeOrders(
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryDate: String, // yyyy-MM-dd
        specialNotes: String?
    ): Result<List<com.startup.recordservice.data.model.OrderResponse>> {
        val userId = tokenManager.getUserPhone()
        if (userId.isNullOrBlank()) {
            return Result.failure(Exception("User not logged in"))
        }

        val cart = _cartItems.value
        if (cart.isEmpty()) return Result.failure(Exception("Cart is empty"))

        // Group by businessId (match web behavior: one order per vendor)
        val byBusiness = cart.groupBy { it.businessId ?: "" }.filterKeys { it.isNotBlank() }
        if (byBusiness.isEmpty()) return Result.failure(Exception("Cart items missing businessId"))

        return try {
            val created = mutableListOf<com.startup.recordservice.data.model.OrderResponse>()
            val failures = mutableListOf<String>()

            for ((businessId, items) in byBusiness) {
                // Availability enforcement per item (date-wise)
                for (it in items) {
                    val dateToCheck = it.bookingDate ?: deliveryDate
                    val itemTypeLower = when (it.type.uppercase()) {
                        "THEME" -> "theme"
                        "INVENTORY" -> "inventory"
                        else -> it.type.lowercase()
                    }
                    val availableQty = availabilityRepository
                        .getAvailableQuantity(it.id, itemTypeLower, dateToCheck)
                        .getOrNull()
                        ?.availableQuantity
                        ?: 0
                    if (availableQty < it.quantity) {
                        return Result.failure(
                            Exception("${it.name} not available on $dateToCheck (available: $availableQty, requested: ${it.quantity})")
                        )
                    }
                }

                val orderItems = items.map { ci ->
                    val itemTypeLower = when (ci.type.uppercase()) {
                        "THEME" -> "theme"
                        "INVENTORY" -> "inventory"
                        else -> ci.type.lowercase()
                    }
                    val numericPrice = if (itemTypeLower == "theme" && ci.price <= 0.0) {
                        themePriceFromThemeId(ci.id)
                    } else ci.price

                    OrderItemRequest(
                        itemId = ci.id,
                        itemName = ci.name,
                        itemPrice = numericPrice,
                        quantity = ci.quantity,
                        itemType = itemTypeLower,
                        businessId = businessId,
                        businessName = ci.businessName,
                        imageUrl = ci.imageUrl,
                        bookingDate = ci.bookingDate,
                        selectedDishes = null
                    )
                }

                val req = OrderRequest(
                    userId = userId,
                    items = orderItems,
                    customerName = customerName,
                    customerEmail = customerEmail,
                    customerPhone = customerPhone,
                    deliveryAddress = deliveryAddress,
                    deliveryDate = deliveryDate,
                    specialNotes = specialNotes
                )

                val result = orderRepository.createOrder(req)
                result.onSuccess { created += it }
                    .onFailure { e -> failures += (e.message ?: "Order failed for business $businessId") }
            }

            if (created.isEmpty()) {
                Result.failure(Exception(failures.joinToString("; ").ifBlank { "Failed to create order(s)" }))
            } else {
                // Clear cart on success (even partial success, match web's "partial success is ok")
                clearCart()
                Result.success(created)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // NOTE: cart mutations below are intentionally centralized above with persistence.
    
    fun refresh() {
        loadData()
    }
}
