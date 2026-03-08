package com.silicon.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.silicon.ui.components.AppAnimations
import com.silicon.ui.components.DeviceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareScreen(isWideScreen: Boolean = false) {
    val context = LocalContext.current

    val processorName = remember { DeviceManager.getProcessorName() }
    val cpuCores = remember { "${DeviceManager.getCpuCount()} Cores" }
    val is64Bit = remember { DeviceManager.is64Bit() }
    val architecture = remember { DeviceManager.getArchitecture() }

    val resolution = remember { DeviceManager.getResolution(context) }
    val refreshRate = remember { DeviceManager.getRefreshRate(context) }
    val density = remember { DeviceManager.getDensity(context) }
    val isHdr = remember { DeviceManager.isHdrSupported(context) }
    val cameraSpecs = remember { DeviceManager.getCameraSpecs(context) }

    var ram by remember { mutableStateOf(DeviceManager.RamData("...", "...", "...", 0f, "...", "", "")) }
    var gpuData by remember { mutableStateOf(DeviceManager.GpuData("Loading...", "...", "...", "...", "...")) }

    val bat by DeviceManager.getBatteryFlow(context).collectAsState(
        initial = DeviceManager.BatteryData("...", "...", "...", "...", "...", "...", "...", 0, 0)
    )

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val data = DeviceManager.getGpuDetails(context)
            withContext(Dispatchers.Main) {
                gpuData = data
            }
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            val ramInfo = DeviceManager.getRamDetails(context)
            withContext(Dispatchers.Main) { ram = ramInfo }
            delay(2000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(if (isWideScreen) 2 else 1),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 20.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                HardwareSectionGroup(title = "Processor", icon = Icons.Default.DeveloperBoard) {
                    HardwareContent(
                        value = processorName,
                        tags = listOf(
                            Pair(cpuCores, Icons.Default.Memory),
                            Pair(is64Bit, Icons.Default.Settings),
                            Pair(architecture, Icons.Default.DeveloperBoard)
                        )
                    )
                }
            }

            if (cameraSpecs.backCameras.isNotEmpty()) {
                item {
                    HardwareSectionGroup(title = "Camera", icon = Icons.Default.Camera) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            cameraSpecs.backCameras.forEachIndexed { index, cam ->
                                CameraLensRow(cam)
                                if (index < cameraSpecs.backCameras.lastIndex) GpuDivider()
                            }
                            if (cameraSpecs.backCameras.isNotEmpty() && cameraSpecs.frontCameras.isNotEmpty()) {
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
                            }
                            cameraSpecs.frontCameras.forEachIndexed { index, cam ->
                                CameraLensRow(cam)
                                if (index < cameraSpecs.frontCameras.lastIndex) GpuDivider()
                            }
                        }
                    }
                }
            }

            item {
                HardwareSectionGroup(title = "GPU", icon = Icons.Default.VideogameAsset) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = gpuData.renderer,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoChip(label = "Vulkan ${gpuData.vulkanVersion}", icon = Icons.Default.Layers)
                            InfoChip(label = gpuData.vendor, icon = Icons.Default.Business)
                        }

                        Spacer(Modifier.height(16.dp))

                        GpuDetailRow(label = "OpenGL Version", value = gpuData.version)
                        GpuDivider()
                        GpuDetailRow(label = "Extensions", value = gpuData.extensionsCount)
                    }
                }
            }

            item {
                HardwareSectionGroup(title = "Display", icon = Icons.Default.Smartphone) {
                    HardwareContent(
                        value = resolution,
                        tags = listOf(
                            Pair(refreshRate, Icons.Default.Refresh),
                            Pair(density, Icons.Default.AspectRatio),
                            Pair(if (isHdr) "HDR" else "SDR", Icons.Default.BrightnessHigh)
                        )
                    )
                }
            }

            item {
                HardwareSectionGroup(title = "Memory", icon = Icons.Default.Memory) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(ram.physicalSize, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(8.dp))
                            Text("Total RAM", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp))
                        }

                        Spacer(Modifier.height(16.dp))

                        SolidProgressBar(
                            progress = ram.progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )

                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Used: ${ram.used}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Free: ${ram.free}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (ram.type.isNotBlank() || ram.vendor.isNotBlank()) {
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (ram.vendor.isNotBlank()) {
                                    InfoChip(label = ram.vendor, icon = Icons.Default.Business)
                                }
                                if (ram.type.isNotBlank()) {
                                    InfoChip(label = ram.type, icon = Icons.Default.Speed)
                                }
                            }
                        }
                    }
                }
            }

            item {
                HardwareSectionGroup(title = "Battery", icon = Icons.Default.BatteryFull) {
                    BatteryContent(bat)
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    label: String,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = contentColor)
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
fun HardwareSectionGroup(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), shape = CircleShape, modifier = Modifier.padding(start = 4.dp)) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(Modifier.width(6.dp))
                Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = AppAnimations.contentSizeSpec)
        ) {
            content()
        }
    }
}

@Composable
fun CameraLensRow(cam: DeviceManager.CameraLensInfo) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = cam.type, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (cam.type.startsWith("Main")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                Text(text = "${cam.megapixels}, ${cam.aperture}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (cam.hasOis) {
                    InfoChip(
                        label = "OIS",
                        icon = Icons.Default.Vibration
                    )
                }
                InfoChip(
                    label = cam.focalLength,
                    icon = null
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(text = "Res: ${cam.resolution}  Sensor: ${cam.sensorSize}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

@Composable
fun GpuDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.weight(0.4f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.End, modifier = Modifier.weight(0.6f))
    }
}

@Composable
fun GpuDivider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
}

@Composable
fun HardwareContent(value: String, tags: List<Pair<String, ImageVector>>) {
    Column(modifier = Modifier.padding(20.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                InfoChip(
                    label = tag.first,
                    icon = tag.second
                )
            }
        }
    }
}

@Composable
fun BatteryContent(bat: com.silicon.ui.components.DeviceManager.BatteryData) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(bat.technology, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Technology", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(50)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("${bat.status} • ${bat.voltage}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ControlBlock(Modifier.weight(1f), Icons.Default.Bolt, "Design Capacity", bat.capacity, true)
            ControlBlock(Modifier.weight(1f), Icons.Default.Thermostat, "Temp / Cycles", "${bat.temp} • ${bat.cycles}", false)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = if (bat.percentInt >= 90) Icons.Default.BatteryChargingFull else Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    if (bat.percentInt >= 90) {
                        Text("~${bat.estimatedCapacity} mAh", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Estimated Capacity (Approximate)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("Charge to 90%+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("to estimate capacity. Figures are approximate.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ControlBlock(modifier: Modifier, icon: ImageVector, label: String, value: String, isActive: Boolean) {
    val containerColor = if (isActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
    val contentColor = if (isActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface

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

@Composable
fun SolidProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color,
    trackColor: Color
) {
    val safeProgress = if (progress.isNaN() || progress < 0f) 0f else if (progress > 1f) 1f else progress
    Box(
        modifier = modifier.background(trackColor, RoundedCornerShape(50))
    ) {
        if (safeProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = safeProgress)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(50))
            )
        }
    }
}