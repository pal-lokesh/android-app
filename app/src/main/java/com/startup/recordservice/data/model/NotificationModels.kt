package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class NotificationResponse(
    val notificationId: String,
    val userId: String? = null,
    val businessId: String? = null,
    val title: String,
    val message: String,
    val type: String,
    val createdAt: String,
    val read: Boolean = false
)
