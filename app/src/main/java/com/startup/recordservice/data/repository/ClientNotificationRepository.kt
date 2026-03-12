package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.ClientNotificationResponse
import com.startup.recordservice.data.model.ClientNotificationsWithCountResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientNotificationRepository @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "ClientNotificationRepo"
    }

    suspend fun getWithCount(clientPhone: String): Result<ClientNotificationsWithCountResponse> = try {
        val res = apiService.getClientNotificationsWithCount(clientPhone)
        if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
        else Result.failure(Exception(res.errorBody()?.string() ?: "Failed to fetch notifications (HTTP ${res.code()})"))
    } catch (e: Exception) {
        Log.e(TAG, "getWithCount error: ${e.message}", e)
        Result.failure(e)
    }

    suspend fun getNotifications(clientPhone: String): Result<List<ClientNotificationResponse>> = try {
        val res = apiService.getClientNotificationsByClient(clientPhone)
        if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
        else Result.failure(Exception(res.errorBody()?.string() ?: "Failed to fetch notifications (HTTP ${res.code()})"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markRead(notificationId: Long): Result<Unit> = try {
        val res = apiService.markClientNotificationRead(notificationId)
        if (res.isSuccessful) Result.success(Unit)
        else Result.failure(Exception(res.errorBody()?.string() ?: "Failed to mark read (HTTP ${res.code()})"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markAllRead(clientPhone: String): Result<Unit> = try {
        val res = apiService.markAllClientNotificationsRead(clientPhone)
        if (res.isSuccessful) Result.success(Unit)
        else Result.failure(Exception(res.errorBody()?.string() ?: "Failed to mark all read (HTTP ${res.code()})"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

