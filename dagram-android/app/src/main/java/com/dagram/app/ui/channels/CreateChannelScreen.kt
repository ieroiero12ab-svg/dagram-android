package com.dagram.app.ui.channels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dagram.app.ui.theme.DagramColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChannelScreen(
    onBack: () -> Unit,
    onChannelCreated: (Int, String) -> Unit,
    viewModel: CreateChannelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var channelName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(uiState.createdChannel) {
        uiState.createdChannel?.let { chat ->
            onChannelCreated(chat.id, chat.name ?: "Channel")
        }
    }

    Scaffold(
        containerColor = DagramColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("New Channel", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    if (channelName.isNotBlank()) {
                        TextButton(onClick = {
                            viewModel.createChannel(channelName, description.ifBlank { null })
                        }) {
                            Text("Create", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DagramColors.Surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4E1111)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(uiState.error!!, color = Color(0xFFFF5252), modifier = Modifier.padding(12.dp))
                }
            }

            OutlinedTextField(
                value = channelName,
                onValueChange = { channelName = it },
                label = { Text("Channel Name", color = Color(0xFF90CAF9)) },
                leadingIcon = { Icon(Icons.Filled.Campaign, null, tint = Color(0xFF9C27B0)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF9C27B0), unfocusedBorderColor = Color(0xFF37474F),
                    cursorColor = Color(0xFF9C27B0), focusedContainerColor = Color(0xFF0D1B2A), unfocusedContainerColor = Color(0xFF0D1B2A)
                ), shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)", color = Color(0xFF90CAF9)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF9C27B0), unfocusedBorderColor = Color(0xFF37474F),
                    cursorColor = Color(0xFF9C27B0), focusedContainerColor = Color(0xFF0D1B2A), unfocusedContainerColor = Color(0xFF0D1B2A)
                ), shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(color = Color(0xFF9C27B0), modifier = Modifier.padding(16.dp))
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2D3D)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row {
                        Icon(Icons.Filled.Info, null, tint = Color(0xFF90CAF9), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Channels", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Channels allow you to broadcast messages to a large audience. Only admins can post in channels.",
                        color = Color(0xFF90A4AE),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
