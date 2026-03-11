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
    @SerializedName(value = "images", alternate = ["imageUrls", "inventoryImages", "inventoryImageUrls", "imageUrl"])
    val images: List<String>? = null,
    @SerializedName("isActive")
    val isActive: Boolean = true,
    @SerializedName("inventoryCategory")
    val category: String? = null
)

// Request payload for creating an inventory item
data class InventoryCreateRequest(
    @SerializedName("businessId")
    val businessId: String,
    @SerializedName("inventoryName")
    val inventoryName: String,
    @SerializedName("inventoryDescription")
    val inventoryDescription: String,
    @SerializedName("inventoryCategory")
    val inventoryCategory: String,
    @SerializedName("price")
    val price: Double,
    @SerializedName("quantity")
    val quantity: Int = 0,
    @SerializedName("isActive")
    val isActive: Boolean = true
)
