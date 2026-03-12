package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.OrderResponse
import com.startup.recordservice.data.repository.OrderRepository
import com.startup.recordservice.data.repository.RatingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClientOrdersUiState {
    data object Idle : ClientOrdersUiState()
    data object Loading : ClientOrdersUiState()
    data class Error(val message: String) : ClientOrdersUiState()
    data object Ready : ClientOrdersUiState()
}

@HiltViewModel
class ClientOrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val tokenManager: TokenManager,
    private val ratingRepository: RatingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClientOrdersUiState>(ClientOrdersUiState.Idle)
    val uiState: StateFlow<ClientOrdersUiState> = _uiState.asStateFlow()

    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders: StateFlow<List<OrderResponse>> = _orders.asStateFlow()

    fun loadOrders() {
        val userId = tokenManager.getUserPhone()
        if (userId.isNullOrBlank()) {
            _uiState.value = ClientOrdersUiState.Error("User not authenticated")
            _orders.value = emptyList()
            return
        }

        viewModelScope.launch {
            _uiState.value = ClientOrdersUiState.Loading
            orderRepository.getUserOrders(userId)
                .onSuccess { list ->
                    _orders.value = list
                    _uiState.value = ClientOrdersUiState.Ready
                }
                .onFailure { e ->
                    _orders.value = emptyList()
                    _uiState.value = ClientOrdersUiState.Error(e.message ?: "Failed to fetch orders")
                }
        }
    }

    fun refresh() = loadOrders()

    fun cancelOrder(orderId: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = ClientOrdersUiState.Loading
            orderRepository.updateOrderStatus(orderId, "CANCELLED")
                .onSuccess {
                    loadOrders()
                    onDone?.invoke()
                }
                .onFailure { e ->
                    _uiState.value = ClientOrdersUiState.Error(e.message ?: "Failed to cancel order")
                }
        }
    }

    fun submitRating(
        businessId: String,
        stars: Int,
        comment: String?,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = ratingRepository.submitRating(businessId, stars, comment)
            onResult(result)
        }
    }
}

