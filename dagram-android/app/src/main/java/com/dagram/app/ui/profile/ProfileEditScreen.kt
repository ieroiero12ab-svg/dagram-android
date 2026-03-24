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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dagram.app.data.models.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    user: User?,
    isFirstSetup: Boolean = false,
    isLoading: Boolean = false,
    error: String? = null,
    usernameAvailable: Boolean? = null,
    isCheckingUsername: Boolean = false,
    onCheckUsername: (String) -> Unit,
    onSave: (username: String, displayName: String, bio: String) -> Unit,
    onSkip: () -> Unit,
    onClearError: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    var username by remember { mutableStateOf(user?.username?.let { if (it.startsWith("user") && it.contains("_")) "" else it } ?: "") }
    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }
    var localError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(username) {
        if (username.length >= 5) {
            onCheckUsername(username)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF050D18), Color(0xFF0A1628), Color(0xFF0D1F3C))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1565C0), Color(0xFF1E88E5))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (displayName.firstOrNull() ?: user?.displayName?.firstOrNull() ?: 'D').uppercase(),
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (isFirstSetup) "Setup Your Profile" else "Edit Profile",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                if (isFirstSetup) "You can change this later" else "Update your information",
                fontSize = 14.sp,
                color = Color(0xFF64B5F6),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E33).copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, Color(0xFF1E3A5F))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val usernameError = when {
                        username.isNotEmpty() && username.length < 5 -> "Username must be at least 5 characters"
                        username.isNotEmpty() && !username.all { it.isLetterOrDigit() || it == '_' } -> "Only letters, numbers and _"
                        usernameAvailable == false -> "This username is already taken"
                        else -> null
                    }
                    val usernameValid = username.isEmpty() || (username.length >= 5 && usernameAvailable == true)

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it.filter { c -> c.isLetterOrDigit() || c == '_' }.lowercase()
                            localError = null
                            onClearError()
                        },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.AlternateEmail, null,
                                tint = when {
                                    usernameError != null -> Color(0xFFEF5350)
                                    usernameAvailable == true && username.isNotEmpty() -> Color(0xFF4CAF50)
                                    username.isNotEmpty() -> Color(0xFF1E88E5)
                                    else -> Color(0xFF546E7A)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            when {
                                isCheckingUsername -> CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF42A5F5)
                                )
                                usernameAvailable == true && username.length >= 5 -> Icon(
                                    Icons.Filled.CheckCircle, null,
                                    tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)
                                )
                                usernameAvailable == false -> Icon(
                                    Icons.Filled.Cancel, null,
                                    tint = Color(0xFFEF5350), modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        supportingText = {
                            when {
                                usernameError != null -> Text(usernameError, color = Color(0xFFEF5350), fontSize = 11.sp)
                                usernameAvailable == true && username.length >= 5 -> Text("@$username is available", color = Color(0xFF4CAF50), fontSize = 11.sp)
                                username.isNotEmpty() -> Text("@$username", color = Color(0xFF42A5F5), fontSize = 11.sp)
                            }
                        },
                        isError = usernameError != null,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it; localError = null; onClearError() },
                        label = { Text("Display Name") },
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
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio (optional)") },
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
                        shape = RoundedCornerShape(14.dp)
                    )

                    val errorMsg = localError ?: error
                    AnimatedVisibility(visible = errorMsg != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF3E0F0F))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.ErrorOutline, null, tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMsg ?: "", color = Color(0xFFFF7043), fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            localError = null
                            when {
                                username.isNotEmpty() && username.length < 5 -> localError = "Username must be at least 5 characters"
                                username.isNotEmpty() && usernameAvailable == false -> localError = "Username already taken, choose another"
                                displayName.isBlank() -> localError = "Display name is required"
                                else -> onSave(
                                    username.trim().ifEmpty { null } ?: user?.username ?: "",
                                    displayName.trim(),
                                    bio.trim()
                                )
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF1565C0), Color(0xFF1E88E5), Color(0xFF42A5F5))
                                    ),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Save, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Save Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    if (isFirstSetup) {
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = onSkip,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Skip for now", color = Color(0xFF78909C), fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
