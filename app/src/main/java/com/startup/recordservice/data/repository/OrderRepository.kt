package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.OrderRequest
import com.startup.recordservice.data.model.OrderResponse
import com.startup.recordservice.data.model.UpdateStatusRequest
import com.startup.recordservice.data.model.UpdateAmountRequest
import org.json.JSONObject
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

    suspend fun getBusinessOrders(businessId: String): Result<List<OrderResponse>> {
        return try {
            Log.d(TAG, "Fetching orders for business: $businessId")
            val response = apiService.getBusinessOrders(businessId)

            if (response.isSuccessful && response.body() != null) {
                val orders = response.body()!!
                Log.d(TAG, "Successfully fetched ${orders.size} orders for business $businessId")
                Result.success(orders)
            } else {
                val errorBody = try {
                    response.errorBody()?.string()
                        ?: "Failed to fetch business orders (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch business orders (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch business orders: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching business orders: ${e.message}", e)
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
                val rawError = try {
                    response.errorBody()?.string() ?: "Failed to create order (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to create order (HTTP ${response.code()}): ${e.message}"
                }

                // Try to extract a clean message from JSON like {"message":"...","error":"..."}
                val userMessage = try {
                    val json = JSONObject(rawError)
                    when {
                        json.has("message") && !json.getString("message").isNullOrBlank() -> json.getString("message")
                        json.has("error") && !json.getString("error").isNullOrBlank() -> json.getString("error")
                        else -> rawError
                    }
                } catch (_: Exception) {
                    rawError
                }

                Log.e(TAG, "Failed to create order: $userMessage")
                Result.failure(Exception(userMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating order: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Result<OrderResponse> {
        return try {
            // Match backend/web contract: status is query param
            val response = apiService.updateOrderStatusQuery(orderId, status)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to update order (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to update order (HTTP ${response.code()}): ${e.message}"
                }
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderAmount(orderId: String, newAmount: Double): Result<OrderResponse> {
        return try {
            Log.d(TAG, "Updating order amount for orderId=$orderId, newAmount=$newAmount")
            val request = UpdateAmountRequest(newAmount = newAmount)
            val response = apiService.updateOrderAmount(orderId, request)
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Successfully updated order amount")
                Result.success(response.body()!!)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to update order amount (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to update order amount (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to update order amount: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order amount: ${e.message}", e)
            Result.failure(e)
        }
    }
}
