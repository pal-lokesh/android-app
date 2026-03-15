package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.model.ChatConversation
import com.startup.recordservice.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VendorConversationsUiState {
    data object Idle : VendorConversationsUiState()
    data object Loading : VendorConversationsUiState()
    data object Success : VendorConversationsUiState()
    data class Error(val message: String) : VendorConversationsUiState()
}

@HiltViewModel
class VendorConversationsViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<VendorConversationsUiState>(VendorConversationsUiState.Idle)
    val uiState: StateFlow<VendorConversationsUiState> = _uiState.asStateFlow()
    
    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()
    
    fun loadConversations(businessId: String) {
        viewModelScope.launch {
            _uiState.value = VendorConversationsUiState.Loading
            try {
                chatRepository.getVendorConversations(businessId)
                    .onSuccess { conversationsList ->
                        _conversations.value = conversationsList.sortedByDescending { 
                            it.lastMessageTime 
                        }
                        _uiState.value = VendorConversationsUiState.Success
                    }
                    .onFailure { e ->
                        _uiState.value = VendorConversationsUiState.Error(
                            e.message ?: "Failed to load conversations"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = VendorConversationsUiState.Error(
                    e.message ?: "Failed to load conversations"
                )
            }
        }
    }
}
