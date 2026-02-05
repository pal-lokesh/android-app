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
