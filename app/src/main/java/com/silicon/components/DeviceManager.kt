package com.silicon.ui.components

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.opengl.GLES20
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.WindowManager
import android.view.WindowMetrics
import com.silicon.ui.components.database.DataPixel
import com.silicon.ui.components.database.DataPlus
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import kotlin.math.pow

object DeviceManager {

    data class RamData(val total: String, val used: String, val free: String, val progress: Float)

    data class BatteryData(
        val level: String, val status: String, val temp: String, val technology: String, val capacity: String, val cycles: String
    )

    data class StorageData(
        val total: String, val used: String, val percent: Int, val progress: Float
    )

    data class GpuData(
        val renderer: String, val vendor: String, val version: String, val extensionsCount: String
    )

    data class CameraLensInfo(
        val type: String,
        val megapixels: String,
        val aperture: String,
        val focalLength: String,
        val resolution: String,
        val sensorSize: String,
        val hasOis: Boolean
    )

    data class CameraSpecs(
        val backCameras: List<CameraLensInfo>,
        val frontCameras: List<CameraLensInfo>
    )

    private var cachedGpuData: GpuData? = null

    private fun getSystemProperty(key: String, defaultValue: String = ""): String {
        return try {
            Class.forName("android.os.SystemProperties").getMethod("get", String::class.java, String::class.java).invoke(null, key, defaultValue) as String
        } catch (_: Exception) { defaultValue }
    }

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

    fun getSecurityPatch(): String = Build.VERSION.SECURITY_PATCH

