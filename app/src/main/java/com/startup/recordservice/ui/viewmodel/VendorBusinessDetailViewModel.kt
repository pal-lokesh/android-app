package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.model.BusinessResponse
import com.startup.recordservice.data.model.ThemeResponse
import com.startup.recordservice.data.model.InventoryResponse
import com.startup.recordservice.data.model.OrderResponse
import com.startup.recordservice.data.model.AvailabilityResponse
import com.startup.recordservice.data.model.PlateResponse
import com.startup.recordservice.data.model.DishResponse
import com.startup.recordservice.data.repository.BusinessRepository
import com.startup.recordservice.data.repository.ThemeRepository
import com.startup.recordservice.data.repository.InventoryRepository
import com.startup.recordservice.data.repository.OrderRepository
import com.startup.recordservice.data.repository.AvailabilityRepository
import com.startup.recordservice.data.repository.PlateRepository
import com.startup.recordservice.data.repository.DishRepository
import com.startup.recordservice.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VendorBusinessDetailUiState {
    data object Idle : VendorBusinessDetailUiState()
    data object Loading : VendorBusinessDetailUiState()
    data class Success(
        val business: BusinessResponse,
        val themes: List<ThemeResponse> = emptyList(),
        val inventory: List<InventoryResponse> = emptyList(),
        val orders: List<OrderResponse> = emptyList(),
        val availability: List<AvailabilityResponse> = emptyList(),
        val plates: List<PlateResponse> = emptyList(),
        val plateDishes: Map<String, List<DishResponse>> = emptyMap()
    ) : VendorBusinessDetailUiState()
    data class Error(val message: String) : VendorBusinessDetailUiState()
}

