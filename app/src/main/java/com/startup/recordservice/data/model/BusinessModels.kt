package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class BusinessResponse(
    val businessId: String,
    val businessName: String,
    val category: String,
    val description: String? = null,
    val phoneNumber: String,
    val email: String? = null,
    val address: String? = null,
    val images: List<String>? = null,
    val isActive: Boolean = true
)
