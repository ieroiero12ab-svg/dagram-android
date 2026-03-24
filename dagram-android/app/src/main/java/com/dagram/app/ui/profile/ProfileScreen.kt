package com.dagram.app.ui.profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dagram.app.data.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User?,
    isLoading: Boolean,
    error: String?,
    usernameAvailable: Boolean?,
    isCheckingUsername: Boolean,
    profileUpdateSuccess: Boolean,
    onCheckUsername: (String) -> Unit,
    onUpdateProfile: (username: String, displayName: String, bio: String) -> Unit,
    onClearError: () -> Unit,
    onClearSuccess: () -> Unit,
    onLogout: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    var isEditing by remember { mutableStateOf(false) }
    var username by remember(user?.username) { mutableStateOf(user?.username ?: "") }
    var displayName by remember(user?.displayName) { mutableStateOf(user?.displayName ?: "") }
    var bio by remember(user?.bio) { mutableStateOf(user?.bio ?: "") }
    var localError by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(username) {
        if (isEditing && username.length >= 5) {
            onCheckUsername(username)
        }
    }

    LaunchedEffect(profileUpdateSuccess) {
        if (profileUpdateSuccess) {
            isEditing = false
            onClearSuccess()
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color(0xFFCFD8DC),
        focusedBorderColor = Color(0xFF1E88E5),
        unfocusedBorderColor = Color(0xFF1E3A5F),
        cursorColor = Color(0xFF42A5F5),
        focusedContainerColor = Color(0xFF071525),
        unfocusedContainerColor = Color(0xFF071525),
        focusedLabelColor = Color(0xFF42A5F5),
        unfocusedLabelColor = Color(0xFF546E7A)
    )

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Filled.Logout, null, tint = Color(0xFFEF5350)) },
            title = { Text("تسجيل الخروج", color = Color.White) },
            text = { Text("هل أنت متأكد أنك تريد تسجيل الخروج؟", color = Color(0xFFCFD8DC)) },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) { Text("تسجيل الخروج") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("إلغاء", color = Color(0xFF64B5F6))
                }
            },
            containerColor = Color(0xFF0F1E33),
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        containerColor = Color(0xFF0A1628),
        topBar = {
            TopAppBar(
                title = { Text("الملف الشخصي", color = Color.White, fontWeight = FontWeight.Bold) },
                actions = {
                    if (isEditing) {
                        TextButton(onClick = {
                            isEditing = false
                            username = user?.username ?: ""
                            displayName = user?.displayName ?: ""
                            bio = user?.bio ?: ""
                            localError = null
                            onClearError()
                        }) {
                            Text("إلغاء", color = Color(0xFFEF5350))
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Filled.Edit, null, tint = Color(0xFF64B5F6))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A1628))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1565C0), Color(0xFF1E88E5))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (user?.displayName?.firstOrNull() ?: 'D').uppercase(),
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1565C0))
                            .border(2.dp, Color(0xFF0A1628), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isEditing) {
                Text(
                    user?.displayName ?: "",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("@${user?.username ?: ""}", color = Color(0xFF64B5F6), fontSize = 14.sp)
                if (!user?.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        user?.bio ?: "",
                        color = Color(0xFF90A4AE),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E33)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "تعديل الملف الشخصي",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        val usernameError = when {
                            username.isNotEmpty() && username.length < 5 -> "اسم المستخدم يجب أن يكون 5 أحرف على الأقل"
                            username.isNotEmpty() && !username.all { it.isLetterOrDigit() || it == '_' } -> "أحرف وأرقام و _ فقط"
                            usernameAvailable == false -> "اسم المستخدم هذا محجوز"
                            else -> null
                        }

                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it.filter { c -> c.isLetterOrDigit() || c == '_' }.lowercase()
                                localError = null
                                onClearError()
                            },
                            label = { Text("اسم المستخدم") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.AlternateEmail, null,
                                    tint = when {
                                        usernameError != null -> Color(0xFFEF5350)
                                        usernameAvailable == true && username.isNotEmpty() -> Color(0xFF4CAF50)
                                        else -> Color(0xFF546E7A)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                when {
                                    isCheckingUsername -> CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF42A5F5)
                                    )
                                    usernameAvailable == true && username.length >= 5 -> Icon(
                                        Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)
                                    )
                                    usernameAvailable == false -> Icon(
                                        Icons.Filled.Cancel, null, tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            supportingText = {
                                if (usernameError != null) Text(usernameError, color = Color(0xFFEF5350), fontSize = 11.sp)
                            },
                            isError = usernameError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it; localError = null; onClearError() },
                            label = { Text("الاسم المعروض") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Person, null,
                                    tint = if (displayName.isNotEmpty()) Color(0xFF1E88E5) else Color(0xFF546E7A),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("نبذة (اختياري)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Info, null,
                                    tint = if (bio.isNotEmpty()) Color(0xFF1E88E5) else Color(0xFF546E7A),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            minLines = 2,
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors,
                            shape = RoundedCornerShape(12.dp)
                        )

                        val errorMsg = localError ?: error
                        AnimatedVisibility(visible = errorMsg != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF3E0F0F))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Error, null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(errorMsg ?: "", color = Color(0xFFFF7043), fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                localError = null
                                when {
                                    username.isNotEmpty() && username.length < 5 ->
                                        localError = "اسم المستخدم يجب أن يكون 5 أحرف على الأقل"
                                    usernameAvailable == false ->
                                        localError = "اسم المستخدم محجوز"
                                    displayName.isBlank() ->
                                        localError = "الاسم المعروض مطلوب"
                                    else -> onUpdateProfile(username.trim(), displayName.trim(), bio.trim())
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF1565C0), Color(0xFF1E88E5))
                                        ),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Save, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "حفظ التغييرات",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E33)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Column {
                        ProfileInfoRow(Icons.Filled.Email, "البريد الإلكتروني", user?.email ?: "")
                        HorizontalDivider(color = Color(0xFF162635), thickness = 0.5.dp)
                        ProfileInfoRow(
                            Icons.Filled.AlternateEmail, "اسم المستخدم",
                            "@${user?.username ?: ""}"
                        )
                        if (!user?.bio.isNullOrBlank()) {
                            HorizontalDivider(color = Color(0xFF162635), thickness = 0.5.dp)
                            ProfileInfoRow(Icons.Filled.Info, "النبذة", user?.bio ?: "")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E33)),
                    border = BorderStroke(1.dp, Color(0xFF1E3A5F))
                ) {
                    Column {
                        SettingsItem(Icons.Filled.Notifications, "الإشعارات", Color(0xFFFF9800)) {}
                        HorizontalDivider(color = Color(0xFF162635), thickness = 0.5.dp)
                        SettingsItem(Icons.Filled.Security, "الخصوصية والأمان", Color(0xFF4CAF50)) {}
                        HorizontalDivider(color = Color(0xFF162635), thickness = 0.5.dp)
                        SettingsItem(Icons.Filled.Palette, "المظهر", Color(0xFF9C27B0)) {}
                        HorizontalDivider(color = Color(0xFF162635), thickness = 0.5.dp)
                        SettingsItem(Icons.Filled.Help, "المساعدة والدعم", Color(0xFF2196F3)) {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350))
            ) {
                Icon(Icons.Filled.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل الخروج", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Dagram v1.0.0", color = Color(0xFF37474F), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color(0xFF1E88E5), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = Color(0xFF78909C), fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 15.sp)
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    ProfileInfoRow(icon, label, value)
}

@Composable
fun SettingsItem(icon: ImageVector, label: String, iconColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF546E7A))
    }
}
