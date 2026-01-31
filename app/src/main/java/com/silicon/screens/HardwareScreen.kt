package com.silicon.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.silicon.ui.components.DeviceManager

@Composable
fun HardwareScreen() {
    val context = LocalContext.current

    val ram = DeviceManager.getRamDetails(context)
    val bat = DeviceManager.getBatteryInfo(context)
    val resolution = DeviceManager.getResolution(context)
    val refreshRate = DeviceManager.getRefreshRate(context)
    val density = DeviceManager.getDensity(context)
    val isHdr = if (DeviceManager.isHdrSupported(context)) "HDR" else ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HardwareSection(
            title = "Processor",
            value = DeviceManager.getProcessorName(),
            tags = listOf(
                "${DeviceManager.getCpuCount()} Cores",
                DeviceManager.is64Bit(),
                DeviceManager.getArchitecture()
            )
        )

        HardwareSection(
            title = "GPU",
            value = DeviceManager.getGpuModel(context),
            tags = listOf("Render")
        )

        HardwareSection(
            title = "Display",
            value = resolution,
            tags = listOfNotNull(refreshRate, density, isHdr.ifEmpty { null })
        )

        HardwareCard(title = "Memory") {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Default.Memory, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Total RAM: ${ram.total}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Used: ${ram.used}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Text("Free: ${ram.free}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = ram.progress,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        HardwareCard(title = "Battery") {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Default.BatteryStd, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(bat.technology, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            Spacer(Modifier.height(8.dp))
            SuggestionChip(
                onClick = {},
                label = { Text(bat.status) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                border = null
            )
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoColumn("Cycles", bat.cycles)
                InfoColumn("Capacity", bat.capacity)
                InfoColumn("Temp", bat.temp)
            }
        }
    }
}

@Composable
fun HardwareSection(title: String, value: String, tags: List<String>) {
    HardwareCard(title = title) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Default.Memory, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(tag) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                )
            }
        }
    }
}

@Composable
fun HardwareCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun InfoColumn(label: String, value: String) {
    Column {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}