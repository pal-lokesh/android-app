package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class PlateResponse(
    val plateId: String? = null,
    val businessId: String? = null,
    val plateName: String? = null,
    val description: String? = null,
    val price: Double = 0.0,
    val images: List<String>? = null,
    val isActive: Boolean = true,
    val category: String? = null,
    val hasOptionalDishes: Boolean = false
)
