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

sealed class ExploreUiState {
    data object Loading : ExploreUiState()
    data class Success(
        val businesses: List<BusinessResponse> = emptyList(),
        val themes: List<ThemeResponse> = emptyList(),
        val inventory: List<InventoryResponse> = emptyList()
    ) : ExploreUiState()
    data class Error(val message: String) : ExploreUiState()
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val themeRepository: ThemeRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()
    
    private val _businesses = MutableStateFlow<List<BusinessResponse>>(emptyList())
    val businesses: StateFlow<List<BusinessResponse>> = _businesses.asStateFlow()
    
    private val _themes = MutableStateFlow<List<ThemeResponse>>(emptyList())
    val themes: StateFlow<List<ThemeResponse>> = _themes.asStateFlow()
    
    private val _inventory = MutableStateFlow<List<InventoryResponse>>(emptyList())
    val inventory: StateFlow<List<InventoryResponse>> = _inventory.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String>("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = ExploreUiState.Loading
                
                val businessesResult = businessRepository.getAllBusinesses()
                val themesResult = themeRepository.getAllThemes()
                val inventoryResult = inventoryRepository.getAllInventory()
                
                val businessesList = businessesResult.getOrElse { emptyList() }
                val themesList = themesResult.getOrElse { emptyList() }
                val inventoryList = inventoryResult.getOrElse { emptyList() }
                
                _businesses.value = businessesList
                _themes.value = themesList
                _inventory.value = inventoryList
                
                _uiState.value = ExploreUiState.Success(
                    businesses = businessesList,
                    themes = themesList,
                    inventory = inventoryList
                )
            } catch (e: Exception) {
                android.util.Log.e("ExploreViewModel", "Error loading data: ${e.message}", e)
                _uiState.value = ExploreUiState.Error(
                    "Failed to load data: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    fun setCategory(category: String) {
        _selectedCategory.value = category
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun getFilteredThemes(): List<ThemeResponse> {
        val category = _selectedCategory.value
        val query = _searchQuery.value.lowercase()
        
        return _themes.value.filter { theme ->
            val matchesCategory = category == "all" || theme.themeCategory?.lowercase() == category.lowercase()
            val matchesSearch = query.isEmpty() || 
                theme.themeName?.lowercase()?.contains(query) == true ||
                theme.themeDescription?.lowercase()?.contains(query) == true ||
                theme.themeCategory?.lowercase()?.contains(query) == true
            matchesCategory && matchesSearch && theme.isActive
        }
    }
    
    fun getFilteredInventory(): List<InventoryResponse> {
        val category = _selectedCategory.value
        val query = _searchQuery.value.lowercase()
        
        return _inventory.value.filter { item ->
            val matchesCategory = category == "all" || item.category?.lowercase() == category.lowercase()
            val matchesSearch = query.isEmpty() || 
                item.itemName?.lowercase()?.contains(query) == true ||
                item.description?.lowercase()?.contains(query) == true ||
                item.category?.lowercase()?.contains(query) == true
            matchesCategory && matchesSearch && item.isActive
        }
    }
    
    fun getFilteredBusinesses(): List<BusinessResponse> {
        val query = _searchQuery.value.lowercase()
        
        return _businesses.value.filter { business ->
            val matchesSearch = query.isEmpty() || 
                business.businessName?.lowercase()?.contains(query) == true ||
                business.category?.lowercase()?.contains(query) == true ||
                business.description?.lowercase()?.contains(query) == true
            matchesSearch && business.isActive
        }
    }
    
    fun refresh() {
        loadData()
    }
}
