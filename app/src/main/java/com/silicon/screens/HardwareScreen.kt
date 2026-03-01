package com.silicon.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.silicon.ui.components.DeviceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareScreen(isWideScreen: Boolean = false) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var ram by remember { mutableStateOf(DeviceManager.RamData("...", "...", "...", 0f)) }
    var storage by remember { mutableStateOf(DeviceManager.StorageData("...", "...", 0, 0f, "...", "...")) }
    var gpuData by remember { mutableStateOf(DeviceManager.GpuData("Loading...", "...", "...", "...")) }

    val bat by DeviceManager.getBatteryFlow(context).collectAsState(
        initial = DeviceManager.BatteryData("...", "...", "...", "...", "...", "...", "...", 0, 0)
    )

    var showPartitionsSheet by remember { mutableStateOf(false) }
    var partitionsList by remember { mutableStateOf<List<DeviceManager.PartitionData>>(emptyList()) }
    var isPartitionsLoading by remember { mutableStateOf(false) }

    var showThrottlingSheet by remember { mutableStateOf(false) }
    var isThrottlingTestRunning by remember { mutableStateOf(false) }
    var throttlingResult by remember { mutableStateOf(DeviceManager.ThrottlingResult(emptyList(), 0f, 0f, 0f)) }

    val resolution = remember { DeviceManager.getResolution(context) }
    val refreshRate = remember { DeviceManager.getRefreshRate(context) }
    val density = remember { DeviceManager.getDensity(context) }
    val isHdr = remember { DeviceManager.isHdrSupported(context) }
    val cameraSpecs = remember { DeviceManager.getCameraSpecs(context) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val data = DeviceManager.getGpuDetails()
            val storageInfo = DeviceManager.getStorageInfo()
            withContext(Dispatchers.Main) {
                gpuData = data
                storage = storageInfo
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

    LaunchedEffect(isThrottlingTestRunning) {
        if (isThrottlingTestRunning) {
            throttlingResult = DeviceManager.ThrottlingResult(emptyList(), 0f, 0f, 0f)

            DeviceManager.runThrottlingTest()
                .flowOn(Dispatchers.IO)
                .collect { result ->
                    throttlingResult = result
                }
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
                        value = DeviceManager.getProcessorName(),
                        tags = listOf(
                            Pair("${DeviceManager.getCpuCount()} Cores", Icons.Default.Memory),
                            Pair(DeviceManager.is64Bit(), Icons.Default.Settings),
                            Pair(DeviceManager.getArchitecture(), Icons.Default.DeveloperBoard)
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showThrottlingSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Throttling Test")
                    }
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
                        Text(text = gpuData.renderer, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(16.dp))
                        GpuDetailRow(label = "Vendor", value = gpuData.vendor)
                        GpuDivider()
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(ram.total, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(8.dp))
                            Text("Total RAM", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    }
                }
            }

            item {
                HardwareSectionGroup(title = "Storage", icon = Icons.Default.SdStorage) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(storage.total, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.width(8.dp))
                            Text("Total Space", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(16.dp))

                        SolidProgressBar(
                            progress = storage.progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )

                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Used: ${storage.used}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${storage.percent}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoChip(
                                label = storage.type,
                                icon = Icons.Default.Memory
                            )
                            if (storage.model != "Unknown Model" && storage.model.isNotEmpty()) {
                                InfoChip(
                                    label = storage.model,
                                    icon = Icons.Default.Info
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                if (!isPartitionsLoading) {
                                    isPartitionsLoading = true
                                    scope.launch(Dispatchers.IO) {
                                        val list = DeviceManager.getDiskPartitions()
                                        withContext(Dispatchers.Main) {
                                            partitionsList = list
                                            showPartitionsSheet = true
                                            isPartitionsLoading = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isPartitionsLoading
                        ) {
                            if (isPartitionsLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isPartitionsLoading) "Loading..." else "Disk Partitions")
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

        if (showPartitionsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showPartitionsSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)) {
                    Text(text = "Disk Partitions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 16.dp))
                    if (partitionsList.isEmpty()) {
                        Text("No partitions accessible.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(32.dp))
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                            items(partitionsList) { partition -> PartitionItemRow(partition) }
                        }
                    }
                }
            }
        }

        if (showThrottlingSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showThrottlingSheet = false
                    isThrottlingTestRunning = false
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)) {

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "CPU Throttling Test", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                        Surface(
                            color = if (isThrottlingTestRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Thermostat, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (isThrottlingTestRunning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer)
                                Spacer(Modifier.width(4.dp))
                                Text(text = bat.temp, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (isThrottlingTestRunning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(text = "Stresses all CPU cores and plots performance to detect thermal throttling.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(24.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Canvas(modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)) {

                            val width = size.width
                            val height = size.height

                            for(i in 0..4) {
                                val y = height * (i / 4f)
                                drawLine(color = Color.Gray.copy(alpha = 0.2f), start = Offset(0f, y), end = Offset(width, y), strokeWidth = 2f)
                            }

                            if (throttlingResult.history.isNotEmpty()) {
                                val maxVal = max(throttlingResult.maxPerf, 1f)
                                val stepX = width / 60f
                                val path = Path()

                                throttlingResult.history.forEachIndexed { index, value ->
                                    val x = index * stepX
                                    val y = height - ((value / maxVal) * height)
                                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }

                                val gradient = Brush.verticalGradient(
                                    colors = listOf(Color.Green, Color.Yellow, Color.Red),
                                    startY = 0f,
                                    endY = height
                                )

                                drawPath(
                                    path = path,
                                    brush = gradient,
                                    style = Stroke(width = 6.dp.toPx())
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ThrottlingStatBlock("Max", String.format("%.2f", throttlingResult.maxPerf), MaterialTheme.colorScheme.primary)
                        ThrottlingStatBlock("Avg", String.format("%.2f", throttlingResult.avgPerf), MaterialTheme.colorScheme.secondary)
                        ThrottlingStatBlock("Min", String.format("%.2f", throttlingResult.minPerf), MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { isThrottlingTestRunning = !isThrottlingTestRunning },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isThrottlingTestRunning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isThrottlingTestRunning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(if (isThrottlingTestRunning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isThrottlingTestRunning) "Stop Test" else "Start Test", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ThrottlingStatBlock(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
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
                .animateContentSize()
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
fun PartitionItemRow(partition: DeviceManager.PartitionData) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = partition.mountPoint, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) {
                    Text(text = partition.fsType, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
            Spacer(Modifier.height(12.dp))

            SolidProgressBar(
                progress = partition.progress,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Used: ${partition.used}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Total: ${partition.total} (${partition.percent}%)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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