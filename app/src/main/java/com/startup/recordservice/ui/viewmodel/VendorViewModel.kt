package com.startup.recordservice.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.BusinessResponse
import com.startup.recordservice.data.model.BusinessCreateRequest
import com.startup.recordservice.data.model.InventoryCreateRequest
import com.startup.recordservice.data.model.InventoryResponse
import com.startup.recordservice.data.model.OrderResponse
import com.startup.recordservice.data.model.ThemeRequest
import com.startup.recordservice.data.model.ThemeResponse
import com.startup.recordservice.data.repository.BusinessRepository
import com.startup.recordservice.data.repository.ImageRepository
import com.startup.recordservice.data.repository.InventoryRepository
import com.startup.recordservice.data.repository.InventoryImageRepository
import com.startup.recordservice.data.repository.OrderRepository
import com.startup.recordservice.data.repository.ThemeRepository
import com.startup.recordservice.data.repository.PlateRepository
import com.startup.recordservice.data.repository.DishRepository
import com.startup.recordservice.data.model.PlateResponse
import com.startup.recordservice.data.model.DishResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VendorUiState {
    data object Idle : VendorUiState()
    data object Loading : VendorUiState()
    data class Success(
        val businesses: List<BusinessResponse> = emptyList(),
        val orders: List<OrderResponse> = emptyList()
    ) : VendorUiState()
    data class Error(val message: String) : VendorUiState()
}

