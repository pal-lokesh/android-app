package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.RatingRequest
import com.startup.recordservice.data.model.RatingResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "RatingRepository"
    }

    suspend fun getClientRatings(): Result<List<RatingResponse>> {
        val phone = tokenManager.getUserPhone()
        if (phone.isNullOrBlank()) {
            return Result.failure(Exception("User phone not available"))
        }
        return try {
            val response = apiService.getClientRatings(phone)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to load ratings (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to load ratings (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "getClientRatings failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading ratings: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun submitRating(businessId: String, stars: Int, comment: String?): Result<Unit> {
        val phone = tokenManager.getUserPhone()
        if (phone.isNullOrBlank()) {
            return Result.failure(Exception("User phone not available"))
        }
        val clamped = stars.coerceIn(1, 5)
        val request = RatingRequest(
            clientPhone = phone,
            businessId = businessId,
            rating = clamped,
            comment = comment?.takeIf { it.isNotBlank() }
        )
        return try {
            val response = apiService.createRating(request)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to submit rating (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to submit rating (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "submitRating failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting rating: ${e.message}", e)
            Result.failure(e)
        }
    }
}

