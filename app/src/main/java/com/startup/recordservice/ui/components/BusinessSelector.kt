package com.startup.recordservice.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startup.recordservice.data.model.BusinessResponse

/**
 * Reusable Business Selector component for multi-business vendors
 * Can be used in any screen where business selection is needed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSelector(
    businesses: List<BusinessResponse>,
    selectedBusinessId: String?,
    onBusinessSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Business",
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedBusiness = businesses.firstOrNull { it.businessId == selectedBusinessId }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedBusiness?.businessName ?: label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            leadingIcon = {
                Icon(Icons.Default.Business, contentDescription = null)
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = if (!enabled) {
                OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                OutlinedTextFieldDefaults.colors()
            }
        )
        
        if (businesses.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { expanded = false }
            ) {
                businesses.forEach { business ->
                    val businessId = business.businessId ?: return@forEach
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = business.businessName ?: "Business",
                                    fontWeight = if (businessId == selectedBusinessId) FontWeight.Bold else FontWeight.Normal
                                )
                                business.category?.let {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = {
                            onBusinessSelected(businessId)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = null,
                                tint = if (businessId == selectedBusinessId) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Compact Business Selector as a Chip/Button group
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessSelectorChips(
    businesses: List<BusinessResponse>,
    selectedBusinessId: String?,
    onBusinessSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (businesses.isEmpty()) {
        Text(
            text = "No businesses available",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(16.dp)
        )
        return
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Select Business",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            businesses.forEach { business ->
                val businessId = business.businessId ?: return@forEach
                FilterChip(
                    selected = businessId == selectedBusinessId,
                    onClick = { onBusinessSelected(businessId) },
                    label = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = business.businessName ?: "Business",
                                style = MaterialTheme.typography.labelMedium
                            )
                            business.category?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
