package com.startup.recordservice.data.api

import com.startup.recordservice.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Authentication
    @POST("auth/signin")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>
    
    @GET("auth/check-phone/{phoneNumber}")
    suspend fun checkPhone(@Path("phoneNumber") phoneNumber: String): Response<CheckResponse>
    
    @GET("auth/check-email/{email}")
    suspend fun checkEmail(@Path("email") email: String): Response<CheckResponse>
    
    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>
    
    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>
    
    // Orders
    @GET("orders/user/{userId}")
    suspend fun getUserOrders(@Path("userId") userId: String): Response<List<OrderResponse>>
    
    @POST("orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<OrderResponse>
    
    @PUT("orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body request: UpdateStatusRequest
    ): Response<OrderResponse>
    
    @PUT("orders/{orderId}/amount")
    suspend fun updateOrderAmount(
        @Path("orderId") orderId: String,
        @Body request: UpdateAmountRequest
    ): Response<OrderResponse>
    
    // Business
    @GET("businesses")
    suspend fun getAllBusinesses(): Response<List<BusinessResponse>>
    
    @GET("businesses/{businessId}")
    suspend fun getBusiness(@Path("businessId") businessId: String): Response<BusinessResponse>
    
    @GET("businesses/user/{phoneNumber}")
    suspend fun getUserBusinesses(@Path("phoneNumber") phoneNumber: String): Response<List<BusinessResponse>>
    
    // Plates
    @GET("plates/business/{businessId}")
    suspend fun getBusinessPlates(@Path("businessId") businessId: String): Response<List<PlateResponse>>
    
    @GET("plates/{plateId}")
    suspend fun getPlate(@Path("plateId") plateId: String): Response<PlateResponse>
    
    // Dishes
    @GET("dishes/plate/{plateId}")
    suspend fun getPlateDishes(@Path("plateId") plateId: String): Response<List<DishResponse>>
    
    // Inventory
    @GET("inventory")
    suspend fun getAllInventory(): Response<List<InventoryResponse>>
    
    @GET("inventory/business/{businessId}")
    suspend fun getBusinessInventory(@Path("businessId") businessId: String): Response<List<InventoryResponse>>
    
    // User
    @GET("users/{phoneNumber}")
    suspend fun getUser(@Path("phoneNumber") phoneNumber: String): Response<UserResponse>
    
    @DELETE("users/{phoneNumber}")
    suspend fun deleteUser(@Path("phoneNumber") phoneNumber: String): Response<MessageResponse>
    
    // Notifications
    @GET("notifications/client/{phoneNumber}")
    suspend fun getClientNotifications(@Path("phoneNumber") phoneNumber: String): Response<List<NotificationResponse>>
    
    @GET("notifications/vendor/{businessId}")
    suspend fun getVendorNotifications(@Path("businessId") businessId: String): Response<List<NotificationResponse>>
    
    // Ratings
    @GET("ratings/client/{phoneNumber}")
    suspend fun getClientRatings(@Path("phoneNumber") phoneNumber: String): Response<List<RatingResponse>>
    
    @POST("ratings")
    suspend fun createRating(@Body request: RatingRequest): Response<RatingResponse>
    
    // Products
    @GET("products")
    suspend fun getProducts(): Response<List<com.startup.recordservice.data.model.Product>>
    
    // Themes
    @GET("themes")
    suspend fun getAllThemes(): Response<List<ThemeResponse>>
    
    @GET("themes/business/{businessId}")
    suspend fun getBusinessThemes(@Path("businessId") businessId: String): Response<List<ThemeResponse>>
    
    @GET("themes/{themeId}")
    suspend fun getTheme(@Path("themeId") themeId: String): Response<ThemeResponse>
    
    @GET("themes/category/{category}")
    suspend fun getThemesByCategory(@Path("category") category: String): Response<List<ThemeResponse>>
    
    @POST("themes")
    suspend fun createTheme(@Body request: ThemeRequest): Response<ThemeResponse>
    
    @PUT("themes/{themeId}")
    suspend fun updateTheme(
        @Path("themeId") themeId: String,
        @Body request: ThemeRequest
    ): Response<ThemeResponse>
    
    @DELETE("themes/{themeId}")
    suspend fun deleteTheme(@Path("themeId") themeId: String): Response<MessageResponse>
}
