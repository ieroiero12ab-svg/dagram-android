package com.dagram.app.ui.channels

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChannelScreen(
    onBack: () -> Unit,
    onChannelCreated: (Int, String) -> Unit,
    viewModel: CreateChannelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var channelName by remember { mutableStateOf("") }
    var channelUsername by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }

    LaunchedEffect(channelUsername) {
        if (channelUsername.length >= 5) viewModel.checkUsername(channelUsername)
    }

    LaunchedEffect(uiState.createdChannel) {
        uiState.createdChannel?.let { chat ->
            onChannelCreated(chat.id, chat.name ?: "قناة")
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color(0xFFCFD8DC),
        focusedBorderColor = Color(0xFF9C27B0),
        unfocusedBorderColor = Color(0xFF37474F),
        cursorColor = Color(0xFF9C27B0),
        focusedContainerColor = Color(0xFF0D1B2A),
        unfocusedContainerColor = Color(0xFF0D1B2A),
        focusedLabelColor = Color(0xFFCE93D8),
        unfocusedLabelColor = Color(0xFF546E7A)
    )

    Scaffold(
        containerColor = Color(0xFF0A1628),
        topBar = {
            TopAppBar(
                title = { Text("قناة جديدة", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    if (channelName.isNotBlank()) {
                        TextButton(
                            onClick = {
                                viewModel.createChannel(
                                    name = channelName,
                                    description = description.ifBlank { null },
                                    username = channelUsername.ifBlank { null },
                                    isPrivate = isPrivate
                                )
                            },
                            enabled = !uiState.isLoading
                        ) {
                            Text("إنشاء", color = Color(0xFFCE93D8), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF162635))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp)
        ) {
            uiState.error?.let { err ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4E1111)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Error, null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(err, color = Color(0xFFFF5252), fontSize = 13.sp)
                    }
                }
            }

            OutlinedTextField(
                value = channelName,
                onValueChange = { channelName = it },
                label = { Text("اسم القناة") },
                leadingIcon = { Icon(Icons.Filled.Campaign, null, tint = Color(0xFF9C27B0)) },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val usernameError = when {
                channelUsername.isNotEmpty() && channelUsername.length < 5 -> "يجب أن يكون 5 أحرف على الأقل"
                channelUsername.isNotEmpty() && !channelUsername.all { it.isLetterOrDigit() || it == '_' } -> "أحرف وأرقام و _ فقط"
                uiState.usernameAvailable == false -> "اسم المستخدم هذا محجوز"
                else -> null
            }

            OutlinedTextField(
                value = channelUsername,
                onValueChange = { channelUsername = it.filter { c -> c.isLetterOrDigit() || c == '_' }.lowercase() },
                label = { Text("معرّف القناة (اختياري)") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.AlternateEmail, null,
                        tint = when {
                            usernameError != null -> Color(0xFFEF5350)
                            uiState.usernameAvailable == true && channelUsername.isNotEmpty() -> Color(0xFF4CAF50)
                            else -> Color(0xFF546E7A)
                        }
                    )
                },
                trailingIcon = {
                    when {
                        uiState.isCheckingUsername -> CircularProgressIndicator(
                            modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFFCE93D8)
                        )
                        uiState.usernameAvailable == true && channelUsername.length >= 5 -> Icon(
                            Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)
                        )
                        uiState.usernameAvailable == false -> Icon(
                            Icons.Filled.Cancel, null, tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp)
                        )
                    }
                },
                supportingText = {
                    if (usernameError != null) Text(usernameError, color = Color(0xFFEF5350), fontSize = 11.sp)
                    else if (uiState.usernameAvailable == true && channelUsername.length >= 5)
                        Text("@$channelUsername متاح", color = Color(0xFF4CAF50), fontSize = 11.sp)
                    else if (channelUsername.isNotEmpty())
                        Text("@$channelUsername", color = Color(0xFFCE93D8), fontSize = 11.sp)
                },
                isError = usernameError != null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("الوصف (اختياري)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162635)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isPrivate) Color(0xFF6A1B9A).copy(0.2f) else Color(0xFF1565C0).copy(0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isPrivate) Icons.Filled.Lock else Icons.Filled.Public,
                                    null,
                                    tint = if (isPrivate) Color(0xFFCE93D8) else Color(0xFF64B5F6),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    if (isPrivate) "قناة خاصة" else "قناة عامة",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    if (isPrivate) "يمكن الانضمام برابط الدعوة فقط"
                                    else "يمكن للجميع البحث والانضمام",
                                    color = Color(0xFF78909C),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF9C27B0),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF37474F)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1E2D)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Info, null, tint = Color(0xFF90CAF9), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("عن القنوات", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "القنوات تتيح بث الرسائل لجمهور واسع. فقط المشرفون يمكنهم النشر في القنوات.",
                            color = Color(0xFF90A4AE),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Spacer(modifier = Modifier.height(20.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF9C27B0),
                    trackColor = Color(0xFF1E2D3D)
                )
            }
        }
    }
}
