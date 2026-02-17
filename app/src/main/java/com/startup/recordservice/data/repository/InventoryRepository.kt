package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.InventoryResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "InventoryRepository"
    }
    
    suspend fun getAllInventory(): Result<List<InventoryResponse>> {
        return try {
            Log.d(TAG, "Fetching all inventory")
            val response = apiService.getAllInventory()
            
            if (response.isSuccessful && response.body() != null) {
                val inventory = response.body()!!
                Log.d(TAG, "Successfully fetched ${inventory.size} inventory items")
                Result.success(inventory)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch inventory (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch inventory (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch inventory: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching inventory: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getBusinessInventory(businessId: String): Result<List<InventoryResponse>> {
        return try {
            Log.d(TAG, "Fetching inventory for business: $businessId")
            val response = apiService.getBusinessInventory(businessId)
            
            if (response.isSuccessful && response.body() != null) {
                val inventory = response.body()!!
                Log.d(TAG, "Successfully fetched ${inventory.size} inventory items")
                Result.success(inventory)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch inventory (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch inventory (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch inventory: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching inventory: ${e.message}", e)
            Result.failure(e)
        }
    }
}
