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
                        // Ensure userType is not null or empty
                        val responseUserType = signupResponse.user.userType?.takeIf { it.isNotBlank() }
                            ?: userType // Fallback to requested userType
                        _uiState.value = AuthUiState.Success(responseUserType)
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
}
