package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.model.BusinessResponse
import com.startup.recordservice.data.model.ThemeResponse
import com.startup.recordservice.data.model.InventoryResponse
import com.startup.recordservice.data.repository.BusinessRepository
import com.startup.recordservice.data.repository.ThemeRepository
import com.startup.recordservice.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StartupUiState {
    data object Loading : StartupUiState()
    data class Success(
        val businesses: List<BusinessResponse> = emptyList(),
        val themes: List<ThemeResponse> = emptyList(),
        val inventory: List<InventoryResponse> = emptyList()
    ) : StartupUiState()
    data class Error(val message: String) : StartupUiState()
}

/**
 * ViewModel to fetch initial data on app startup
 * This ensures data is available immediately when user logs in
 */
@HiltViewModel
class StartupViewModel @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val themeRepository: ThemeRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<StartupUiState>(StartupUiState.Loading)
    val uiState: StateFlow<StartupUiState> = _uiState.asStateFlow()
    
    private val _businesses = MutableStateFlow<List<BusinessResponse>>(emptyList())
    val businesses: StateFlow<List<BusinessResponse>> = _businesses.asStateFlow()
    
    private val _themes = MutableStateFlow<List<ThemeResponse>>(emptyList())
    val themes: StateFlow<List<ThemeResponse>> = _themes.asStateFlow()
    
    private val _inventory = MutableStateFlow<List<InventoryResponse>>(emptyList())
    val inventory: StateFlow<List<InventoryResponse>> = _inventory.asStateFlow()
    
    init {
        // Fetch data immediately when ViewModel is created
        loadStartupData()
    }
    
    fun loadStartupData() {
        viewModelScope.launch {
            try {
                _uiState.value = StartupUiState.Loading
                android.util.Log.d("StartupViewModel", "Loading startup data...")
                
                // Fetch all data in parallel for better performance
                val businessesResult = businessRepository.getAllBusinesses()
                val themesResult = themeRepository.getAllThemes()
                val inventoryResult = inventoryRepository.getAllInventory()
                
                val businessesList = businessesResult.getOrElse { 
                    android.util.Log.w("StartupViewModel", "Failed to load businesses: ${businessesResult.exceptionOrNull()?.message}")
                    emptyList() 
                }
                val themesList = themesResult.getOrElse { 
                    android.util.Log.w("StartupViewModel", "Failed to load themes: ${themesResult.exceptionOrNull()?.message}")
                    emptyList() 
                }
                val inventoryList = inventoryResult.getOrElse { 
                    android.util.Log.w("StartupViewModel", "Failed to load inventory: ${inventoryResult.exceptionOrNull()?.message}")
                    emptyList() 
                }
                
                _businesses.value = businessesList
                _themes.value = themesList
                _inventory.value = inventoryList
                
                android.util.Log.d("StartupViewModel", "Loaded: ${businessesList.size} businesses, ${themesList.size} themes, ${inventoryList.size} inventory items")
                
                _uiState.value = StartupUiState.Success(
                    businesses = businessesList,
                    themes = themesList,
                    inventory = inventoryList
                )
            } catch (e: Exception) {
                android.util.Log.e("StartupViewModel", "Error loading startup data: ${e.message}", e)
                _uiState.value = StartupUiState.Error(
                    "Failed to load data: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    fun refresh() {
        loadStartupData()
    }
}
