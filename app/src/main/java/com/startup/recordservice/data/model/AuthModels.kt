package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("type")
    val type: String? = "Bearer",
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

data class SignupRequest(
    @SerializedName("firstName")
    val firstName: String,
    @SerializedName("lastName")
    val lastName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("userType")
    val userType: String
)

data class SignupResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("user")
    val user: UserResponse
)

data class ApiError(
    @SerializedName("error")
    val error: String?,
    @SerializedName("message")
    val message: String?
)
