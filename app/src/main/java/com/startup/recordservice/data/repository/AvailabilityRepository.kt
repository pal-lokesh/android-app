package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.AvailabilityCheckResponse
import com.startup.recordservice.data.model.AvailabilityRequest
import com.startup.recordservice.data.model.AvailabilityResponse
import com.startup.recordservice.data.model.AvailableQuantityResponse
import com.startup.recordservice.data.model.CheckAvailabilityRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AvailabilityRepository @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "AvailabilityRepository"
    }

    suspend fun createOrUpdateAvailability(request: AvailabilityRequest): Result<AvailabilityResponse> {
        return try {
            val response = apiService.createOrUpdateAvailability(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to create/update availability (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error createOrUpdateAvailability: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getAvailability(itemId: String, itemType: String, date: String): Result<AvailabilityResponse?> {
        return try {
            val response = apiService.getAvailability(itemId, itemType, date)
            when {
                response.isSuccessful -> Result.success(response.body())
                response.code() == 404 -> Result.success(null)
                else -> {
                    val errorBody = response.errorBody()?.string() ?: "Failed to get availability (HTTP ${response.code()})"
                    Result.failure(Exception(errorBody))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailabilitiesForItem(itemId: String, itemType: String): Result<List<AvailabilityResponse>> {
        return try {
            val response = apiService.getAvailabilitiesForItem(itemId, itemType)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to get availabilities (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailabilitiesInRange(
        itemId: String,
        itemType: String,
        startDate: String,
        endDate: String
    ): Result<List<AvailabilityResponse>> {
        return try {
            val response = apiService.getAvailabilitiesInRange(itemId, itemType, startDate, endDate)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to get availabilities (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailabilitiesForBusiness(businessId: String): Result<List<AvailabilityResponse>> {
        return try {
            val response = apiService.getAvailabilitiesForBusiness(businessId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to get business availabilities (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkAvailability(request: CheckAvailabilityRequest): Result<AvailabilityCheckResponse> {
        return try {
            val response = apiService.checkAvailability(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to check availability (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAvailableQuantity(itemId: String, itemType: String, date: String): Result<AvailableQuantityResponse> {
        return try {
            val response = apiService.getAvailableQuantity(itemId, itemType, date)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to get available quantity (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAvailability(itemId: String, itemType: String, date: String): Result<Unit> {
        return try {
            val response = apiService.deleteAvailability(itemId, itemType, date)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete availability (HTTP ${response.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllAvailabilitiesForItem(itemId: String, itemType: String): Result<Unit> {
        return try {
            val response = apiService.deleteAllAvailabilitiesForItem(itemId, itemType)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete availabilities (HTTP ${response.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

