package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class InventoryResponse(
    @SerializedName("inventoryId")
    val inventoryId: String? = null,
    @SerializedName("businessId")
    val businessId: String? = null,
    @SerializedName("inventoryName")
    val itemName: String? = null,
    @SerializedName("inventoryDescription")
    val description: String? = null,
    @SerializedName("price")
    val price: Double = 0.0,
    @SerializedName("quantity")
    val quantity: Int = 0,
    @SerializedName("images")
    val images: List<String>? = null,
    @SerializedName("isActive")
    val isActive: Boolean = true,
    @SerializedName("inventoryCategory")
    val category: String? = null
)
