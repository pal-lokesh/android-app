package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class DishResponse(
    val dishId: String,
    val plateId: String,
    val dishName: String,
    val description: String? = null,
    val price: Double,
    val images: List<String>? = null,
    val isOptional: Boolean = false,
    val isActive: Boolean = true
)
