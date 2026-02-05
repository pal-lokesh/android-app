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

sealed class ClientUiState {
    data object Idle : ClientUiState()
    data object Loading : ClientUiState()
    data class Success(
        val businesses: List<BusinessResponse> = emptyList(),
        val orders: List<OrderResponse> = emptyList()
    ) : ClientUiState()
    data class Error(val message: String) : ClientUiState()
}

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val orderRepository: OrderRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ClientUiState>(ClientUiState.Idle)
    val uiState: StateFlow<ClientUiState> = _uiState.asStateFlow()
    
    private val _businesses = MutableStateFlow<List<BusinessResponse>>(emptyList())
    val businesses: StateFlow<List<BusinessResponse>> = _businesses.asStateFlow()
    
    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders: StateFlow<List<OrderResponse>> = _orders.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = ClientUiState.Loading
                
                // Load businesses
                businessRepository.getAllBusinesses()
                    .onSuccess { businessList ->
                        _businesses.value = businessList
                    }
                    .onFailure { exception ->
                        android.util.Log.e("ClientViewModel", "Failed to load businesses: ${exception.message}")
                        // Continue even if businesses fail
                    }
                
                // Load orders
                val userId = tokenManager.getUserPhone()
                if (userId != null && userId.isNotBlank()) {
                    orderRepository.getUserOrders(userId)
                        .onSuccess { orderList ->
                            _orders.value = orderList
                        }
                        .onFailure { exception ->
                            android.util.Log.e("ClientViewModel", "Failed to load orders: ${exception.message}")
                            // Continue even if orders fail
                        }
                } else {
                    android.util.Log.w("ClientViewModel", "User ID is null or blank, skipping order load")
                }
                
                _uiState.value = ClientUiState.Success(
                    businesses = _businesses.value,
                    orders = _orders.value
                )
            } catch (e: Exception) {
                android.util.Log.e("ClientViewModel", "Error loading data: ${e.message}", e)
                _uiState.value = ClientUiState.Error(
                    "Failed to load data: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    fun refresh() {
        loadData()
    }
    
    fun getCurrentUserId(): String? {
        return tokenManager.getUserPhone()
    }
}
