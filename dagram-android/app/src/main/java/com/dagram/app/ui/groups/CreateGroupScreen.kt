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
import com.dagram.app.data.models.User
import com.dagram.app.ui.theme.DagramColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    onBack: () -> Unit,
    onGroupCreated: (Int, String) -> Unit,
    viewModel: CreateGroupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(uiState.createdGroup) {
        uiState.createdGroup?.let { chat ->
            onGroupCreated(chat.id, chat.name ?: "Group")
        }
    }

    Scaffold(
        containerColor = DagramColors.Background,
        topBar = {
            TopAppBar(
                title = { Text("New Group", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    if (uiState.selectedUsers.isNotEmpty() && groupName.isNotBlank()) {
                        TextButton(onClick = {
                            viewModel.createGroup(groupName, description.ifBlank { null })
                        }) {
                            Text("Create", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DagramColors.Surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name", color = Color(0xFF90CAF9)) },
                leadingIcon = { Icon(Icons.Filled.Group, null, tint = Color(0xFF2196F3)) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF2196F3), unfocusedBorderColor = Color(0xFF37474F),
                    cursorColor = Color(0xFF2196F3), focusedContainerColor = Color(0xFF0D1B2A), unfocusedContainerColor = Color(0xFF0D1B2A)
                ), shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)", color = Color(0xFF90CAF9)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF2196F3), unfocusedBorderColor = Color(0xFF37474F),
                    cursorColor = Color(0xFF2196F3), focusedContainerColor = Color(0xFF0D1B2A), unfocusedContainerColor = Color(0xFF0D1B2A)
                ), shape = RoundedCornerShape(12.dp)
            )

            if (uiState.selectedUsers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Selected (${uiState.selectedUsers.size})",
                    color = Color(0xFF90CAF9),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp)) {
                    uiState.selectedUsers.forEach { user ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
                            Box {
                                Box(
                                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF1565C0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(user.displayName.first().uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { viewModel.toggleUser(user) },
                                    modifier = Modifier.size(18.dp).align(Alignment.TopEnd).background(Color(0xFFEF5350), CircleShape)
                                ) {
                                    Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            }
                            Text(user.displayName.take(8), color = Color.White, fontSize = 10.sp)
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
                label = { Text("Add members", color = Color(0xFF90CAF9)) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = Color(0xFF2196F3)) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF2196F3), unfocusedBorderColor = Color(0xFF37474F),
                    cursorColor = Color(0xFF2196F3), focusedContainerColor = Color(0xFF0D1B2A), unfocusedContainerColor = Color(0xFF0D1B2A)
                ), shape = RoundedCornerShape(12.dp)
            )

            LazyColumn {
                items(uiState.searchResults) { user ->
                    val isSelected = uiState.selectedUsers.contains(user)
                    ListItem(
                        headlineContent = { Text(user.displayName, color = Color.White) },
                        supportingContent = { Text("@${user.username}", color = Color(0xFF90CAF9), fontSize = 13.sp) },
                        leadingContent = {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF1565C0)), contentAlignment = Alignment.Center) {
                                Text(user.displayName.first().uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
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
    }
}
