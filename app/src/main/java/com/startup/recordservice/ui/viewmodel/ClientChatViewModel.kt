package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.ChatConversation
import com.startup.recordservice.data.model.ChatMessage
import com.startup.recordservice.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClientChatUiState {
    data object Idle : ClientChatUiState()
    data object Loading : ClientChatUiState()
    data object Success : ClientChatUiState()
    data class Error(val message: String) : ClientChatUiState()
}

@HiltViewModel
class ClientChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ClientChatUiState>(ClientChatUiState.Idle)
    val uiState: StateFlow<ClientChatUiState> = _uiState.asStateFlow()
    
    private val _conversation = MutableStateFlow<ChatConversation?>(null)
    val conversation: StateFlow<ChatConversation?> = _conversation.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    fun loadOrCreateConversation(businessId: String) {
        viewModelScope.launch {
            val phone = tokenManager.getUserPhone()
            if (phone.isNullOrBlank()) {
                _uiState.value = ClientChatUiState.Error("User phone not available")
                return@launch
            }
            
            _uiState.value = ClientChatUiState.Loading
            
            // Try to find existing conversation
            chatRepository.getClientConversations(phone)
                .onSuccess { conversations ->
                    val existingConversation = conversations.find { it.businessId == businessId }
                    if (existingConversation != null) {
                        _conversation.value = existingConversation
                        loadMessages(existingConversation.conversationId ?: return@onSuccess)
                    } else {
                        // Create new conversation
                        chatRepository.createConversation(phone, businessId)
                            .onSuccess { newConversation ->
                                _conversation.value = newConversation
                                _messages.value = emptyList()
                                _uiState.value = ClientChatUiState.Success
                            }
                            .onFailure { e ->
                                _uiState.value = ClientChatUiState.Error(
                                    e.message ?: "Failed to create conversation"
                                )
                            }
                    }
                }
                .onFailure { e ->
                    // Try to create conversation anyway
                    chatRepository.createConversation(phone, businessId)
                        .onSuccess { newConversation ->
                            _conversation.value = newConversation
                            _messages.value = emptyList()
                            _uiState.value = ClientChatUiState.Success
                        }
                        .onFailure { error ->
                            _uiState.value = ClientChatUiState.Error(
                                error.message ?: "Failed to load conversation"
                            )
                        }
                }
        }
    }
    
    private fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            chatRepository.getConversationMessages(conversationId)
                .onSuccess { messagesList ->
                    _messages.value = messagesList
                    _uiState.value = ClientChatUiState.Success
                }
                .onFailure { e ->
                    _uiState.value = ClientChatUiState.Error(
                        e.message ?: "Failed to load messages"
                    )
                }
        }
    }
    
    fun sendMessage(messageText: String) {
        val conversationId = _conversation.value?.conversationId
        if (conversationId.isNullOrBlank()) {
            _uiState.value = ClientChatUiState.Error("No active conversation")
            return
        }
        
        val phone = tokenManager.getUserPhone()
        if (phone.isNullOrBlank()) {
            _uiState.value = ClientChatUiState.Error("User phone not available")
            return
        }
        
        viewModelScope.launch {
            // Optimistically add message
            val tempMessage = ChatMessage(
                messageId = null,
                conversationId = conversationId,
                senderId = phone,
                senderType = "CLIENT",
                message = messageText,
                createdAt = null,
                isRead = false
            )
            _messages.value = _messages.value + tempMessage
            
            chatRepository.sendMessage(conversationId, messageText, phone, null)
                .onSuccess { sentMessage ->
                    // Replace temp message with actual message
                    _messages.value = _messages.value.map { msg ->
                        if (msg.messageId == null && msg.message == messageText) {
                            sentMessage
                        } else {
                            msg
                        }
                    }
                }
                .onFailure { e ->
                    // Remove failed message
                    _messages.value = _messages.value.filter { it.messageId != null }
                    _uiState.value = ClientChatUiState.Error(
                        e.message ?: "Failed to send message"
                    )
                }
        }
    }
}
