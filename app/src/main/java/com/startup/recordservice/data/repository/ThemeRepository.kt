package com.startup.recordservice.data.repository

import android.util.Log
import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.ThemeResponse
import com.startup.recordservice.data.model.ThemeRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "ThemeRepository"
    }
    
    suspend fun getAllThemes(): Result<List<ThemeResponse>> {
        return try {
            Log.d(TAG, "Fetching all themes")
            val response = apiService.getAllThemes()
            
            if (response.isSuccessful && response.body() != null) {
                val themes = response.body()!!
                Log.d(TAG, "Successfully fetched ${themes.size} themes")
                Result.success(themes)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch themes (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch themes (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch themes: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching themes: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getBusinessThemes(businessId: String): Result<List<ThemeResponse>> {
        return try {
            Log.d(TAG, "Fetching themes for business: $businessId")
            val response = apiService.getBusinessThemes(businessId)
            
            if (response.isSuccessful && response.body() != null) {
                val themes = response.body()!!
                Log.d(TAG, "Successfully fetched ${themes.size} themes for business")
                Result.success(themes)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch business themes (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch business themes (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch business themes: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching business themes: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getTheme(themeId: String): Result<ThemeResponse> {
        return try {
            Log.d(TAG, "Fetching theme: $themeId")
            val response = apiService.getTheme(themeId)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Successfully fetched theme: $themeId")
                Result.success(response.body()!!)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch theme (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch theme (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch theme: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching theme: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun getThemesByCategory(category: String): Result<List<ThemeResponse>> {
        return try {
            Log.d(TAG, "Fetching themes by category: $category")
            val response = apiService.getThemesByCategory(category)
            
            if (response.isSuccessful && response.body() != null) {
                val themes = response.body()!!
                Log.d(TAG, "Successfully fetched ${themes.size} themes for category")
                Result.success(themes)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to fetch themes by category (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to fetch themes by category (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to fetch themes by category: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching themes by category: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createTheme(request: ThemeRequest): Result<ThemeResponse> {
        return try {
            Log.d(TAG, "Creating theme: ${request.themeName}")
            val vendorPhone = tokenManager.getUserPhone()
            val response = apiService.createTheme(request, vendorPhone)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Successfully created theme")
                Result.success(response.body()!!)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to create theme (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to create theme (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to create theme: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating theme: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateTheme(themeId: String, request: ThemeRequest): Result<ThemeResponse> {
        return try {
            Log.d(TAG, "Updating theme: $themeId")
            val vendorPhone = tokenManager.getUserPhone()
            val response = apiService.updateTheme(themeId, request, vendorPhone)
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Successfully updated theme")
                Result.success(response.body()!!)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to update theme (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to update theme (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to update theme: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating theme: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteTheme(themeId: String): Result<Unit> {
        return try {
            Log.d(TAG, "Deleting theme: $themeId")
            val vendorPhone = tokenManager.getUserPhone()
            val response = apiService.deleteTheme(themeId, vendorPhone)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully deleted theme")
                Result.success(Unit)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to delete theme (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to delete theme (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to delete theme: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting theme: ${e.message}", e)
            Result.failure(e)
        }
    }
}
