package com.dagram.app.ui.groups

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
fun CreateGroupScreen(
    onBack: () -> Unit,
    onGroupCreated: (Int, String) -> Unit,
    viewModel: CreateGroupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var groupName by remember { mutableStateOf("") }
    var groupUsername by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }

    LaunchedEffect(groupUsername) {
        if (groupUsername.length >= 5) viewModel.checkUsername(groupUsername)
    }

    LaunchedEffect(uiState.createdGroup) {
        uiState.createdGroup?.let { chat ->
            onGroupCreated(chat.id, chat.name ?: "مجموعة")
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color(0xFFCFD8DC),
        focusedBorderColor = Color(0xFF1E88E5),
        unfocusedBorderColor = Color(0xFF37474F),
        cursorColor = Color(0xFF2196F3),
        focusedContainerColor = Color(0xFF0D1B2A),
        unfocusedContainerColor = Color(0xFF0D1B2A),
        focusedLabelColor = Color(0xFF90CAF9),
        unfocusedLabelColor = Color(0xFF546E7A)
    )

    Scaffold(
        containerColor = Color(0xFF0A1628),
        topBar = {
            TopAppBar(
                title = { Text("مجموعة جديدة", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    if (uiState.selectedUsers.isNotEmpty() && groupName.isNotBlank()) {
                        TextButton(
                            onClick = {
                                viewModel.createGroup(
                                    name = groupName,
                                    description = description.ifBlank { null },
                                    username = groupUsername.ifBlank { null },
                                    isPrivate = isPrivate
                                )
                            },
                            enabled = !uiState.isLoading
                        ) {
                            Text("إنشاء", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF162635))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                uiState.error?.let { err ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4E1111)),
                        modifier = Modifier.fillMaxWidth().padding(16.dp, 16.dp, 16.dp, 0.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(err, color = Color(0xFFFF5252), modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("اسم المجموعة") },
                    leadingIcon = { Icon(Icons.Filled.Group, null, tint = Color(0xFF2196F3)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = fieldColors, shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                val usernameError = when {
                    groupUsername.isNotEmpty() && groupUsername.length < 5 -> "يجب أن يكون 5 أحرف على الأقل"
                    uiState.usernameAvailable == false -> "اسم المستخدم هذا محجوز"
                    else -> null
                }

                OutlinedTextField(
                    value = groupUsername,
                    onValueChange = { groupUsername = it.filter { c -> c.isLetterOrDigit() || c == '_' }.lowercase() },
                    label = { Text("معرّف المجموعة (اختياري)") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.AlternateEmail, null,
                            tint = when {
                                usernameError != null -> Color(0xFFEF5350)
                                uiState.usernameAvailable == true && groupUsername.isNotEmpty() -> Color(0xFF4CAF50)
                                else -> Color(0xFF546E7A)
                            }
                        )
                    },
                    trailingIcon = {
                        when {
                            uiState.isCheckingUsername -> CircularProgressIndicator(
                                modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF42A5F5)
                            )
                            uiState.usernameAvailable == true && groupUsername.length >= 5 -> Icon(
                                Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)
                            )
                            uiState.usernameAvailable == false -> Icon(
                                Icons.Filled.Cancel, null, tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    supportingText = {
                        if (usernameError != null) Text(usernameError, color = Color(0xFFEF5350), fontSize = 11.sp)
                        else if (groupUsername.isNotEmpty()) Text("@$groupUsername", color = Color(0xFF42A5F5), fontSize = 11.sp)
                    },
                    isError = usernameError != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = fieldColors, shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("الوصف (اختياري)") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = fieldColors, shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF162635)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
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
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (isPrivate) "مجموعة خاصة" else "مجموعة عامة",
                                    color = Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp
                                )
                                Text(
                                    if (isPrivate) "بدعوة فقط" else "قابلة للبحث",
                                    color = Color(0xFF78909C), fontSize = 12.sp
                                )
                            }
                        }
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF1565C0),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF37474F)
                            )
                        )
                    }
                }

                if (uiState.selectedUsers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "المحددون (${uiState.selectedUsers.size})",
                        color = Color(0xFF90CAF9),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        uiState.selectedUsers.forEach { user ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Box {
                                    Box(
                                        modifier = Modifier.size(48.dp).clip(CircleShape)
                                            .background(Color(0xFF1565C0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            user.displayName.firstOrNull()?.uppercase() ?: "?",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier.size(20.dp).align(Alignment.TopEnd)
                                            .clip(CircleShape).background(Color(0xFFEF5350))
                                            .clickable { viewModel.toggleUser(user) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                                Text(user.displayName.take(8), color = Color(0xFF90CAF9), fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFF1E2D3D))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.searchUsers(it)
                    },
                    label = { Text("إضافة أعضاء") },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color(0xFF2196F3)) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = fieldColors, shape = RoundedCornerShape(12.dp)
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp)
                ) {
                    items(uiState.searchResults) { user ->
                        val isSelected = uiState.selectedUsers.contains(user)
                        ListItem(
                            headlineContent = { Text(user.displayName, color = Color.White) },
                            supportingContent = {
                                Text("@${user.username}", color = Color(0xFF90CAF9), fontSize = 13.sp)
                            },
                            leadingContent = {
                                Box(
                                    modifier = Modifier.size(44.dp).clip(CircleShape)
                                        .background(Color(0xFF1565C0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        user.displayName.firstOrNull()?.uppercase() ?: "?",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            trailingContent = {
                                if (isSelected) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF2196F3))
                                }
                            },
                            modifier = Modifier.clickable { viewModel.toggleUser(user) },
                            colors = ListItemDefaults.colors(
                                containerColor = if (isSelected) Color(0xFF0D1B2A) else Color.Transparent
                            )
                        )
                        HorizontalDivider(color = Color(0xFF1E2D3D), thickness = 0.5.dp)
                    }
                }
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF2196F3),
                    trackColor = Color(0xFF1E2D3D)
                )
            }
        }
    }
}
