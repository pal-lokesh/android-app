package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class ThemeImageCreateRequest(
    @SerializedName("themeId")
    val themeId: String,
    @SerializedName("imageName")
    val imageName: String,
    @SerializedName("imageUrl")
    val imageUrl: String,
    @SerializedName("imagePath")
    val imagePath: String? = null,
    @SerializedName("imageSize")
    val imageSize: Long = 0,
    @SerializedName("imageType")
    val imageType: String? = null,
    @SerializedName("primary")
    val primary: Boolean = true,
    @SerializedName("metadata")
    val metadata: String? = null
)

data class ThemeImageResponse(
    @SerializedName("imageId")
    val imageId: String? = null,
    @SerializedName("themeId")
    val themeId: String? = null,
    @SerializedName("imageName")
    val imageName: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null
)

// Generic image + file upload models used for Android uploads
data class ImageCreateRequest(
    @SerializedName("themeId") val themeId: String? = null,
    @SerializedName("inventoryId") val inventoryId: String? = null,
    @SerializedName("imageName") val imageName: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("imagePath") val imagePath: String? = null,
    @SerializedName("imageSize") val imageSize: Long = 0,
    @SerializedName("imageType") val imageType: String? = null,
    @SerializedName("isPrimary") val isPrimary: Boolean = false
)

data class ImageResponse(
    @SerializedName("imageId") val imageId: String? = null,
    @SerializedName("themeId") val themeId: String? = null,
    @SerializedName("inventoryId") val inventoryId: String? = null,
    @SerializedName("imageName") val imageName: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("imagePath") val imagePath: String? = null,
    @SerializedName("imageSize") val imageSize: Long? = null,
    @SerializedName("imageType") val imageType: String? = null,
    @SerializedName("isPrimary") val isPrimary: Boolean? = null
)

data class FileUploadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("filePath") val filePath: String? = null,
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("fileSize") val fileSize: Long? = null
)
