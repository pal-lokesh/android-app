package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.model.BusinessResponse
import com.startup.recordservice.data.model.PlateResponse
import com.startup.recordservice.data.model.DishResponse
import com.startup.recordservice.data.model.InventoryResponse
import com.startup.recordservice.data.repository.BusinessRepository
import com.startup.recordservice.data.repository.PlateRepository
import com.startup.recordservice.data.repository.DishRepository
import com.startup.recordservice.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class BusinessDetailUiState {
    data object Idle : BusinessDetailUiState()
    data object Loading : BusinessDetailUiState()
    data class Success(
        val business: BusinessResponse,
        val plates: List<PlateResponse> = emptyList(),
        val inventory: List<InventoryResponse> = emptyList()
    ) : BusinessDetailUiState()
    data class Error(val message: String) : BusinessDetailUiState()
}

@HiltViewModel
class BusinessDetailViewModel @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val plateRepository: PlateRepository,
    private val dishRepository: DishRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<BusinessDetailUiState>(BusinessDetailUiState.Idle)
    val uiState: StateFlow<BusinessDetailUiState> = _uiState.asStateFlow()
    
    private val _business = MutableStateFlow<BusinessResponse?>(null)
    val business: StateFlow<BusinessResponse?> = _business.asStateFlow()
    
    private val _plates = MutableStateFlow<List<PlateResponse>>(emptyList())
    val plates: StateFlow<List<PlateResponse>> = _plates.asStateFlow()
    
    private val _inventory = MutableStateFlow<List<InventoryResponse>>(emptyList())
    val inventory: StateFlow<List<InventoryResponse>> = _inventory.asStateFlow()
    
    private val _plateDishes = MutableStateFlow<Map<String, List<DishResponse>>>(emptyMap())
    val plateDishes: StateFlow<Map<String, List<DishResponse>>> = _plateDishes.asStateFlow()
    
    fun loadBusiness(businessId: String) {
        if (businessId.isBlank()) {
            android.util.Log.e("BusinessDetailViewModel", "Cannot load business: ID is blank")
            _uiState.value = BusinessDetailUiState.Error("Invalid business ID")
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = BusinessDetailUiState.Loading
                android.util.Log.d("BusinessDetailViewModel", "Loading business with ID: $businessId")
                
                // Load business details
                val businessResult = businessRepository.getBusiness(businessId)
                
                if (businessResult.isFailure) {
                    _uiState.value = BusinessDetailUiState.Error(
                        businessResult.exceptionOrNull()?.message ?: "Failed to load business details"
                    )
                    return@launch
                }
                
                val business = businessResult.getOrNull()!!
                _business.value = business
                
                // Load plates, inventory, and dishes in parallel
                val platesResult = plateRepository.getBusinessPlates(businessId)
                val inventoryResult = inventoryRepository.getBusinessInventory(businessId)
                
                val platesList = platesResult.getOrElse { emptyList() }
                val inventoryList = inventoryResult.getOrElse { emptyList() }
                
                _plates.value = platesList
                _inventory.value = inventoryList
                
                // Load dishes for each plate
                val dishesMap = mutableMapOf<String, List<DishResponse>>()
                platesList.forEach { plate ->
                    dishRepository.getPlateDishes(plate.plateId)
                        .onSuccess { dishes ->
                            dishesMap[plate.plateId] = dishes
                        }
                        .onFailure { exception ->
                            android.util.Log.e("BusinessDetailViewModel", "Failed to load dishes for plate ${plate.plateId}: ${exception.message}")
                        }
                }
                _plateDishes.value = dishesMap
                
                _uiState.value = BusinessDetailUiState.Success(
                    business = business,
                    plates = platesList,
                    inventory = inventoryList
                )
                
                android.util.Log.d("BusinessDetailViewModel", "Loaded business: ${business.businessName}, ${platesList.size} plates, ${inventoryList.size} inventory items")
            } catch (e: Exception) {
                android.util.Log.e("BusinessDetailViewModel", "Error loading business: ${e.message}", e)
                _uiState.value = BusinessDetailUiState.Error(
                    "Failed to load business: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
}
