package com.silicon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SettingsSystemDaydream
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silicon.ui.components.DeviceManager

@Composable
fun AndroidScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AndroidInfoCard(title = "Software") {
            AndroidRow(Icons.Default.Android, "Android", DeviceManager.getAndroidVersion())
            AndroidRow(Icons.Default.Code, "SDK", DeviceManager.getSdkVersion())
            AndroidRow(Icons.Default.SettingsSystemDaydream, "Codename", DeviceManager.getAndroidCodename())
        }

        AndroidInfoCard(title = "Firmware") {
            AndroidRow(Icons.Default.Memory, "Kernel", DeviceManager.getKernelVersion())
            AndroidRow(Icons.Default.BugReport, "Build number", DeviceManager.getBuildNumber())
            AndroidRow(Icons.Default.Fingerprint, "Fingerprint", DeviceManager.getFingerprint())
        }

        AndroidInfoCard(title = "Treble") {
            AndroidRow(Icons.Default.Layers, "Project Treble", DeviceManager.isTrebleSupported())
            AndroidRow(Icons.Default.SystemUpdate, "VNDK", DeviceManager.getVndkVersion())
        }
    }
}

@Composable
fun AndroidInfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                title,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun AndroidRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, color = MaterialTheme.colorScheme.onSurface)
            Text(
                value,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}