    fun isRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) return true

        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su",
            "/data/adb/magisk", "/sbin/magisk", "/system/xbin/daemonsu"
        )
        if (paths.any { File(it).exists() }) return true

        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine() != null
        } catch (t: Throwable) {
            false
        }
    }

    fun getBootloaderStatus(context: Context): String {
        val customRomPackages = listOf(
            "org.evolution.settings",
            "com.evolution.settings",
            "org.lineageos.settings",
            "org.lineageos.updater",
            "com.crdroid.settings",
            "org.pixelexperience.updater",
            "com.paranoid.settings"
        )

        val pm = context.packageManager
        for (pkg in customRomPackages) {
            try {
                pm.getPackageInfo(pkg, 0)
                return "Unlocked"
            } catch (e: Exception) {
            }
        }

        val flavor = getSystemProperty("ro.build.flavor").lowercase()
        val host = getSystemProperty("ro.build.host").lowercase()
        val user = getSystemProperty("ro.build.user").lowercase()

        if (flavor.contains("evolution") || host.contains("evolution") || user.contains("evolution") || flavor.contains("lineage")) {
            return "Unlocked)"
        }

        val state = getSystemProperty("ro.boot.verifiedbootstate")
        val locked = getSystemProperty("ro.boot.flash.locked")

        return when {
            state == "orange" || locked == "0" -> "Unlocked"
            state == "green" || locked == "1" -> "Locked"
            else -> "Unknown"
        }
    }

    fun getGpuDetails(): GpuData {
        if (cachedGpuData != null) return cachedGpuData!!

        var renderer = "Unknown"; var vendor = "Unknown"; var version = "Unknown"; var extensions = "0"
        try {
            val egl = EGLContext.getEGL() as EGL10
            val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            egl.eglInitialize(display, IntArray(2))

            val configAttribs = intArrayOf(EGL10.EGL_RENDERABLE_TYPE, 4, EGL10.EGL_NONE)
            val configs = arrayOfNulls<EGLConfig>(1)
            egl.eglChooseConfig(display, configAttribs, configs, 1, IntArray(1))

            val contextAttribs = intArrayOf(0x3098, 2, EGL10.EGL_NONE)
            val eglContext = egl.eglCreateContext(display, configs[0], EGL10.EGL_NO_CONTEXT, contextAttribs)
            val surfAttribs = intArrayOf(EGL10.EGL_WIDTH, 1, EGL10.EGL_HEIGHT, 1, EGL10.EGL_NONE)
            val eglSurface = egl.eglCreatePbufferSurface(display, configs[0], surfAttribs)
            egl.eglMakeCurrent(display, eglSurface, eglSurface, eglContext)

            renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
            vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"
            version = GLES20.glGetString(GLES20.GL_VERSION)?.split(" ")?.take(3)?.joinToString(" ") ?: "Unknown"
            extensions = (GLES20.glGetString(GLES20.GL_EXTENSIONS)?.split(" ")?.size ?: 0).toString()

            egl.eglDestroySurface(display, eglSurface); egl.eglDestroyContext(display, eglContext); egl.eglTerminate(display)
        } catch (e: Exception) { Log.e("Silicon", "GPU Error: ${e.message}") }

        return GpuData(renderer, vendor, version, extensions).also { cachedGpuData = it }
    }

    fun getCameraSpecs(context: Context): CameraSpecs {
        val possibleNames = listOf(Build.DEVICE, Build.MODEL, Build.PRODUCT)

        var pixelSpecs: List<CameraLensInfo>? = null
        for (name in possibleNames) {
            pixelSpecs = DataPixel.getSpecs(name)
            if (pixelSpecs != null) break
        }

        if (pixelSpecs != null) {
            return CameraSpecs(
                backCameras = pixelSpecs.filter { !it.type.startsWith("Front") },
                frontCameras = pixelSpecs.filter { it.type.startsWith("Front") }
            )
        }

        var onePlusSpecs: List<CameraLensInfo>? = null
        for (name in possibleNames) {
            onePlusSpecs = DataPlus.getSpecs(name)
            if (onePlusSpecs != null) break
        }

        if (onePlusSpecs != null) {
            return CameraSpecs(
                backCameras = onePlusSpecs.filter { !it.type.startsWith("Front") },
                frontCameras = onePlusSpecs.filter { it.type.startsWith("Front") }
            )
        }

        return CameraSpecs(emptyList(), emptyList())
    }

    private fun mapApiToName(apiLevel: Int): String {
        return when (apiLevel) {
            36 -> "Baklava"
            35 -> "Vanilla Ice Cream"
            34 -> "Upside Down Cake"
            33 -> "Tiramisu"
            32 -> "Snow Cone V2"
            31 -> "Snow Cone"
            else -> "Legacy ($apiLevel)"
        }
    }

    fun getAndroidCodename(): String = mapApiToName(Build.VERSION.SDK_INT)
    fun getVndkVersion(): String = getSystemProperty("ro.vndk.version").ifEmpty { getSystemProperty("ro.board.api_level") }
    fun getDeviceCodename(): String = Build.DEVICE
    fun getDeviceName(): String = "${Build.MANUFACTURER} ${Build.MODEL}".split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

    fun getProcessorName(): String = Build.SOC_MODEL
    fun getCpuCount(): Int = Runtime.getRuntime().availableProcessors()
    fun getArchitecture(): String = System.getProperty("os.arch") ?: "Unknown"
    fun is64Bit(): String = if (Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()) "64-bit" else "32-bit"

    fun getResolution(context: Context): String {
        val metrics: WindowMetrics = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).currentWindowMetrics
        return "${metrics.bounds.width()}x${metrics.bounds.height()}"
    }

    fun getRefreshRate(context: Context): String = "${context.display?.refreshRate?.toInt() ?: 60}Hz"
    fun getDensity(context: Context): String = "${context.resources.displayMetrics.densityDpi} dpi"
    fun isHdrSupported(context: Context): Boolean = context.display?.isHdr == true

    fun getUptime(): String {
        val uptimeMillis = SystemClock.elapsedRealtime()
        val days = TimeUnit.MILLISECONDS.toDays(uptimeMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60
        return "${days}d ${hours}h ${minutes}m"
    }

    fun getRamDetails(context: Context): RamData {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val totalBytes = memInfo.totalMem
        val availBytes = memInfo.availMem
        val usedBytes = totalBytes - availBytes

        val totalGB = totalBytes.toDouble() / (1024.0.pow(3.0))
        val marketingTotal = when {
            totalGB > 20 -> 24
            totalGB > 14 -> 16
            totalGB > 10 -> 12
            totalGB > 7 -> 8
            totalGB > 5 -> 6
            totalGB > 3 -> 4
            else -> 3
        }

        val df = DecimalFormat("#.##")
        return RamData(
            total = "$marketingTotal GB",
            used = "${df.format(usedBytes / (1024.0.pow(3.0)))} GB",
            free = "${df.format(availBytes / (1024.0.pow(3.0)))} GB",
            progress = usedBytes.toFloat() / totalBytes.toFloat()
        )
    }

    fun getStorageInfo(): StorageData {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalBytes = totalBlocks * blockSize
        val availableBytes = availableBlocks * blockSize
        val usedBytes = totalBytes - availableBytes

        val totalGB = totalBytes.toDouble() / (1024.0.pow(3.0))
        val usedGB = usedBytes.toDouble() / (1024.0.pow(3.0))
        val percent = if (totalBytes > 0) ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt() else 0

        val df = DecimalFormat("#.#")
        return StorageData(
            total = "${df.format(totalGB)} GB",
            used = "${df.format(usedGB)} GB",
            percent = percent,
            progress = percent / 100f
        )
    }

    fun getBatteryInfo(context: Context): BatteryData {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val percent = if (scale > 0) (level * 100 / scale.toFloat()).toInt() else 0

        val status = when (intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            else -> "Idle"
        }

        val temp = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10.0
        val tech = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"
        var cycles = if (Build.VERSION.SDK_INT >= 34) intent?.getIntExtra("android.os.extra.CYCLE_COUNT", -1) ?: -1 else -1
        if (cycles == -1) cycles = findValueInFiles(listOf("/sys/class/power_supply/battery/cycle_count", "/sys/class/power_supply/battery/battery_cycle")).toIntOrNull() ?: 0

        var cap = findValueInFiles(listOf("/sys/class/power_supply/battery/charge_full_design", "/sys/class/power_supply/battery/batt_capacity_max")).toIntOrNull()?.div(1000) ?: 0
        if (cap <= 0) {
            try {
                cap = (Class.forName("com.android.internal.os.PowerProfile").getConstructor(Context::class.java).newInstance(context).let {
                    it.javaClass.getMethod("getBatteryCapacity").invoke(it) as Double
                }).toInt()
            } catch (_: Exception) {}
        }

        return BatteryData("$percent%", status, "$temp°C", tech, if (cap > 0) "$cap mAh" else "Unknown", if (cycles > 0) "$cycles" else "—")
    }

    fun getAndroidVersion(): String = Build.VERSION.RELEASE
    fun getSdkVersion(): String = Build.VERSION.SDK_INT.toString()
    fun getKernelVersion(): String = System.getProperty("os.version") ?: "Unavailable"
    fun getBuildNumber(): String = Build.DISPLAY
    fun getFingerprint(): String = Build.FINGERPRINT
    fun isTrebleSupported(): String = if (getSystemProperty("ro.treble.enabled", "false") == "true") "Yes" else "No"
}
