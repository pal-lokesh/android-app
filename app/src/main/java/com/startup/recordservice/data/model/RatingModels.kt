package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class RatingResponse(
    val ratingId: String,
    val clientPhone: String,
    val businessId: String,
    val rating: Int,
    val comment: String? = null,
    val createdAt: String
)

data class RatingRequest(
    val clientPhone: String,
    val businessId: String,
    val rating: Int,
    val comment: String? = null
)
