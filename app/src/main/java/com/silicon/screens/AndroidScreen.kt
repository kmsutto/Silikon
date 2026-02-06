package com.silicon.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silicon.ui.components.DeviceManager

@Composable
fun AndroidScreen() {
    val context = LocalContext.current
    val isRooted = remember { DeviceManager.isRooted() }
    val patch = remember { DeviceManager.getSecurityPatch() }
    val bootloader = remember { DeviceManager.getBootloaderStatus() }
    val integrity = remember { DeviceManager.getIntegrityPrediction() }

    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        AndroidSectionGroup(title = "Software", icon = Icons.Default.Android) {
            InfoRow(
                icon = Icons.Default.Smartphone,
                label = "Android Version",
                value = DeviceManager.getAndroidVersion(),
                showDivider = true,
                onClick = {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime < 500) {
                        clickCount++
                    } else {
                        clickCount = 1
                    }
                    lastClickTime = currentTime

                    if (clickCount >= 6) {
                        launchEasterEgg(context)
                        clickCount = 0
                    }
                }
            )
            InfoRow(Icons.Default.Code, "SDK Level", DeviceManager.getSdkVersion(), true)
            InfoRow(Icons.Default.SettingsSystemDaydream, "Codename", DeviceManager.getAndroidCodename(), false)
        }

        AndroidSectionGroup(title = "Firmware", icon = Icons.Default.Build) {
            InfoRow(Icons.Default.Memory, "Kernel", DeviceManager.getKernelVersion(), true)
            InfoRow(Icons.Default.BugReport, "Build Number", DeviceManager.getBuildNumber(), true)
            InfoRow(Icons.Default.Fingerprint, "Fingerprint", DeviceManager.getFingerprint(), false)
        }

        AndroidSectionGroup(title = "Security", icon = Icons.Default.Security) {
            InfoRow(Icons.Default.Update, "Security Patch", patch, true)
            InfoRow(Icons.Default.Lock, "Bootloader", bootloader, true)
            InfoRow(Icons.Default.Shield, "Root Access", if (isRooted) "Detected" else "Not Detected", true)
            InfoRow(Icons.Default.VerifiedUser, "Play Integrity", integrity, false)
        }

        AndroidSectionGroup(title = "Treble", icon = Icons.Default.Layers) {
            InfoRow(Icons.Default.ViewQuilt, "Project Treble", DeviceManager.isTrebleSupported(), true)
            InfoRow(Icons.Default.SystemUpdate, "VNDK Version", DeviceManager.getVndkVersion(), false)
        }

        Spacer(Modifier.height(32.dp))
    }
}

private fun launchEasterEgg(context: Context) {
    try {
        val eggIntent = Intent(Intent.ACTION_MAIN)
        eggIntent.setPackage("com.android.egg")

        val activities = context.packageManager.queryIntentActivities(eggIntent, 0)

        if (activities.isNotEmpty()) {
            eggIntent.setClassName("com.android.egg", activities[0].activityInfo.name)
            eggIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(eggIntent)
        } else {
            val settingsIntent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)
            settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(settingsIntent)
            Toast.makeText(context, "Easter Egg not found directly, opening Settings", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Could not launch Easter Egg: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AndroidSectionGroup(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = CircleShape,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    showDivider: Boolean,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 64.dp, end = 20.dp)
                    .height(1.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            )
        }
    }
}