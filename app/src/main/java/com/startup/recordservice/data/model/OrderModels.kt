package com.startup.recordservice.data.model

import com.google.gson.annotations.SerializedName

data class OrderRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("items")
    val items: List<OrderItemRequest>,
    @SerializedName("customerName")
    val customerName: String,
    @SerializedName("customerEmail")
    val customerEmail: String,
    @SerializedName("customerPhone")
    val customerPhone: String,
    @SerializedName("deliveryAddress")
    val deliveryAddress: String,
    @SerializedName("deliveryDate")
    val deliveryDate: String,
    @SerializedName("deliveryLatitude")
    val deliveryLatitude: Double? = null,
    @SerializedName("deliveryLongitude")
    val deliveryLongitude: Double? = null,
    @SerializedName("specialNotes")
    val specialNotes: String? = null
)

data class OrderItemRequest(
    @SerializedName("itemId")
    val itemId: String,
    @SerializedName("itemName")
    val itemName: String,
    @SerializedName("itemPrice")
    val itemPrice: Double,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("itemType")
    val itemType: String, // "theme", "inventory", "plate"
    @SerializedName("businessId")
    val businessId: String,
    @SerializedName("businessName")
    val businessName: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("bookingDate")
    val bookingDate: String? = null, // Format: "yyyy-MM-dd"
    @SerializedName("selectedDishes")
    val selectedDishes: String? = null // JSON string for selected dishes
)

data class OrderResponse(
    @SerializedName("orderId")
    val orderId: Long,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("customerName")
    val customerName: String? = null,
    @SerializedName("customerEmail")
    val customerEmail: String? = null,
    @SerializedName("customerPhone")
    val customerPhone: String? = null,
    @SerializedName("orderDate")
    val orderDate: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("totalAmount")
    val totalAmount: Double,
    @SerializedName("orderItems")
    val items: List<OrderItemResponse>,
    @SerializedName("deliveryAddress")
    val deliveryAddress: String? = null,
    @SerializedName("deliveryLatitude")
    val deliveryLatitude: Double? = null,
    @SerializedName("deliveryLongitude")
    val deliveryLongitude: Double? = null,
    @SerializedName("deliveryDate")
    val deliveryDate: String? = null,
    @SerializedName("specialNotes")
    val specialNotes: String? = null
)

data class OrderItemResponse(
    val itemId: String,
    val itemName: String,
    val itemType: String,
    val businessId: String,
    val businessName: String? = null,
    val quantity: Int,
    val price: Double,
    val selectedDishes: List<String>? = null,
    val bookingDate: String? = null
)

data class UpdateStatusRequest(
    val status: String
)

data class UpdateAmountRequest(
    val newAmount: Double
)
