package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.repository.AuthRepository
import com.startup.recordservice.data.repository.BusinessRepository
import com.startup.recordservice.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VendorProfileUiState(
    val displayName: String = "Vendor User",
    val phone: String? = null,
    val userId: String? = null,
    val businessesCount: Int = 0,
    val totalOrdersCount: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class VendorProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository,
    private val businessRepository: BusinessRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VendorProfileUiState(
            phone = tokenManager.getUserPhone(),
            userId = tokenManager.getUserId(),
            isLoading = true
        )
    )
    val uiState: StateFlow<VendorProfileUiState> = _uiState.asStateFlow()

    init {
        val phone = tokenManager.getUserPhone()
        _uiState.value = _uiState.value.copy(
            displayName = phone?.takeIf { it.isNotBlank() } ?: "Vendor User",
            phone = phone
        )
        loadStatistics()
    }
    
    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val phone = tokenManager.getUserPhone()
            if (phone.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }
            
            // Load businesses count
            val businessesResult = businessRepository.getUserBusinesses(phone)
            val businessesCount = businessesResult.getOrNull()?.size ?: 0
            
            // Load total orders count across all businesses
            var totalOrders = 0
            businessesResult.getOrNull()?.forEach { business ->
                business.businessId?.let { businessId ->
                    orderRepository.getBusinessOrders(businessId)
                        .getOrNull()?.let { orders ->
                            totalOrders += orders.size
                        }
                }
            }
            
            _uiState.value = _uiState.value.copy(
                businessesCount = businessesCount,
                totalOrdersCount = totalOrders,
                isLoading = false
            )
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.changePassword(currentPassword, newPassword)
            onResult(result)
        }
    }

    fun deleteAccount(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.deleteCurrentUser()
            onResult(result)
        }
    }
}

