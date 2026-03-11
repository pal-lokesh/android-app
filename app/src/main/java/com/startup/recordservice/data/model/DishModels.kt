package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class DishResponse(
    @SerializedName(value = "dishId", alternate = ["id"])
    val dishId: String? = null,

    // Backend uses businessId; legacy app used plateId relation
    @SerializedName(value = "businessId", alternate = ["vendorId"])
    val businessId: String? = null,

    @SerializedName("plateId")
    val plateId: String? = null,

    @SerializedName(value = "dishName", alternate = ["name"])
    val dishName: String? = null,
    @SerializedName(value = "dishDescription", alternate = ["description"])
    val description: String? = null,

    @SerializedName("dishImage")
    val dishImage: String? = null,

    @SerializedName("price")
    val price: Double = 0.0,

    @SerializedName(value = "dishType", alternate = ["category"])
    val dishType: String? = null,

    @SerializedName("quantity")
    val quantity: Int? = null,

    @SerializedName("isAvailable")
    val isAvailable: Boolean? = null,

    @SerializedName(value = "images", alternate = ["imageUrls", "dishImages", "dishImageUrls", "imageUrl"])
    val images: List<String>? = null,

    // Legacy flags not present in backend
    @SerializedName("isOptional")
    val isOptional: Boolean = false,
    @SerializedName(value = "isActive", alternate = ["active"])
    val isActive: Boolean = true,

    @SerializedName("availabilityDates")
    val availabilityDates: List<String>? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)
