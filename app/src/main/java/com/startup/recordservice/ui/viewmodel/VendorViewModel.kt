package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.BusinessResponse
import com.startup.recordservice.data.model.OrderResponse
import com.startup.recordservice.data.repository.BusinessRepository
import com.startup.recordservice.data.repository.OrderRepository
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
    private val businessRepository: BusinessRepository,
    private val orderRepository: OrderRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<VendorUiState>(VendorUiState.Idle)
    val uiState: StateFlow<VendorUiState> = _uiState.asStateFlow()
    
    private val _businesses = MutableStateFlow<List<BusinessResponse>>(emptyList())
    val businesses: StateFlow<List<BusinessResponse>> = _businesses.asStateFlow()
    
    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders: StateFlow<List<OrderResponse>> = _orders.asStateFlow()
    
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
                            
                            // Load orders for all businesses
                            loadOrdersForBusinesses(businessList)
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
    
    private suspend fun loadOrdersForBusinesses(businesses: List<BusinessResponse>) {
        try {
            // For now, we'll load user orders. In the future, we can load orders per business
            val userId = tokenManager.getUserPhone()
            if (userId != null && userId.isNotBlank()) {
                orderRepository.getUserOrders(userId)
                    .onSuccess { orderList ->
                        _orders.value = orderList
                        _uiState.value = VendorUiState.Success(
                            businesses = businesses,
                            orders = orderList
                        )
                    }
                    .onFailure { exception ->
                        android.util.Log.e("VendorViewModel", "Failed to load orders: ${exception.message}")
                        _uiState.value = VendorUiState.Success(
                            businesses = businesses,
                            orders = emptyList()
                        )
                    }
            } else {
                android.util.Log.w("VendorViewModel", "User ID is null or blank, skipping order load")
                _uiState.value = VendorUiState.Success(
                    businesses = businesses,
                    orders = emptyList()
                )
            }
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
}
