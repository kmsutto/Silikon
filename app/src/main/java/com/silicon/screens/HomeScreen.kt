package com.silicon.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.silicon.ui.components.DeviceManager
import com.silicon.ui.components.ToolsManager
import com.silicon.ui.components.UpdateManager
import com.silicon.ui.screens.BurnScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var updateState by remember { mutableStateOf<UpdateManager.UpdateState>(UpdateManager.UpdateState.Checking) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    var isBurnScreenVisible by remember { mutableStateOf(false) }
    var isVibrating by remember { mutableStateOf(false) }

    fun checkUpdates() {
        scope.launch {
            updateState = UpdateManager.UpdateState.Checking
            val result = UpdateManager.checkForUpdates()
            updateState = result
            if (result is UpdateManager.UpdateState.Available) showUpdateDialog = true
        }
    }

    fun startVibration() {
        if (isVibrating) return
        isVibrating = true
        ToolsManager.runVibrationTest(context, 5000)

        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = "Testing Vibration motor...",
                duration = SnackbarDuration.Short
            )
        }
        scope.launch {
            delay(5000)
            isVibrating = false
        }
    }

    LaunchedEffect(Unit) { checkUpdates() }

    if (isBurnScreenVisible) {
        BurnScreen(onDismiss = { isBurnScreenVisible = false })
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(bottom = 30.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shadowElevation = 6.dp
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.width(12.dp))
                            Text(data.visuals.message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(icon = Icons.Default.Dashboard, title = "Overview")
            DashboardCard(Icons.Default.Smartphone, DeviceManager.getDeviceName(), "Looks Good!", DeviceManager.getDeviceCodename(), true, enabled = !isVibrating)

            val updateData = when (val state = updateState) {
                is UpdateManager.UpdateState.Checking -> UpdateCardModel(Icons.Default.Refresh, "Updates", "Checking...", "Wait...", false, null)
                is UpdateManager.UpdateState.UpToDate -> UpdateCardModel(Icons.Default.CheckCircle, "Updates", "Up to date", "Latest", false, { checkUpdates() })
                is UpdateManager.UpdateState.Available -> UpdateCardModel(Icons.Default.SystemUpdate, "Update Available", state.info.version, "Details", true, { showUpdateDialog = true })
                is UpdateManager.UpdateState.Error -> UpdateCardModel(Icons.Default.Warning, "Error", "Failed", "Retry", false, { checkUpdates() })
            }
            DashboardCard(updateData.icon, updateData.title, updateData.subtitle, updateData.footer, updateData.isPrimary, updateData.onClick, enabled = !isVibrating)

            Spacer(Modifier.height(4.dp))
            SectionHeader(icon = Icons.Default.Build, title = "Tools")

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ToolCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Healing,
                    title = "Fix Burn",
                    subtitle = "RGB Flash",
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    onClick = { isBurnScreenVisible = true },
                    enabled = !isVibrating
                )

                ToolCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Vibration,
                    title = "Vibration",
                    subtitle = if (isVibrating) "Testing..." else "Test Motor",
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    onClick = { startVibration() },
                    enabled = !isVibrating,
                    isActive = isVibrating
                )
            }
        }
    }
}

@Composable
fun SectionHeader(icon: ImageVector, title: String) {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
fun ToolCard(modifier: Modifier, icon: ImageVector, title: String, subtitle: String, containerColor: Color, onClick: () -> Unit, enabled: Boolean, isActive: Boolean = false) {
    Card(colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = MaterialTheme.colorScheme.onSurface), shape = RoundedCornerShape(24.dp), modifier = modifier.height(110.dp).then(if (enabled && !isActive) Modifier.clickable { onClick() } else Modifier)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Icon(icon, null, tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
                Text(subtitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class UpdateCardModel(val icon: ImageVector, val title: String, val subtitle: String, val footer: String, val isPrimary: Boolean, val onClick: (() -> Unit)?)

@Composable
fun DashboardCard(icon: ImageVector, title: String, subtitle: String, footer: String, isPrimary: Boolean, onClick: (() -> Unit)? = null, enabled: Boolean) {
    val containerColor = if (isPrimary) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val contentColor = if (isPrimary) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
    Card(colors = CardDefaults.cardColors(containerColor = containerColor), shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().then(if (onClick != null && enabled) Modifier.clickable { onClick() } else Modifier)) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(16.dp), color = if(isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(24.dp), tint = if(isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary) }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(0.8f))
                Text(subtitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
                if (footer.isNotEmpty()) { Spacer(Modifier.height(4.dp)); Text(footer, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(0.6f)) }
            }
        }
    }
}