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
            Log.d(TAG, "Fetching businesses for vendor: $phoneNumber")
            val response = apiService.getBusinessesByVendorPhone(phoneNumber)

            if (response.isSuccessful && response.body() != null) {
                val businesses = response.body()!!
                Log.d(TAG, "Successfully fetched ${businesses.size} businesses for vendor")
                Result.success(businesses)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch vendor businesses (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch vendor businesses (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch vendor businesses: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching vendor businesses: ${e.message}", e)
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

    suspend fun updateBusiness(businessId: String, request: BusinessCreateRequest, vendorPhone: String?): Result<BusinessResponse> {
        return try {
            Log.d(TAG, "Updating business $businessId for vendor: $vendorPhone")
            val response = apiService.updateBusiness(businessId, request, vendorPhone)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to update business (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to update business (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to update business: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating business: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteBusiness(businessId: String, vendorPhone: String?): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting business $businessId for vendor: $vendorPhone")
            val response = apiService.deleteBusiness(businessId, vendorPhone)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to delete business (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to delete business (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to delete business: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting business: ${e.message}", e)
            Result.failure(e)
        }
    }
}
