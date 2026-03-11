package com.startup.recordservice.ui.screens.vendor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.startup.recordservice.data.model.InventoryCreateRequest
import com.startup.recordservice.ui.viewmodel.VendorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInventoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: VendorViewModel = hiltViewModel()
) {
    val businesses by viewModel.businesses.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedImages by viewModel.inventoryImageUris.collectAsStateWithLifecycle()

    var selectedBusinessId by remember {
        mutableStateOf(businesses.firstOrNull()?.businessId.orEmpty())
    }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    // Load businesses when screen opens
    LaunchedEffect(Unit) {
        if (businesses.isEmpty()) {
            viewModel.loadData()
        }
    }

    // Ensure a business is selected once data is loaded
    LaunchedEffect(businesses) {
        if (businesses.isNotEmpty() && selectedBusinessId.isBlank()) {
            selectedBusinessId = businesses.first().businessId.orEmpty()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        viewModel.setInventoryImageUris(uris)
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Add Inventory Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Inventory Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (businesses.size > 1) {
                // Simple dropdown: for now, just pick first business
                Text(
                    text = "Business: ${businesses.find { it.businessId == selectedBusinessId }?.businessName.orEmpty()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (businesses.size == 1) {
                selectedBusinessId = businesses.first().businessId ?: ""
                Text(
                    text = "Business: ${businesses.first().businessName.orEmpty()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "No business found. Create a business first.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name") },
                leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                maxLines = 4
            )
            if (description.isBlank()) {
                Text(
                    text = "Description is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Category dropdown for inventory
            val inventoryCategories = listOf(
                "Tent",
                "Furniture",
                "Lighting",
                "Sound System",
                "Decoration",
                "Catering Equipment",
                "Other"
            )
            var categoryExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Category") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    inventoryCategories.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Upload Images section
            Text(
                text = "Upload Images *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Add one or more images for this inventory item (max 10).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Select Images (${selectedImages.size} selected)")
            }

            if (selectedImages.isNotEmpty()) {
                // Use regular Column/Row instead of LazyVerticalGrid to avoid infinite constraints
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedImages.chunked(3).forEach { rowImages ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowImages.forEach { uri ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeInventoryImage(uri) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .background(
                                                Color.Black.copy(alpha = 0.6f),
                                                RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            // Fill remaining space if row has less than 3 items
                            repeat(3 - rowImages.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val businessId = selectedBusinessId
                    val priceValue = price.toDoubleOrNull() ?: 0.0
                    val qtyValue = quantity.toIntOrNull() ?: 0

                    if (businessId.isBlank()) return@Button

                    val request = InventoryCreateRequest(
                        businessId = businessId,
                        inventoryName = name,
                        inventoryDescription = description,
                        inventoryCategory = category,
                        price = priceValue,
                        quantity = qtyValue,
                        isActive = true
                    )

                    viewModel.createInventory(request, selectedImages) {
                        onNavigateBack()
                    }
                },
                // Backend requires inventoryDescription to be non-empty
                enabled = name.isNotBlank()
                    && description.isNotBlank()
                    && price.isNotBlank()
                    && category.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Add Inventory Item")
            }

            if (uiState is com.startup.recordservice.ui.viewmodel.VendorUiState.Error) {
                Text(
                    text = (uiState as com.startup.recordservice.ui.viewmodel.VendorUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

