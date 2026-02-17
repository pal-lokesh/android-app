package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.PlateResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlateRepository @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "PlateRepository"
    }
    
    suspend fun getBusinessPlates(businessId: String): Result<List<PlateResponse>> {
        return try {
            Log.d(TAG, "Fetching plates for business: $businessId")
            val response = apiService.getBusinessPlates(businessId)
            
            if (response.isSuccessful && response.body() != null) {
                val plates = response.body()!!
                Log.d(TAG, "Successfully fetched ${plates.size} plates")
                Result.success(plates)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch plates (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch plates (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch plates: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching plates: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getPlate(plateId: String): Result<PlateResponse> {
        return try {
            Log.d(TAG, "Fetching plate: $plateId")
            val response = apiService.getPlate(plateId)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Successfully fetched plate: $plateId")
                Result.success(response.body()!!)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch plate (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch plate (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch plate: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching plate: ${e.message}", e)
            Result.failure(e)
        }
    }
}
