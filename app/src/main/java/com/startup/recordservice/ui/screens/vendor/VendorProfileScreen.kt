package com.startup.recordservice.ui.screens.vendor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AccountCircle
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
    , snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                    text = "Phone: $it",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            uiState.userId?.let {
                Text(
                    text = "User ID: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { showChangePassword = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Password")
            }
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Account")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
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

