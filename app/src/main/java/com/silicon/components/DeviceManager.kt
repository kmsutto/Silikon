package com.silicon.ui.components

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.graphics.ImageFormat
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.opengl.GLES20
import android.os.*
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
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
import kotlin.math.roundToInt

object DeviceManager {

    data class RamData(val total: String, val used: String, val free: String, val progress: Float, val physicalSize: String, val vendor: String, val type: String)
    data class BatteryData(val level: String, val status: String, val voltage: String, val temp: String, val technology: String, val capacity: String, val cycles: String, val percentInt: Int, val estimatedCapacity: Int)
    data class StorageData(val total: String)
    data class GpuData(val renderer: String, val vendor: String, val version: String, val extensionsCount: String, val vulkanVersion: String)
    data class CameraLensInfo(val type: String, val megapixels: String, val aperture: String, val focalLength: String, val resolution: String, val sensorSize: String, val hasOis: Boolean)
    data class CameraSpecs(val backCameras: List<CameraLensInfo>, val frontCameras: List<CameraLensInfo>)

    private var cachedGpuData: GpuData? = null
    private var cachedBatteryCycles = -1
    private var cachedBatteryCap = 0
    private val cachedExactRamProp by lazy { getExactRamProp() }

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

    private fun mapApiToName(apiLevel: Int): String = when (apiLevel) {
        36 -> "Baklava"
        35 -> "Vanilla Ice Cream"
        34 -> "Upside Down Cake"
        33 -> "Tiramisu"
        32, 31 -> "Snow Cone"
        else -> "Android $apiLevel"
    }

    fun getAndroidVersion(): String = Build.VERSION.RELEASE
    fun getSdkVersion(): String = Build.VERSION.SDK_INT.toString()
    fun getAndroidCodename(): String = mapApiToName(Build.VERSION.SDK_INT)
    fun getSecurityPatch(): String = Build.VERSION.SECURITY_PATCH
    fun getKernelVersion(): String = System.getProperty("os.version") ?: "Unavailable"
    fun getBuildNumber(): String = Build.DISPLAY
    fun getFingerprint(): String = Build.FINGERPRINT
    fun isTrebleSupported(): String = if (getSystemProperty("ro.treble.enabled", "false") == "true") "Yes" else "No"
    fun isABUpdateSupported(): String = if (getSystemProperty("ro.build.ab_update", "false") == "true") "Yes" else "No"
    fun getVndkVersion(): String = getSystemProperty("ro.vndk.version").ifEmpty { getSystemProperty("ro.board.api_level") }

