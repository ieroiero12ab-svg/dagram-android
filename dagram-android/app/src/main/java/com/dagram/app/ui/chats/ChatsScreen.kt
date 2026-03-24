package com.dagram.app.ui.chats

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.dagram.app.data.models.Chat
import com.dagram.app.data.models.User
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onChatClick: (Int, String) -> Unit,
    onCreateGroup: () -> Unit,
    onCreateChannel: () -> Unit,
    viewModel: ChatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFabMenu by remember { mutableStateOf(false) }
    var isCreatingChat by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF0A1628),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dagram",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.MoreVert, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A1628)
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.Start) {
                AnimatedVisibility(
                    visible = showFabMenu,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        FabMenuItem("مجموعة جديدة", Icons.Filled.Group) {
                            showFabMenu = false
                            onCreateGroup()
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        FabMenuItem("قناة جديدة", Icons.Filled.Campaign) {
                            showFabMenu = false
                            onCreateChannel()
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = Color(0xFF2196F3),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        if (showFabMenu) Icons.Filled.Close else Icons.Filled.Add,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Start
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBarInline(
                query = uiState.searchQuery,
                onQueryChange = viewModel::searchUsers,
                onClear = { viewModel.clearSearch() }
            )

            if (uiState.isCreatingChat) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF2196F3),
                    trackColor = Color(0xFF1E2D3D)
                )
            }

            uiState.error?.let { err ->
                if (err.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF4E1111)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            err,
                            color = Color(0xFFFF5252),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            if (uiState.searchQuery.length >= 2) {
                SearchResultsList(
                    results = uiState.searchResults,
                    isLoading = uiState.isSearching,
                    onUserClick = { user ->
                        viewModel.createChatWithUser(user.id) { chatId ->
                            onChatClick(chatId, user.displayName)
                        }
                    }
                )
            } else {
                ChatList(
                    chats = uiState.chats,
                    isLoading = uiState.isLoading,
                    onChatClick = onChatClick
                )
            }
        }
    }
}

@Composable
fun SearchBarInline(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("البحث في المحادثات", color = Color(0xFF546E7A), fontSize = 15.sp) },
            leadingIcon = {
                Icon(Icons.Filled.Search, null, tint = Color(0xFF546E7A), modifier = Modifier.size(22.dp))
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Filled.Close, null, tint = Color(0xFF546E7A), modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF162635),
                unfocusedContainerColor = Color(0xFF162635),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ChatList(
    chats: List<Chat>,
    isLoading: Boolean,
    onChatClick: (Int, String) -> Unit
) {
    if (isLoading && chats.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF2196F3))
        }
        return
    }

    if (chats.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Forum, null, tint = Color(0xFF37474F), modifier = Modifier.size(72.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("لا توجد محادثات", color = Color(0xFF90A4AE), fontSize = 18.sp)
                Text("ابدأ محادثة جديدة!", color = Color(0xFF546E7A), fontSize = 14.sp)
            }
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(chats, key = { it.id }) { chat ->
            ChatItem(chat = chat, onClick = { onChatClick(chat.id, chat.getDisplayName()) })
            HorizontalDivider(
                color = Color(0xFF162635),
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 84.dp)
            )
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(54.dp)) {
            val avatarUrl = chat.getDisplayAvatar()
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            color = when (chat.type) {
                                "group" -> Color(0xFF2E7D32)
                                "channel" -> Color(0xFF6A1B9A)
                                else -> Color(0xFF1565C0)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (chat.type == "direct") {
                        Text(
                            text = chat.getDisplayName().firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            when (chat.type) {
                                "group" -> Icons.Filled.Group
                                "channel" -> Icons.Filled.Campaign
                                else -> Icons.Filled.Person
                            },
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            if (chat.type == "direct" && (chat.otherUser?.isOnline == true)) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .border(2.dp, Color(0xFF0A1628), CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.getDisplayName(),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                chat.lastMessage?.let {
                    Text(
                        text = formatTime(it.createdAt),
                        color = if (chat.unreadCount > 0) Color(0xFF2196F3) else Color(0xFF546E7A),
                        fontSize = 12.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val lastMsgText = chat.lastMessage?.let {
                    when (it.type) {
                        "image" -> "📷 صورة"
                        "audio" -> "🎤 رسالة صوتية"
                        "system" -> it.content
                        else -> if (chat.type != "direct") "${it.senderName}: ${it.content}" else it.content
                    }
                } ?: when (chat.type) {
                    "group" -> "${chat.memberCount} أعضاء"
                    "channel" -> "قناة"
                    else -> "ابدأ المحادثة"
                }

                Text(
                    text = lastMsgText,
                    color = Color(0xFF78909C),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .defaultMinSize(minWidth = 22.dp)
                            .background(Color(0xFF2196F3), CircleShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FabMenuItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.padding(start = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = Color(0xFF1565C0),
            shape = CircleShape
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2D3D)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun SearchResultsList(results: List<User>, isLoading: Boolean, onUserClick: (User) -> Unit) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF2196F3))
        }
        return
    }

    if (results.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.SearchOff, null, tint = Color(0xFF37474F), modifier = Modifier.size(56.dp))
                Text("لم يتم العثور على مستخدمين", color = Color(0xFF78909C), modifier = Modifier.padding(top = 12.dp))
            }
        }
        return
    }

    LazyColumn {
        items(results, key = { it.id }) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserClick(user) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1565C0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user.displayName.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(user.displayName, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("@${user.username}", color = Color(0xFF90CAF9), fontSize = 13.sp)
                }
                if (user.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                }
            }
            HorizontalDivider(
                color = Color(0xFF162635),
                thickness = 0.5.dp,
                modifier = Modifier.padding(start = 80.dp)
            )
        }
    }
}

private fun formatTime(isoString: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(isoString) ?: return ""
        val now = Date()
        val diff = now.time - date.time
        when {
            diff < 60_000 -> "الآن"
            diff < 3_600_000 -> "${diff / 60_000}د"
            diff < 86_400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
            else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(date)
        }
    } catch (_: Exception) { "" }
}
