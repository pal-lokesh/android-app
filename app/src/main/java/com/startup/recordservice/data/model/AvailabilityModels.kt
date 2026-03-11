package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class AvailabilityRequest(
    @SerializedName("itemId")
    val itemId: String,
    @SerializedName("itemType")
    val itemType: String, // "theme", "inventory", "plate"
    @SerializedName("businessId")
    val businessId: String,
    // Backend uses LocalDate; Gson will serialize/deserialize ISO date strings
    @SerializedName("availabilityDate")
    val availabilityDate: String, // yyyy-MM-dd
    @SerializedName("availableQuantity")
    val availableQuantity: Int,
    @SerializedName("isAvailable")
    val isAvailable: Boolean? = true,
    @SerializedName("priceOverride")
    val priceOverride: Double? = null
)

data class AvailabilityResponse(
    @SerializedName("availabilityId")
    val availabilityId: Long? = null,
    @SerializedName("itemId")
    val itemId: String? = null,
    @SerializedName("itemType")
    val itemType: String? = null,
    @SerializedName("businessId")
    val businessId: String? = null,
    @SerializedName("availabilityDate")
    val availabilityDate: String? = null, // yyyy-MM-dd
    @SerializedName("availableQuantity")
    val availableQuantity: Int? = null,
    @SerializedName("isAvailable")
    val isAvailable: Boolean? = null,
    @SerializedName("priceOverride")
    val priceOverride: Double? = null
)

data class CheckAvailabilityRequest(
    @SerializedName("itemId")
    val itemId: String,
    @SerializedName("itemType")
    val itemType: String, // "theme", "inventory", "plate"
    @SerializedName("date")
    val date: String, // yyyy-MM-dd
    @SerializedName("quantity")
    val quantity: Int
)

data class AvailabilityCheckResponse(
    @SerializedName("isAvailable")
    val isAvailable: Boolean = false
)

data class AvailableQuantityResponse(
    @SerializedName("availableQuantity")
    val availableQuantity: Int = 0
)

