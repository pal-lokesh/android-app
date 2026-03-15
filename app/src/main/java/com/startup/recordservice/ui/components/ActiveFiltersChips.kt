package com.startup.recordservice.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveFiltersChips(
    filters: FilterOptions,
    onRemoveFilter: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeFilters = mutableListOf<Pair<String, String>>()
    
    if (filters.eventType != "all") {
        activeFilters.add("eventType" to when (filters.eventType) {
            "wedding" -> "Weddings"
            "birthday" -> "Birthdays"
            "corporate" -> "Corporate Events"
            "anniversary" -> "Anniversaries"
            else -> filters.eventType
        })
    }
    
    if (filters.category != "all") {
        activeFilters.add("category" to when (filters.category) {
            "tent" -> "Tent & Events"
            "caters" -> "Catering"
            "decoration" -> "Decoration"
            else -> filters.category
        })
    }
    
    if (filters.location != "all") {
        val locationLabel = when (filters.location) {
            "nearby" -> "Nearby (10 km)"
            "custom" -> "Custom (${filters.radius} km)"
            else -> filters.location
        }
        activeFilters.add("location" to locationLabel)
    }
    
    if (filters.budget != "all") {
        activeFilters.add("budget" to "₹${filters.minBudget.toInt()} - ₹${filters.maxBudget.toInt()}")
    }
    
    if (filters.sortBy != "default") {
        activeFilters.add("sortBy" to when (filters.sortBy) {
            "price-low" -> "Price: Low to High"
            "price-high" -> "Price: High to Low"
            "rating-high" -> "Rating: High to Low"
            "rating-low" -> "Rating: Low to High"
            else -> filters.sortBy
        })
    }
    
    if (filters.minRating > 0) {
        activeFilters.add("minRating" to "${filters.minRating}+ Stars")
    }
    
    if (activeFilters.isEmpty()) {
        return
    }
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active Filters:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            TextButton(onClick = onClearAll) {
                Text("Clear All", style = MaterialTheme.typography.labelSmall)
            }
        }
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(activeFilters, key = { it.first }) { (key, label) ->
                FilterChip(
                    selected = true,
                    onClick = { onRemoveFilter(key) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    trailingIcon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}
