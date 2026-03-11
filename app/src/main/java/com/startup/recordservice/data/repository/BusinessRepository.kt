package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.BusinessCreateRequest
import com.startup.recordservice.data.model.BusinessResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusinessRepository @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "BusinessRepository"
    }
    
    suspend fun getAllBusinesses(): Result<List<BusinessResponse>> {
        return try {
            Log.d(TAG, "Fetching all businesses")
            val response = apiService.getAllBusinesses()
            
            if (response.isSuccessful && response.body() != null) {
                val businesses = response.body()!!
                Log.d(TAG, "Successfully fetched ${businesses.size} businesses")
                Result.success(businesses)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch businesses (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch businesses (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch businesses: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching businesses: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getBusiness(businessId: String): Result<BusinessResponse> {
        return try {
            Log.d(TAG, "Fetching business: $businessId")
            val response = apiService.getBusiness(businessId)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Successfully fetched business: $businessId")
                Result.success(response.body()!!)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch business (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch business (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch business: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching business: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUserBusinesses(phoneNumber: String): Result<List<BusinessResponse>> {
        return try {
            Log.d(TAG, "Fetching businesses for user: $phoneNumber")
            val response = apiService.getBusinessByPhone(phoneNumber)
            
            when {
                response.isSuccessful && response.body() != null -> {
                    val business = response.body()!!
                    Log.d(TAG, "Successfully fetched business for user")
                    Result.success(listOf(business))
                }
                // If no business exists yet for this vendor, backend returns 404 – treat as empty list
                response.code() == 404 -> {
                    Log.w(TAG, "No businesses found for user $phoneNumber (HTTP 404). Returning empty list.")
                    Result.success(emptyList())
                }
                else -> {
                    val errorBody = try {
                        response.errorBody()?.string() ?: "Failed to fetch user businesses (HTTP ${response.code()})"
                    } catch (e: Exception) {
                        "Failed to fetch user businesses (HTTP ${response.code()}): ${e.message}"
                    }
                    Log.e(TAG, "Failed to fetch user businesses: $errorBody")
                    Result.failure(Exception(errorBody))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user businesses: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createBusiness(request: BusinessCreateRequest): Result<BusinessResponse> {
        return try {
            Log.d(TAG, "Creating business for phone: ${request.phoneNumber}")
            val response = apiService.createBusiness(request)
            
            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!
                Log.d(TAG, "Business created with id=${created.businessId}")
                Result.success(created)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to create business (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to create business (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to create business: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating business: ${e.message}", e)
            Result.failure(e)
        }
    }
}