@HiltViewModel
class VendorViewModel @Inject constructor(
    application: Application,
    private val businessRepository: BusinessRepository,
    private val orderRepository: OrderRepository,
    private val inventoryRepository: InventoryRepository,
    private val themeRepository: ThemeRepository,
    private val plateRepository: PlateRepository,
    private val dishRepository: DishRepository,
    private val imageRepository: ImageRepository,
    private val inventoryImageRepository: InventoryImageRepository,
    private val tokenManager: TokenManager
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow<VendorUiState>(VendorUiState.Idle)
    val uiState: StateFlow<VendorUiState> = _uiState.asStateFlow()
    
    private val _businesses = MutableStateFlow<List<BusinessResponse>>(emptyList())
    val businesses: StateFlow<List<BusinessResponse>> = _businesses.asStateFlow()
    
    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders: StateFlow<List<OrderResponse>> = _orders.asStateFlow()
    
    // Inventory grouped by businessId for vendor inventory tab
    private val _inventoryByBusiness =
        MutableStateFlow<Map<String, List<InventoryResponse>>>(emptyMap())
    val inventoryByBusiness: StateFlow<Map<String, List<InventoryResponse>>> =
        _inventoryByBusiness.asStateFlow()

    // Themes grouped by businessId for vendor theme tab
    private val _themesByBusiness =
        MutableStateFlow<Map<String, List<ThemeResponse>>>(emptyMap())
    val themesByBusiness: StateFlow<Map<String, List<ThemeResponse>>> =
        _themesByBusiness.asStateFlow()

    // ThemeId -> first image URL (for vendor dashboard)
    private val _themeImageUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val themeImageUrls: StateFlow<Map<String, String>> = _themeImageUrls.asStateFlow()

    // InventoryId -> first image URL (for vendor dashboard)
    private val _inventoryImageUrls = MutableStateFlow<Map<String, String>>(emptyMap())
    val inventoryImageUrls: StateFlow<Map<String, String>> = _inventoryImageUrls.asStateFlow()

    // Image selection state for create screens
    private val _themeImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val themeImageUris: StateFlow<List<Uri>> = _themeImageUris.asStateFlow()

    private val _inventoryImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val inventoryImageUris: StateFlow<List<Uri>> = _inventoryImageUris.asStateFlow()
    
    init {
        // Don't load data in init - wait for explicit call after navigation
    }
    
    fun loadData() {
        viewModelScope.launch {
            try {
                // Check if user is logged in before loading data
                if (!tokenManager.isLoggedIn()) {
                    android.util.Log.w("VendorViewModel", "User not logged in, skipping data load")
                    _uiState.value = VendorUiState.Error("User not logged in")
                    return@launch
                }
                
                _uiState.value = VendorUiState.Loading
                
                // Load vendor's businesses
                val phoneNumber = tokenManager.getUserPhone()
                if (phoneNumber != null && phoneNumber.isNotBlank()) {
                    businessRepository.getUserBusinesses(phoneNumber)
                        .onSuccess { businessList ->
                            _businesses.value = businessList
                            android.util.Log.d("VendorViewModel", "Loaded ${businessList.size} businesses")
                            
                            // Load orders, inventory and themes for all businesses
                            loadOrdersForBusinesses(businessList)
                            loadInventoryForBusinesses(businessList)
                            loadThemesForBusinesses(businessList)
                        }
                        .onFailure { exception ->
                            android.util.Log.e("VendorViewModel", "Failed to load businesses: ${exception.message}")
                            _uiState.value = VendorUiState.Error(
                                exception.message ?: "Failed to load businesses"
                            )
                        }
                } else {
                    android.util.Log.w("VendorViewModel", "User phone number is null or blank")
                    _uiState.value = VendorUiState.Error("User not logged in")
                }
            } catch (e: Exception) {
                android.util.Log.e("VendorViewModel", "Error loading data: ${e.message}", e)
                _uiState.value = VendorUiState.Error(
                    "Failed to load data: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    private suspend fun loadInventoryForBusinesses(businesses: List<BusinessResponse>) {
        try {
            val inventoryMap = mutableMapOf<String, List<InventoryResponse>>()
            val imageUrlMap = mutableMapOf<String, String>()
            
            for (business in businesses) {
                val businessId = business.businessId
                if (businessId.isNullOrBlank()) {
                    continue
                }
                
                inventoryRepository.getBusinessInventory(businessId)
                    .onSuccess { items ->
                        inventoryMap[businessId] = items
                        android.util.Log.d(
                            "VendorViewModel",
                            "Loaded ${items.size} inventory items for business $businessId"
                        )
                        // For each item, try to load its primary image
                        items.forEach { inv ->
                            val invId = inv.inventoryId
                            if (!invId.isNullOrBlank()) {
                                inventoryImageRepository.getInventoryImagesByInventoryId(invId)
                                    .onSuccess { images ->
                                        val primary = images.firstOrNull { it.isPrimary == true } ?: images.firstOrNull()
                                        val url = primary?.imageUrl
                                        if (!url.isNullOrBlank()) {
                                            imageUrlMap[invId] = url
                                        }
                                    }
                                    .onFailure { e ->
                                        android.util.Log.e("VendorViewModel", "Failed to load inventory images for $invId: ${e.message}")
                                    }
                            }
                        }
                    }
                    .onFailure { exception ->
                        android.util.Log.e(
                            "VendorViewModel",
                            "Failed to load inventory for $businessId: ${exception.message}"
                        )
                    }
            }
            
            _inventoryByBusiness.value = inventoryMap
            _inventoryImageUrls.value = imageUrlMap
        } catch (e: Exception) {
            android.util.Log.e("VendorViewModel", "Error loading inventory: ${e.message}", e)
        }
    }

    private suspend fun loadThemesForBusinesses(businesses: List<BusinessResponse>) {
        try {
            val themeMap = mutableMapOf<String, List<ThemeResponse>>()
            val imageUrlMap = mutableMapOf<String, String>()

            for (business in businesses) {
                val businessId = business.businessId
                if (businessId.isNullOrBlank()) continue

                themeRepository.getBusinessThemes(businessId)
                    .onSuccess { themes ->
                        themeMap[businessId] = themes
                        android.util.Log.d(
                            "VendorViewModel",
                            "Loaded ${themes.size} themes for business $businessId"
                        )
                        // For each theme, try to load its primary image
                        themes.forEach { theme ->
                            val themeId = theme.themeId
                            if (!themeId.isNullOrBlank()) {
                                imageRepository.getImagesByThemeId(themeId)
                                    .onSuccess { images ->
                                        val primary = images.firstOrNull { it.isPrimary == true } ?: images.firstOrNull()
                                        val url = primary?.imageUrl
                                        if (!url.isNullOrBlank()) {
                                            imageUrlMap[themeId] = url
                                        }
                                    }
                                    .onFailure { e ->
                                        android.util.Log.e("VendorViewModel", "Failed to load theme images for $themeId: ${e.message}")
                                    }
                            }
                        }
                    }
                    .onFailure { exception ->
                        android.util.Log.e(
                            "VendorViewModel",
                            "Failed to load themes for $businessId: ${exception.message}"
                        )
                    }
            }

            _themesByBusiness.value = themeMap
            _themeImageUrls.value = imageUrlMap
        } catch (e: Exception) {
            android.util.Log.e("VendorViewModel", "Error loading themes: ${e.message}", e)
        }
    }
    
    private suspend fun loadOrdersForBusinesses(businesses: List<BusinessResponse>) {
        try {
            // Match web behavior: vendors see orders per business, not per phone/user
            val allOrders = mutableListOf<OrderResponse>()

            for (business in businesses) {
                val businessId = business.businessId
                if (businessId.isNullOrBlank()) continue

                orderRepository.getBusinessOrders(businessId)
                    .onSuccess { ordersForBusiness ->
                        android.util.Log.d(
                            "VendorViewModel",
                            "Loaded ${ordersForBusiness.size} orders for business $businessId"
                        )
                        allOrders += ordersForBusiness
                    }
                    .onFailure { e ->
                        android.util.Log.e(
                            "VendorViewModel",
                            "Failed to load orders for business $businessId: ${e.message}"
                        )
                    }
            }

            _orders.value = allOrders
            _uiState.value = VendorUiState.Success(
                businesses = businesses,
                orders = allOrders
            )
        } catch (e: Exception) {
            android.util.Log.e("VendorViewModel", "Error loading orders: ${e.message}", e)
            _uiState.value = VendorUiState.Success(
                businesses = businesses,
                orders = emptyList()
            )
        }
    }
    
    fun refresh() {
        loadData()
    }
    
    fun getCurrentUserId(): String? {
        return tokenManager.getUserPhone()
    }

    fun updateOrderStatus(
        orderId: Long,
        newStatus: String,
        onResult: (Result<OrderResponse>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = orderRepository.updateOrderStatus(orderId.toString(), newStatus)
                result
                    .onSuccess { updated ->
                        // Refresh orders so dashboards reflect new status
                        loadData()
                        onResult(Result.success(updated))
                    }
                    .onFailure { e ->
                        onResult(Result.failure(e))
                    }
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun setThemeImageUris(uris: List<Uri>) {
        _themeImageUris.value = uris.take(10)
    }

    fun setInventoryImageUris(uris: List<Uri>) {
        _inventoryImageUris.value = uris.take(10)
    }

    fun removeThemeImage(uri: Uri) {
        _themeImageUris.value = _themeImageUris.value.filterNot { it == uri }
    }

    fun removeInventoryImage(uri: Uri) {
        _inventoryImageUris.value = _inventoryImageUris.value.filterNot { it == uri }
    }

    fun clearThemeImages() {
        _themeImageUris.value = emptyList()
    }

    fun clearInventoryImages() {
        _inventoryImageUris.value = emptyList()
    }

    fun updateInventory(
        inventoryId: String,
        request: InventoryCreateRequest,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = VendorUiState.Loading
                inventoryRepository.updateInventory(inventoryId, request)
                    .onSuccess {
                        loadData()
                        onSuccess()
                    }
                    .onFailure { e ->
                        _uiState.value = VendorUiState.Error(e.message ?: "Failed to update inventory")
                    }
            } catch (e: Exception) {
                _uiState.value = VendorUiState.Error(e.message ?: "Failed to update inventory")
            }
        }
    }

    fun deleteInventory(
        inventoryId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = VendorUiState.Loading
                inventoryRepository.deleteInventory(inventoryId)
                    .onSuccess {
                        loadData()
                        onSuccess()
                    }
                    .onFailure { e ->
                        _uiState.value = VendorUiState.Error(e.message ?: "Failed to delete inventory")
                    }
            } catch (e: Exception) {
                _uiState.value = VendorUiState.Error(e.message ?: "Failed to delete inventory")
            }
        }
    }

    fun updateTheme(
        themeId: String,
        request: ThemeRequest,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = VendorUiState.Loading
                themeRepository.updateTheme(themeId, request)
                    .onSuccess {
                        loadData()
                        onSuccess()
                    }
                    .onFailure { e ->
                        _uiState.value = VendorUiState.Error(e.message ?: "Failed to update theme")
                    }
            } catch (e: Exception) {
                _uiState.value = VendorUiState.Error(e.message ?: "Failed to update theme")
            }
        }
    }

    fun deleteTheme(
        themeId: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = VendorUiState.Loading
                themeRepository.deleteTheme(themeId)
                    .onSuccess {
                        loadData()
                        onSuccess()
                    }
                    .onFailure { e ->
                        _uiState.value = VendorUiState.Error(e.message ?: "Failed to delete theme")
                    }
            } catch (e: Exception) {
                _uiState.value = VendorUiState.Error(e.message ?: "Failed to delete theme")
            }
        }
    }

    fun createBusiness(
        request: BusinessCreateRequest,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = VendorUiState.Loading
                businessRepository.createBusiness(request)
                    .onSuccess {
                        // Reload data so new business appears on dashboard
                        loadData()
                        onSuccess()
                    }
                    .onFailure { exception ->
                        _uiState.value = VendorUiState.Error(
                            exception.message ?: "Failed to create business"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = VendorUiState.Error(
                    "Failed to create business: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun updateBusiness(
        business: BusinessResponse,
        updatedName: String,
        updatedDescription: String?,
        updatedCategory: String?,
        updatedAddress: String?,
        updatedPhone: String?,
        updatedEmail: String?,
        onResult: (Result<Unit>) -> Unit
    ) {
        val businessId = business.businessId.orEmpty()
        if (businessId.isBlank()) {
            onResult(Result.failure(Exception("Business ID missing")))
            return
        }
        val vendorPhone = tokenManager.getUserPhone()
        val req = BusinessCreateRequest(
            phoneNumber = vendorPhone ?: business.phoneNumber.orEmpty(),
            businessName = updatedName,
            businessDescription = updatedDescription ?: business.description,
            businessCategory = updatedCategory ?: business.category,
            businessAddress = updatedAddress ?: business.address,
            businessPhone = updatedPhone ?: business.phoneNumber,
            businessEmail = updatedEmail ?: business.email.orEmpty(),
            minOrderAmount = null
        )
        viewModelScope.launch {
            val result = businessRepository.updateBusiness(businessId, req, vendorPhone)
            result
                .onSuccess {
                    loadData()
                    onResult(Result.success(Unit))
                }
                .onFailure { e ->
                    _uiState.value = VendorUiState.Error(e.message ?: "Failed to update business")
                    onResult(Result.failure(e))
                }
        }
    }

    fun deleteBusiness(
        businessId: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val vendorPhone = tokenManager.getUserPhone()
        viewModelScope.launch {
            val result = businessRepository.deleteBusiness(businessId, vendorPhone)
            result
                .onSuccess {
                    loadData()
                    onResult(Result.success(Unit))
                }
                .onFailure { e ->
                    _uiState.value = VendorUiState.Error(e.message ?: "Failed to delete business")
                    onResult(Result.failure(e))
                }
        }
    }

    fun createInventory(
        request: InventoryCreateRequest,
        imageUris: List<Uri>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = VendorUiState.Loading
                inventoryRepository.createInventory(request)
                    .onSuccess { createdInventory ->
                        val context = getApplication<Application>().applicationContext
                        val inventoryId = createdInventory.inventoryId

                        if (inventoryId != null && imageUris.isNotEmpty()) {
                            imageUris.forEachIndexed { index, uri ->
                                val imageName = "${request.inventoryName}_${index}"
                                imageRepository.uploadFile(context, uri, "inventory", inventoryId)
                                    .onSuccess { imageUrl ->
                                        val imageRequest = com.startup.recordservice.data.model.InventoryImageRequest(
                                            inventoryId = inventoryId,
                                            imageName = imageName,
                                            imageUrl = imageUrl,
                                            isPrimary = index == 0
                                        )
                                        inventoryImageRepository.createInventoryImage(imageRequest)
                                            .onFailure { e ->
                                                android.util.Log.e("VendorViewModel", "Failed to create image record for inventory $inventoryId: ${e.message}")
                                            }
                                    }
                                    .onFailure { e ->
                                        android.util.Log.e("VendorViewModel", "Failed to upload image for inventory $inventoryId: ${e.message}")
                                    }
                            }
                        }

                        loadData()
                        clearInventoryImages()
                        onSuccess()
                    }
                    .onFailure { exception ->
                        _uiState.value = VendorUiState.Error(
                            exception.message ?: "Failed to create inventory item"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = VendorUiState.Error(
                    "Failed to create inventory item: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun createTheme(
        request: ThemeRequest,
        imageUris: List<Uri>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = VendorUiState.Loading
                themeRepository.createTheme(request)
                    .onSuccess { createdTheme ->
                        val themeId = createdTheme.themeId
                        val context = getApplication<Application>().applicationContext

                        if (themeId != null && imageUris.isNotEmpty()) {
                            imageUris.forEachIndexed { index, uri ->
                                val imageName = "${request.themeName}_${index}"
                                imageRepository.uploadFile(context, uri, "themes", themeId)
                                    .onSuccess { imageUrl ->
                                        val imageRequest = com.startup.recordservice.data.model.ImageCreateRequest(
                                            themeId = themeId,
                                            imageName = imageName,
                                            imageUrl = imageUrl,
                                            isPrimary = index == 0
                                        )
                                        imageRepository.createImage(imageRequest)
                                            .onFailure { e ->
                                                android.util.Log.e("VendorViewModel", "Failed to create image record for theme $themeId: ${e.message}")
                                            }
                                    }
                                    .onFailure { e ->
                                        android.util.Log.e("VendorViewModel", "Failed to upload image for theme $themeId: ${e.message}")
                                    }
                            }
                        }

                        loadData()
                        clearThemeImages()
                        onSuccess()
                    }
                    .onFailure { exception ->
                        _uiState.value = VendorUiState.Error(
                            exception.message ?: "Failed to create theme"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = VendorUiState.Error(
                    "Failed to create theme: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun updateThemeImages(
        themeId: String,
        imageUris: List<Uri>
    ) {
        viewModelScope.launch {
            if (themeId.isBlank() || imageUris.isEmpty()) return@launch
            val context = getApplication<Application>().applicationContext
            imageUris.forEachIndexed { index, uri ->
                val imageName = "theme_${themeId}_$index"
                imageRepository.uploadFile(context, uri, "themes", themeId)
                    .onSuccess { imageUrl ->
                        val imageRequest = com.startup.recordservice.data.model.ImageCreateRequest(
                            themeId = themeId,
                            imageName = imageName,
                            imageUrl = imageUrl,
                            isPrimary = index == 0
                        )
                        imageRepository.createImage(imageRequest)
                            .onFailure { e ->
                                android.util.Log.e("VendorViewModel", "Failed to create image record for theme $themeId: ${e.message}")
                            }
                    }
                    .onFailure { e ->
                        android.util.Log.e("VendorViewModel", "Failed to upload image for theme $themeId: ${e.message}")
                    }
            }
            loadData()
        }
    }

    fun updateInventoryImages(
        inventoryId: String,
        imageUris: List<Uri>
    ) {
        viewModelScope.launch {
            if (inventoryId.isBlank() || imageUris.isEmpty()) return@launch
            val context = getApplication<Application>().applicationContext
            imageUris.forEachIndexed { index, uri ->
                val imageName = "inventory_${inventoryId}_$index"
                imageRepository.uploadFile(context, uri, "inventory", inventoryId)
                    .onSuccess { imageUrl ->
                        val imageRequest = com.startup.recordservice.data.model.InventoryImageRequest(
                            inventoryId = inventoryId,
                            imageName = imageName,
                            imageUrl = imageUrl,
                            isPrimary = index == 0
                        )
                        inventoryImageRepository.createInventoryImage(imageRequest)
                            .onFailure { e ->
                                android.util.Log.e("VendorViewModel", "Failed to create image record for inventory $inventoryId: ${e.message}")
                            }
                    }
                    .onFailure { e ->
                        android.util.Log.e("VendorViewModel", "Failed to upload image for inventory $inventoryId: ${e.message}")
                    }
            }
            loadData()
        }
    }

    fun createPlate(
        request: PlateResponse,
        imageUris: List<Uri>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = VendorUiState.Loading
                plateRepository.createPlate(request)
                    .onSuccess { createdPlate ->
                        val plateId = createdPlate.plateId
                        if (plateId != null && imageUris.isNotEmpty()) {
                            val context = getApplication<Application>().applicationContext
                            imageUris.forEachIndexed { index, uri ->
                                val imageName = "plate_${plateId}_$index"
                                imageRepository.uploadFile(context, uri, "plates", plateId)
                                    .onSuccess { imageUrl ->
                                        android.util.Log.d("VendorViewModel", "Successfully uploaded plate image: $imageUrl")
                                        // Note: Backend may handle plate image records automatically via upload endpoint
                                        // If separate image record creation is needed, it would go here
                                    }
                                    .onFailure { e ->
                                        android.util.Log.e("VendorViewModel", "Failed to upload image for plate $plateId: ${e.message}")
                                    }
                            }
                        }
                        loadData()
                        _uiState.value = VendorUiState.Success()
                        onSuccess()
                    }
                    .onFailure { e ->
                        _uiState.value = VendorUiState.Error(
                            "Failed to create plate: ${e.message ?: "Unknown error"}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = VendorUiState.Error(
                    "Failed to create plate: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun createDish(
        request: DishResponse,
        imageUris: List<Uri>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = VendorUiState.Loading
                dishRepository.createDish(request)
                    .onSuccess { createdDish ->
                        val dishId = createdDish.dishId
                        if (dishId != null && imageUris.isNotEmpty()) {
                            val context = getApplication<Application>().applicationContext
                            imageUris.forEachIndexed { index, uri ->
                                val imageName = "dish_${dishId}_$index"
                                imageRepository.uploadFile(context, uri, "dishes", dishId)
                                    .onSuccess { imageUrl ->
                                        android.util.Log.d("VendorViewModel", "Successfully uploaded dish image: $imageUrl")
                                        // Note: Backend may handle dish image records automatically via upload endpoint
                                        // If separate image record creation is needed, it would go here
                                    }
                                    .onFailure { e ->
                                        android.util.Log.e("VendorViewModel", "Failed to upload image for dish $dishId: ${e.message}")
                                    }
                            }
                        }
                        loadData()
                        _uiState.value = VendorUiState.Success()
                        onSuccess()
                    }
                    .onFailure { e ->
                        _uiState.value = VendorUiState.Error(
                            "Failed to create dish: ${e.message ?: "Unknown error"}"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = VendorUiState.Error(
                    "Failed to create dish: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
}
