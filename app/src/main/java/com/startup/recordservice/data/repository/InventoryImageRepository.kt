package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.InventoryImageRequest
import com.startup.recordservice.data.model.InventoryImageResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryImageRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "InventoryImageRepository"
    }

    suspend fun createInventoryImage(request: InventoryImageRequest): Result<InventoryImageResponse> {
        return try {
            val vendorPhone = tokenManager.getUserPhone()
            val response = apiService.createInventoryImage(request, vendorPhone)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to create inventory image (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error createInventoryImage: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getInventoryImagesByInventoryId(inventoryId: String): Result<List<InventoryImageResponse>> {
        return try {
            val response = apiService.getInventoryImagesByInventoryId(inventoryId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to fetch inventory images (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInventoryImage(imageId: String, request: InventoryImageRequest): Result<InventoryImageResponse> {
        return try {
            val vendorPhone = tokenManager.getUserPhone()
            val response = apiService.updateInventoryImage(imageId, request, vendorPhone)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to update inventory image (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteInventoryImage(imageId: String): Result<Unit> {
        return try {
            val vendorPhone = tokenManager.getUserPhone()
            val response = apiService.deleteInventoryImage(imageId, vendorPhone)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(response.errorBody()?.string() ?: "Failed to delete inventory image (HTTP ${response.code()})"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setPrimaryInventoryImage(imageId: String): Result<InventoryImageResponse> {
        return try {
            val vendorPhone = tokenManager.getUserPhone()
            val response = apiService.setPrimaryInventoryImage(imageId, vendorPhone)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to set primary inventory image (HTTP ${response.code()})"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

