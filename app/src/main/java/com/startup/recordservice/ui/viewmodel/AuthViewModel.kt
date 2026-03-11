package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.model.LoginRequest
import com.startup.recordservice.data.model.SignupRequest
import com.startup.recordservice.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val userType: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    // Verification states
    private val _emailVerified = MutableStateFlow<Boolean?>(null)
    val emailVerified: StateFlow<Boolean?> = _emailVerified.asStateFlow()
    
    private val _phoneVerified = MutableStateFlow<Boolean?>(null)
    val phoneVerified: StateFlow<Boolean?> = _phoneVerified.asStateFlow()
    
    private val _emailVerifying = MutableStateFlow(false)
    val emailVerifying: StateFlow<Boolean> = _emailVerifying.asStateFlow()
    
    private val _phoneVerifying = MutableStateFlow(false)
    val phoneVerifying: StateFlow<Boolean> = _phoneVerifying.asStateFlow()
    
    // OTP states
    private val _emailOtpSent = MutableStateFlow(false)
    val emailOtpSent: StateFlow<Boolean> = _emailOtpSent.asStateFlow()
    
    private val _phoneOtpSent = MutableStateFlow(false)
    val phoneOtpSent: StateFlow<Boolean> = _phoneOtpSent.asStateFlow()
    
    private val _emailOtpVerified = MutableStateFlow(false)
    val emailOtpVerified: StateFlow<Boolean> = _emailOtpVerified.asStateFlow()
    
    private val _phoneOtpVerified = MutableStateFlow(false)
    val phoneOtpVerified: StateFlow<Boolean> = _phoneOtpVerified.asStateFlow()
    
    private val _sendingEmailOtp = MutableStateFlow(false)
    val sendingEmailOtp: StateFlow<Boolean> = _sendingEmailOtp.asStateFlow()
    
    private val _sendingPhoneOtp = MutableStateFlow(false)
    val sendingPhoneOtp: StateFlow<Boolean> = _sendingPhoneOtp.asStateFlow()
    
    private val _verifyingEmailOtp = MutableStateFlow(false)
    val verifyingEmailOtp: StateFlow<Boolean> = _verifyingEmailOtp.asStateFlow()
    
    private val _verifyingPhoneOtp = MutableStateFlow(false)
    val verifyingPhoneOtp: StateFlow<Boolean> = _verifyingPhoneOtp.asStateFlow()
    
    fun login(phoneNumber: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                
                authRepository.login(phoneNumber, password)
                    .onSuccess { loginResponse ->
                        // Ensure userType is not null or empty
                        val userType = loginResponse.userType?.takeIf { it.isNotBlank() } 
                            ?: "CLIENT" // Default to CLIENT if null/empty
                        _uiState.value = AuthUiState.Success(userType)
                    }
                    .onFailure { exception ->
                        _uiState.value = AuthUiState.Error(
                            exception.message ?: "Login failed. Please check your credentials."
                        )
                    }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Login error: ${e.message}", e)
                _uiState.value = AuthUiState.Error(
                    "An unexpected error occurred: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    fun signup(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String,
        password: String,
        userType: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                
                val request = SignupRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phoneNumber = phoneNumber,
                    password = password,
                    userType = userType
                )
                
                authRepository.signup(request)
                    .onSuccess { signupResponse ->
                        // After successful signup, automatically log the user in so dashboards work
                        val loginPhone = signupResponse.user.phoneNumber?.takeIf { it.isNotBlank() }
                            ?: phoneNumber
                        
                        authRepository.login(loginPhone, password)
                            .onSuccess { loginResponse ->
                                // Ensure userType is not null or empty
                                val responseUserType = loginResponse.userType?.takeIf { it.isNotBlank() }
                                    ?: signupResponse.user.userType?.takeIf { it.isNotBlank() }
                                    ?: userType
                                _uiState.value = AuthUiState.Success(responseUserType)
                            }
                            .onFailure { exception ->
                                _uiState.value = AuthUiState.Error(
                                    "Signup succeeded but login failed: ${exception.message ?: "Unknown error"}"
                                )
                            }
                    }
                    .onFailure { exception ->
                        _uiState.value = AuthUiState.Error(
                            exception.message ?: "Signup failed. Please try again."
                        )
                    }
            } catch (e: Exception) {
                android.util.Log.e("AuthViewModel", "Signup error: ${e.message}", e)
                _uiState.value = AuthUiState.Error(
                    "An unexpected error occurred: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState.Idle
        }
    }
    
    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
    
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
    
    fun getCurrentUserType(): String? {
        return authRepository.getCurrentUserType()
    }
    
    fun verifyEmail(email: String) {
        if (email.isBlank()) {
            _emailVerified.value = null
            return
        }
        
        viewModelScope.launch {
            try {
                _emailVerifying.value = true
                _emailVerified.value = null
                
                authRepository.checkEmail(email)
                    .onSuccess { exists ->
                        _emailVerified.value = !exists // If email doesn't exist, it's available (verified)
                        android.util.Log.d("AuthViewModel", "Email verification: exists=$exists, available=${!exists}")
                    }
                    .onFailure { exception ->
                        _emailVerified.value = null
                        android.util.Log.e("AuthViewModel", "Email verification failed: ${exception.message}")
                    }
            } catch (e: Exception) {
                _emailVerified.value = null
                android.util.Log.e("AuthViewModel", "Email verification error: ${e.message}", e)
            } finally {
                _emailVerifying.value = false
            }
        }
    }
    
    fun verifyPhone(phoneNumber: String) {
        if (phoneNumber.isBlank()) {
            _phoneVerified.value = null
            return
        }
        
        viewModelScope.launch {
            try {
                _phoneVerifying.value = true
                _phoneVerified.value = null
                
                authRepository.checkPhone(phoneNumber)
                    .onSuccess { exists ->
                        _phoneVerified.value = !exists // If phone doesn't exist, it's available (verified)
                        android.util.Log.d("AuthViewModel", "Phone verification: exists=$exists, available=${!exists}")
                    }
                    .onFailure { exception ->
                        _phoneVerified.value = null
                        android.util.Log.e("AuthViewModel", "Phone verification failed: ${exception.message}")
                    }
            } catch (e: Exception) {
                _phoneVerified.value = null
                android.util.Log.e("AuthViewModel", "Phone verification error: ${e.message}", e)
            } finally {
                _phoneVerifying.value = false
            }
        }
    }
    
    fun resetVerification() {
        _emailVerified.value = null
        _phoneVerified.value = null
    }
    
    fun sendEmailOtp(email: String) {
        if (email.isBlank()) return
        
        viewModelScope.launch {
            try {
                _sendingEmailOtp.value = true
                authRepository.sendEmailOtp(email)
                    .onSuccess {
                        _emailOtpSent.value = true
                        android.util.Log.d("AuthViewModel", "Email OTP sent successfully")
                    }
                    .onFailure { exception ->
                        _emailOtpSent.value = false
                        _uiState.value = AuthUiState.Error(exception.message ?: "Failed to send OTP")
                        android.util.Log.e("AuthViewModel", "Failed to send email OTP: ${exception.message}")
                    }
            } catch (e: Exception) {
                _emailOtpSent.value = false
                _uiState.value = AuthUiState.Error("Failed to send OTP: ${e.message ?: "Unknown error"}")
                android.util.Log.e("AuthViewModel", "Error sending email OTP: ${e.message}", e)
            } finally {
                _sendingEmailOtp.value = false
            }
        }
    }
    
    fun sendPhoneOtp(phoneNumber: String) {
        if (phoneNumber.isBlank()) return
        
        viewModelScope.launch {
            try {
                _sendingPhoneOtp.value = true
                authRepository.sendPhoneOtp(phoneNumber)
                    .onSuccess {
                        _phoneOtpSent.value = true
                        android.util.Log.d("AuthViewModel", "Phone OTP sent successfully")
                    }
                    .onFailure { exception ->
                        _phoneOtpSent.value = false
                        _uiState.value = AuthUiState.Error(exception.message ?: "Failed to send OTP")
                        android.util.Log.e("AuthViewModel", "Failed to send phone OTP: ${exception.message}")
                    }
            } catch (e: Exception) {
                _phoneOtpSent.value = false
                _uiState.value = AuthUiState.Error("Failed to send OTP: ${e.message ?: "Unknown error"}")
                android.util.Log.e("AuthViewModel", "Error sending phone OTP: ${e.message}", e)
            } finally {
                _sendingPhoneOtp.value = false
            }
        }
    }
    
    fun verifyEmailOtp(email: String, otp: String) {
        if (email.isBlank() || otp.isBlank()) return
        
        viewModelScope.launch {
            try {
                _verifyingEmailOtp.value = true
                authRepository.verifyEmailOtp(email, otp)
                    .onSuccess {
                        _emailOtpVerified.value = true
                        android.util.Log.d("AuthViewModel", "Email OTP verified successfully")
                    }
                    .onFailure { exception ->
                        _emailOtpVerified.value = false
                        _uiState.value = AuthUiState.Error(exception.message ?: "Invalid OTP")
                        android.util.Log.e("AuthViewModel", "Failed to verify email OTP: ${exception.message}")
                    }
            } catch (e: Exception) {
                _emailOtpVerified.value = false
                _uiState.value = AuthUiState.Error("Failed to verify OTP: ${e.message ?: "Unknown error"}")
                android.util.Log.e("AuthViewModel", "Error verifying email OTP: ${e.message}", e)
            } finally {
                _verifyingEmailOtp.value = false
            }
        }
    }
    
    fun verifyPhoneOtp(phoneNumber: String, otp: String) {
        if (phoneNumber.isBlank() || otp.isBlank()) return
        
        viewModelScope.launch {
            try {
                _verifyingPhoneOtp.value = true
                authRepository.verifyPhoneOtp(phoneNumber, otp)
                    .onSuccess {
                        _phoneOtpVerified.value = true
                        android.util.Log.d("AuthViewModel", "Phone OTP verified successfully")
                    }
                    .onFailure { exception ->
                        _phoneOtpVerified.value = false
                        _uiState.value = AuthUiState.Error(exception.message ?: "Invalid OTP")
                        android.util.Log.e("AuthViewModel", "Failed to verify phone OTP: ${exception.message}")
                    }
            } catch (e: Exception) {
                _phoneOtpVerified.value = false
                _uiState.value = AuthUiState.Error("Failed to verify OTP: ${e.message ?: "Unknown error"}")
                android.util.Log.e("AuthViewModel", "Error verifying phone OTP: ${e.message}", e)
            } finally {
                _verifyingPhoneOtp.value = false
            }
        }
    }
    
    fun resetOtpState() {
        _emailOtpSent.value = false
        _phoneOtpSent.value = false
        _emailOtpVerified.value = false
        _phoneOtpVerified.value = false
    }
}
