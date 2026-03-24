package com.dagram.app.ui.messages

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dagram.app.data.models.Message
import com.dagram.app.ui.theme.DagramColors
import java.io.File
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
    val context = LocalContext.current
    var isRecording by remember { mutableStateOf(false) }
    var showAttachMenu by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendMediaMessage(it, "image") }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendMediaMessage(it, "audio") }
    }

    val recordAudioPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startRecording(context)
            isRecording = true
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        containerColor = Color(0xFF0A1628),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            chatName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (uiState.isTyping) {
                            Text("يكتب...", color = Color(0xFF4CAF50), fontSize = 12.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Search, null, tint = Color.White)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.MoreVert, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF162635))
            )
        },
        bottomBar = {
            Column {
                AnimatedVisibility(visible = showAttachMenu) {
                    AttachmentMenu(
                        onImageClick = {
                            showAttachMenu = false
                            imagePickerLauncher.launch("image/*")
                        },
                        onAudioClick = {
                            showAttachMenu = false
                            audioPickerLauncher.launch("audio/*")
                        },
                        onCameraClick = {
                            showAttachMenu = false
                        }
                    )
                }
                MessageInputBar(
                    text = uiState.messageText,
                    onTextChange = viewModel::onMessageTextChange,
                    onSend = viewModel::sendMessage,
                    isSending = uiState.isSending,
                    isRecording = isRecording,
                    onAttachClick = { showAttachMenu = !showAttachMenu },
                    onRecordStart = {
                        val permission = Manifest.permission.RECORD_AUDIO
                        if (ContextCompat.checkSelfPermission(context, permission) ==
                            android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            viewModel.startRecording(context)
                            isRecording = true
                        } else {
                            recordAudioPermission.launch(permission)
                        }
                    },
                    onRecordStop = {
                        isRecording = false
                        viewModel.stopRecordingAndSend()
                    },
                    onRecordCancel = {
                        isRecording = false
                        viewModel.cancelRecording()
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0A1628))
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
                    Text("لا توجد رسائل بعد", color = Color(0xFF78909C), fontSize = 16.sp)
                    Text("قل مرحباً!", color = Color(0xFF546E7A), fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
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

            uiState.error?.let { err ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("حسناً", color = Color(0xFF2196F3))
                        }
                    },
                    containerColor = Color(0xFF2D1515)
                ) {
                    Text(err, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AttachmentMenu(
    onImageClick: () -> Unit,
    onAudioClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Surface(
        color = Color(0xFF162635),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AttachItem(Icons.Filled.Image, "صورة", Color(0xFF1565C0), onImageClick)
            AttachItem(Icons.Filled.AudioFile, "صوت", Color(0xFF6A1B9A), onAudioClick)
            AttachItem(Icons.Filled.CameraAlt, "كاميرا", Color(0xFF2E7D32), onCameraClick)
        }
    }
}

@Composable
fun AttachItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color(0xFF90CAF9), fontSize = 12.sp)
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
            ) {
                when (message.type) {
                    "image" -> {
                        Column(modifier = Modifier.padding(4.dp)) {
                            val imageUrl = message.mediaUrl ?: message.content
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "صورة",
                                modifier = Modifier
                                    .widthIn(max = 240.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentScale = ContentScale.FillWidth
                            )
                            Text(
                                formatMessageTime(message.createdAt),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 2.dp, end = 6.dp, bottom = 4.dp)
                            )
                        }
                    }
                    "audio" -> {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                LinearProgressIndicator(
                                    progress = { 0f },
                                    modifier = Modifier.width(120.dp),
                                    color = Color.White,
                                    trackColor = Color.White.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    formatMessageTime(message.createdAt),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    else -> {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
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

        if (isOwn) {
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    isRecording: Boolean,
    onAttachClick: () -> Unit,
    onRecordStart: () -> Unit,
    onRecordStop: () -> Unit,
    onRecordCancel: () -> Unit
) {
    Surface(
        color = Color(0xFF162635),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF2D1515))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.FiberManualRecord, null, tint = Color(0xFFEF5350), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("جارٍ التسجيل...", color = Color.White, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onRecordCancel,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF37474F), CircleShape)
                ) {
                    Icon(Icons.Filled.Delete, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onRecordStop,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                ) {
                    Icon(Icons.Filled.Stop, null, tint = Color.White)
                }
            } else {
                IconButton(
                    onClick = onAttachClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Filled.AttachFile, null, tint = Color(0xFF78909C), modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    placeholder = { Text("اكتب رسالة...", color = Color(0xFF546E7A)) },
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

                if (text.isNotBlank()) {
                    IconButton(
                        onClick = onSend,
                        enabled = !isSending,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF2196F3), CircleShape)
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
                                contentDescription = "إرسال",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                } else {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF2196F3), CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        onRecordStart()
                                        tryAwaitRelease()
                                        onRecordStop()
                                    }
                                )
                            }
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "تسجيل",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
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
