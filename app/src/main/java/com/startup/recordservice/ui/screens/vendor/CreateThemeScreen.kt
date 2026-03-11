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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.startup.recordservice.data.model.ThemeRequest
import com.startup.recordservice.ui.viewmodel.VendorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateThemeScreen(
    onNavigateBack: () -> Unit,
    viewModel: VendorViewModel = hiltViewModel()
) {
    val businesses by viewModel.businesses.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedImages by viewModel.themeImageUris.collectAsStateWithLifecycle()

    var selectedBusinessId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priceRange by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    // Load businesses when screen opens
    LaunchedEffect(Unit) {
        android.util.Log.d("CreateThemeScreen", "Screen opened, businesses count: ${businesses.size}")
        if (businesses.isEmpty()) {
            android.util.Log.d("CreateThemeScreen", "No businesses found, loading data...")
            viewModel.loadData()
        } else {
            android.util.Log.d("CreateThemeScreen", "Businesses already loaded: ${businesses.map { it.businessName }}")
        }
    }

    // Ensure a business is selected once data is loaded
    LaunchedEffect(businesses) {
        if (businesses.isNotEmpty()) {
            val firstBusinessId = businesses.first().businessId.orEmpty()
            android.util.Log.d("CreateThemeScreen", "Businesses loaded: ${businesses.size}, first businessId: $firstBusinessId")
            if (selectedBusinessId.isBlank() || !businesses.any { it.businessId == selectedBusinessId }) {
                selectedBusinessId = firstBusinessId
                android.util.Log.d("CreateThemeScreen", "Selected businessId: $selectedBusinessId")
            }
        }
    }
    
    // Get the current valid business ID
    val currentBusinessId = remember(selectedBusinessId, businesses) {
        val businessId = if (selectedBusinessId.isNotBlank() && businesses.any { it.businessId == selectedBusinessId }) {
            selectedBusinessId
        } else {
            businesses.firstOrNull()?.businessId.orEmpty()
        }
        android.util.Log.d("CreateThemeScreen", "Current businessId: $businessId (from ${businesses.size} businesses)")
        businessId
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        viewModel.setThemeImageUris(uris)
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Add Theme") },
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
                text = "Theme Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (businesses.isNotEmpty()) {
                val displayBusiness = businesses.find { it.businessId == currentBusinessId } 
                    ?: businesses.first()
                Text(
                    text = "Business: ${displayBusiness.businessName.orEmpty()}",
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
                label = { Text("Theme Name") },
                leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) },
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

            // Category dropdown (fixed options similar to web)
            val themeCategories = listOf(
                "Tent House",
                "Wedding Theme",
                "Birthday Theme",
                "Corporate Event",
                "Festival",
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
                    themeCategories.forEach { option ->
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
                value = priceRange,
                onValueChange = { priceRange = it },
                label = { Text("Price Range (e.g. ₹5000 - ₹10000)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity / Capacity") },
                modifier = Modifier.fillMaxWidth()
            )

            // Upload Images section
            Text(
                text = "Upload Images *",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "At least one image is recommended for the theme (max 10).",
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
                                        onClick = { viewModel.removeThemeImage(uri) },
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
                    val businessId = currentBusinessId
                    val qty = quantity.toIntOrNull() ?: 0
                    
                    if (businessId.isBlank()) {
                        android.util.Log.e("CreateThemeScreen", "Button clicked but businessId is blank!")
                        return@Button
                    }

                    android.util.Log.d("CreateThemeScreen", "Creating theme with businessId=$businessId, name=$name, category=$category")
                    
                    val request = ThemeRequest(
                        businessId = businessId,
                        themeName = name,
                        themeDescription = description,
                        themeCategory = category,
                        priceRange = priceRange,
                        quantity = qty
                    )

                    viewModel.createTheme(request, selectedImages) {
                        onNavigateBack()
                    }
                },
                enabled = name.isNotBlank() 
                    && category.isNotBlank() 
                    && currentBusinessId.isNotBlank(),
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
                    Text("Add Theme")
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

