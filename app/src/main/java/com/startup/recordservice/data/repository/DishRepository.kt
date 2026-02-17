package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.DishResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DishRepository @Inject constructor(
    private val apiService: ApiService
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
}
