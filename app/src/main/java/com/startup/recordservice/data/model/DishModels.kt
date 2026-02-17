package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class DishResponse(
    val dishId: String? = null,
    val plateId: String? = null,
    val dishName: String? = null,
    val description: String? = null,
    val price: Double = 0.0,
    val images: List<String>? = null,
    val isOptional: Boolean = false,
    val isActive: Boolean = true
)
