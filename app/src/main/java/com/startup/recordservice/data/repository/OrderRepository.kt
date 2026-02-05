package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.OrderRequest
import com.startup.recordservice.data.model.OrderResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "OrderRepository"
    }
    
    suspend fun getUserOrders(userId: String): Result<List<OrderResponse>> {
        return try {
            Log.d(TAG, "Fetching orders for user: $userId")
            val response = apiService.getUserOrders(userId)
            
            if (response.isSuccessful && response.body() != null) {
                val orders = response.body()!!
                Log.d(TAG, "Successfully fetched ${orders.size} orders")
                Result.success(orders)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch orders (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch orders (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch orders: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching orders: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createOrder(orderRequest: OrderRequest): Result<OrderResponse> {
        return try {
            Log.d(TAG, "Creating order for user: ${orderRequest.userId}")
            val response = apiService.createOrder(orderRequest)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Successfully created order")
                Result.success(response.body()!!)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to create order (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to create order (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to create order: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating order: ${e.message}", e)
            Result.failure(e)
        }
    }
}
