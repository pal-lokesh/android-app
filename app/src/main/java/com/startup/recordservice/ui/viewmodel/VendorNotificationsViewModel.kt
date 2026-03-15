package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.model.NotificationResponse
import com.startup.recordservice.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VendorNotificationsUiState {
    data object Idle : VendorNotificationsUiState()
    data object Loading : VendorNotificationsUiState()
    data object Success : VendorNotificationsUiState()
    data class Error(val message: String) : VendorNotificationsUiState()
}

@HiltViewModel
class VendorNotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<VendorNotificationsUiState>(VendorNotificationsUiState.Idle)
    val uiState: StateFlow<VendorNotificationsUiState> = _uiState.asStateFlow()
    
    private val _notifications = MutableStateFlow<List<NotificationResponse>>(emptyList())
    val notifications: StateFlow<List<NotificationResponse>> = _notifications.asStateFlow()
    
    fun loadNotifications(businessId: String) {
        viewModelScope.launch {
            _uiState.value = VendorNotificationsUiState.Loading
            try {
                notificationRepository.getVendorNotifications(businessId)
                    .onSuccess { notificationsList ->
                        _notifications.value = notificationsList.sortedByDescending { 
                            it.createdAt 
                        }
                        _uiState.value = VendorNotificationsUiState.Success
                    }
                    .onFailure { e ->
                        _uiState.value = VendorNotificationsUiState.Error(
                            e.message ?: "Failed to load notifications"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = VendorNotificationsUiState.Error(
                    e.message ?: "Failed to load notifications"
                )
            }
        }
    }
}
