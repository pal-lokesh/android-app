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
    
    // Signup OTP Verification (matches backend VerificationController)
    // POST /api/verification/signup/email/send
    @POST("verification/signup/email/send")
    suspend fun sendSignupEmailOtp(@Body request: SendOtpRequest): Response<OtpResponse>
    
    // POST /api/verification/signup/phone/send
    @POST("verification/signup/phone/send")
    suspend fun sendSignupPhoneOtp(@Body request: SendOtpRequest): Response<OtpResponse>
    
    // POST /api/verification/signup/email/verify
    @POST("verification/signup/email/verify")
    suspend fun verifySignupEmailOtp(@Body request: VerifyOtpRequest): Response<OtpResponse>
    
    // POST /api/verification/signup/phone/verify
    @POST("verification/signup/phone/verify")
    suspend fun verifySignupPhoneOtp(@Body request: VerifyOtpRequest): Response<OtpResponse>
    
    // Orders
    @GET("orders/user/{userId}")
    suspend fun getUserOrders(@Path("userId") userId: String): Response<List<OrderResponse>>

    // Vendor-side: get orders for a specific business (matches web: GET /orders/business/{businessId})
    @GET("orders/business/{businessId}")
    suspend fun getBusinessOrders(@Path("businessId") businessId: String): Response<List<OrderResponse>>
    
    @POST("orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<OrderResponse>
    
    @PUT("orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body request: UpdateStatusRequest
    ): Response<OrderResponse>

    // Web uses query param: PUT /api/orders/{orderId}/status?status=CANCELLED
    @PUT("orders/{orderId}/status")
    suspend fun updateOrderStatusQuery(
        @Path("orderId") orderId: String,
        @Query("status") status: String
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
    
    // Backend exposes /api/businesses/vendor/{phoneNumber} returning a list of businesses for a vendor
    @GET("businesses/vendor/{phoneNumber}")
    suspend fun getBusinessesByVendorPhone(@Path("phoneNumber") phoneNumber: String): Response<List<BusinessResponse>>
    
    @POST("businesses")
    suspend fun createBusiness(@Body request: BusinessCreateRequest): Response<BusinessResponse>

    @PUT("businesses/{businessId}")
    suspend fun updateBusiness(
        @Path("businessId") businessId: String,
        @Body request: BusinessCreateRequest,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<BusinessResponse>

    @DELETE("businesses/{businessId}")
    suspend fun deleteBusiness(
        @Path("businessId") businessId: String,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<MessageResponse>
    
    // Plates
    @GET("plates")
    suspend fun getAllPlates(): Response<List<PlateResponse>>

    @GET("plates/business/{businessId}")
    suspend fun getBusinessPlates(@Path("businessId") businessId: String): Response<List<PlateResponse>>
    
    @GET("plates/{plateId}")
    suspend fun getPlate(@Path("plateId") plateId: String): Response<PlateResponse>

    @POST("plates")
    suspend fun createPlate(
        @Body request: PlateResponse,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<PlateResponse>

    @PUT("plates/{plateId}")
    suspend fun updatePlate(
        @Path("plateId") plateId: String,
        @Body request: PlateResponse,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<PlateResponse>

    @DELETE("plates/{plateId}")
    suspend fun deletePlate(
        @Path("plateId") plateId: String,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<MessageResponse>
    
    // Dishes
    @GET("dishes")
    suspend fun getAllDishes(): Response<List<DishResponse>>

    @GET("dishes/{dishId}")
    suspend fun getDish(@Path("dishId") dishId: String): Response<DishResponse>

    @GET("dishes/business/{businessId}")
    suspend fun getBusinessDishes(@Path("businessId") businessId: String): Response<List<DishResponse>>

    @GET("dishes/plate/{plateId}")
    suspend fun getPlateDishes(@Path("plateId") plateId: String): Response<List<DishResponse>>

    @POST("dishes")
    suspend fun createDish(
        @Body request: DishResponse,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<DishResponse>

    @PUT("dishes/{dishId}")
    suspend fun updateDish(
        @Path("dishId") dishId: String,
        @Body request: DishResponse,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<DishResponse>

    @DELETE("dishes/{dishId}")
    suspend fun deleteDish(
        @Path("dishId") dishId: String,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<MessageResponse>

    // Availability
    @POST("availability")
    suspend fun createOrUpdateAvailability(@Body request: AvailabilityRequest): Response<AvailabilityResponse>

    @GET("availability/item/{itemId}/type/{itemType}/date/{date}")
    suspend fun getAvailability(
        @Path("itemId") itemId: String,
        @Path("itemType") itemType: String,
        @Path("date") date: String
    ): Response<AvailabilityResponse>

    @GET("availability/item/{itemId}/type/{itemType}")
    suspend fun getAvailabilitiesForItem(
        @Path("itemId") itemId: String,
        @Path("itemType") itemType: String
    ): Response<List<AvailabilityResponse>>

    @GET("availability/item/{itemId}/type/{itemType}/range")
    suspend fun getAvailabilitiesInRange(
        @Path("itemId") itemId: String,
        @Path("itemType") itemType: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Response<List<AvailabilityResponse>>

    @GET("availability/business/{businessId}")
    suspend fun getAvailabilitiesForBusiness(@Path("businessId") businessId: String): Response<List<AvailabilityResponse>>

    @POST("availability/check")
    suspend fun checkAvailability(@Body request: CheckAvailabilityRequest): Response<AvailabilityCheckResponse>

    @GET("availability/item/{itemId}/type/{itemType}/date/{date}/quantity")
    suspend fun getAvailableQuantity(
        @Path("itemId") itemId: String,
        @Path("itemType") itemType: String,
        @Path("date") date: String
    ): Response<AvailableQuantityResponse>

    @DELETE("availability/item/{itemId}/type/{itemType}/date/{date}")
    suspend fun deleteAvailability(
        @Path("itemId") itemId: String,
        @Path("itemType") itemType: String,
        @Path("date") date: String
    ): Response<MessageResponse>

    @DELETE("availability/item/{itemId}/type/{itemType}")
    suspend fun deleteAllAvailabilitiesForItem(
        @Path("itemId") itemId: String,
        @Path("itemType") itemType: String
    ): Response<MessageResponse>

    // Inventory Images
    @POST("inventory/images")
    suspend fun createInventoryImage(
        @Body request: InventoryImageRequest,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<InventoryImageResponse>

    @GET("inventory/images/inventory/{inventoryId}")
    suspend fun getInventoryImagesByInventoryId(@Path("inventoryId") inventoryId: String): Response<List<InventoryImageResponse>>

    @PUT("inventory/images/{imageId}")
    suspend fun updateInventoryImage(
        @Path("imageId") imageId: String,
        @Body request: InventoryImageRequest,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<InventoryImageResponse>

    @DELETE("inventory/images/{imageId}")
    suspend fun deleteInventoryImage(
        @Path("imageId") imageId: String,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<MessageResponse>

    @PUT("inventory/images/{imageId}/primary")
    suspend fun setPrimaryInventoryImage(
        @Path("imageId") imageId: String,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<InventoryImageResponse>
    
    // Inventory
    @GET("inventory")
    suspend fun getAllInventory(): Response<List<InventoryResponse>>
    
    @GET("inventory/business/{businessId}")
    suspend fun getBusinessInventory(@Path("businessId") businessId: String): Response<List<InventoryResponse>>
    
    @POST("inventory")
    suspend fun createInventory(
        @Body request: InventoryCreateRequest,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<InventoryResponse>

    @PUT("inventory/{inventoryId}")
    suspend fun updateInventory(
        @Path("inventoryId") inventoryId: String,
        @Body request: InventoryCreateRequest,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<InventoryResponse>

    @DELETE("inventory/{inventoryId}")
    suspend fun deleteInventory(
        @Path("inventoryId") inventoryId: String,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<MessageResponse>
    
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

    // Client Notifications (date-wise stock + order events) - matches backend ClientNotificationController
    @GET("client-notifications/client/{clientPhone}")
    suspend fun getClientNotificationsByClient(@Path("clientPhone") clientPhone: String): Response<List<ClientNotificationResponse>>

    @GET("client-notifications/client/{clientPhone}/unread-count")
    suspend fun getClientUnreadCount(@Path("clientPhone") clientPhone: String): Response<Int>

    @PUT("client-notifications/{notificationId}/mark-read")
    suspend fun markClientNotificationRead(@Path("notificationId") notificationId: Long): Response<MessageResponse>

    @PUT("client-notifications/client/{clientPhone}/mark-all-read")
    suspend fun markAllClientNotificationsRead(@Path("clientPhone") clientPhone: String): Response<MessageResponse>

    @DELETE("client-notifications/{notificationId}")
    suspend fun deleteClientNotification(@Path("notificationId") notificationId: Long): Response<MessageResponse>

    @GET("client-notifications/client/{clientPhone}/recent")
    suspend fun getClientRecentNotifications(@Path("clientPhone") clientPhone: String): Response<List<ClientNotificationResponse>>

    @GET("client-notifications/client/{clientPhone}/with-count")
    suspend fun getClientNotificationsWithCount(@Path("clientPhone") clientPhone: String): Response<ClientNotificationsWithCountResponse>
    
    // Ratings
    @GET("ratings/client/{phoneNumber}")
    suspend fun getClientRatings(@Path("phoneNumber") phoneNumber: String): Response<List<RatingResponse>>
    
    @POST("ratings")
    suspend fun createRating(@Body request: RatingRequest): Response<RatingResponse>

    // Stock notifications (subscribe when date-wise availability is 0)
    @POST("stock-notifications/subscribe")
    suspend fun subscribeStockNotification(@Body request: StockSubscribeRequest): Response<StockNotificationResponse>

    @GET("stock-notifications/check")
    suspend fun checkStockSubscription(
        @Query("userId") userId: String,
        @Query("itemId") itemId: String,
        @Query("itemType") itemType: String,
        @Query("requestedDate") requestedDate: String? = null
    ): Response<StockSubscriptionCheckResponse>
    
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
    suspend fun createTheme(
        @Body request: ThemeRequest,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<ThemeResponse>
    
    @PUT("themes/{themeId}")
    suspend fun updateTheme(
        @Path("themeId") themeId: String,
        @Body request: ThemeRequest,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<ThemeResponse>
    
    @DELETE("themes/{themeId}")
    suspend fun deleteTheme(
        @Path("themeId") themeId: String,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<MessageResponse>

    // Theme images
    @POST("images")
    suspend fun createThemeImage(
        @Body request: ThemeImageCreateRequest,
        @Header("X-Vendor-Phone") vendorPhone: String
    ): Response<ThemeImageResponse>

    @GET("images/theme/{themeId}")
    suspend fun getImagesByThemeId(
        @Path("themeId") themeId: String
    ): Response<List<ImageResponse>>

    // Generic images for themes/inventory
    @POST("images")
    suspend fun createImage(
        @Body request: ImageCreateRequest,
        @Header("X-Vendor-Phone") vendorPhone: String?
    ): Response<ImageResponse>

    // File upload (used by Android for image uploads)
    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("category") category: okhttp3.RequestBody,
        @Part("itemId") itemId: okhttp3.RequestBody
    ): Response<FileUploadResponse>
}
