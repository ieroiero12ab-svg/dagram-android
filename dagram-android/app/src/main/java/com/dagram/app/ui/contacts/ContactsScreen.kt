package com.dagram.app.ui.contacts

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dagram.app.data.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onUserClick: (Int, String) -> Unit,
    viewModel: ContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color(0xFF0A1628),
        topBar = {
            TopAppBar(
                title = { Text("جهات الاتصال", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A1628))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::search,
                placeholder = { Text("بحث...", color = Color(0xFF546E7A)) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color(0xFF546E7A)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color(0xFF1E3A5F),
                    cursorColor = Color(0xFF2196F3),
                    focusedContainerColor = Color(0xFF162635),
                    unfocusedContainerColor = Color(0xFF162635)
                ),
                shape = RoundedCornerShape(24.dp)
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2196F3))
                }
            } else if (uiState.users.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Contacts, null, tint = Color(0xFF37474F), modifier = Modifier.size(72.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (uiState.searchQuery.isNotEmpty()) "لم يتم العثور على مستخدمين" else "لا توجد جهات اتصال",
                            color = Color(0xFF90A4AE), fontSize = 16.sp
                        )
                        if (uiState.searchQuery.isEmpty()) {
                            Text("ابحث عن المستخدمين بالأعلى", color = Color(0xFF546E7A), fontSize = 13.sp)
                        }
                    }
                }
            } else {
                LazyColumn {
                    items(uiState.users, key = { it.id }) { user ->
                        ContactItem(user = user, onClick = { onUserClick(user.id, user.displayName) })
                        HorizontalDivider(
                            color = Color(0xFF162635),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 80.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(52.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1565C0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    user.displayName.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (user.isOnline) {
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
            Text(
                user.displayName,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "@${user.username}",
                color = Color(0xFF90CAF9),
                fontSize = 13.sp
            )
        }

        if (user.isOnline) {
            Text("متصل", color = Color(0xFF4CAF50), fontSize = 11.sp)
        } else if (!user.lastSeen.isNullOrBlank()) {
            Text("غير متصل", color = Color(0xFF546E7A), fontSize = 11.sp)
        }
    }
}
