package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    @SerializedName("messageId")
    val messageId: String? = null,
    
    @SerializedName("conversationId")
    val conversationId: String? = null,
    
    @SerializedName("senderId")
    val senderId: String? = null,
    
    @SerializedName("senderType")
    val senderType: String? = null, // "CLIENT" | "VENDOR"
    
    @SerializedName("senderName")
    val senderName: String? = null,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("isRead")
    val isRead: Boolean = false
)

data class ChatConversation(
    @SerializedName("conversationId")
    val conversationId: String? = null,
    
    @SerializedName("clientPhone")
    val clientPhone: String? = null,
    
    @SerializedName("businessId")
    val businessId: String? = null,
    
    @SerializedName("businessName")
    val businessName: String? = null,
    
    @SerializedName("clientName")
    val clientName: String? = null,
    
    @SerializedName("lastMessage")
    val lastMessage: String? = null,
    
    @SerializedName("lastMessageTime")
    val lastMessageTime: String? = null,
    
    @SerializedName("unreadCount")
    val unreadCount: Int = 0,
    
    @SerializedName("createdAt")
    val createdAt: String? = null
)

data class SendMessageRequest(
    @SerializedName("conversationId")
    val conversationId: String? = null,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("clientPhone")
    val clientPhone: String? = null,
    
    @SerializedName("businessId")
    val businessId: String? = null
)

data class CreateConversationRequest(
    @SerializedName("clientPhone")
    val clientPhone: String,
    
    @SerializedName("businessId")
    val businessId: String
)
