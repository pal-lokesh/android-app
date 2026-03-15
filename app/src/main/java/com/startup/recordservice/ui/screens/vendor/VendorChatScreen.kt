package com.startup.recordservice.ui.screens.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.startup.recordservice.data.model.ChatMessage
import com.startup.recordservice.ui.viewmodel.VendorChatViewModel
import com.startup.recordservice.ui.viewmodel.VendorChatUiState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorChatScreen(
    conversationId: String,
    clientName: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: VendorChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    
    LaunchedEffect(conversationId) {
        viewModel.loadConversation(conversationId)
    }
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    val displayName = clientName ?: conversation?.clientName ?: "Client"

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(displayName) },
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
        ) {
            // Messages List
            when (uiState) {
                is VendorChatUiState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is VendorChatUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is VendorChatUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = (uiState as VendorChatUiState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { viewModel.loadConversation(conversationId) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is VendorChatUiState.Success -> {
                    if (messages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Start a conversation",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Send a message to $displayName",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages, key = { it.messageId ?: it.hashCode().toString() }) { message ->
                                MessageBubble(message = message, isVendor = true)
                            }
                        }
                    }
                }
                is VendorChatUiState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Message Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    maxLines = 4,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(messageText)
                                    messageText = ""
                                }
                            },
                            enabled = messageText.isNotBlank() && uiState !is VendorChatUiState.Loading
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (messageText.isNotBlank()) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isVendor: Boolean
) {
    val isSentByMe = (isVendor && message.senderType == "VENDOR") || 
                     (!isVendor && message.senderType == "CLIENT")
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isSentByMe) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(12.dp),
            horizontalAlignment = if (isSentByMe) Alignment.End else Alignment.Start
        ) {
            if (!isSentByMe && message.senderName != null) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSentByMe) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            message.createdAt?.let { date ->
                Text(
                    text = formatMessageTime(date),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSentByMe) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

fun formatMessageTime(dateString: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val dateTime = LocalDateTime.parse(dateString, formatter)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        dateTime.format(timeFormatter)
    } catch (e: Exception) {
        dateString
    }
}
