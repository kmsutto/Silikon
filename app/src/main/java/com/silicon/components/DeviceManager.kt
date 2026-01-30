package com.silicon.ui.components

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.WindowMetrics
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.text.DecimalFormat
import kotlin.math.roundToInt

object DeviceManager {

    data class RamData(val total: String, val used: String, val free: String, val progress: Float)

    data class BatteryData(
        val level: String, val status: String, val temp: String, val technology: String, val capacity: String, val cycles: String
    )

    private fun findValueInFiles(paths: List<String>): String {
        for (path in paths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val value = BufferedReader(FileReader(file)).use { it.readLine()?.trim() }
                    if (!value.isNullOrEmpty()) return value
                }
            } catch (_: Exception) { }
        }
        return ""
    }

    private fun getPowerProfileCapacity(context: Context): Double {
        return try {
            val mPowerProfile = Class.forName("com.android.internal.os.PowerProfile")
                .getConstructor(Context::class.java).newInstance(context)
            Class.forName("com.android.internal.os.PowerProfile")
                .getMethod("getBatteryCapacity").invoke(mPowerProfile) as Double
        } catch (_: Exception) { 0.0 }
    }

    fun getDeviceCodename(): String = Build.DEVICE

    fun getAndroidCodename(): String {
        return when (Build.VERSION.SDK_INT) {
            36 -> "Baklava"
            35 -> "Vanilla Ice Cream"
            34 -> "Upside Down Cake"
            33 -> "Tiramisu"
            32 -> "Snow Cone v2"
            31 -> "Snow Cone"
            30 -> "Red Velvet Cake"
            29 -> "Quince Tart"
            28 -> "Pie"
            27 -> "Oreo v2"
            26 -> "Oreo"
            else -> "Legacy"
        }
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.replaceFirstChar { it.uppercase() }
        } else {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        }
    }

    fun getProcessorName(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL
        } else {
            Build.HARDWARE.uppercase()
        }
    }

    fun getCpuCount(): Int = Runtime.getRuntime().availableProcessors()
    fun getArchitecture(): String = System.getProperty("os.arch") ?: "Unknown"
    fun is64Bit(): String = if (Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()) "64-bit" else "32-bit"

    fun getGpuModel(): String {
        val soc = getProcessorName().lowercase()
        return when {
            "tensor" in soc && !soc.contains("g2") && !soc.contains("g3") -> "Mali-G78"
            "tensor g2" in soc -> "Mali-G710"
            "tensor g3" in soc -> "Mali-G715"
            "snapdragon" in soc -> "Adreno"
            "mt6" in soc || "dimensity" in soc -> "Mali (Dimensity)"
            "exynos" in soc -> "Mali/Xclipse"
            else -> "Unknown Renderer"
        }
    }

    fun getResolution(context: Context): String {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics = windowManager.currentWindowMetrics
            "${metrics.bounds.width()}x${metrics.bounds.height()}"
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)
            "${metrics.widthPixels}x${metrics.heightPixels}"
        }
    }

    fun getRefreshRate(context: Context): String {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay
        }
        return "${display?.refreshRate?.toInt() ?: 60}Hz"
    }

    fun getDensity(context: Context): String = "${context.resources.displayMetrics.densityDpi} dpi"

    fun isHdrSupported(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.isHdr == true
        } else {
            false
        }
    }

    fun getRamDetails(context: Context): RamData {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val rawTotalGB = memInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
        val niceTotalGB = when {
            rawTotalGB <= 3 -> 3.0
            rawTotalGB <= 4 -> 4.0
            rawTotalGB <= 6 -> 6.0
            rawTotalGB <= 8 -> 8.0
            rawTotalGB <= 12 -> 12.0
            rawTotalGB <= 16 -> 16.0
            else -> rawTotalGB.roundToInt().toDouble()
        }
        val niceTotalBytes = niceTotalGB * 1024 * 1024 * 1024
        val availBytes = memInfo.availMem.toDouble()
        val usedBytes = niceTotalBytes - availBytes

        val df = DecimalFormat("#.##")
        return RamData(
            total = "${niceTotalGB.toInt()} GB",
            used = df.format(usedBytes / (1024 * 1024 * 1024)) + " GB",
            free = df.format(availBytes / (1024 * 1024 * 1024)) + " GB",
            progress = (usedBytes / niceTotalBytes).toFloat()
        )
    }

    fun getBatteryInfo(context: Context): BatteryData {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = (level * 100 / scale.toFloat()).toInt()

        val status = when (intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            else -> "Idle"
        }

        val tempRaw = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempString = "${tempRaw / 10.0}°C"
        val technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        var cycles = -1
        if (Build.VERSION.SDK_INT >= 34) {
            cycles = intent?.getIntExtra("android.os.extra.CYCLE_COUNT", -1) ?: -1
        }
        if (cycles == -1) {
            val cyclesStr = findValueInFiles(listOf("/sys/class/power_supply/battery/cycle_count", "/sys/class/power_supply/battery/battery_cycle"))
            cycles = cyclesStr.toIntOrNull() ?: 0
        }

        var designCap = findValueInFiles(listOf("/sys/class/power_supply/battery/charge_full_design", "/sys/class/power_supply/battery/batt_capacity_max")).toIntOrNull()?.div(1000) ?: 0
        if (designCap <= 0) designCap = getPowerProfileCapacity(context).toInt()

        return BatteryData("$batteryPct%", status, tempString, technology, if (designCap > 0) "$designCap mAh" else "Unknown", if (cycles > 0) "$cycles" else "—")
    }

    fun getAndroidVersion(): String = Build.VERSION.RELEASE
    fun getSdkVersion(): String = Build.VERSION.SDK_INT.toString()
    fun getKernelVersion(): String = System.getProperty("os.version") ?: "Unavailable"
    fun getBuildNumber(): String = Build.DISPLAY
    fun getFingerprint(): String = Build.FINGERPRINT

    private fun getSystemProperty(key: String, defaultValue: String = "Unknown"): String {
        return try {
            Class.forName("android.os.SystemProperties").getMethod("get", String::class.java, String::class.java).invoke(null, key, defaultValue) as String
        } catch (_: Exception) { defaultValue }
    }

    fun isTrebleSupported(): String = if (getSystemProperty("ro.treble.enabled", "false") == "true") "Supported" else "Unsupported"

    fun getVndkVersion(): String {
        val vendorBuild = getSystemProperty("ro.vendor.build.version.sdk", "")
        if (vendorBuild.isNotEmpty()) return vendorBuild

        val vendorApi = getSystemProperty("ro.vendor.api_level", "")
        if (vendorApi.isNotEmpty()) return vendorApi

        val boardApi = getSystemProperty("ro.board.api_level", "")
        if (boardApi.isNotEmpty()) return "API $boardApi"

        val vndk = getSystemProperty("ro.vndk.version", "")
        if (vndk.isNotEmpty()) return vndk

        return "Not Found"
    }
}