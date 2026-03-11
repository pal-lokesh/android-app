package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.DishResponse
import com.startup.recordservice.data.local.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "DishRepository"
    }
    
    suspend fun getPlateDishes(plateId: String): Result<List<DishResponse>> {
        return try {
            Log.d(TAG, "Fetching dishes for plate: $plateId")
            val response = apiService.getPlateDishes(plateId)
            
            if (response.isSuccessful && response.body() != null) {
                val dishes = response.body()!!
                Log.d(TAG, "Successfully fetched ${dishes.size} dishes")
                Result.success(dishes)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch dishes (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch dishes (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch dishes: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching dishes: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAllDishes(): Result<List<DishResponse>> {
        return try {
            val response = apiService.getAllDishes()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to fetch dishes (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBusinessDishes(businessId: String): Result<List<DishResponse>> {
        return try {
            val response = apiService.getBusinessDishes(businessId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to fetch dishes (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createDish(request: DishResponse): Result<DishResponse> {
        return try {
            val vendorPhone = tokenManager.getUserPhone()
            val response = apiService.createDish(request, vendorPhone)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to create dish (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateDish(dishId: String, request: DishResponse): Result<DishResponse> {
        return try {
            val vendorPhone = tokenManager.getUserPhone()
            val response = apiService.updateDish(dishId, request, vendorPhone)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to update dish (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDish(dishId: String): Result<Unit> {
        return try {
            val vendorPhone = tokenManager.getUserPhone()
            val response = apiService.deleteDish(dishId, vendorPhone)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to delete dish (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
