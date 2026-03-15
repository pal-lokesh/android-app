package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.StockSubscribeRequest
import com.startup.recordservice.data.repository.StockNotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StockNotificationViewModel @Inject constructor(
    private val stockNotificationRepository: StockNotificationRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun checkSubscription(itemId: String, itemType: String, requestedDate: String?) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: tokenManager.getUserPhone() ?: return@launch
            _isLoading.value = true
            stockNotificationRepository.isSubscribed(userId, itemId, itemType, requestedDate)
                .onSuccess { subscribed ->
                    _isSubscribed.value = subscribed
                }
                .onFailure {
                    // Default to false if check fails
                    _isSubscribed.value = false
                }
            _isLoading.value = false
        }
    }
    
    fun subscribe(itemId: String, itemType: String, itemName: String, businessId: String, requestedDate: String?) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: tokenManager.getUserPhone() ?: return@launch
            _isLoading.value = true
            val request = StockSubscribeRequest(
                userId = userId,
                itemId = itemId,
                itemType = itemType,
                itemName = itemName,
                businessId = businessId,
                requestedDate = requestedDate
            )
            stockNotificationRepository.subscribe(request)
                .onSuccess {
                    _isSubscribed.value = true
                }
                .onFailure {
                    // Handle error - could show snackbar
                }
            _isLoading.value = false
        }
    }
    
    fun unsubscribe(itemId: String, itemType: String, requestedDate: String?) {
        viewModelScope.launch {
            val userId = tokenManager.getUserId() ?: tokenManager.getUserPhone() ?: return@launch
            _isLoading.value = true
            stockNotificationRepository.unsubscribe(userId, itemId, itemType, requestedDate)
                .onSuccess {
                    _isSubscribed.value = false
                }
                .onFailure {
                    // Handle error - could show snackbar
                }
            _isLoading.value = false
        }
    }
}
