package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("userType")
    val userType: String,
    @SerializedName("phoneVerified")
    val phoneVerified: Boolean = false,
    @SerializedName("emailVerified")
    val emailVerified: Boolean = false
)
