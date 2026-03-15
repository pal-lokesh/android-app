package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.StockNotificationResponse
import com.startup.recordservice.data.model.StockSubscribeRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockNotificationRepository @Inject constructor(
    private val apiService: ApiService
) {
    companion object { private const val TAG = "StockNotifRepo" }

    suspend fun isSubscribed(
        userId: String,
        itemId: String,
        itemType: String,
        requestedDate: String?
    ): Result<Boolean> = try {
        val res = apiService.checkStockSubscription(userId, itemId, itemType, requestedDate)
        if (res.isSuccessful && res.body() != null) Result.success(res.body()!!.subscribed)
        else Result.failure(Exception(res.errorBody()?.string() ?: "Failed to check subscription (HTTP ${res.code()})"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun subscribe(request: StockSubscribeRequest): Result<StockNotificationResponse> = try {
        val res = apiService.subscribeStockNotification(request)
        if (res.isSuccessful && res.body() != null) Result.success(res.body()!!)
        else Result.failure(Exception(res.errorBody()?.string() ?: "Failed to subscribe (HTTP ${res.code()})"))
    } catch (e: Exception) {
        Log.e(TAG, "subscribe error: ${e.message}", e)
        Result.failure(e)
    }
    
    suspend fun unsubscribe(
        userId: String,
        itemId: String,
        itemType: String,
        requestedDate: String?
    ): Result<Unit> = try {
        val res = apiService.unsubscribeStockNotification(userId, itemId, itemType, requestedDate)
        if (res.isSuccessful) Result.success(Unit)
        else Result.failure(Exception(res.errorBody()?.string() ?: "Failed to unsubscribe (HTTP ${res.code()})"))
    } catch (e: Exception) {
        Log.e(TAG, "unsubscribe error: ${e.message}", e)
        Result.failure(e)
    }
}

