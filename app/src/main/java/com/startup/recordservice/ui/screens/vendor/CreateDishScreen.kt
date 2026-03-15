package com.startup.recordservice.ui.screens.vendor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.startup.recordservice.data.model.DishResponse
import com.startup.recordservice.data.model.PlateResponse
import com.startup.recordservice.ui.viewmodel.VendorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDishScreen(
    plateId: String,
    onNavigateBack: () -> Unit,
    viewModel: VendorViewModel = hiltViewModel()
) {
    val businesses by viewModel.businesses.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dishType by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var selectedImages by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }
    
    // Get businessId from plateId - we'll need to fetch the plate first
    // For now, use first business
    val currentBusinessId = remember(businesses) {
        businesses.firstOrNull()?.businessId.orEmpty()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedImages = uris.take(10) // Limit to 10 images
    }

    // Load businesses when screen opens
    LaunchedEffect(Unit) {
        if (businesses.isEmpty()) {
            viewModel.loadData()
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Add Dish") },
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
                text = "Dish Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Plate ID: $plateId",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Dish Name") },
                leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                maxLines = 4
            )

            // Dish Type dropdown
            val dishTypes = listOf(
                "Veg",
                "Non-Veg",
                "Vegan",
                "Dessert",
                "Beverage",
                "Appetizer",
                "Main Course",
                "Side Dish",
                "Other"
            )
            var dishTypeExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = dishTypeExpanded,
                onExpandedChange = { dishTypeExpanded = !dishTypeExpanded }
            ) {
                OutlinedTextField(
                    value = dishType,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Dish Type") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dishTypeExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dishTypeExpanded,
                    onDismissRequest = { dishTypeExpanded = false }
                ) {
                    dishTypes.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                dishType = option
                                dishTypeExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (₹)") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity / Serving Size") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Upload Images section
            Text(
                text = "Upload Images",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Add images for the dish (max 10).",
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
                                        onClick = { selectedImages = selectedImages.filter { it != uri } },
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
                    val bid = currentBusinessId
                    val priceValue = price.toDoubleOrNull() ?: 0.0
                    val qty = quantity.toIntOrNull() ?: 0
                    
                    if (bid.isBlank() || plateId.isBlank()) {
                        android.util.Log.e("CreateDishScreen", "BusinessId or PlateId is blank!")
                        return@Button
                    }

                    val request = DishResponse(
                        businessId = bid,
                        plateId = plateId,
                        dishName = name,
                        description = description,
                        dishType = dishType,
                        price = priceValue,
                        quantity = qty
                    )

                    android.util.Log.d("CreateDishScreen", "Creating dish: $name for plate: $plateId")
                    viewModel.createDish(request, selectedImages) { onNavigateBack() }
                },
                enabled = name.isNotBlank() 
                    && dishType.isNotBlank() 
                    && currentBusinessId.isNotBlank()
                    && plateId.isNotBlank()
                    && price.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState is com.startup.recordservice.ui.viewmodel.VendorUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Dish")
                }
            }

            if (uiState is com.startup.recordservice.ui.viewmodel.VendorUiState.Error) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = (uiState as com.startup.recordservice.ui.viewmodel.VendorUiState.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
