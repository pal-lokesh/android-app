package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class BusinessResponse(
    @SerializedName("businessId")
    val businessId: String? = null,
    @SerializedName("businessName")
    val businessName: String? = null,
    @SerializedName("businessCategory")
    val category: String? = null,
    @SerializedName("businessDescription")
    val description: String? = null,
    @SerializedName("businessPhone")
    val phoneNumber: String? = null,
    @SerializedName("businessEmail")
    val email: String? = null,
    @SerializedName("businessAddress")
    val address: String? = null,
    @SerializedName("images")
    val images: List<String>? = null,
    @SerializedName("isActive")
    val isActive: Boolean = true
)

// Request payload for creating a business (matches backend Business entity field names)
data class BusinessCreateRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("businessName")
    val businessName: String,
    @SerializedName("businessDescription")
    val businessDescription: String? = null,
    @SerializedName("businessCategory")
    val businessCategory: String? = null,
    @SerializedName("businessAddress")
    val businessAddress: String? = null,
    @SerializedName("businessPhone")
    val businessPhone: String? = null,
    @SerializedName("businessEmail")
    val businessEmail: String,
    @SerializedName("minOrderAmount")
    val minOrderAmount: Double? = null
)