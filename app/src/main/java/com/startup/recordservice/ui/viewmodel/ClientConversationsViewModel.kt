package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.ChatConversation
import com.startup.recordservice.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClientConversationsUiState {
    data object Idle : ClientConversationsUiState()
    data object Loading : ClientConversationsUiState()
    data object Success : ClientConversationsUiState()
    data class Error(val message: String) : ClientConversationsUiState()
}

@HiltViewModel
class ClientConversationsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ClientConversationsUiState>(ClientConversationsUiState.Idle)
    val uiState: StateFlow<ClientConversationsUiState> = _uiState.asStateFlow()
    
    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()
    
    fun loadConversations() {
        viewModelScope.launch {
            val phone = tokenManager.getUserPhone()
            if (phone.isNullOrBlank()) {
                _uiState.value = ClientConversationsUiState.Error("User phone not available")
                return@launch
            }
            
            _uiState.value = ClientConversationsUiState.Loading
            try {
                chatRepository.getClientConversations(phone)
                    .onSuccess { conversationsList ->
                        _conversations.value = conversationsList.sortedByDescending { 
                            it.lastMessageTime 
                        }
                        _uiState.value = ClientConversationsUiState.Success
                    }
                    .onFailure { e ->
                        _uiState.value = ClientConversationsUiState.Error(
                            e.message ?: "Failed to load conversations"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = ClientConversationsUiState.Error(
                    e.message ?: "Failed to load conversations"
                )
            }
        }
    }
}
