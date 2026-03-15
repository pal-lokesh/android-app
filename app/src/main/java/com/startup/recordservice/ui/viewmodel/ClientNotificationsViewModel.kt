package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.repository.ClientNotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClientNotificationsUiState {
    data object Idle : ClientNotificationsUiState()
    data object Loading : ClientNotificationsUiState()
    data object Success : ClientNotificationsUiState()
    data class Error(val message: String) : ClientNotificationsUiState()
}

@HiltViewModel
class ClientNotificationsViewModel @Inject constructor(
    private val notificationRepository: ClientNotificationRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ClientNotificationsUiState>(ClientNotificationsUiState.Idle)
    val uiState: StateFlow<ClientNotificationsUiState> = _uiState.asStateFlow()
    
    private val _notifications = MutableStateFlow<List<com.startup.recordservice.data.model.ClientNotificationResponse>>(emptyList())
    val notifications: StateFlow<List<com.startup.recordservice.data.model.ClientNotificationResponse>> = _notifications.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    fun loadNotifications() {
        viewModelScope.launch {
            val phone = tokenManager.getUserPhone()
            if (phone.isNullOrBlank()) {
                _uiState.value = ClientNotificationsUiState.Error("User phone not available")
                return@launch
            }
            
            _uiState.value = ClientNotificationsUiState.Loading
            try {
                notificationRepository.getWithCount(phone)
                    .onSuccess { response ->
                        _notifications.value = response.notifications
                        _unreadCount.value = response.unreadCount
                        _uiState.value = ClientNotificationsUiState.Success
                    }
                    .onFailure { e ->
                        _uiState.value = ClientNotificationsUiState.Error(
                            e.message ?: "Failed to load notifications"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = ClientNotificationsUiState.Error(
                    e.message ?: "Failed to load notifications"
                )
            }
        }
    }
    
    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.markRead(notificationId)
                .onSuccess {
                    // Update local state
                    _notifications.value = _notifications.value.map { notification ->
                        if (notification.notificationId == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                    _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
                }
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            val phone = tokenManager.getUserPhone()
            if (phone.isNullOrBlank()) return@launch
            
            notificationRepository.markAllRead(phone)
                .onSuccess {
                    _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                    _unreadCount.value = 0
                }
        }
    }
}
