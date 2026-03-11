package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class ThemeResponse(
    @SerializedName("themeId")
    val themeId: String? = null,
    @SerializedName("businessId")
    val businessId: String? = null,
    @SerializedName("themeName")
    val themeName: String? = null,
    @SerializedName("themeDescription")
    val themeDescription: String? = null,
    @SerializedName("themeCategory")
    val themeCategory: String? = null,
    @SerializedName("priceRange")
    val priceRange: String? = null,
    @SerializedName("quantity")
    val quantity: Int = 0,
    @SerializedName("isActive")
    val isActive: Boolean = true,
    // Optional list of image URLs for the theme (backend may use different field names)
    @SerializedName(value = "images", alternate = ["imageUrls", "themeImages", "themeImageUrls", "imageUrl"])
    val images: List<String>? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

data class ThemeRequest(
    @SerializedName("businessId")
    val businessId: String,
    @SerializedName("themeName")
    val themeName: String,
    @SerializedName("themeDescription")
    val themeDescription: String? = null,
    @SerializedName("themeCategory")
    val themeCategory: String? = null,
    @SerializedName("priceRange")
    val priceRange: String? = null,
    @SerializedName("quantity")
    val quantity: Int = 0
)
