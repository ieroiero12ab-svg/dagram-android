package com.dagram.app.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        containerColor = Color(0xFF0A1628),
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A1628))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = "الإشعارات") {
                SettingsRow(Icons.Filled.Notifications, "الإشعارات", Color(0xFFFF9800)) {}
                SettingsRow(Icons.Filled.NotificationsActive, "الأصوات", Color(0xFFFF5722)) {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSection(title = "الخصوصية") {
                SettingsRow(Icons.Filled.Lock, "الخصوصية", Color(0xFF4CAF50)) {}
                SettingsRow(Icons.Filled.Security, "الأمان", Color(0xFF2196F3)) {}
                SettingsRow(Icons.Filled.Block, "المحظورون", Color(0xFFEF5350)) {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSection(title = "المظهر") {
                SettingsRow(Icons.Filled.Palette, "السمة", Color(0xFF9C27B0)) {}
                SettingsRow(Icons.Filled.TextFields, "حجم الخط", Color(0xFF00BCD4)) {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSection(title = "التخزين") {
                SettingsRow(Icons.Filled.Storage, "استخدام البيانات", Color(0xFF607D8B)) {}
                SettingsRow(Icons.Filled.CloudDownload, "التنزيلات التلقائية", Color(0xFF795548)) {}
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingsSection(title = "الدعم") {
                SettingsRow(Icons.Filled.Help, "المساعدة والدعم", Color(0xFF03A9F4)) {}
                SettingsRow(Icons.Filled.Info, "عن التطبيق", Color(0xFF8BC34A)) {}
                SettingsRow(Icons.Filled.BugReport, "الإبلاغ عن مشكلة", Color(0xFFFF9800)) {}
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Dagram v1.0.0", color = Color(0xFF37474F), fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title,
        color = Color(0xFF64B5F6),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E33)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Color(0xFF1E3A5F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = Color(0xFF546E7A))
    }
    HorizontalDivider(color = Color(0xFF162635), thickness = 0.5.dp, modifier = Modifier.padding(start = 68.dp))
}
