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

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            Log.d(TAG, "Changing password for user: ${tokenManager.getUserPhone() ?: "unknown"}")
            val response = apiService.changePassword(ChangePasswordRequest(currentPassword, newPassword))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to change password (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to change password (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Change password failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error changing password: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteCurrentUser(): Result<Unit> {
        val phone = tokenManager.getUserPhone()
        if (phone.isNullOrBlank()) {
            return Result.failure(Exception("User phone not available"))
        }
        return try {
            Log.d(TAG, "Deleting user account for phone: $phone")
            val response = apiService.deleteUser(phone)
            if (response.isSuccessful) {
                tokenManager.clear()
                Result.success(Unit)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to delete account (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to delete account (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Delete account failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting account: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun checkPhone(phoneNumber: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Checking if phone number exists: $phoneNumber")
            val response = apiService.checkPhone(phoneNumber)
            
            if (response.isSuccessful && response.body() != null) {
                val exists = response.body()!!.exists
                Log.d(TAG, "Phone check result: exists=$exists")
                Result.success(exists)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to check phone (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to check phone (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Phone check failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking phone: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun checkEmail(email: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Checking if email exists: $email")
            val response = apiService.checkEmail(email)
            
            if (response.isSuccessful && response.body() != null) {
                val exists = response.body()!!.exists
                Log.d(TAG, "Email check result: exists=$exists")
                Result.success(exists)
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to check email (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to check email (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Email check failed: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking email: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun sendPhoneOtp(phoneNumber: String): Result<String> {
        return try {
            Log.d(TAG, "Sending OTP to phone: $phoneNumber")
            val response = apiService.sendSignupPhoneOtp(SendOtpRequest(phoneNumber = phoneNumber))
            
            if (response.isSuccessful && response.body() != null) {
                val otpResponse = response.body()!!
                Log.d(TAG, "OTP sent successfully to phone")
                Result.success(otpResponse.message ?: "OTP sent successfully")
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to send OTP (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to send OTP (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to send OTP: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending OTP: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun sendEmailOtp(email: String): Result<String> {
        return try {
            Log.d(TAG, "Sending OTP to email: $email")
            val response = apiService.sendSignupEmailOtp(SendOtpRequest(email = email))
            
            if (response.isSuccessful && response.body() != null) {
                val otpResponse = response.body()!!
                Log.d(TAG, "OTP sent successfully to email")
                Result.success(otpResponse.message ?: "OTP sent successfully")
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to send OTP (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to send OTP (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to send OTP: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending OTP: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun verifyPhoneOtp(phoneNumber: String, otp: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Verifying OTP for phone: $phoneNumber")
            val response = apiService.verifySignupPhoneOtp(
                VerifyOtpRequest(phoneNumber = phoneNumber, code = otp)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val otpResponse = response.body()!!
                val verified = otpResponse.verified ?: true
                if (verified) {
                    Log.d(TAG, "Phone OTP verified successfully")
                    Result.success(true)
                } else {
                    Result.failure(Exception(otpResponse.message ?: "Invalid OTP"))
                }
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to verify OTP (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to verify OTP (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to verify OTP: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying OTP: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun verifyEmailOtp(email: String, otp: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Verifying OTP for email: $email")
            val response = apiService.verifySignupEmailOtp(
                VerifyOtpRequest(email = email, code = otp)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val otpResponse = response.body()!!
                val verified = otpResponse.verified ?: true
                if (verified) {
                    Log.d(TAG, "Email OTP verified successfully")
                    Result.success(true)
                } else {
                    Result.failure(Exception(otpResponse.message ?: "Invalid OTP"))
                }
            } else {
                val errorBody = try {
                    response.errorBody()?.string() ?: "Failed to verify OTP (HTTP ${response.code()})"
                } catch (e: Exception) {
                    "Failed to verify OTP (HTTP ${response.code()}): ${e.message}"
                }
                Log.e(TAG, "Failed to verify OTP: $errorBody")
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying OTP: ${e.message}", e)
            Result.failure(e)
        }
    }
}
