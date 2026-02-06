package com.silicon.ui.components

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
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
import kotlin.math.pow
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
        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"
        )
        return paths.any { File(it).exists() }
    }

    fun getBootloaderStatus(): String {
        val state = getSystemProperty("ro.boot.verifiedbootstate")
        val locked = getSystemProperty("ro.boot.flash.locked")
        return when {
            state == "green" || locked == "1" -> "Locked"
            state == "orange" || locked == "0" -> "Unlocked"
            state == "yellow" -> "Locked (Custom Key)"
            state == "red" -> "Unlocked (Warning)"
            else -> if (state.isNotEmpty()) state.replaceFirstChar { it.uppercase() } else "Unknown"
        }
    }

    fun getIntegrityPrediction(): String {
        return if (getBootloaderStatus().startsWith("Locked") && !isRooted()) "Meets Basic & Strong" else "Unlikely to pass"
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
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val backList = mutableListOf<CameraLensInfo>()
        val frontList = mutableListOf<CameraLensInfo>()

        try {
            for (id in manager.cameraIdList) {
                val chars = manager.getCameraCharacteristics(id)
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                if (facing == CameraCharacteristics.LENS_FACING_EXTERNAL) continue

                val physicalIds = chars.physicalCameraIds

                if (physicalIds.isNotEmpty()) {
                    for (physId in physicalIds) {
                        val physChars = manager.getCameraCharacteristics(physId)
                        addLensInfo(physChars, facing, backList, frontList)
                    }
                } else {
                    addLensInfo(chars, facing, backList, frontList)
                }
            }
        } catch (e: Exception) {
            Log.e("Silicon", "Camera error: ${e.message}")
        }

        return CameraSpecs(
            backCameras = backList.distinctBy { it.focalLength + it.megapixels }.sortedBy {
                it.focalLength.replace(" mm", "").replace(",", ".").toFloatOrNull() ?: 0f
            },
            frontCameras = frontList.distinctBy { it.megapixels }
        )
    }

    private fun addLensInfo(
        chars: CameraCharacteristics,
        facing: Int?,
        backList: MutableList<CameraLensInfo>,
        frontList: MutableList<CameraLensInfo>
    ) {
        val info = extractLensInfo(chars, facing)
        if (facing == CameraCharacteristics.LENS_FACING_BACK) backList.add(info) else frontList.add(info)
    }

    private fun extractLensInfo(chars: CameraCharacteristics, facing: Int?): CameraLensInfo {
        val activeArray = chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
        val pixelArray = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)

        val width = activeArray?.width() ?: pixelArray?.width ?: 0
        val height = activeArray?.height() ?: pixelArray?.height ?: 0

        val mpCount = (width * height) / 1_000_000.0
        val mpStr = DecimalFormat("#.#").format(mpCount) + " MP"
        val resStr = "$width x $height"

        val apertures = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
        val apertureVal = apertures?.minOrNull() ?: 0f
        val apStr = "f/${DecimalFormat("#.##").format(apertureVal)}"

        val focalLengths = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        val fl = focalLengths?.firstOrNull() ?: 0f
        val flStr = "${DecimalFormat("#.#").format(fl)} mm"

        val sensorSizeRect = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        val sensorStr = if (sensorSizeRect != null) {
            "${DecimalFormat("#.##").format(sensorSizeRect.width)} x ${DecimalFormat("#.##").format(sensorSizeRect.height)} mm"
        } else "Unknown"

        val modes = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
        val hasOis = modes?.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON) == true

        val type = if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
            "Front"
        } else {
            when {
                fl > 0f && fl < 3.8f -> "Ultrawide"
                fl >= 3.8f && fl < 7.5f -> "Main"
                fl >= 7.5f -> "Telephoto"
                else -> "Main"
            }
        }

        return CameraLensInfo(type, mpStr, apStr, flStr, resStr, sensorStr, hasOis)
    }

    private fun mapApiToName(apiLevel: Int): String {
        return when (apiLevel) {
            36 -> "Baklava"
            35 -> "Vanilla Ice Cream"
            34 -> "Upside Down Cake"
            33 -> "Tiramisu"
            32 -> "Snow Cone V2"
            31 -> "Snow Cone"
            30 -> "Red Velvet Cake"
            else -> "Legacy ($apiLevel)"
        }
    }

    fun getAndroidCodename(): String = mapApiToName(Build.VERSION.SDK_INT)

    fun getVndkVersion(): String {
        return getSystemProperty("ro.vndk.version").ifEmpty {
            getSystemProperty("ro.board.api_level").ifEmpty { "Not Found" }
        }
    }

    fun getDeviceCodename(): String = Build.DEVICE
    fun getDeviceName(): String = "${Build.MANUFACTURER} ${Build.MODEL}".split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

    fun getProcessorName(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Build.SOC_MODEL else Build.HARDWARE.uppercase()
    }

    fun getCpuCount(): Int = Runtime.getRuntime().availableProcessors()
    fun getArchitecture(): String = System.getProperty("os.arch") ?: "Unknown"
    fun is64Bit(): String = if (Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()) "64-bit" else "32-bit"

    fun getResolution(context: Context): String {
        val metrics: WindowMetrics = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).currentWindowMetrics
        return "${metrics.bounds.width()}x${metrics.bounds.height()}"
    }

    fun getRefreshRate(context: Context): String {
        return "${context.display?.refreshRate?.toInt() ?: 60}Hz"
    }

    fun getDensity(context: Context): String = "${context.resources.displayMetrics.densityDpi} dpi"
    fun isHdrSupported(context: Context): Boolean = context.display?.isHdr == true

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
            else -> 4
        }

        val df = DecimalFormat("#.##")
        return RamData(
            total = "$marketingTotal GB",
            used = "${df.format(usedBytes / (1024.0.pow(3.0)))} GB",
            free = "${df.format(availBytes / (1024.0.pow(3.0)))} GB",
            progress = usedBytes.toFloat() / totalBytes.toFloat()
        )
    }

    fun getBatteryInfo(context: Context): BatteryData {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val percent = (level * 100 / scale.toFloat()).toInt()

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
        if (cap <= 0) cap = (Class.forName("com.android.internal.os.PowerProfile").getConstructor(Context::class.java).newInstance(context).let {
            it.javaClass.getMethod("getBatteryCapacity").invoke(it) as Double
        }).toInt()

        return BatteryData("$percent%", status, "$temp°C", tech, if (cap > 0) "$cap mAh" else "Unknown", if (cycles > 0) "$cycles" else "—")
    }

    fun getAndroidVersion(): String = Build.VERSION.RELEASE
    fun getSdkVersion(): String = Build.VERSION.SDK_INT.toString()
    fun getKernelVersion(): String = System.getProperty("os.version") ?: "Unavailable"
    fun getBuildNumber(): String = Build.DISPLAY
    fun getFingerprint(): String = Build.FINGERPRINT
    fun isTrebleSupported(): String = if (getSystemProperty("ro.treble.enabled", "false") == "true") "Yes" else "No"
}
