package com.dagram.app.ui.messages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dagram.app.data.models.Message
import com.dagram.app.ui.theme.DagramColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    chatName: String,
    currentUserId: Int,
    onBack: () -> Unit,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = DagramColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(chatName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.MoreVert, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DagramColors.Surface)
            )
        },
        bottomBar = {
            MessageInputBar(
                text = uiState.messageText,
                onTextChange = viewModel::onMessageTextChange,
                onSend = viewModel::sendMessage,
                isSending = uiState.isSending
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DagramColors.Background)
        ) {
            if (uiState.isLoading && uiState.messages.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF2196F3)
                )
            } else if (uiState.messages.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Chat, null, tint = Color(0xFF37474F), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No messages yet", color = Color(0xFF78909C), fontSize = 16.sp)
                    Text("Say hello!", color = Color(0xFF546E7A), fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isOwn = message.senderId == currentUserId
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isOwn: Boolean) {
    val isSystem = message.type == "system"

    if (isSystem) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2D3D)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    message.content,
                    color = Color(0xFF90CAF9),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isOwn) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1565C0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    message.senderName.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
        ) {
            if (!isOwn) {
                Text(
                    message.senderName,
                    color = Color(0xFF64B5F6),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = if (isOwn) Color(0xFF1565C0) else Color(0xFF1E2D3D),
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isOwn) 18.dp else 4.dp,
                            bottomEnd = if (isOwn) 4.dp else 18.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Column {
                    Text(
                        message.content,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                    Text(
                        formatMessageTime(message.createdAt),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Surface(
        color = DagramColors.Surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = { Text("Message...", color = Color(0xFF546E7A)) },
                modifier = Modifier.weight(1f),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = Color(0xFF37474F),
                    cursorColor = Color(0xFF2196F3),
                    focusedContainerColor = Color(0xFF0D1B2A),
                    unfocusedContainerColor = Color(0xFF0D1B2A)
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (text.isNotBlank()) Color(0xFF2196F3) else Color(0xFF37474F),
                        CircleShape
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private fun formatMessageTime(isoString: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoString) ?: return ""
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    } catch (_: Exception) { "" }
}
