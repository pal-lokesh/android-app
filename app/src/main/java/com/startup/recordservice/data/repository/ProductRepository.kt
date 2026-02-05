package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.model.Product
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "ProductRepository"
    }
    
    suspend fun getProducts(): Result<List<Product>> {
        return try {
            Log.d(TAG, "Fetching products from API")
            val response = apiService.getProducts()
            
            if (response.isSuccessful && response.body() != null) {
                val products = response.body()!!
                Log.d(TAG, "Successfully fetched ${products.size} products")
                Result.success(products)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch products (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch products (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch products: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: java.net.UnknownHostException) {
            val errorMsg = "Cannot connect to server. Please check your internet connection."
            Log.e(TAG, "Network error: ${e.message}", e)
            Result.failure(Exception(errorMsg))
        } catch (e: java.net.ConnectException) {
            val errorMsg = "Connection refused. Please ensure backend server is running."
            Log.e(TAG, "Connection error: ${e.message}", e)
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching products: ${e.message}", e)
            Result.failure(Exception("Network error: ${e.message ?: "Unknown error"}"))
        }
    }
}
