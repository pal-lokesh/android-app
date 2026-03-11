package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class PlateResponse(
    @SerializedName(value = "plateId", alternate = ["id"])
    val plateId: String? = null,
    @SerializedName("businessId")
    val businessId: String? = null,

    // Backend uses dishName/dishDescription; old app UI used plateName/description
    @SerializedName(value = "dishName", alternate = ["plateName", "name"])
    val plateName: String? = null,
    @SerializedName(value = "dishDescription", alternate = ["description"])
    val description: String? = null,

    @SerializedName("plateImage")
    val plateImage: String? = null,

    @SerializedName("price")
    val price: Double = 0.0,

    @SerializedName(value = "images", alternate = ["imageUrls", "plateImages", "plateImageUrls", "imageUrl"])
    val images: List<String>? = null,

    @SerializedName(value = "dishType", alternate = ["category"])
    val category: String? = null,

    @SerializedName("quantity")
    val quantity: Int? = null,

    @SerializedName("isActive")
    val isActive: Boolean = true,

    // Legacy flag from earlier iterations; backend does not use it
    @SerializedName("hasOptionalDishes")
    val hasOptionalDishes: Boolean = false,

    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
