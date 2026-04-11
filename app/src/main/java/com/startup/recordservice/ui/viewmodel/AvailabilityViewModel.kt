package com.startup.recordservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.AvailabilityRequest
import com.startup.recordservice.data.model.AvailabilityResponse
import com.startup.recordservice.data.model.BusinessResponse
import com.startup.recordservice.data.model.InventoryResponse
import com.startup.recordservice.data.model.PlateResponse
import com.startup.recordservice.data.model.ThemeResponse
import com.startup.recordservice.data.repository.AvailabilityRepository
import com.startup.recordservice.data.repository.BusinessRepository
import com.startup.recordservice.data.repository.InventoryRepository
import com.startup.recordservice.data.repository.PlateRepository
import com.startup.recordservice.data.repository.DishRepository
import com.startup.recordservice.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class AvailabilityItem(
    val id: String,
    val name: String,
    val type: String // "theme" | "inventory" | "plate" | "dish"
)

sealed class AvailabilityUiState {
    data object Idle : AvailabilityUiState()
    data object Loading : AvailabilityUiState()
    data class Error(val message: String) : AvailabilityUiState()
    data object Ready : AvailabilityUiState()
}

@HiltViewModel
class AvailabilityViewModel @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val themeRepository: ThemeRepository,
    private val inventoryRepository: InventoryRepository,
    private val plateRepository: PlateRepository,
    private val dishRepository: DishRepository,
    private val availabilityRepository: AvailabilityRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _uiState = MutableStateFlow<AvailabilityUiState>(AvailabilityUiState.Idle)
    val uiState: StateFlow<AvailabilityUiState> = _uiState.asStateFlow()

    private val _businesses = MutableStateFlow<List<BusinessResponse>>(emptyList())
    val businesses: StateFlow<List<BusinessResponse>> = _businesses.asStateFlow()

    private val _selectedBusinessId = MutableStateFlow<String?>(null)
    val selectedBusinessId: StateFlow<String?> = _selectedBusinessId.asStateFlow()

    private val _items = MutableStateFlow<List<AvailabilityItem>>(emptyList())
    val items: StateFlow<List<AvailabilityItem>> = _items.asStateFlow()

    private val _selectedItem = MutableStateFlow<AvailabilityItem?>(null)
    val selectedItem: StateFlow<AvailabilityItem?> = _selectedItem.asStateFlow()

    private val _availabilities = MutableStateFlow<List<AvailabilityResponse>>(emptyList())
    val availabilities: StateFlow<List<AvailabilityResponse>> = _availabilities.asStateFlow()

    fun loadInitial() {
        viewModelScope.launch {
            try {
                if (!tokenManager.isLoggedIn()) {
                    _uiState.value = AvailabilityUiState.Error("User not logged in")
                    return@launch
                }
                _uiState.value = AvailabilityUiState.Loading

                val phone = tokenManager.getUserPhone()
                if (phone.isNullOrBlank()) {
                    _uiState.value = AvailabilityUiState.Error("User phone not found")
                    return@launch
                }

                businessRepository.getUserBusinesses(phone)
                    .onSuccess { list ->
                        _businesses.value = list
                        val firstId = list.firstOrNull()?.businessId
                        _selectedBusinessId.value = firstId
                        _uiState.value = AvailabilityUiState.Ready
                        if (!firstId.isNullOrBlank()) {
                            loadItemsForBusiness(firstId)
                        }
                    }
                    .onFailure { e ->
                        _uiState.value = AvailabilityUiState.Error(e.message ?: "Failed to load businesses")
                    }
            } catch (e: Exception) {
                _uiState.value = AvailabilityUiState.Error(e.message ?: "Failed to load availability")
            }
        }
    }

    fun selectBusiness(businessId: String) {
        _selectedBusinessId.value = businessId
        _selectedItem.value = null
        _availabilities.value = emptyList()
        loadItemsForBusiness(businessId)
    }

    private fun loadItemsForBusiness(businessId: String) {
        viewModelScope.launch {
            _uiState.value = AvailabilityUiState.Loading

            val all = mutableListOf<AvailabilityItem>()

            themeRepository.getBusinessThemes(businessId)
                .onSuccess { themes: List<ThemeResponse> ->
                    themes.forEach { t ->
                        val id = t.themeId
                        if (!id.isNullOrBlank()) {
                            all.add(AvailabilityItem(id = id, name = t.themeName ?: "Theme", type = "theme"))
                        }
                    }
                }

            inventoryRepository.getBusinessInventory(businessId)
                .onSuccess { inv: List<InventoryResponse> ->
                    inv.forEach { i ->
                        val id = i.inventoryId
                        if (!id.isNullOrBlank()) {
                            // InventoryResponse maps backend `inventoryName` into `itemName`
                            val name = i.itemName ?: "Inventory"
                            all.add(AvailabilityItem(id = id, name = name, type = "inventory"))
                        }
                    }
                }

            plateRepository.getBusinessPlates(businessId)
                .onSuccess { plates: List<PlateResponse> ->
                    plates.forEach { p ->
                        val id = p.plateId
                        if (!id.isNullOrBlank()) {
                            // PlateResponse maps backend `dishName` into `plateName`
                            all.add(AvailabilityItem(id = id, name = p.plateName ?: "Plate", type = "plate"))
                        }
                    }
                }

            // Dishes are needed for catering workflows (client orders may include DISH items directly)
            dishRepository.getBusinessDishes(businessId)
                .onSuccess { dishes: List<com.startup.recordservice.data.model.DishResponse> ->
                    dishes.forEach { d ->
                        val id = d.dishId
                        if (!id.isNullOrBlank()) {
                            all.add(
                                AvailabilityItem(
                                    id = id,
                                    name = d.dishName ?: "Dish",
                                    type = "dish"
                                )
                            )
                        }
                    }
                }

            _items.value = all
            _uiState.value = AvailabilityUiState.Ready

            // auto-select first item to match web UX
            val first = all.firstOrNull()
            if (first != null) {
                selectItem(first)
            }
        }
    }

    fun selectItem(item: AvailabilityItem) {
        _selectedItem.value = item
        refreshAvailabilities()
    }

    fun refreshAvailabilities() {
        val item = _selectedItem.value ?: return
        viewModelScope.launch {
            _uiState.value = AvailabilityUiState.Loading
            availabilityRepository.getAvailabilitiesForItem(item.id, item.type)
                .onSuccess { list ->
                    _availabilities.value = list
                    _uiState.value = AvailabilityUiState.Ready
                }
                .onFailure { e ->
                    _uiState.value = AvailabilityUiState.Error(e.message ?: "Failed to load availabilities")
                }
        }
    }

    fun saveAvailability(
        date: LocalDate,
        availableQuantity: Int,
        isAvailable: Boolean,
        priceOverride: Double?
    ) {
        val businessId = _selectedBusinessId.value
        val item = _selectedItem.value
        if (businessId.isNullOrBlank() || item == null) return

        viewModelScope.launch {
            _uiState.value = AvailabilityUiState.Loading
            val req = AvailabilityRequest(
                itemId = item.id,
                itemType = item.type,
                businessId = businessId,
                availabilityDate = date.format(dateFormatter),
                availableQuantity = availableQuantity,
                isAvailable = isAvailable,
                priceOverride = priceOverride
            )
            availabilityRepository.createOrUpdateAvailability(req)
                .onSuccess {
                    refreshAvailabilities()
                }
                .onFailure { e ->
                    _uiState.value = AvailabilityUiState.Error(e.message ?: "Failed to save availability")
                }
        }
    }

    fun deleteAvailability(date: LocalDate) {
        val item = _selectedItem.value ?: return
        viewModelScope.launch {
            _uiState.value = AvailabilityUiState.Loading
            availabilityRepository.deleteAvailability(item.id, item.type, date.format(dateFormatter))
                .onSuccess {
                    refreshAvailabilities()
                }
                .onFailure { e ->
                    _uiState.value = AvailabilityUiState.Error(e.message ?: "Failed to delete availability")
                }
        }
    }
}

