package com.silicon.ui.components

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.opengl.GLES20
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import android.view.WindowManager
import android.view.WindowMetrics
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.text.DecimalFormat
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import kotlin.math.roundToInt

object DeviceManager {

    data class RamData(val total: String, val used: String, val free: String, val progress: Float)

    data class BatteryData(
        val level: String, val status: String, val temp: String, val technology: String, val capacity: String, val cycles: String
    )

    data class GpuData(
        val renderer: String,
        val vendor: String,
        val version: String,
        val extensionsCount: String
    )

    private var cachedGpuData: GpuData? = null

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

    private fun getSystemProperty(key: String, defaultValue: String = ""): String {
        return try {
            Class.forName("android.os.SystemProperties").getMethod("get", String::class.java, String::class.java).invoke(null, key, defaultValue) as String
        } catch (_: Exception) { defaultValue }
    }

    fun getGpuDetails(): GpuData {
        if (cachedGpuData != null) return cachedGpuData!!

        var renderer = "Unknown"
        var vendor = "Unknown"
        var version = "Unknown"
        var extensions = "0"

        try {
            val egl = EGLContext.getEGL() as EGL10
            val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            val ver = IntArray(2)
            egl.eglInitialize(display, ver)

            val configAttribs = intArrayOf(
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL10.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfig = IntArray(1)
            egl.eglChooseConfig(display, configAttribs, configs, 1, numConfig)

            val contextAttribs = intArrayOf(0x3098, 2, EGL10.EGL_NONE)
            val eglContext = egl.eglCreateContext(display, configs[0], EGL10.EGL_NO_CONTEXT, contextAttribs)

            val surfAttribs = intArrayOf(EGL10.EGL_WIDTH, 1, EGL10.EGL_HEIGHT, 1, EGL10.EGL_NONE)
            val eglSurface = egl.eglCreatePbufferSurface(display, configs[0], surfAttribs)

            egl.eglMakeCurrent(display, eglSurface, eglSurface, eglContext)

            renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
            vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"

            val fullVersion = GLES20.glGetString(GLES20.GL_VERSION) ?: "Unknown"
            version = if (fullVersion.startsWith("OpenGL ES")) {
                fullVersion.split(" ").take(3).joinToString(" ")
            } else {
                fullVersion
            }

            val extString = GLES20.glGetString(GLES20.GL_EXTENSIONS)
            extensions = extString?.split(" ")?.size?.toString() ?: "0"

            egl.eglDestroySurface(display, eglSurface)
            egl.eglDestroyContext(display, eglContext)
            egl.eglTerminate(display)

        } catch (e: Exception) {
            Log.e("DeviceManager", "Failed to get GPU details: ${e.message}")
        }

        val data = GpuData(renderer, vendor, version, extensions)
        cachedGpuData = data
        return data
    }

    private fun mapApiToName(apiLevel: Int): String {
        return when (apiLevel) {
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
            25 -> "Nougat MR1"
            24 -> "Nougat"
            else -> "Legacy"
        }
    }

    fun getAndroidCodename(): String = mapApiToName(Build.VERSION.SDK_INT)

    fun getVndkVersion(): String {
        var version = getSystemProperty("ro.vndk.version")
        if (version.isEmpty()) version = getSystemProperty("ro.board.api_level")
        if (version.isEmpty()) version = getSystemProperty("ro.vendor.build.version.sdk")
        return if (version.isNotEmpty()) version else "Not Found"
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

    fun getResolution(context: Context): String {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics = windowManager.currentWindowMetrics
            "${metrics.bounds.width()}x${metrics.bounds.height()}"
        } else {
            val metrics = android.util.DisplayMetrics()
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
    fun isTrebleSupported(): String = if (getSystemProperty("ro.treble.enabled", "false") == "true") "Supported" else "Unsupported"
}