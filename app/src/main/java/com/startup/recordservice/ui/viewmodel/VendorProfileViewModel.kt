package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.startup.recordservice.data.local.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
    private val tokenManager: TokenManager
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
}

