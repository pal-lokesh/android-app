package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.model.BusinessResponse
import com.startup.recordservice.data.model.PlateResponse
import com.startup.recordservice.data.model.DishResponse
import com.startup.recordservice.data.model.InventoryResponse
import com.startup.recordservice.data.repository.BusinessRepository
import com.startup.recordservice.data.repository.PlateRepository
import com.startup.recordservice.data.repository.DishRepository
import com.startup.recordservice.data.repository.InventoryRepository
import com.startup.recordservice.data.repository.OrderRepository
import com.startup.recordservice.data.repository.AvailabilityRepository
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.OrderRequest
import com.startup.recordservice.data.model.OrderItemRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BusinessDetailUiState {
    data object Idle : BusinessDetailUiState()
    data object Loading : BusinessDetailUiState()
    data class Success(
        val business: BusinessResponse,
        val plates: List<PlateResponse> = emptyList(),
        val inventory: List<InventoryResponse> = emptyList()
    ) : BusinessDetailUiState()
    data class Error(val message: String) : BusinessDetailUiState()
}

data class BusinessCartItem(
    val id: String,
    val name: String,
    val price: Double,
    val type: String, // "INVENTORY", "PLATE", "DISH"
    val businessId: String,
    val businessName: String?,
    val quantity: Int = 1,
    val bookingDate: String? = null
)

