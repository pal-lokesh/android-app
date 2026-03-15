package com.startup.recordservice.ui.screens.vendor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.startup.recordservice.ui.viewmodel.VendorProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: VendorProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showChangePassword by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var changingPassword by remember { mutableStateOf(false) }
    var deletingAccount by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Vendor Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    , snackbarHost = { SnackbarHost(snackbarHostState) }    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Icon",
                        modifier = Modifier.size(96.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    uiState.phone?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Statistics Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Businesses",
                    value = uiState.businessesCount.toString(),
                    icon = Icons.Default.Business,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Orders",
                    value = uiState.totalOrdersCount.toString(),
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Account Information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Account Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Divider()
                    InfoRow(label = "Phone Number", value = uiState.phone ?: "N/A", icon = Icons.Default.Phone)
                    InfoRow(label = "User ID", value = uiState.userId ?: "N/A", icon = Icons.Default.Person)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showChangePassword = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Password")
                }
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Account")
                }
                OutlinedButton(
                    onClick = {
                        viewModel.loadStatistics()
                        scope.launch {
                            snackbarHostState.showSnackbar("Statistics refreshed")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh Statistics")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showChangePassword) {
        AlertDialog(
            onDismissRequest = { if (!changingPassword) showChangePassword = false },
            title = { Text("Change Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") }
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") }
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") }
                    )
                    if (passwordError != null) {
                        Text(passwordError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !changingPassword,
                    onClick = {
                        when {
                            currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() -> {
                                passwordError = "All fields are required"
                            }
                            newPassword.length < 6 -> {
                                passwordError = "New password must be at least 6 characters"
                            }
                            newPassword != confirmPassword -> {
                                passwordError = "New passwords do not match"
                            }
                            currentPassword == newPassword -> {
                                passwordError = "New password must be different from current password"
                            }
                            else -> {
                                changingPassword = true
                                passwordError = null
                                viewModel.changePassword(currentPassword, newPassword) { result ->
                                    changingPassword = false
                                    result
                                        .onSuccess {
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            showChangePassword = false
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Password changed successfully")
                                            }
                                        }
                                        .onFailure { e ->
                                            passwordError = e.message ?: "Failed to change password"
                                        }
                                }
                            }
                        }
                    }
                ) {
                    Text(if (changingPassword) "Changing…" else "Change")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !changingPassword,
                    onClick = { showChangePassword = false }
                ) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!deletingAccount) showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("This will permanently delete your account and all data. Are you sure?") },
            confirmButton = {
                TextButton(
                    enabled = !deletingAccount,
                    onClick = {
                        deletingAccount = true
                        viewModel.deleteAccount { result ->
                            deletingAccount = false
                            result
                                .onSuccess { onLogout() }
                                .onFailure { e ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(e.message ?: "Failed to delete account")
                                    }
                                }
                        }
                    }
                ) {
                    Text(if (deletingAccount) "Deleting…" else "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !deletingAccount,
                    onClick = { showDeleteDialog = false }
                ) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
