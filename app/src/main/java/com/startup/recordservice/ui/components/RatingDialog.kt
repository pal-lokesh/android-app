package com.startup.recordservice.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Reusable Rating Dialog Component
 * Can be used to submit ratings for businesses after orders
 */
@Composable
fun RatingDialog(
    businessName: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String?) -> Unit,
    isLoading: Boolean = false
) {
    var selectedRating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Column {
                Text("Rate Your Experience")
                Text(
                    text = businessName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Star Rating Selector
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "How would you rate this business?",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        repeat(5) { index ->
                            Icon(
                                if (index < selectedRating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "${index + 1} stars",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { selectedRating = index + 1 },
                                tint = if (index < selectedRating) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                    if (selectedRating > 0) {
                        Text(
                            text = when (selectedRating) {
                                1 -> "Poor"
                                2 -> "Fair"
                                3 -> "Good"
                                4 -> "Very Good"
                                5 -> "Excellent"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Divider()
                
                // Comment Section
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Add a comment (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    minLines = 2,
                    placeholder = { Text("Share your experience...") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(selectedRating, comment.takeIf { it.isNotBlank() })
                },
                enabled = selectedRating > 0 && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit Rating")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}
