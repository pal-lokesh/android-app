package com.startup.recordservice.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.startup.recordservice.ui.viewmodel.ClientProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileScreen(
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onNavigateToOrders: () -> Unit = {},
    viewModel: ClientProfileViewModel = hiltViewModel()
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
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    , snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = uiState.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Client",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Phone: ${uiState.phone.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (!uiState.userId.isNullOrBlank()) {
                        Text(
                            text = "User ID: ${uiState.userId}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onNavigateToOrders,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("My Orders")
            }

            Button(
                onClick = { showChangePassword = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Change Password")
            }

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Account")
            }

            Button(
                onClick = {
                    viewModel.logout()
                    onLogoutSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
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
                        // Local validation mirroring web rules
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
                                    result.onSuccess {
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                        showChangePassword = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Password changed successfully")
                                        }
                                    }.onFailure { e ->
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
                                .onSuccess { onLogoutSuccess() }
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

