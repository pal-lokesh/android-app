package com.startup.recordservice.ui.screens.vendor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.startup.recordservice.data.model.BusinessResponse
import com.startup.recordservice.data.model.ImageResponse
import com.startup.recordservice.ui.viewmodel.ImageManagementViewModel
import com.startup.recordservice.ui.viewmodel.ImageManagementUiState
import com.startup.recordservice.ui.viewmodel.ImageManagementItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImageManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val businesses by viewModel.businesses.collectAsStateWithLifecycle()
    val selectedBusinessId by viewModel.selectedBusinessId.collectAsStateWithLifecycle()
    val itemType by viewModel.selectedItemType.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val selectedItemId by viewModel.selectedItemId.collectAsStateWithLifecycle()
    val images by viewModel.images.collectAsStateWithLifecycle()
    
    var showDeleteDialog by remember { mutableStateOf<ImageResponse?>(null) }
    var showAddImageDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadInitial()
    }
    
    LaunchedEffect(selectedBusinessId) {
        if (selectedBusinessId != null) {
            viewModel.loadItemsForBusiness(selectedBusinessId!!)
        }
    }
    
    LaunchedEffect(selectedItemId, itemType) {
        if (selectedItemId != null && itemType != null) {
            viewModel.loadImages(selectedItemId!!, itemType!!)
        }
    }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty() && selectedItemId != null && itemType != null) {
            viewModel.uploadImages(uris, selectedItemId!!, itemType!!)
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Image Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedItemId != null) {
                        IconButton(
                            onClick = { showAddImageDialog = true },
                            enabled = selectedItemId != null
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Images")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Business Selection
            BusinessSelector(
                businesses = businesses,
                selectedBusinessId = selectedBusinessId,
                onBusinessSelected = { viewModel.selectBusiness(it) },
                label = "Select Business"
            )
            
            // Item Type Selection
            if (selectedBusinessId != null) {
                ItemTypeSelector(
                    selectedType = itemType,
                    onTypeSelected = { viewModel.selectItemType(it) }
                )
            }
            
            // Item Selection
            if (itemType != null && items.isNotEmpty()) {
                ItemSelector(
                    items = items,
                    selectedItemId = selectedItemId,
                    onItemSelected = { viewModel.selectItem(it) }
                )
            }
            
            Divider()
            
            // Images Grid
            if (selectedItemId != null) {
                Text(
                    text = "Images (${images.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (uiState is ImageManagementUiState.Loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (images.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No images yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { showAddImageDialog = true }) {
                                Text("Add Images")
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(images, key = { it.imageId ?: it.hashCode().toString() }) { image ->
                            ImageItem(
                                image = image,
                                onDelete = { showDeleteDialog = image }
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select a business, item type, and item to manage images",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Error message
            if (uiState is ImageManagementUiState.Error) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = (uiState as ImageManagementUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        
        // Delete confirmation dialog
        showDeleteDialog?.let { image ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Image") },
                text = { Text("Are you sure you want to delete this image?") },
                confirmButton = {
                    Button(
                        onClick = {
                            image.imageId?.let { imageId ->
                                viewModel.deleteImage(imageId, itemType ?: "") {
                                    showDeleteDialog = null
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Add image dialog
        if (showAddImageDialog) {
            AlertDialog(
                onDismissRequest = { showAddImageDialog = false },
                title = { Text("Add Images") },
                text = {
                    Text("Select images to upload for this item. You can select multiple images.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                            showAddImageDialog = false
                        }
                    ) {
                        Text("Select Images")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddImageDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSelector(
    businesses: List<BusinessResponse>,
    selectedBusinessId: String?,
    onBusinessSelected: (String) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedBusiness = businesses.firstOrNull { it.businessId == selectedBusinessId }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedBusiness?.businessName ?: label,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            businesses.forEach { business ->
                val businessId = business.businessId ?: return@forEach
                DropdownMenuItem(
                    text = { Text(business.businessName ?: "Business") },
                    onClick = {
                        onBusinessSelected(businessId)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemTypeSelector(
    selectedType: String?,
    onTypeSelected: (String) -> Unit
) {
    val itemTypes = listOf("theme", "inventory", "plate", "dish")
    
    Column {
        Text(
            text = "Item Type",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemTypes.forEach { type ->
                FilterChip(
                    selected = type == selectedType,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.replaceFirstChar { it.uppercase() }) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSelector(
    items: List<ImageManagementItem>,
    selectedItemId: String?,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedItem = items.firstOrNull { it.id == selectedItemId }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedItem?.name ?: "Select Item",
            onValueChange = {},
            readOnly = true,
            label = { Text("Item") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.name) },
                    onClick = {
                        onItemSelected(item.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ImageItem(
    image: ImageResponse,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onDelete() }
    ) {
        AsyncImage(
            model = image.imageUrl,
            contentDescription = image.imageName,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        // Delete overlay
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
        // Primary badge
        if (image.isPrimary == true) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 8.dp),
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Text(
                    text = "Primary",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