@HiltViewModel
class BusinessDetailViewModel @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val plateRepository: PlateRepository,
    private val dishRepository: DishRepository,
    private val inventoryRepository: InventoryRepository,
    private val orderRepository: OrderRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<BusinessDetailUiState>(BusinessDetailUiState.Idle)
    val uiState: StateFlow<BusinessDetailUiState> = _uiState.asStateFlow()
    
    private val _business = MutableStateFlow<BusinessResponse?>(null)
    val business: StateFlow<BusinessResponse?> = _business.asStateFlow()
    
    private val _plates = MutableStateFlow<List<PlateResponse>>(emptyList())
    val plates: StateFlow<List<PlateResponse>> = _plates.asStateFlow()
    
    private val _inventory = MutableStateFlow<List<InventoryResponse>>(emptyList())
    val inventory: StateFlow<List<InventoryResponse>> = _inventory.asStateFlow()
    
    private val _plateDishes = MutableStateFlow<Map<String, List<DishResponse>>>(emptyMap())
    val plateDishes: StateFlow<Map<String, List<DishResponse>>> = _plateDishes.asStateFlow()
    
    private val _cartItems = MutableStateFlow<List<BusinessCartItem>>(emptyList())
    val cartItems: StateFlow<List<BusinessCartItem>> = _cartItems.asStateFlow()
    
    fun loadBusiness(businessId: String) {
        if (businessId.isBlank()) {
            android.util.Log.e("BusinessDetailViewModel", "Cannot load business: ID is blank")
            _uiState.value = BusinessDetailUiState.Error("Invalid business ID")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = BusinessDetailUiState.Loading
                android.util.Log.d("BusinessDetailViewModel", "Loading business with ID: $businessId")
                
                // Load business details
                val businessResult = businessRepository.getBusiness(businessId)
                
                if (businessResult.isFailure) {
                    _uiState.value = BusinessDetailUiState.Error(
                        businessResult.exceptionOrNull()?.message ?: "Failed to load business details"
                    )
                    return@launch
                }
                
                val business = businessResult.getOrNull()!!
                _business.value = business
                
                // Load plates, inventory, and dishes in parallel
                val platesResult = plateRepository.getBusinessPlates(businessId)
                val inventoryResult = inventoryRepository.getBusinessInventory(businessId)
                
                val platesList = platesResult.getOrElse { emptyList() }
                val inventoryList = inventoryResult.getOrElse { emptyList() }
                
                _plates.value = platesList
                _inventory.value = inventoryList
                
                // Load dishes for each plate
                val dishesMap = mutableMapOf<String, List<DishResponse>>()
                platesList.forEach { plate ->
                    val plateId = plate.plateId
                    if (plateId.isNullOrBlank()) {
                        android.util.Log.w(
                            "BusinessDetailViewModel",
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
                                "BusinessDetailViewModel",
                                "Failed to load dishes for plate $plateId: ${exception.message}"
                            )
                        }
                }
                _plateDishes.value = dishesMap
                
                _uiState.value = BusinessDetailUiState.Success(
                    business = business,
                    plates = platesList,
                    inventory = inventoryList
                )
                
                android.util.Log.d("BusinessDetailViewModel", "Loaded business: ${business.businessName}, ${platesList.size} plates, ${inventoryList.size} inventory items")
            } catch (e: Exception) {
                android.util.Log.e("BusinessDetailViewModel", "Error loading business: ${e.message}", e)
                _uiState.value = BusinessDetailUiState.Error(
                    "Failed to load business: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    fun addInventoryToCart(inventory: InventoryResponse, businessId: String, businessName: String?) {
        val item = BusinessCartItem(
            id = inventory.inventoryId ?: "",
            name = inventory.itemName ?: "Unknown Item",
            price = inventory.price ?: 0.0,
            type = "INVENTORY",
            businessId = businessId,
            businessName = businessName,
            quantity = 1
        )
        val current = _cartItems.value.toMutableList()
        current.add(item)
        _cartItems.value = current
    }
    
    fun addPlateToCart(plate: PlateResponse, businessId: String, businessName: String?) {
        val item = BusinessCartItem(
            id = plate.plateId ?: "",
            name = plate.plateName ?: "Unknown Plate",
            price = plate.price ?: 0.0,
            type = "PLATE",
            businessId = businessId,
            businessName = businessName,
            quantity = 1
        )
        val current = _cartItems.value.toMutableList()
        current.add(item)
        _cartItems.value = current
    }
    
    fun addDishToCart(dish: DishResponse, businessId: String, businessName: String?) {
        val item = BusinessCartItem(
            id = dish.dishId ?: "",
            name = dish.dishName ?: "Unknown Dish",
            price = dish.price ?: 0.0,
            type = "DISH",
            businessId = businessId,
            businessName = businessName,
            quantity = 1
        )
        val current = _cartItems.value.toMutableList()
        current.add(item)
        _cartItems.value = current
    }
    
    fun removeCartItem(index: Int) {
        val current = _cartItems.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _cartItems.value = current
        }
    }
    
    fun clearCart() {
        _cartItems.value = emptyList()
    }
    
    suspend fun placeOrder(
        customerName: String,
        customerEmail: String,
        customerPhone: String,
        deliveryAddress: String,
        deliveryDate: String,
        specialNotes: String?
    ): Result<com.startup.recordservice.data.model.OrderResponse> {
        val userId = tokenManager.getUserPhone()
        if (userId.isNullOrBlank()) {
            return Result.failure(Exception("User not logged in"))
        }
        
        val cart = _cartItems.value
        if (cart.isEmpty()) return Result.failure(Exception("Cart is empty"))
        
        val businessId = cart.firstOrNull()?.businessId ?: return Result.failure(Exception("Invalid business ID"))
        
        // Check availability for each item
        for (item in cart) {
            val dateToCheck = item.bookingDate ?: deliveryDate
            val itemTypeLower = when (item.type.uppercase()) {
                "INVENTORY" -> "inventory"
                "PLATE" -> "plate"
                "DISH" -> "dish"
                else -> item.type.lowercase()
            }
            val availableQty = availabilityRepository
                .getAvailableQuantity(item.id, itemTypeLower, dateToCheck)
                .getOrNull()
                ?.availableQuantity
                ?: 0
            if (availableQty < item.quantity) {
                return Result.failure(
                    Exception("${item.name} not available on $dateToCheck (available: $availableQty, requested: ${item.quantity})")
                )
            }
        }
        
        val orderItems = cart.map { ci ->
            val itemTypeLower = when (ci.type.uppercase()) {
                "INVENTORY" -> "inventory"
                "PLATE" -> "plate"
                "DISH" -> "dish"
                else -> ci.type.lowercase()
            }
            
            OrderItemRequest(
                itemId = ci.id,
                itemName = ci.name,
                itemPrice = ci.price,
                quantity = ci.quantity,
                itemType = itemTypeLower,
                businessId = businessId,
                businessName = ci.businessName,
                imageUrl = null,
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
        if (result.isSuccess) {
            clearCart()
        }
        return result
    }
}
