package com.dagram.app.ui.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dagram.app.data.models.User
import com.dagram.app.ui.theme.DagramColors

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DagramColors.Background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1565C0), Color(0xFF0F1923))
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user?.displayName?.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    user?.displayName ?: "Loading...",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "@${user?.username ?: ""}",
                    color = Color(0xFF90CAF9),
                    fontSize = 15.sp
                )
                if (user?.bio != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(user.bio, color = Color(0xFF90A4AE), fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF162635)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Account Info", color = Color(0xFF90CAF9), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow(Icons.Filled.Email, "Email", user?.email ?: "")
                HorizontalDivider(color = Color(0xFF1E2D3D), modifier = Modifier.padding(vertical = 8.dp))
                InfoRow(Icons.Filled.AlternateEmail, "Username", "@${user?.username ?: ""}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF162635)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                SettingsItem(Icons.Filled.Notifications, "Notifications", Color(0xFFFF9800)) {}
                HorizontalDivider(color = Color(0xFF1E2D3D))
                SettingsItem(Icons.Filled.Security, "Privacy & Security", Color(0xFF4CAF50)) {}
                HorizontalDivider(color = Color(0xFF1E2D3D))
                SettingsItem(Icons.Filled.Palette, "Appearance", Color(0xFF9C27B0)) {}
                HorizontalDivider(color = Color(0xFF1E2D3D))
                SettingsItem(Icons.Filled.Help, "Help & Support", Color(0xFF2196F3)) {}
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onLogout),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1515)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Logout, null, tint = Color(0xFFEF5350), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("Sign Out", color = Color(0xFFEF5350), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Dagram v1.0.0",
            color = Color(0xFF37474F),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color(0xFF2196F3), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = Color(0xFF78909C), fontSize = 12.sp)
            Text(value, color = Color.White, fontSize = 15.sp)
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, label: String, iconColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF546E7A))
    }
}
