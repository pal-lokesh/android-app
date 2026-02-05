package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class PlateResponse(
    val plateId: String,
    val businessId: String,
    val plateName: String,
    val description: String? = null,
    val price: Double,
    val images: List<String>? = null,
    val isActive: Boolean = true,
    val category: String? = null,
    val hasOptionalDishes: Boolean = false
)
