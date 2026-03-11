package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class InventoryImageRequest(
    @SerializedName("inventoryId")
    val inventoryId: String,
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
    val primary: Boolean? = null,
    @SerializedName("isPrimary")
    val isPrimary: Boolean? = null
)

data class InventoryImageResponse(
    @SerializedName("imageId")
    val imageId: String? = null,
    @SerializedName("inventoryId")
    val inventoryId: String? = null,
    @SerializedName("imageName")
    val imageName: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("imagePath")
    val imagePath: String? = null,
    @SerializedName("imageSize")
    val imageSize: Long? = null,
    @SerializedName("imageType")
    val imageType: String? = null,
    @SerializedName("isPrimary")
    val isPrimary: Boolean? = null,
    @SerializedName("uploadedAt")
    val uploadedAt: String? = null,
    @SerializedName("metadata")
    val metadata: String? = null
)

