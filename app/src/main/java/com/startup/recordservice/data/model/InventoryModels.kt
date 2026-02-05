package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class InventoryResponse(
    val inventoryId: String,
    val businessId: String,
    val itemName: String,
    val description: String? = null,
    val price: Double,
    val quantity: Int,
    val images: List<String>? = null,
    val isActive: Boolean = true,
    val category: String? = null
)
