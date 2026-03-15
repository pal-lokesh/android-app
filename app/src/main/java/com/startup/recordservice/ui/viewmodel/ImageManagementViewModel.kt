package com.startup.recordservice.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.startup.recordservice.data.local.TokenManager
import com.startup.recordservice.data.model.BusinessResponse
import com.startup.recordservice.data.model.ImageResponse
import com.startup.recordservice.data.repository.BusinessRepository
import com.startup.recordservice.data.repository.ImageRepository
import com.startup.recordservice.data.repository.InventoryImageRepository
import com.startup.recordservice.data.repository.ThemeRepository
import com.startup.recordservice.data.repository.InventoryRepository
import com.startup.recordservice.data.repository.PlateRepository
import com.startup.recordservice.data.repository.DishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImageManagementItem(
    val id: String,
    val name: String,
    val type: String
)

sealed class ImageManagementUiState {
    data object Idle : ImageManagementUiState()
    data object Loading : ImageManagementUiState()
    data class Error(val message: String) : ImageManagementUiState()
    data object Success : ImageManagementUiState()
}

@HiltViewModel
class ImageManagementViewModel @Inject constructor(
    application: Application,
    private val tokenManager: TokenManager,
    private val businessRepository: BusinessRepository,
    private val imageRepository: ImageRepository,
    private val inventoryImageRepository: InventoryImageRepository,
    private val themeRepository: ThemeRepository,
    private val inventoryRepository: InventoryRepository,
    private val plateRepository: PlateRepository,
    private val dishRepository: DishRepository
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow<ImageManagementUiState>(ImageManagementUiState.Idle)
    val uiState: StateFlow<ImageManagementUiState> = _uiState.asStateFlow()
    
    private val _businesses = MutableStateFlow<List<BusinessResponse>>(emptyList())
    val businesses: StateFlow<List<BusinessResponse>> = _businesses.asStateFlow()
    
    private val _selectedBusinessId = MutableStateFlow<String?>(null)
    val selectedBusinessId: StateFlow<String?> = _selectedBusinessId.asStateFlow()
    
    private val _selectedItemType = MutableStateFlow<String?>(null)
    val selectedItemType: StateFlow<String?> = _selectedItemType.asStateFlow()
    
    private val _items = MutableStateFlow<List<ImageManagementItem>>(emptyList())
    val items: StateFlow<List<ImageManagementItem>> = _items.asStateFlow()
    
    private val _selectedItemId = MutableStateFlow<String?>(null)
    val selectedItemId: StateFlow<String?> = _selectedItemId.asStateFlow()
    
    private val _images = MutableStateFlow<List<ImageResponse>>(emptyList())
    val images: StateFlow<List<ImageResponse>> = _images.asStateFlow()
    
    fun loadInitial() {
        viewModelScope.launch {
            try {
                if (!tokenManager.isLoggedIn()) {
                    _uiState.value = ImageManagementUiState.Error("User not logged in")
                    return@launch
                }
                
                val phone = tokenManager.getUserPhone()
                if (phone.isNullOrBlank()) {
                    _uiState.value = ImageManagementUiState.Error("User phone not found")
                    return@launch
                }
                
                _uiState.value = ImageManagementUiState.Loading
                businessRepository.getUserBusinesses(phone)
                    .onSuccess { businesses ->
                        _businesses.value = businesses
                        _selectedBusinessId.value = businesses.firstOrNull()?.businessId
                        _uiState.value = ImageManagementUiState.Success
                    }
                    .onFailure { e ->
                        _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to load businesses")
                    }
            } catch (e: Exception) {
                _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to load data")
            }
        }
    }
    
    fun selectBusiness(businessId: String) {
        _selectedBusinessId.value = businessId
        _selectedItemType.value = null
        _selectedItemId.value = null
        _items.value = emptyList()
        _images.value = emptyList()
    }
    
    fun selectItemType(type: String) {
        _selectedItemType.value = type
        _selectedItemId.value = null
        _images.value = emptyList()
        loadItemsForBusiness(_selectedBusinessId.value ?: return)
    }
    
    fun selectItem(itemId: String) {
        _selectedItemId.value = itemId
    }
    
    fun loadItemsForBusiness(businessId: String) {
        viewModelScope.launch {
            val itemType = _selectedItemType.value ?: return@launch
            
            _uiState.value = ImageManagementUiState.Loading
            try {
                val itemsList = mutableListOf<ImageManagementItem>()
                
                when (itemType) {
                    "theme" -> {
                        themeRepository.getBusinessThemes(businessId)
                            .onSuccess { themes ->
                                themes.forEach { theme ->
                                    theme.themeId?.let { id ->
                                        itemsList.add(ImageManagementItem(
                                            id = id,
                                            name = theme.themeName ?: "Unnamed Theme",
                                            type = "theme"
                                        ))
                                    }
                                }
                                _items.value = itemsList
                                _uiState.value = ImageManagementUiState.Success
                            }
                            .onFailure { e ->
                                _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to load themes")
                            }
                    }
                    "inventory" -> {
                        inventoryRepository.getBusinessInventory(businessId)
                            .onSuccess { inventory ->
                                inventory.forEach { item ->
                                    item.inventoryId?.let { id ->
                                        itemsList.add(ImageManagementItem(
                                            id = id,
                                            name = item.itemName ?: "Unnamed Item",
                                            type = "inventory"
                                        ))
                                    }
                                }
                                _items.value = itemsList
                                _uiState.value = ImageManagementUiState.Success
                            }
                            .onFailure { e ->
                                _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to load inventory")
                            }
                    }
                    "plate" -> {
                        plateRepository.getBusinessPlates(businessId)
                            .onSuccess { plates ->
                                plates.forEach { plate ->
                                    plate.plateId?.let { id ->
                                        itemsList.add(ImageManagementItem(
                                            id = id,
                                            name = plate.plateName ?: "Unnamed Plate",
                                            type = "plate"
                                        ))
                                    }
                                }
                                _items.value = itemsList
                                _uiState.value = ImageManagementUiState.Success
                            }
                            .onFailure { e ->
                                _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to load plates")
                            }
                    }
                    "dish" -> {
                        // For dishes, we need to get plates first, then dishes for each plate
                        plateRepository.getBusinessPlates(businessId)
                            .onSuccess { plates ->
                                plates.forEach { plate ->
                                    plate.plateId?.let { plateId ->
                                        dishRepository.getPlateDishes(plateId)
                                            .onSuccess { dishes ->
                                                dishes.forEach { dish ->
                                                    dish.dishId?.let { dishId ->
                                                        itemsList.add(ImageManagementItem(
                                                            id = dishId,
                                                            name = dish.dishName ?: "Unnamed Dish",
                                                            type = "dish"
                                                        ))
                                                    }
                                                }
                                                _items.value = itemsList.toList()
                                                _uiState.value = ImageManagementUiState.Success
                                            }
                                    }
                                }
                            }
                            .onFailure { e ->
                                _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to load dishes")
                            }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to load items")
            }
        }
    }
    
    fun loadImages(itemId: String, itemType: String) {
        viewModelScope.launch {
            _uiState.value = ImageManagementUiState.Loading
            try {
                when (itemType) {
                    "theme" -> {
                        imageRepository.getImagesByThemeId(itemId)
                            .onSuccess { images ->
                                _images.value = images
                                _uiState.value = ImageManagementUiState.Success
                            }
                            .onFailure { e ->
                                _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to load images")
                            }
                    }
                    "inventory" -> {
                        inventoryImageRepository.getInventoryImagesByInventoryId(itemId)
                            .onSuccess { images ->
                                _images.value = images.map { invImg ->
                                    ImageResponse(
                                        imageId = invImg.imageId,
                                        imageUrl = invImg.imageUrl,
                                        imageName = invImg.imageName,
                                        inventoryId = invImg.inventoryId
                                    )
                                }
                                _uiState.value = ImageManagementUiState.Success
                            }
                            .onFailure { e ->
                                _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to load images")
                            }
                    }
                    // For plates and dishes, we'll use the generic image endpoint
                    "plate", "dish" -> {
                        // Note: Backend may need specific endpoints for plate/dish images
                        // For now, we'll try to use theme images endpoint as a fallback
                        imageRepository.getImagesByThemeId(itemId)
                            .onSuccess { images ->
                                _images.value = images
                                _uiState.value = ImageManagementUiState.Success
                            }
                            .onFailure {
                                _images.value = emptyList()
                                _uiState.value = ImageManagementUiState.Success
                            }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to load images")
            }
        }
    }
    
    fun uploadImages(imageUris: List<Uri>, itemId: String, itemType: String) {
        viewModelScope.launch {
            _uiState.value = ImageManagementUiState.Loading
            try {
                val context = getApplication<Application>().applicationContext
                val category = when (itemType) {
                    "theme" -> "themes"
                    "inventory" -> "inventory"
                    "plate" -> "plates"
                    "dish" -> "dishes"
                    else -> itemType
                }
                
                imageUris.forEachIndexed { index, uri ->
                    imageRepository.uploadFile(context, uri, category, itemId)
                        .onSuccess { imageUrl ->
                            val imageRequest = com.startup.recordservice.data.model.ImageCreateRequest(
                                themeId = if (itemType == "theme") itemId else null,
                                inventoryId = if (itemType == "inventory") itemId else null,
                                imageName = "${itemType}_${itemId}_$index",
                                imageUrl = imageUrl,
                                isPrimary = index == 0
                            )
                            imageRepository.createImage(imageRequest)
                                .onFailure { e ->
                                    android.util.Log.e("ImageManagementViewModel", "Failed to create image record: ${e.message}")
                                }
                        }
                        .onFailure { e ->
                            android.util.Log.e("ImageManagementViewModel", "Failed to upload image: ${e.message}")
                        }
                }
                
                // Reload images after upload
                loadImages(itemId, itemType)
            } catch (e: Exception) {
                _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to upload images")
            }
        }
    }
    
    fun deleteImage(imageId: String, itemType: String, onResult: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = ImageManagementUiState.Loading
            try {
                when (itemType) {
                    "inventory" -> {
                        inventoryImageRepository.deleteInventoryImage(imageId)
                            .onSuccess {
                                val itemId = _selectedItemId.value
                                if (itemId != null) {
                                    loadImages(itemId, itemType)
                                }
                                onResult()
                            }
                            .onFailure { e ->
                                _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to delete image")
                            }
                    }
                    else -> {
                        // For other types, we may need a generic delete endpoint
                        // For now, just reload images
                        val itemId = _selectedItemId.value
                        if (itemId != null) {
                            loadImages(itemId, itemType)
                        }
                        onResult()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ImageManagementUiState.Error(e.message ?: "Failed to delete image")
            }
        }
    }
}
