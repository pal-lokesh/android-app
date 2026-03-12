package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class ClientNotificationResponse(
    @SerializedName("notificationId")
    val notificationId: Long,
    @SerializedName("clientPhone")
    val clientPhone: String? = null,
    @SerializedName("businessId")
    val businessId: String? = null,
    @SerializedName("businessName")
    val businessName: String? = null,
    @SerializedName("orderId")
    val orderId: Long? = null,
    @SerializedName("customerName")
    val customerName: String? = null,
    @SerializedName("customerEmail")
    val customerEmail: String? = null,
    @SerializedName("customerPhone")
    val customerPhone: String? = null,
    @SerializedName("totalAmount")
    val totalAmount: Double? = null,
    @SerializedName("deliveryDate")
    val deliveryDate: String? = null,
    @SerializedName("deliveryAddress")
    val deliveryAddress: String? = null,
    @SerializedName("notificationType")
    val notificationType: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("isRead")
    val isRead: Boolean = false,
    @SerializedName("createdAt")
    val createdAt: String? = null
)

data class ClientNotificationsWithCountResponse(
    @SerializedName("notifications")
    val notifications: List<ClientNotificationResponse> = emptyList(),
    @SerializedName("unreadCount")
    val unreadCount: Int = 0
)

data class StockSubscribeRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("itemId")
    val itemId: String,
    @SerializedName("itemType")
    val itemType: String, // "THEME" | "INVENTORY" | "PLATE" | "DISH"
    @SerializedName("itemName")
    val itemName: String,
    @SerializedName("businessId")
    val businessId: String,
    @SerializedName("requestedDate")
    val requestedDate: String? = null // yyyy-MM-dd
)

data class StockNotificationResponse(
    @SerializedName("notificationId")
    val notificationId: Long? = null,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("itemId")
    val itemId: String? = null,
    @SerializedName("itemType")
    val itemType: String? = null,
    @SerializedName("itemName")
    val itemName: String? = null,
    @SerializedName("businessId")
    val businessId: String? = null,
    @SerializedName("requestedDate")
    val requestedDate: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null
)

data class StockSubscriptionCheckResponse(
    @SerializedName("subscribed")
    val subscribed: Boolean = false
)

