package com.dagram.app.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val animOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim"
    )

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onRegisterSuccess()
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
    val fieldShape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050D18),
                        Color(0xFF0A1628),
                        Color(0xFF0D1F3C)
                    )
                )
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (40 * animOffset).dp)
                .blur(80.dp)
                .background(Color(0xFF0288D1).copy(alpha = 0.2f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = (-60 * animOffset).dp)
                .blur(80.dp)
                .background(Color(0xFF1565C0).copy(alpha = 0.2f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Back button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF0F1E33))
                        .size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        null,
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Header
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1E88E5).copy(alpha = 0.25f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1565C0), Color(0xFF1E88E5))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PersonAdd,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Create Account",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.3.sp
            )
            Text(
                "Join Dagram today",
                fontSize = 14.sp,
                color = Color(0xFF64B5F6),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F1E33).copy(alpha = 0.9f)
                ),
                border = BorderStroke(1.dp, Color(0xFF1E3A5F))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; localError = null; viewModel.clearError() },
                        label = { Text("Email address") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Email, null,
                                tint = if (email.isNotEmpty()) Color(0xFF1E88E5) else Color(0xFF546E7A),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors, shape = fieldShape
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it.filter { c -> c.isLetterOrDigit() || c == '_' }.lowercase()
                            localError = null; viewModel.clearError()
                        },
                        label = { Text("Username") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.AlternateEmail, null,
                                tint = if (username.isNotEmpty()) Color(0xFF1E88E5) else Color(0xFF546E7A),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        supportingText = {
                            if (username.isNotEmpty()) {
                                Text(
                                    "@$username",
                                    color = Color(0xFF42A5F5),
                                    fontSize = 11.sp
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors, shape = fieldShape
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Display Name
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name (optional)") },
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
                        colors = fieldColors, shape = fieldShape
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; localError = null; viewModel.clearError() },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Lock, null,
                                tint = if (password.isNotEmpty()) Color(0xFF1E88E5) else Color(0xFF546E7A),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    null,
                                    tint = Color(0xFF546E7A),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        supportingText = {
                            if (password.isNotEmpty()) {
                                val strength = when {
                                    password.length >= 10 && password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> "Strong"
                                    password.length >= 8 -> "Medium"
                                    else -> "Weak"
                                }
                                val color = when (strength) {
                                    "Strong" -> Color(0xFF4CAF50)
                                    "Medium" -> Color(0xFFFFC107)
                                    else -> Color(0xFFEF5350)
                                }
                                Text(strength, color = color, fontSize = 11.sp)
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors, shape = fieldShape
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; localError = null; viewModel.clearError() },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            val matchColor = when {
                                confirmPassword.isEmpty() -> Color(0xFF546E7A)
                                confirmPassword == password -> Color(0xFF4CAF50)
                                else -> Color(0xFFEF5350)
                            }
                            Icon(
                                Icons.Filled.LockPerson, null,
                                tint = matchColor,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (confirmPassword.isNotEmpty()) {
                                Icon(
                                    if (confirmPassword == password) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                    null,
                                    tint = if (confirmPassword == password) Color(0xFF4CAF50) else Color(0xFFEF5350),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFFCFD8DC),
                            focusedBorderColor = if (confirmPassword.isNotEmpty() && confirmPassword != password)
                                Color(0xFFEF5350) else Color(0xFF1E88E5),
                            unfocusedBorderColor = if (confirmPassword.isNotEmpty() && confirmPassword != password)
                                Color(0xFFEF5350).copy(0.5f) else Color(0xFF1E3A5F),
                            cursorColor = Color(0xFF42A5F5),
                            focusedContainerColor = Color(0xFF071525),
                            unfocusedContainerColor = Color(0xFF071525),
                            focusedLabelColor = Color(0xFF42A5F5),
                            unfocusedLabelColor = Color(0xFF546E7A)
                        ),
                        shape = fieldShape
                    )

                    // Error message
                    val errorMsg = localError ?: uiState.error
                    AnimatedVisibility(
                        visible = errorMsg != null,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF3E0F0F))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.ErrorOutline,
                                null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                errorMsg ?: "",
                                color = Color(0xFFFF7043),
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Register Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            localError = null
                            viewModel.clearError()
                            when {
                                email.isBlank() -> localError = "Email is required"
                                !email.contains("@") -> localError = "Enter a valid email"
                                username.isBlank() -> localError = "Username is required"
                                username.length < 3 -> localError = "Username must be at least 3 characters"
                                password.length < 6 -> localError = "Password must be at least 6 characters"
                                password != confirmPassword -> localError = "Passwords do not match"
                                else -> viewModel.register(
                                    email.trim(),
                                    password,
                                    username.trim(),
                                    displayName.trim().ifEmpty { username.trim() }
                                )
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color(0xFF0D2744)
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (!uiState.isLoading)
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFF1565C0), Color(0xFF1E88E5), Color(0xFF42A5F5))
                                        )
                                    else Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF0D2744), Color(0xFF0D2744))
                                    ),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Filled.PersonAdd,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Create Account",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Already have an account?",
                    color = Color(0xFF78909C),
                    fontSize = 14.sp
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        "Sign In",
                        color = Color(0xFF42A5F5),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
