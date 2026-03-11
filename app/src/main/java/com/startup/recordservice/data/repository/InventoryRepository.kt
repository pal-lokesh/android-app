package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.InventoryCreateRequest
import com.startup.recordservice.data.model.InventoryResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
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

    suspend fun createInventory(request: InventoryCreateRequest): Result<InventoryResponse> {
        return try {
            Log.d(TAG, "Creating inventory for business: ${request.businessId}")
            val vendorPhone = tokenManager.getUserPhone()
            val response = apiService.createInventory(request, vendorPhone)

            if (response.isSuccessful && response.body() != null) {
                val created = response.body()!!
                Log.d(TAG, "Inventory created with id=${created.inventoryId}")
                Result.success(created)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to create inventory (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to create inventory (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to create inventory: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating inventory: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateInventory(inventoryId: String, request: InventoryCreateRequest): Result<InventoryResponse> {
        return try {
            val vendorPhone = tokenManager.getUserPhone()
            Log.d(TAG, "Updating inventory id=$inventoryId vendorPhone=$vendorPhone")
            val response = apiService.updateInventory(inventoryId, request, vendorPhone)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to update inventory (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to update inventory (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to update inventory: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating inventory: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteInventory(inventoryId: String): Result<Unit> {
        return try {
            val vendorPhone = tokenManager.getUserPhone()
            Log.d(TAG, "Deleting inventory id=$inventoryId vendorPhone=$vendorPhone")
            val response = apiService.deleteInventory(inventoryId, vendorPhone)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to delete inventory (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to delete inventory (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to delete inventory: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting inventory: ${e.message}", e)
            Result.failure(e)
        }
    }
}
