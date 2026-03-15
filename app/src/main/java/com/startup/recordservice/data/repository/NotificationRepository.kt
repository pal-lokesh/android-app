package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.NotificationResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val TAG = "NotificationRepository"

    suspend fun getVendorNotifications(businessId: String): Result<List<NotificationResponse>> {
        return try {
            val response = apiService.getVendorNotifications(businessId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Failed to get vendor notifications"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting vendor notifications: ${e.message}", e)
            Result.failure(e)
        }
    }
}
