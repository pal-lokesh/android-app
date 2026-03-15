package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.model.BusinessResponse
import com.startup.recordservice.data.model.RatingResponse
import com.startup.recordservice.data.repository.BusinessRepository
import com.startup.recordservice.data.repository.RatingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ClientRatingsUiState {
    data object Idle : ClientRatingsUiState()
    data object Loading : ClientRatingsUiState()
    data object Success : ClientRatingsUiState()
    data class Error(val message: String) : ClientRatingsUiState()
}

@HiltViewModel
class ClientRatingsViewModel @Inject constructor(
    private val ratingRepository: RatingRepository,
    private val businessRepository: BusinessRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ClientRatingsUiState>(ClientRatingsUiState.Idle)
    val uiState: StateFlow<ClientRatingsUiState> = _uiState.asStateFlow()
    
    private val _ratings = MutableStateFlow<List<RatingResponse>>(emptyList())
    val ratings: StateFlow<List<RatingResponse>> = _ratings.asStateFlow()
    
    private val _businesses = MutableStateFlow<Map<String, BusinessResponse>>(emptyMap())
    val businesses: StateFlow<Map<String, BusinessResponse>> = _businesses.asStateFlow()
    
    fun loadRatings() {
        viewModelScope.launch {
            _uiState.value = ClientRatingsUiState.Loading
            try {
                ratingRepository.getClientRatings()
                    .onSuccess { ratingsList ->
                        _ratings.value = ratingsList
                        
                        // Load business details for each rating
                        val businessIds = ratingsList.mapNotNull { it.businessId }.distinct()
                        val businessesMap = mutableMapOf<String, BusinessResponse>()
                        
                        businessIds.forEach { businessId ->
                            businessRepository.getBusiness(businessId)
                                .onSuccess { business ->
                                    businessesMap[businessId] = business
                                }
                                .onFailure {
                                    android.util.Log.e("ClientRatingsViewModel", "Failed to load business $businessId")
                                }
                        }
                        
                        _businesses.value = businessesMap
                        _uiState.value = ClientRatingsUiState.Success
                    }
                    .onFailure { e ->
                        _uiState.value = ClientRatingsUiState.Error(
                            e.message ?: "Failed to load ratings"
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = ClientRatingsUiState.Error(
                    e.message ?: "Failed to load ratings"
                )
            }
        }
    }
}