    fun isRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) return true
        val paths = arrayOf("/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/adb/magisk")
        if (paths.any { File(it).exists() }) return true
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            BufferedReader(InputStreamReader(process.inputStream)).readLine() != null
        } catch (_: Throwable) { false }
    }

    fun getBootloaderStatus(): String {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val alias = "SiliconBootloaderCheck"
            if (keyStore.containsAlias(alias)) keyStore.deleteEntry(alias)

            val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
            keyPairGenerator.initialize(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                .setAttestationChallenge("silicon_challenge".toByteArray())
                .setDigests(KeyProperties.DIGEST_SHA256).build())
            keyPairGenerator.generateKeyPair()

            val cert = keyStore.getCertificateChain(alias)[0] as X509Certificate
            val extensionValue = cert.getExtensionValue("1.3.6.1.4.1.11129.2.1.17")
            if (extensionValue != null) {
                return when (parseDeviceLockedFromAsn1(extensionValue)) {
                    true -> "Locked"
                    false -> "Unlocked"
                    else -> "Unknown"
                }
            }
        } catch (_: Throwable) { }

        val flashLocked = getSystemProperty("ro.boot.flash.locked")
        if (flashLocked == "1") return "Locked"
        if (flashLocked == "0") return "Unlocked"
        return "Unknown"
    }

    private fun parseDeviceLockedFromAsn1(extensionValue: ByteArray): Boolean? {
        try {
            val rootOfTrustTag = byteArrayOf(0xBF.toByte(), 0x85.toByte(), 0x40.toByte())
            for (i in 0 until extensionValue.size - 5) {
                if (extensionValue[i] == rootOfTrustTag[0] && extensionValue[i + 1] == rootOfTrustTag[1] && extensionValue[i + 2] == rootOfTrustTag[2]) {
                    var offset = i + 3
                    var length = extensionValue[offset++].toInt() and 0xFF
                    if (length >= 128) offset += (length and 0x7F)
                    if (extensionValue[offset] == 0x30.toByte()) {
                        offset++
                        var seqLen = extensionValue[offset++].toInt() and 0xFF
                        if (seqLen >= 128) offset += (seqLen and 0x7F)
                    }
                    if (extensionValue[offset++] != 0x04.toByte()) continue
                    var keyLen = extensionValue[offset++].toInt() and 0xFF
                    if (keyLen >= 128) {
                        val numBytes = keyLen and 0x7F
                        keyLen = 0
                        for (j in 0 until numBytes) keyLen = (keyLen shl 8) or (extensionValue[offset++].toInt() and 0xFF)
                    }
                    offset += keyLen
                    if (extensionValue[offset++] == 0x01.toByte()) {
                        val boolLen = extensionValue[offset++].toInt() and 0xFF
                        if (boolLen == 1) return extensionValue[offset].toInt() != 0
                    }
                }
            }
        } catch (_: Exception) { }
        return null
    }

    fun getDeviceCodename(): String = Build.DEVICE
    fun getDeviceName(): String = "${Build.MANUFACTURER} ${Build.MODEL}".split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
    fun getProcessorName(): String = Build.SOC_MODEL
    fun getCpuCount(): Int = Runtime.getRuntime().availableProcessors()
    fun getArchitecture(): String = System.getProperty("os.arch") ?: "Unknown"
    fun is64Bit(): String = if (Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()) "64-bit" else "32-bit"

    fun getResolution(context: Context): String {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val bounds = wm.currentWindowMetrics.bounds
        return "${bounds.width()}x${bounds.height()}"
    }

    fun getRefreshRate(context: Context): String = "${context.display?.refreshRate?.toInt() ?: 60}Hz"
    fun getDensity(context: Context): String = "${context.resources.displayMetrics.densityDpi} dpi"
    fun isHdrSupported(context: Context): Boolean = context.display?.isHdr == true

    private fun getExactRamProp(): Triple<String, String, String> {
        val ddrProp = getSystemProperty("ro.boot.hardware.ddr")
        if (ddrProp.isNotBlank() && ddrProp.contains(",")) {
            val parts = ddrProp.split(",")
            if (parts.size >= 3) {
                val size = parts[0].replace("i", "").replace("GB", " GB").trim()
                val vendor = parts[1].trim()
                val type = parts[2].trim()
                return Triple(size, vendor, type)
            }
        }
        return Triple("", "", "")
    }

    fun getRamDetails(context: Context): RamData {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo().apply { actManager.getMemoryInfo(this) }
        val usedBytes = memInfo.totalMem - memInfo.availMem
        val df = DecimalFormat("#.##")

        val totalMemGB = memInfo.totalMem.toDouble() / (1024.0.pow(3.0))
        val exactData = cachedExactRamProp

        return RamData(
            total = "${df.format(totalMemGB)} GB",
            used = "${df.format(usedBytes / (1024.0.pow(3.0)))} GB",
            free = "${df.format(memInfo.availMem / (1024.0.pow(3.0)))} GB",
            progress = usedBytes.toFloat() / memInfo.totalMem.toFloat(),
            physicalSize = if (exactData.first.isNotBlank()) exactData.first else "${kotlin.math.ceil(totalMemGB).toInt()} GB",
            vendor = exactData.second,
            type = exactData.third
        )
    }

    fun getGpuDetails(context: Context): GpuData {
        if (cachedGpuData != null) return cachedGpuData!!
        var renderer = "Unknown"; var vendor = "Unknown"; var version = "Unknown"; var extensions = "0"
        var vulkanVersion = "Not Supported"

        runCatching {
            val pm = context.packageManager
            val features = pm.systemAvailableFeatures
            val vulkanFeature = features.find { it.name == android.content.pm.PackageManager.FEATURE_VULKAN_HARDWARE_VERSION }
            if (vulkanFeature != null) {
                val v = vulkanFeature.version
                vulkanVersion = "${v shr 22}.${(v shr 12) and 0x3FF}.${v and 0xFFF}"
            }
        }

        runCatching {
            val egl = EGLContext.getEGL() as EGL10
            val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            egl.eglInitialize(display, IntArray(2))
            val configs = arrayOfNulls<EGLConfig>(1)
            egl.eglChooseConfig(display, intArrayOf(EGL10.EGL_RENDERABLE_TYPE, 4, EGL10.EGL_NONE), configs, 1, IntArray(1))
            val eglContext = egl.eglCreateContext(display, configs[0], EGL10.EGL_NO_CONTEXT, intArrayOf(0x3098, 2, EGL10.EGL_NONE))
            val eglSurface = egl.eglCreatePbufferSurface(display, configs[0], intArrayOf(EGL10.EGL_WIDTH, 1, EGL10.EGL_HEIGHT, 1, EGL10.EGL_NONE))
            egl.eglMakeCurrent(display, eglSurface, eglSurface, eglContext)

            renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
            vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"
            version = GLES20.glGetString(GLES20.GL_VERSION) ?: "Unknown"
            extensions = (GLES20.glGetString(GLES20.GL_EXTENSIONS)?.split(" ")?.size ?: 0).toString()

            egl.eglDestroySurface(display, eglSurface); egl.eglDestroyContext(display, eglContext); egl.eglTerminate(display)
        }

        return GpuData(renderer, vendor, version, extensions, vulkanVersion).also { cachedGpuData = it }
    }

    fun getCameraSpecs(context: Context): CameraSpecs {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val backCameras = mutableListOf<CameraLensInfo>()
        val frontCameras = mutableListOf<CameraLensInfo>()
        val processedIds = mutableSetOf<String>()

        try {
            for (logicalId in manager.cameraIdList) {
                val logicalChars = manager.getCameraCharacteristics(logicalId)
                val physicalIds = logicalChars.physicalCameraIds

                if (physicalIds.isNotEmpty()) {
                    for (physId in physicalIds) {
                        if (processedIds.add(physId)) parseCameraAndAdd(physId, manager.getCameraCharacteristics(physId), logicalChars, backCameras, frontCameras)
                    }
                } else {
                    if (processedIds.add(logicalId)) parseCameraAndAdd(logicalId, logicalChars, logicalChars, backCameras, frontCameras)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        return CameraSpecs(
            backCameras.sortedByDescending { it.megapixels.substringBefore(" ").toFloatOrNull() ?: 0f },
            frontCameras.sortedByDescending { it.megapixels.substringBefore(" ").toFloatOrNull() ?: 0f }
        )
    }

    private fun parseCameraAndAdd(id: String, chars: CameraCharacteristics, logicalChars: CameraCharacteristics, back: MutableList<CameraLensInfo>, front: MutableList<CameraLensInfo>) {
        val isFront = chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
        var sensorStr = "Unknown"
        var cropFactor = 0.0

        chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)?.let { size ->
            if (size.width > 0 && size.height > 0) {
                val diagMm = kotlin.math.sqrt((size.width * size.width + size.height * size.height).toDouble())
                if (diagMm > 0) {
                    sensorStr = String.format(java.util.Locale.US, "1/%.2f\"", 16.0 / diagMm)
                    cropFactor = 43.27 / diagMm
                }
            }
        }

        var maxWidth = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)?.width ?: 0
        var maxHeight = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)?.height ?: 0

        chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)?.let { active ->
            if (active.width() * active.height() > maxWidth * maxHeight) { maxWidth = active.width(); maxHeight = active.height() }
        }

        chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP_MAXIMUM_RESOLUTION)?.getOutputSizes(ImageFormat.JPEG)?.maxByOrNull { it.width * it.height }?.let { maxHighRes ->
            if ((maxHighRes.width * maxHighRes.height) > (maxWidth * maxHeight)) { maxWidth = maxHighRes.width; maxHeight = maxHighRes.height }
        }

        val mpFloat = (maxWidth * maxHeight) / 1_000_000f
        val mpString = if (mpFloat > 0) String.format(java.util.Locale.US, "%.1f", mpFloat).replace(".0", "") + " MP" else "Unknown"
        val apertureStr = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)?.firstOrNull()?.let { "f/$it" } ?: "Unknown"

        var focalStr = "Unknown"
        var lensType = "Lens"
        chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.firstOrNull()?.let { focalPhys ->
            if (cropFactor > 0) {
                val eq = (focalPhys * cropFactor).roundToInt()
                focalStr = "~${eq}mm"
                lensType = when { eq < 20 -> "Ultrawide"; eq in 20..35 -> "Main"; else -> "Telephoto" }
            } else {
                focalStr = "${focalPhys}mm (Phys)"
                lensType = when { focalPhys < 3.5f -> "Ultrawide"; focalPhys in 3.5f..7.5f -> "Main"; else -> "Telephoto" }
            }
        }

        val hasOis = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)?.any { it != CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_OFF } == true ||
                logicalChars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)?.any { it != CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_OFF } == true && (lensType == "Main" || lensType == "Telephoto")

        val info = CameraLensInfo(if (isFront) "Front ($lensType)" else lensType, mpString, apertureStr, focalStr, if (maxWidth > 0) "$maxWidth x $maxHeight" else "Unknown", sensorStr, hasOis)
        if (isFront) front.add(info) else back.add(info)
    }

    fun getBatteryFlow(context: Context): Flow<BatteryData> = callbackFlow {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
                val percent = if (scale > 0) (level * 100 / scale.toFloat()).toInt() else 0

                val status = when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                    BatteryManager.BATTERY_STATUS_FULL -> "Full"
                    else -> "Idle"
                }

                val temp = (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10.0
                val tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

                if (cachedBatteryCycles <= 0) {
                    var cycles = intent.getIntExtra("android.os.extra.CYCLE_COUNT", -1)
                    if (cycles <= 0) cycles = bm.getIntProperty(4)
                    if (cycles <= 0) {
                        val cyclePaths = listOf(
                            "/sys/class/power_supply/bms/cycle_count",
                            "/sys/class/power_supply/maxfg/cycle_count",
                            "/sys/class/power_supply/battery/cycle_count",
                            "/sys/class/power_supply/battery/battery_cycle"
                        )
                        cycles = findValueInFiles(cyclePaths).toIntOrNull() ?: -1
                    }
                    cachedBatteryCycles = cycles
                }

                if (cachedBatteryCap <= 0) {
                    var cap = findValueInFiles(listOf("/sys/class/power_supply/battery/charge_full_design", "/sys/class/power_supply/battery/batt_capacity_max")).toIntOrNull()?.div(1000) ?: 0
                    if (cap <= 0) {
                        try {
                            val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
                            val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(context)
                            cap = (powerProfileClass.getMethod("getBatteryCapacity").invoke(powerProfile) as Double).toInt()
                        } catch (_: Exception) {}
                    }
                    cachedBatteryCap = cap
                }

                val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                val voltageStr = String.format(java.util.Locale.US, "%.1f V", if (voltageMv > 100) voltageMv / 1000f else voltageMv.toFloat())

                val chargeCounter = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000
                val estimatedCapacity = if (percent > 0) (chargeCounter * 100) / percent else 0

                trySend(
                    BatteryData(
                        level = "$percent%",
                        status = status,
                        voltage = voltageStr,
                        temp = "$temp°C",
                        technology = tech,
                        capacity = if (cachedBatteryCap > 0) "$cachedBatteryCap mAh" else "Unknown",
                        cycles = if (cachedBatteryCycles >= 0) "$cachedBatteryCycles" else "—",
                        percentInt = percent,
                        estimatedCapacity = estimatedCapacity
                    )
                )
            }
        }

        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        awaitClose { context.unregisterReceiver(receiver) }
    }.flowOn(Dispatchers.IO)

    fun getAccelerometerData(context: Context): Flow<FloatArray> = callbackFlow {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) { if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) trySend(event.values.clone()) }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sm.registerListener(listener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sm.unregisterListener(listener) }
    }.sample(16L)

    fun runVibrationTest(context: Context, durationMs: Long = 5000) {
        val vibrator = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        if (vibrator.hasVibrator()) vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun setImmersiveMode(window: Window, view: View, enable: Boolean) {
        val insetsController = WindowCompat.getInsetsController(window, view)
        val params = window.attributes
        if (enable) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
            insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
        window.attributes = params
    }

    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }

    @Composable
    fun TouchscreenTestScreen(onDismiss: () -> Unit) {
        val context = LocalContext.current
        val view = LocalView.current
        var touchedCells by remember { mutableStateOf(emptySet<Pair<Int, Int>>()) }
        val density = LocalDensity.current
        val cellSizePx = remember(density) { with(density) { 48.dp.toPx() } }

        DisposableEffect(Unit) {
            val window = context.findActivity()?.window
            if (window != null) setImmersiveMode(window, view, true)
            onDispose { if (window != null) setImmersiveMode(window, view, false) }
        }

        Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black).pointerInput(Unit) {
                    awaitEachGesture {
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    val cell = (change.position.x / cellSizePx).toInt() to (change.position.y / cellSizePx).toInt()
                                    if (!touchedCells.contains(cell)) {
                                        val newSet = touchedCells.toMutableSet()
                                        newSet.add(cell)
                                        touchedCells = newSet
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cols = (size.width / cellSizePx).toInt() + 1
                    val rows = (size.height / cellSizePx).toInt() + 1
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            val cell = c to r
                            drawRect(
                                color = if (touchedCells.contains(cell)) Color.Green else Color.DarkGray,
                                topLeft = Offset(c * cellSizePx + 2f, r * cellSizePx + 2f),
                                size = Size(cellSizePx - 4f, cellSizePx - 4f)
                            )
                        }
                    }
                }
            }
            BackHandler { onDismiss() }
        }
    }
}