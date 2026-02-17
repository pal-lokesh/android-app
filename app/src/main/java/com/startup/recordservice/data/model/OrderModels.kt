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
    val orderId: Long? = null,
    @SerializedName("userId")
    val userId: String? = null,
    @SerializedName("customerName")
    val customerName: String? = null,
    @SerializedName("customerEmail")
    val customerEmail: String? = null,
    @SerializedName("customerPhone")
    val customerPhone: String? = null,
    @SerializedName("orderDate")
    val orderDate: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("totalAmount")
    val totalAmount: Double? = null,
    @SerializedName("orderItems")
    val items: List<OrderItemResponse>? = null,
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
    @SerializedName("itemId")
    val itemId: String? = null,
    @SerializedName("itemName")
    val itemName: String? = null,
    @SerializedName("itemType")
    val itemType: String? = null,
    @SerializedName("businessId")
    val businessId: String? = null,
    @SerializedName("businessName")
    val businessName: String? = null,
    @SerializedName("quantity")
    val quantity: Int = 0,
    @SerializedName("itemPrice")
    val price: Double = 0.0,
    @SerializedName("selectedDishes")
    val selectedDishes: String? = null, // Backend returns as JSON string, not array
    @SerializedName("bookingDate")
    val bookingDate: String? = null
)

data class UpdateStatusRequest(
    val status: String
)

data class UpdateAmountRequest(
    val newAmount: Double
)
