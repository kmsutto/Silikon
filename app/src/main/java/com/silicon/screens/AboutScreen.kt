package com.silicon.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silicon.ui.components.UpdateManager
import kotlinx.coroutines.launch
import com.silicon.R

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var updateState by remember { mutableStateOf<UpdateManager.UpdateState>(UpdateManager.UpdateState.Checking) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    fun checkUpdates() {
        scope.launch {
            updateState = UpdateManager.UpdateState.Checking
            val result = UpdateManager.checkForUpdates()
            updateState = result
            if (result is UpdateManager.UpdateState.Available) showUpdateDialog = true
        }
    }

    LaunchedEffect(Unit) { checkUpdates() }

    if (showUpdateDialog && updateState is UpdateManager.UpdateState.Available) {
        val updateInfo = (updateState as UpdateManager.UpdateState.Available).info
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            icon = { Icon(Icons.Default.SystemUpdate, contentDescription = null) },
            title = { Text("Update Available: ${updateInfo.version}") },
            text = {
                Column(modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())) {
                    Text("New version available!", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Changelog:", fontWeight = FontWeight.Bold)
                    Text(text = updateInfo.changelog, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUpdateDialog = false
                        UpdateManager.downloadAndInstall(context, updateInfo.downloadUrl, "Silicon_${updateInfo.version}.apk")
                    }
                ) { Text("Download") }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) { Text("Later") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(48.dp))

        Surface(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(120.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(painter = painterResource(id = R.drawable.ic_launcher_monochrome), contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        FilledTonalButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kmsutto/Silikon"))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(0.7f),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.Default.Code, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("GitHub Repository", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        val updateData = when (val state = updateState) {
            is UpdateManager.UpdateState.Checking -> AboutUpdateCardModel(Icons.Default.Refresh, "Updates", "Checking...", "Wait...", false, null)
            is UpdateManager.UpdateState.UpToDate -> AboutUpdateCardModel(Icons.Default.CheckCircle, "Updates", "Up to date", "Latest", false, { checkUpdates() })
            is UpdateManager.UpdateState.Available -> AboutUpdateCardModel(Icons.Default.SystemUpdate, "Update Available", state.info.version, "Details", true, { showUpdateDialog = true })
            is UpdateManager.UpdateState.Tester -> AboutUpdateCardModel(Icons.Default.BugReport, "Beta Channel", state.currentVersion, "Tester Build", true, { checkUpdates() })
            is UpdateManager.UpdateState.Error -> AboutUpdateCardModel(Icons.Default.Warning, "Error", "Failed", "Retry", false, { checkUpdates() })
        }

        AboutUpdateCard(updateData)

        Spacer(modifier = Modifier.weight(1f))
        Text(text = "Release", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp))
    }
}

private data class AboutUpdateCardModel(val icon: ImageVector, val title: String, val subtitle: String, val footer: String, val isPrimary: Boolean, val onClick: (() -> Unit)?)

@Composable
private fun AboutUpdateCard(data: AboutUpdateCardModel) {
    val containerColor = if (data.isPrimary) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (data.isPrimary) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface

    Card(colors = CardDefaults.cardColors(containerColor = containerColor), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().then(if (data.onClick != null) Modifier.clickable { data.onClick.invoke() } else Modifier)) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(16.dp), color = if (data.isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(data.icon, null, Modifier.size(24.dp), tint = if (data.isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(data.title, style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(0.8f))
                Text(data.subtitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
                if (data.footer.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(data.footer, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(0.6f))
                }
            }
        }
    }
}