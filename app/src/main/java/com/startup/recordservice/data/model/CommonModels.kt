package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class CheckResponse(
    @SerializedName("exists")
    val exists: Boolean
)

data class MessageResponse(
    @SerializedName("message")
    val message: String
)

data class ResetPasswordRequest(
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("method")
    val method: String,
    @SerializedName("newPassword")
    val newPassword: String
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword")
    val currentPassword: String,
    @SerializedName("newPassword")
    val newPassword: String
)

data class SendOtpRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    @SerializedName("email")
    val email: String? = null
)

data class VerifyOtpRequest(
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,
    @SerializedName("email")
    val email: String? = null,
    // Backend expects field name "code" for the 6-digit OTP
    @SerializedName("code")
    val code: String
)

data class OtpResponse(
    @SerializedName("message")
    val message: String? = null,
    // For verify endpoints backend may return {"verified": true}
    @SerializedName("verified")
    val verified: Boolean? = null
)