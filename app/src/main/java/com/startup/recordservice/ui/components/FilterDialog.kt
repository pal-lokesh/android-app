package com.startup.recordservice.ui.components

import androidx.compose.foundation.layout.*
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
import kotlin.ranges.ClosedFloatingPointRange

data class FilterOptions(
    val eventType: String = "all",
    val category: String = "all",
    val location: String = "all", // "all", "nearby", "custom"
    val radius: Int = 10, // km
    val budget: String = "all", // "all", "custom"
    val minBudget: Double = 0.0,
    val maxBudget: Double = 100000.0,
    val sortBy: String = "default", // "default", "price-low", "price-high", "rating-high", "rating-low"
    val minRating: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    onApply: (FilterOptions) -> Unit,
    onReset: () -> Unit,
    initialFilters: FilterOptions = FilterOptions(),
    activeFilterCount: Int = 0
) {
    var filters by remember { mutableStateOf(initialFilters) }
    
    LaunchedEffect(initialFilters) {
        filters = initialFilters
    }
    
    val hasActiveFilters = activeFilterCount > 0 ||
        filters.eventType != "all" ||
        filters.category != "all" ||
        filters.location != "all" ||
        filters.budget != "all" ||
        filters.sortBy != "default" ||
        filters.minRating > 0
    
    if (open) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Filters",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (hasActiveFilters) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = activeFilterCount.toString(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Event Type Filter
                    FilterSection(title = "Event Type") {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = when (filters.eventType) {
                                    "all" -> "All Events"
                                    "wedding" -> "Weddings"
                                    "birthday" -> "Birthdays"
                                    "corporate" -> "Corporate Events"
                                    "anniversary" -> "Anniversaries"
                                    else -> filters.eventType
                                },
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf(
                                    "all" to "All Events",
                                    "wedding" to "Weddings",
                                    "birthday" to "Birthdays",
                                    "corporate" to "Corporate Events",
                                    "anniversary" to "Anniversaries"
                                ).forEach { (value, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            filters = filters.copy(eventType = value)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Category Filter
                    FilterSection(title = "Category") {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = when (filters.category) {
                                    "all" -> "All Categories"
                                    "tent" -> "Tent & Events"
                                    "caters" -> "Catering"
                                    "decoration" -> "Decoration"
                                    else -> filters.category
                                },
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf(
                                    "all" to "All Categories",
                                    "tent" to "Tent & Events",
                                    "caters" to "Catering",
                                    "decoration" to "Decoration"
                                ).forEach { (value, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            filters = filters.copy(category = value)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Divider()
                    
                    // Location Filter
                    FilterSection(title = "Location") {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = when (filters.location) {
                                    "all" -> "All Locations"
                                    "nearby" -> "Nearby (10 km)"
                                    "custom" -> "Custom Radius"
                                    else -> filters.location
                                },
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf(
                                    "all" to "All Locations",
                                    "nearby" to "Nearby (10 km)",
                                    "custom" to "Custom Radius"
                                ).forEach { (value, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            filters = filters.copy(location = value)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        if (filters.location == "custom") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Radius: ${filters.radius} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = filters.radius.toFloat(),
                                onValueChange = { filters = filters.copy(radius = it.toInt()) },
                                valueRange = 1f..50f,
                                steps = 48
                            )
                        }
                    }
                    
                    Divider()
                    
                    // Budget Filter
                    FilterSection(title = "Budget Range") {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = if (filters.budget == "all") "All Budgets" else "Custom Range",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All Budgets") },
                                    onClick = {
                                        filters = filters.copy(budget = "all")
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Custom Range") },
                                    onClick = {
                                        filters = filters.copy(budget = "custom")
                                        expanded = false
                                    }
                                )
                            }
                        }
                        
                        if (filters.budget == "custom") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = filters.minBudget.toInt().toString(),
                                    onValueChange = {
                                        filters = filters.copy(minBudget = it.toDoubleOrNull() ?: 0.0)
                                    },
                                    label = { Text("Min Budget (₹)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = filters.maxBudget.toInt().toString(),
                                    onValueChange = {
                                        filters = filters.copy(maxBudget = it.toDoubleOrNull() ?: 100000.0)
                                    },
                                    label = { Text("Max Budget (₹)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Range: ₹${filters.minBudget.toInt()} - ₹${filters.maxBudget.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // Use two separate sliders for min and max budget
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Min: ₹${filters.minBudget.toInt()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Slider(
                                    value = filters.minBudget.toFloat(),
                                    onValueChange = { 
                                        val newMin = it.toDouble().coerceIn(0.0, filters.maxBudget)
                                        filters = filters.copy(minBudget = newMin)
                                    },
                                    valueRange = 0f..filters.maxBudget.toFloat().coerceAtMost(200000f),
                                    steps = ((filters.maxBudget.toInt() / 5000).coerceAtMost(39)).toInt()
                                )
                                Text(
                                    text = "Max: ₹${filters.maxBudget.toInt()}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Slider(
                                    value = filters.maxBudget.toFloat(),
                                    onValueChange = { 
                                        val newMax = it.toDouble().coerceIn(filters.minBudget, 200000.0)
                                        filters = filters.copy(maxBudget = newMax)
                                    },
                                    valueRange = filters.minBudget.toFloat().coerceAtLeast(0f)..200000f,
                                    steps = (((200000 - filters.minBudget.toInt()) / 5000).coerceAtMost(39)).toInt()
                                )
                            }
                        }
                    }
                    
                    Divider()
                    
                    // Sort By
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterSection(title = "Sort By", modifier = Modifier.weight(1f)) {
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = when (filters.sortBy) {
                                        "default" -> "Default"
                                        "price-low" -> "Price: Low to High"
                                        "price-high" -> "Price: High to Low"
                                        "rating-high" -> "Rating: High to Low"
                                        "rating-low" -> "Rating: Low to High"
                                        else -> filters.sortBy
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    listOf(
                                        "default" to "Default",
                                        "price-low" to "Price: Low to High",
                                        "price-high" to "Price: High to Low",
                                        "rating-high" to "Rating: High to Low",
                                        "rating-low" to "Rating: Low to High"
                                    ).forEach { (value, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                filters = filters.copy(sortBy = value)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Minimum Rating
                        FilterSection(title = "Min Rating", modifier = Modifier.weight(1f)) {
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = when (filters.minRating) {
                                        0.0 -> "Any Rating"
                                        3.0 -> "3+ Stars"
                                        4.0 -> "4+ Stars"
                                        4.5 -> "4.5+ Stars"
                                        else -> "${filters.minRating}+ Stars"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    listOf(
                                        0.0 to "Any Rating",
                                        3.0 to "3+ Stars",
                                        4.0 to "4+ Stars",
                                        4.5 to "4.5+ Stars"
                                    ).forEach { (value, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                filters = filters.copy(minRating = value)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            filters = FilterOptions()
                            onReset()
                        },
                        enabled = hasActiveFilters
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onApply(filters) }
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply")
                    }
                }
            }
        )
    }
}

@Composable
private fun FilterSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}
