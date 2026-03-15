package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.model.ChatConversation
import com.startup.recordservice.data.model.ChatMessage
import com.startup.recordservice.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class VendorChatUiState {
    data object Idle : VendorChatUiState()
    data object Loading : VendorChatUiState()
    data object Success : VendorChatUiState()
    data class Error(val message: String) : VendorChatUiState()
}

@HiltViewModel
class VendorChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<VendorChatUiState>(VendorChatUiState.Idle)
    val uiState: StateFlow<VendorChatUiState> = _uiState.asStateFlow()
    
    private val _conversation = MutableStateFlow<ChatConversation?>(null)
    val conversation: StateFlow<ChatConversation?> = _conversation.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            _uiState.value = VendorChatUiState.Loading
            
            // Load conversation details and messages
            chatRepository.getConversationMessages(conversationId)
                .onSuccess { messagesList ->
                    _messages.value = messagesList
                    _uiState.value = VendorChatUiState.Success
                    
                    // Mark conversation as read
                    chatRepository.markConversationAsRead(conversationId)
                }
                .onFailure { e ->
                    _uiState.value = VendorChatUiState.Error(
                        e.message ?: "Failed to load messages"
                    )
                }
        }
    }
    
    fun sendMessage(messageText: String) {
        val conversationId = _conversation.value?.conversationId
        if (conversationId.isNullOrBlank()) {
            _uiState.value = VendorChatUiState.Error("No active conversation")
            return
        }
        
        val businessId = _conversation.value?.businessId
        if (businessId.isNullOrBlank()) {
            _uiState.value = VendorChatUiState.Error("Business ID not available")
            return
        }
        
        viewModelScope.launch {
            // Optimistically add message
            val tempMessage = ChatMessage(
                messageId = null,
                conversationId = conversationId,
                senderId = businessId,
                senderType = "VENDOR",
                message = messageText,
                createdAt = null,
                isRead = false
            )
            _messages.value = _messages.value + tempMessage
            
            chatRepository.sendMessage(conversationId, messageText, null, businessId)
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
                    _uiState.value = VendorChatUiState.Error(
                        e.message ?: "Failed to send message"
                    )
                }
        }
    }
}
