package com.startup.recordservice.data.repository

import com.startup.recordservice.data.api.ApiService
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.*
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    suspend fun login(phoneNumber: String, password: String): Result<LoginResponse> {
        return try {
            Log.d(TAG, "Attempting login for phone: $phoneNumber")
            val response = apiService.login(LoginRequest(phoneNumber, password))
            
            Log.d(TAG, "Login response code: ${response.code()}")
            Log.d(TAG, "Login response isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                val token = loginResponse.token
                if (token.isNotBlank()) {
                    tokenManager.saveToken(token)
                    tokenManager.saveUserPhone(loginResponse.phoneNumber)
                    tokenManager.saveUserType(loginResponse.userType)
                    tokenManager.saveUserId(loginResponse.phoneNumber)
                    Log.d(TAG, "Login successful for user: ${loginResponse.phoneNumber}")
                    Result.success(loginResponse)
                } else {
                    Log.e(TAG, "Login failed: Token is blank")
                    Result.failure(Exception("Invalid response from server: Token is missing"))
                }
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Login failed (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Login failed (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Login failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: java.net.UnknownHostException) {
            val errorMsg = "Cannot connect to server. Please check:\n" +
                    "1. Backend is running on port 8080\n" +
                    "2. Correct IP address (emulator: 10.0.2.2, device: your computer's IP)\n" +
                    "3. Phone and computer are on same network"
            Log.e(TAG, "Network error: ${e.message}", e)
            Result.failure(Exception(errorMsg))
        } catch (e: java.net.ConnectException) {
            val errorMsg = "Connection refused. Please ensure:\n" +
                    "1. Backend server is running\n" +
                    "2. Backend is accessible from your device/emulator"
            Log.e(TAG, "Connection error: ${e.message}", e)
            Result.failure(Exception(errorMsg))
        } catch (e: java.net.SocketTimeoutException) {
            val errorMsg = "Request timeout. Server may be slow or unreachable."
            Log.e(TAG, "Timeout error: ${e.message}", e)
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            Result.failure(Exception("Network error: ${e.message ?: "Unknown error"}"))
        }
    }
    
    suspend fun signup(request: SignupRequest): Result<SignupResponse> {
        return try {
            Log.d(TAG, "Attempting signup for phone: ${request.phoneNumber}")
            val response = apiService.signup(request)
            
            Log.d(TAG, "Signup response code: ${response.code()}")
            Log.d(TAG, "Signup response isSuccessful: ${response.isSuccessful}")
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Signup successful")
                Result.success(response.body()!!)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Signup failed (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Signup failed (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Signup failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: java.net.UnknownHostException) {
            val errorMsg = "Cannot connect to server. Please check:\n" +
                    "1. Backend is running on port 8080\n" +
                    "2. Correct IP address (emulator: 10.0.2.2, device: your computer's IP)\n" +
                    "3. Phone and computer are on same network"
            Log.e(TAG, "Network error: ${e.message}", e)
            Result.failure(Exception(errorMsg))
        } catch (e: java.net.ConnectException) {
            val errorMsg = "Connection refused. Please ensure:\n" +
                    "1. Backend server is running\n" +
                    "2. Backend is accessible from your device/emulator"
            Log.e(TAG, "Connection error: ${e.message}", e)
            Result.failure(Exception(errorMsg))
        } catch (e: java.net.SocketTimeoutException) {
            val errorMsg = "Request timeout. Server may be slow or unreachable."
            Log.e(TAG, "Timeout error: ${e.message}", e)
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            Result.failure(Exception("Network error: ${e.message ?: "Unknown error"}"))
        }
    }
    
    suspend fun logout() {
        tokenManager.clear()
        Log.d(TAG, "User logged out")
    }
    
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
    
    fun getCurrentUserType(): String? {
        return tokenManager.getUserType()
    }
    
    fun getCurrentUserId(): String? {
        return tokenManager.getUserId()
    }
}
