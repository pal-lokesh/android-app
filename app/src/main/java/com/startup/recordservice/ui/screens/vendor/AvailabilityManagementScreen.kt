package com.startup.recordservice.ui.screens.vendor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.startup.recordservice.data.model.AvailabilityResponse
import com.startup.recordservice.ui.viewmodel.AvailabilityItem
import com.startup.recordservice.ui.viewmodel.AvailabilityUiState
import com.startup.recordservice.ui.viewmodel.AvailabilityViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityManagementScreen(
    onNavigateBack: () -> Unit,
    onDashboardClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onThemeTabClick: () -> Unit = {},
    onInventoryTabClick: () -> Unit = {},
    onAvailabilityClick: () -> Unit = {},
    viewModel: AvailabilityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val businesses by viewModel.businesses.collectAsStateWithLifecycle()
    val selectedBusinessId by viewModel.selectedBusinessId.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    val availabilities by viewModel.availabilities.collectAsStateWithLifecycle()

    var showDialog by remember { mutableStateOf(false) }
    var dialogDate by remember { mutableStateOf<LocalDate?>(null) }
    var dialogQty by remember { mutableStateOf("0") }
    var dialogIsAvailable by remember { mutableStateOf(true) }
    var dialogPriceOverride by remember { mutableStateOf("") }

    var pendingDeleteDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadInitial()
    }

    fun openForEdit(existing: AvailabilityResponse?) {
        val date = existing?.availabilityDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
        dialogDate = date
        dialogQty = (existing?.availableQuantity ?: 0).toString()
        dialogIsAvailable = existing?.isAvailable ?: true
        dialogPriceOverride = existing?.priceOverride?.toString() ?: ""
        showDialog = true
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Availability Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
        ,
        bottomBar = {
            VendorBottomNav(
                currentTab = VendorTab.AVAILABILITY,
                onDashboardClick = onDashboardClick,
                onOrdersClick = onOrdersClick,
                onThemeTabClick = onThemeTabClick,
                onInventoryTabClick = onInventoryTabClick,
                onAvailabilityClick = onAvailabilityClick
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
            Text(
                text = "Set date-wise availability for your themes, inventory, and plates",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (uiState is AvailabilityUiState.Error) {
                val msg = (uiState as AvailabilityUiState.Error).message
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            BusinessDropdown(
                businesses = businesses,
                selectedBusinessId = selectedBusinessId,
                onSelect = { id -> viewModel.selectBusiness(id) }
            )

            ItemDropdown(
                items = items,
                selected = selectedItem,
                onSelect = { it -> viewModel.selectItem(it) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedItem?.let { "Availability for: ${it.name} (${it.type})" } ?: "Select an item",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState is AvailabilityUiState.Loading) {
                        Text(
                            text = "Loading…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Button(
                    onClick = { openForEdit(null) },
                    enabled = selectedItem != null
                ) {
                    Text("Add")
                }
            }

            Divider()

            if (selectedItem == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Select an item to manage availability.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (availabilities.isEmpty() && uiState !is AvailabilityUiState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No availability set yet. Tap Add to create one.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(availabilities, key = { it.availabilityId ?: it.hashCode().toLong() }) { a ->
                        AvailabilityRow(
                            availability = a,
                            onEdit = { openForEdit(a) },
                            onDelete = { pendingDeleteDate = a.availabilityDate?.let { LocalDate.parse(it) } }
                        )
                    }
                }
            }
        }

        if (showDialog) {
            AvailabilityEditDialog(
                initialDate = dialogDate ?: LocalDate.now(),
                qty = dialogQty,
                onQtyChange = { dialogQty = it },
                isAvailable = dialogIsAvailable,
                onIsAvailableChange = { dialogIsAvailable = it },
                priceOverride = dialogPriceOverride,
                onPriceOverrideChange = { dialogPriceOverride = it },
                onDismiss = { showDialog = false },
                onSave = { localDate ->
                    val q = dialogQty.toIntOrNull() ?: 0
                    val price = dialogPriceOverride.trim().takeIf { it.isNotEmpty() }?.toDoubleOrNull()
                    viewModel.saveAvailability(
                        date = localDate,
                        availableQuantity = q,
                        isAvailable = dialogIsAvailable,
                        priceOverride = price
                    )
                    showDialog = false
                }
            )
        }

        if (pendingDeleteDate != null) {
            AlertDialog(
                onDismissRequest = { pendingDeleteDate = null },
                title = { Text("Delete availability?") },
                text = { Text("This will remove availability for ${pendingDeleteDate}.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val d = pendingDeleteDate
                            pendingDeleteDate = null
                            if (d != null) viewModel.deleteAvailability(d)
                        }
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteDate = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BusinessDropdown(
    businesses: List<com.startup.recordservice.data.model.BusinessResponse>,
    selectedBusinessId: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = businesses.firstOrNull { it.businessId == selectedBusinessId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.businessName ?: "Select business",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text("Business") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            businesses.forEach { b ->
                val id = b.businessId ?: return@forEach
                DropdownMenuItem(
                    text = { Text(b.businessName ?: "Business") },
                    onClick = {
                        expanded = false
                        onSelect(id)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDropdown(
    items: List<AvailabilityItem>,
    selected: AvailabilityItem?,
    onSelect: (AvailabilityItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.let { "${it.name} (${it.type})" } ?: "Select item",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            label = { Text("Item") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text("${item.name} (${item.type})") },
                    onClick = {
                        expanded = false
                        onSelect(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun AvailabilityRow(
    availability: AvailabilityResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = availability.availabilityDate ?: "Date",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                val qty = availability.availableQuantity ?: 0
                val available = availability.isAvailable ?: true
                Text(
                    text = "Qty: $qty • ${if (available) "Available" else "Unavailable"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                availability.priceOverride?.let {
                    Text(
                        text = "Price override: ₹$it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvailabilityEditDialog(
    initialDate: LocalDate,
    qty: String,
    onQtyChange: (String) -> Unit,
    isAvailable: Boolean,
    onIsAvailableChange: (Boolean) -> Unit,
    priceOverride: String,
    onPriceOverrideChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )

    var showDateDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { java.time.format.DateTimeFormatter.ISO_LOCAL_DATE }
    var dateText by remember {
        mutableStateOf(
            initialDate.format(dateFormatter)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Availability") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Select date",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = { showDateDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(dateText)
                }

                OutlinedTextField(
                    value = qty,
                    onValueChange = onQtyChange,
                    label = { Text("Available Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceOverride,
                    onValueChange = onPriceOverrideChange,
                    label = { Text("Price Override (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isAvailable,
                        onClick = { onIsAvailableChange(true) },
                        label = { Text("Available") }
                    )
                    FilterChip(
                        selected = !isAvailable,
                        onClick = { onIsAvailableChange(false) },
                        label = { Text("Unavailable") }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val date = LocalDate.parse(dateText, dateFormatter)
                    onSave(date)
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDateDialog) {
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val d = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                            dateText = d.format(dateFormatter)
                        }
                        showDateDialog = false
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) { Text("Cancel") }
            },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

