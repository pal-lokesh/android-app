package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientProfileUiState(
    val displayName: String = "Guest",
    val phone: String? = null,
    val userId: String? = null
)

@HiltViewModel
class ClientProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ClientProfileUiState(
            phone = tokenManager.getUserPhone(),
            userId = tokenManager.getUserId()
        )
    )
    val uiState: StateFlow<ClientProfileUiState> = _uiState.asStateFlow()

    init {
        val phone = tokenManager.getUserPhone()
        _uiState.value = _uiState.value.copy(
            displayName = phone?.takeIf { it.isNotBlank() } ?: "Client User",
            phone = phone
        )
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (e: Exception) {
                android.util.Log.e("ClientProfileViewModel", "Logout failed: ${e.message}", e)
            }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.changePassword(currentPassword, newPassword)
            onResult(result)
        }
    }

    fun deleteAccount(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.deleteCurrentUser()
            onResult(result)
        }
    }
}