@HiltViewModel
class VendorBusinessDetailViewModel @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val themeRepository: ThemeRepository,
    private val inventoryRepository: InventoryRepository,
    private val orderRepository: OrderRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val plateRepository: PlateRepository,
    private val dishRepository: DishRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<VendorBusinessDetailUiState>(VendorBusinessDetailUiState.Idle)
    val uiState: StateFlow<VendorBusinessDetailUiState> = _uiState.asStateFlow()
    
    private val _business = MutableStateFlow<BusinessResponse?>(null)
    val business: StateFlow<BusinessResponse?> = _business.asStateFlow()
    
    private val _themes = MutableStateFlow<List<ThemeResponse>>(emptyList())
    val themes: StateFlow<List<ThemeResponse>> = _themes.asStateFlow()
    
    private val _inventory = MutableStateFlow<List<InventoryResponse>>(emptyList())
    val inventory: StateFlow<List<InventoryResponse>> = _inventory.asStateFlow()
    
    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders: StateFlow<List<OrderResponse>> = _orders.asStateFlow()
    
    private val _availability = MutableStateFlow<List<AvailabilityResponse>>(emptyList())
    val availability: StateFlow<List<AvailabilityResponse>> = _availability.asStateFlow()
    
    private val _plates = MutableStateFlow<List<PlateResponse>>(emptyList())
    val plates: StateFlow<List<PlateResponse>> = _plates.asStateFlow()
    
    private val _plateDishes = MutableStateFlow<Map<String, List<DishResponse>>>(emptyMap())
    val plateDishes: StateFlow<Map<String, List<DishResponse>>> = _plateDishes.asStateFlow()
    
    fun loadBusinessData(businessId: String) {
        if (businessId.isBlank()) {
            android.util.Log.e("VendorBusinessDetailViewModel", "Cannot load business: ID is blank")
            _uiState.value = VendorBusinessDetailUiState.Error("Invalid business ID")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = VendorBusinessDetailUiState.Loading
                android.util.Log.d("VendorBusinessDetailViewModel", "Loading business data for ID: $businessId")
                
                // Load business details
                val businessResult = businessRepository.getBusiness(businessId)
                
                if (businessResult.isFailure) {
                    _uiState.value = VendorBusinessDetailUiState.Error(
                        businessResult.exceptionOrNull()?.message ?: "Failed to load business details"
                    )
                    return@launch
                }
                
                val business = businessResult.getOrNull()!!
                _business.value = business
                
                // Load all business-related data in parallel
                val themesResult = themeRepository.getBusinessThemes(businessId)
                val inventoryResult = inventoryRepository.getBusinessInventory(businessId)
                val ordersResult = orderRepository.getBusinessOrders(businessId)
                val availabilityResult = availabilityRepository.getAvailabilitiesForBusiness(businessId)
                val platesResult = plateRepository.getBusinessPlates(businessId)
                
                val themesList = themesResult.getOrElse { emptyList() }
                val inventoryList = inventoryResult.getOrElse { emptyList() }
                val ordersList = ordersResult.getOrElse { emptyList() }
                val availabilityList = availabilityResult.getOrElse { emptyList() }
                val platesList = platesResult.getOrElse { emptyList() }
                
                _themes.value = themesList
                _inventory.value = inventoryList
                _orders.value = ordersList
                _availability.value = availabilityList
                _plates.value = platesList
                
                // Load dishes for each plate
                val dishesMap = mutableMapOf<String, List<DishResponse>>()
                platesList.forEach { plate ->
                    val plateId = plate.plateId
                    if (plateId.isNullOrBlank()) {
                        android.util.Log.w(
                            "VendorBusinessDetailViewModel",
                            "Skipping dishes load: plateId is null or blank for plate ${plate.plateName}"
                        )
                        return@forEach
                    }
                    
                    dishRepository.getPlateDishes(plateId)
                        .onSuccess { dishes ->
                            dishesMap[plateId] = dishes
                        }
                        .onFailure { exception ->
                            android.util.Log.e(
                                "VendorBusinessDetailViewModel",
                                "Failed to load dishes for plate $plateId: ${exception.message}"
                            )
                        }
                }
                _plateDishes.value = dishesMap
                
                _uiState.value = VendorBusinessDetailUiState.Success(
                    business = business,
                    themes = themesList,
                    inventory = inventoryList,
                    orders = ordersList,
                    availability = availabilityList,
                    plates = platesList,
                    plateDishes = dishesMap
                )
                
                android.util.Log.d(
                    "VendorBusinessDetailViewModel",
                    "Loaded business: ${business.businessName}, ${themesList.size} themes, ${inventoryList.size} inventory, ${ordersList.size} orders, ${availabilityList.size} availability entries, ${platesList.size} plates"
                )
            } catch (e: Exception) {
                android.util.Log.e("VendorBusinessDetailViewModel", "Error loading business data: ${e.message}", e)
                _uiState.value = VendorBusinessDetailUiState.Error(
                    "Failed to load business data: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    fun refresh(businessId: String) {
        loadBusinessData(businessId)
    }
    
    fun deleteTheme(themeId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = themeRepository.deleteTheme(themeId)
            if (result.isSuccess) {
                // Refresh themes
                val businessId = _business.value?.businessId
                if (businessId != null) {
                    themeRepository.getBusinessThemes(businessId)
                        .onSuccess { themes ->
                            _themes.value = themes
                        }
                }
            }
            onResult(result)
        }
    }
    
    fun deleteInventory(inventoryId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = inventoryRepository.deleteInventory(inventoryId)
            if (result.isSuccess) {
                // Refresh inventory
                val businessId = _business.value?.businessId
                if (businessId != null) {
                    inventoryRepository.getBusinessInventory(businessId)
                        .onSuccess { inventory ->
                            _inventory.value = inventory
                        }
                }
            }
            onResult(result)
        }
    }
    
    fun updateBusiness(
        businessId: String,
        name: String,
        description: String,
        category: String,
        address: String,
        phone: String,
        email: String,
        onResult: (Result<BusinessResponse>) -> Unit
    ) {
        viewModelScope.launch {
            val vendorPhone = tokenManager.getUserPhone()
            val request = com.startup.recordservice.data.model.BusinessCreateRequest(
                phoneNumber = vendorPhone ?: "",
                businessName = name,
                businessDescription = description.ifBlank { null },
                businessCategory = category.ifBlank { null },
                businessAddress = address.ifBlank { null },
                businessPhone = phone.ifBlank { null },
                businessEmail = email,
                minOrderAmount = null
            )
            val result = businessRepository.updateBusiness(businessId, request, vendorPhone)
            if (result.isSuccess) {
                // Refresh business data
                loadBusinessData(businessId)
            }
            onResult(result)
        }
    }
    
    fun deleteBusiness(businessId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val vendorPhone = tokenManager.getUserPhone()
            val result = businessRepository.deleteBusiness(businessId, vendorPhone)
            onResult(result)
        }
    }
    
    fun deletePlate(plateId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = plateRepository.deletePlate(plateId)
            if (result.isSuccess) {
                // Refresh plates
                val businessId = _business.value?.businessId
                if (businessId != null) {
                    plateRepository.getBusinessPlates(businessId)
                        .onSuccess { plates ->
                            _plates.value = plates
                        }
                }
            }
            onResult(result)
        }
    }
    
    fun deleteDish(dishId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = dishRepository.deleteDish(dishId)
            if (result.isSuccess) {
                // Refresh dishes for all plates
                val businessId = _business.value?.businessId
                if (businessId != null) {
                    val platesList = _plates.value
                    val dishesMap = mutableMapOf<String, List<DishResponse>>()
                    platesList.forEach { plate ->
                        val plateId = plate.plateId
                        if (plateId != null) {
                            dishRepository.getPlateDishes(plateId)
                                .onSuccess { dishes ->
                                    dishesMap[plateId] = dishes
                                }
                        }
                    }
                    _plateDishes.value = dishesMap
                }
            }
            onResult(result)
        }
    }
    
    fun updateOrderAmount(orderId: String, newAmount: Double, onResult: (Result<OrderResponse>) -> Unit) {
        viewModelScope.launch {
            val result = orderRepository.updateOrderAmount(orderId, newAmount)
            if (result.isSuccess) {
                // Refresh orders
                val businessId = _business.value?.businessId
                if (businessId != null) {
                    orderRepository.getBusinessOrders(businessId)
                        .onSuccess { orders ->
                            _orders.value = orders
                        }
                }
            }
            onResult(result)
        }
    }
    
    fun updateOrderStatus(orderId: String, status: String, onResult: (Result<OrderResponse>) -> Unit) {
        viewModelScope.launch {
            val result = orderRepository.updateOrderStatus(orderId, status)
            if (result.isSuccess) {
                // Refresh orders
                val businessId = _business.value?.businessId
                if (businessId != null) {
                    orderRepository.getBusinessOrders(businessId)
                        .onSuccess { orders ->
                            _orders.value = orders
                        }
                }
            }
            onResult(result)
        }
    }
}
