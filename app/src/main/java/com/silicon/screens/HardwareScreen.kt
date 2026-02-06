package com.silicon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.silicon.ui.components.DeviceManager

@Composable
fun HardwareScreen() {
    val context = LocalContext.current
    val ram = remember { DeviceManager.getRamDetails(context) }
    val bat = remember { DeviceManager.getBatteryInfo(context) }
    val resolution = remember { DeviceManager.getResolution(context) }
    val gpuData = remember { DeviceManager.getGpuDetails() }
    val refreshRate = remember { DeviceManager.getRefreshRate(context) }
    val density = remember { DeviceManager.getDensity(context) }
    val isHdr = remember { DeviceManager.isHdrSupported(context) }
    val cameraSpecs = remember { DeviceManager.getCameraSpecs(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        HardwareSectionGroup(title = "Processor", icon = Icons.Default.DeveloperBoard) {
            HardwareContent(
                value = DeviceManager.getProcessorName(),
                tags = listOf("${DeviceManager.getCpuCount()} Cores", DeviceManager.is64Bit(), DeviceManager.getArchitecture())
            )
        }

        HardwareSectionGroup(title = "Camera", icon = Icons.Default.CameraAlt) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (cameraSpecs.backCameras.isNotEmpty()) {
                    cameraSpecs.backCameras.forEachIndexed { index, cam ->
                        CameraLensRow(cam)
                        if (index < cameraSpecs.backCameras.lastIndex) {
                            GpuDivider()
                        }
                    }
                } else {
                    Text("No back cameras detected", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (cameraSpecs.backCameras.isNotEmpty() && cameraSpecs.frontCameras.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }

                cameraSpecs.frontCameras.forEach { cam ->
                    CameraLensRow(cam)
                }
            }
        }

        HardwareSectionGroup(title = "GPU", icon = Icons.Default.VideogameAsset) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = gpuData.renderer,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(16.dp))

                GpuDetailRow(label = "Vendor", value = gpuData.vendor)
                GpuDivider()
                GpuDetailRow(label = "OpenGL Version", value = gpuData.version)
                GpuDivider()
                GpuDetailRow(label = "Extensions", value = gpuData.extensionsCount)
            }
        }

        HardwareSectionGroup(title = "Display", icon = Icons.Default.Smartphone) {
            HardwareContent(
                value = resolution,
                tags = listOf(refreshRate, density, if (isHdr) "HDR" else "SDR")
            )
        }

        HardwareSectionGroup(title = "Memory", icon = Icons.Default.Memory) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        ram.total,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Total RAM", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { ram.progress },
                    modifier = Modifier.fillMaxWidth().height(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer,
                    strokeCap = StrokeCap.Round,
                )

                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Used: ${ram.used}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Free: ${ram.free}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        HardwareSectionGroup(title = "Battery", icon = Icons.Default.BatteryStd) {
            BatteryContent(bat)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun HardwareSectionGroup(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
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
            content()
        }
    }
}

@Composable
fun CameraLensRow(cam: DeviceManager.CameraLensInfo) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cam.type,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (cam.type == "Main") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${cam.megapixels}, ${cam.aperture}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (cam.hasOis) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(Modifier.width(4.dp))
                            Text("OIS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = cam.focalLength,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Res: ${cam.resolution} • Sensor: ${cam.sensorSize}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun GpuDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
fun GpuDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    )
}

@Composable
fun HardwareContent(value: String, tags: List<String>) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(tag) },
                    shape = RoundedCornerShape(12.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    border = null
                )
            }
        }
    }
}

@Composable
fun BatteryContent(bat: com.silicon.ui.components.DeviceManager.BatteryData) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(bat.technology, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Technology", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(50)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Icon(Icons.Default.BatteryStd, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(bat.status, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ControlBlock(Modifier.weight(1f), Icons.Default.Bolt, "Capacity", bat.capacity, true)
            ControlBlock(Modifier.weight(1f), Icons.Default.Thermostat, "Temp / Cycles", "${bat.temp} • ${bat.cycles}", false)
        }
    }
}

@Composable
fun ControlBlock(modifier: Modifier, icon: ImageVector, label: String, value: String, isActive: Boolean) {
    val containerColor = if(isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val contentColor = if(isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    Card(modifier.height(100.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(Modifier.padding(16.dp).fillMaxSize(), Arrangement.SpaceBetween) {
            Icon(icon, null, tint = contentColor)
            Column {
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
                Text(label, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.7f))
            }
        }
    }
}