package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VendorProfileUiState(
    val displayName: String = "Vendor User",
    val phone: String? = null,
    val userId: String? = null
)

@HiltViewModel
class VendorProfileViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VendorProfileUiState(
            phone = tokenManager.getUserPhone(),
            userId = tokenManager.getUserId()
        )
    )
    val uiState: StateFlow<VendorProfileUiState> = _uiState.asStateFlow()

    init {
        val phone = tokenManager.getUserPhone()
        _uiState.value = _uiState.value.copy(
            displayName = phone?.takeIf { it.isNotBlank() } ?: "Vendor User",
            phone = phone
        )
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

