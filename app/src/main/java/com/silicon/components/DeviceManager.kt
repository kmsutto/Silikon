package com.silicon.ui.components

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.opengl.GLES20
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.view.WindowManager
import android.view.WindowMetrics
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.concurrent.atomic.AtomicLong
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.roundToInt

object DeviceManager {
    data class RamData(val total: String, val used: String, val free: String, val progress: Float)
    data class BatteryData(val level: String, val status: String, val voltage: String, val temp: String, val technology: String, val capacity: String, val cycles: String, val percentInt: Int, val estimatedCapacity: Int)
    data class StorageData(val total: String, val used: String, val percent: Int, val progress: Float, val type: String, val model: String)
    data class GpuData(val renderer: String, val vendor: String, val version: String, val extensionsCount: String)
    data class CameraLensInfo(val type: String, val megapixels: String, val aperture: String, val focalLength: String, val resolution: String, val sensorSize: String, val hasOis: Boolean)
    data class CameraSpecs(val backCameras: List<CameraLensInfo>, val frontCameras: List<CameraLensInfo>)
    data class PartitionData(val mountPoint: String, val fsType: String, val total: String, val used: String, val percent: Int, val progress: Float)

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

    fun getUptime(): String {
        val uptimeMillis = SystemClock.elapsedRealtime()
        val days = TimeUnit.MILLISECONDS.toDays(uptimeMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(uptimeMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60
        return "${days}d ${hours}h ${minutes}m"
    }

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
        } catch (_: Exception) { }

        return GpuData(renderer, vendor, version, extensions).also { cachedGpuData = it }
    }

    fun getCameraSpecs(context: Context): CameraSpecs {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val backCameras = mutableListOf<CameraLensInfo>()
        val frontCameras = mutableListOf<CameraLensInfo>()
        val processedIds = mutableSetOf<String>()

        try {
            for (logicalId in manager.cameraIdList) {
                val logicalChars = manager.getCameraCharacteristics(logicalId)

                val physicalIds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    logicalChars.physicalCameraIds
                } else emptySet()

                if (physicalIds.isNotEmpty()) {
                    for (physId in physicalIds) {
                        if (processedIds.add(physId)) {
                            val physChars = manager.getCameraCharacteristics(physId)
                            parseCameraAndAdd(physId, physChars, logicalChars, backCameras, frontCameras)
                        }
                    }
                } else {
                    if (processedIds.add(logicalId)) {
                        parseCameraAndAdd(logicalId, logicalChars, logicalChars, backCameras, frontCameras)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return CameraSpecs(
            backCameras.sortedByDescending { it.megapixels.substringBefore(" ").toFloatOrNull() ?: 0f },
            frontCameras.sortedByDescending { it.megapixels.substringBefore(" ").toFloatOrNull() ?: 0f }
        )
    }

    private fun parseCameraAndAdd(
        id: String,
        chars: CameraCharacteristics,
        logicalChars: CameraCharacteristics,
        back: MutableList<CameraLensInfo>,
        front: MutableList<CameraLensInfo>
    ) {
        val facing = chars.get(CameraCharacteristics.LENS_FACING)
        val isFront = facing == CameraCharacteristics.LENS_FACING_FRONT

        var sensorStr = "Unknown"
        var cropFactor = 0.0
        var diagMm = 0.0
        val sensorSize = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)

        if (sensorSize != null && sensorSize.width > 0 && sensorSize.height > 0) {
            diagMm = kotlin.math.sqrt((sensorSize.width * sensorSize.width + sensorSize.height * sensorSize.height).toDouble())
            if (diagMm > 0) {
                val opticalFraction = 16.0 / diagMm
                sensorStr = String.format(java.util.Locale.US, "1/%.2f\"", opticalFraction)
                cropFactor = 43.27 / diagMm
            }
        }

        var resString = "Unknown"
        var mpString = "Unknown"
        var mpFloat = 0f

        val pixelArraySize = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
        if (pixelArraySize != null) {
            var width = pixelArraySize.width
            var height = pixelArraySize.height

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val maxResMap = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP_MAXIMUM_RESOLUTION)
                val sizes = maxResMap?.getOutputSizes(android.graphics.ImageFormat.JPEG)
                val maxObj = sizes?.maxByOrNull { it.width * it.height }
                if (maxObj != null && (maxObj.width * maxObj.height) > (width * height)) {
                    width = maxObj.width
                    height = maxObj.height
                }
            }

            mpFloat = (width * height) / 1_000_000f

            val vendor = Build.MANUFACTURER.lowercase()
            val isChineseOEM = vendor in listOf("oneplus", "oppo", "vivo", "xiaomi", "redmi", "poco", "realme", "iqoo")

            if (isChineseOEM) {
                if (mpFloat in 11.5..13.5) {
                    mpFloat *= 4; width *= 2; height *= 2
                } else if (mpFloat in 3.5..4.5) {
                    mpFloat *= 4; width *= 2; height *= 2
                } else if (mpFloat in 7.0..8.5) {
                    mpFloat *= 4; width *= 2; height *= 2
                } else if (mpFloat in 15.0..16.5) {
                    mpFloat *= 4; width *= 2; height *= 2
                }
            } else {
                var pixelPitch = 0.0
                if (width > 0 && diagMm > 0) pixelPitch = (sensorSize!!.width.toDouble() / width) * 1000.0

                if (mpFloat in 11.5..13.0 && diagMm > 11.0) {
                    mpFloat *= 16; width *= 4; height *= 4
                } else if (mpFloat in 11.0..12.5 && diagMm > 9.0 && pixelPitch > 1.9) {
                    mpFloat *= 9; width *= 3; height *= 3
                }
            }

            resString = "$width x $height"
            mpString = if (mpFloat > 10) {
                String.format(java.util.Locale.US, "%.0f MP", mpFloat)
            } else {
                String.format(java.util.Locale.US, "%.1f MP", mpFloat)
            }
        }

        val apertures = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
        val apertureStr = if (apertures != null && apertures.isNotEmpty()) "f/${apertures[0]}" else "Unknown"

        var focalStr = "Unknown"
        var lensType = "Lens"
        val focalLengths = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)

        if (focalLengths != null && focalLengths.isNotEmpty()) {
            val focalPhys = focalLengths[0]
            if (cropFactor > 0) {
                val eq = (focalPhys * cropFactor).roundToInt()
                focalStr = "~${eq}mm"
                when {
                    eq < 20 -> lensType = "Ultrawide"
                    eq in 20..35 -> lensType = "Main"
                    eq > 35 -> lensType = "Telephoto"
                }
            } else {
                focalStr = "${focalPhys}mm (Phys)"
                when {
                    focalPhys < 3.5f -> lensType = "Ultrawide"
                    focalPhys in 3.5f..7.5f -> lensType = "Main"
                    focalPhys > 7.5f -> lensType = "Telephoto"
                }
            }
        }

        var hasOis = false
        val oisModes = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
        if (oisModes != null && oisModes.any { it != CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_OFF }) {
            hasOis = true
        } else {
            val logicalOisModes = logicalChars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
            if (logicalOisModes != null && logicalOisModes.any { it != CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_OFF }) {
                if (lensType == "Main" || lensType == "Telephoto") {
                    hasOis = true
                }
            }
        }

        val typeName = if (isFront) "Front ($lensType)" else lensType

        val info = CameraLensInfo(typeName, mpString, apertureStr, focalStr, resString, sensorStr, hasOis)
        if (isFront) front.add(info) else back.add(info)
    }

    fun getRamDetails(context: Context): RamData {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)

        val totalBytes = memInfo.totalMem
        val availBytes = memInfo.availMem
        val usedBytes = totalBytes - availBytes

        val totalGB = totalBytes.toDouble() / (1024.0.pow(3.0))

        val df = DecimalFormat("#.##")
        return RamData(
            total = "${df.format(totalGB)} GB",
            used = "${df.format(usedBytes / (1024.0.pow(3.0)))} GB",
            free = "${df.format(availBytes / (1024.0.pow(3.0)))} GB",
            progress = usedBytes.toFloat() / totalBytes.toFloat()
        )
    }

    private fun getStorageHardwareInfo(): Pair<String, String> {
        var type = "Unknown Type"
        var modelStr = "Unknown Model"

        try {
            val bootDevice = getSystemProperty("ro.boot.bootdevice", "").lowercase()
            if (bootDevice.contains("ufs")) type = "UFS"
            else if (bootDevice.contains("mmc")) type = "eMMC"
            else if (bootDevice.contains("nvme")) type = "NVMe"

            val sdaModelFile = File("/sys/block/sda/device/model")
            val mmcNameFile = File("/sys/block/mmcblk0/device/name")

            if (sdaModelFile.exists() && sdaModelFile.canRead()) {
                modelStr = sdaModelFile.readText().trim()
                if (type == "Unknown Type") {
                    type = if (File("/sys/block/sda/device/rev").exists() || modelStr.contains("UFS", true)) "UFS" else "NVMe"
                }
            } else if (mmcNameFile.exists() && mmcNameFile.canRead()) {
                if (type == "Unknown Type") type = "eMMC"
                modelStr = mmcNameFile.readText().trim()
            }
        } catch (_: Exception) {}

        return Pair(type, modelStr)
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

        val hardwareInfo = getStorageHardwareInfo()

        val df = DecimalFormat("#.#")
        return StorageData(
            total = "${df.format(totalGB)} GB",
            used = "${df.format(usedGB)} GB",
            percent = percent,
            progress = percent / 100f,
            type = hardwareInfo.first,
            model = hardwareInfo.second
        )
    }

    fun getDiskPartitions(): List<PartitionData> {
        val partitions = mutableListOf<PartitionData>()
        val df = DecimalFormat("#.##")
        try {
            val file = File("/proc/mounts")
            if (file.exists() && file.canRead()) {
                val reader = BufferedReader(FileReader(file))
                val seenMounts = mutableSetOf<String>()

                val allowedMounts = listOf(
                    "/", "/system", "/system_ext", "/product", "/vendor", "/vendor_dlkm",
                    "/boot", "/recovery", "/efs", "/persist",
                    "/firmware", "/sbl1", "/sbl2", "/sbl3", "/aboot",
                    "/rpm", "/tz", "/keymaster", "/splash", "/chglogo",
                    "/odm", "/odm_dlkm", "/cache", "/metadata",
                )

                reader.forEachLine { line ->
                    val parts = line.split(" ")
                    if (parts.size >= 3) {
                        val mountPoint = parts[1]
                        val fsType = parts[2]

                        if (allowedMounts.contains(mountPoint) && !seenMounts.contains(mountPoint)) {
                            try {
                                val stat = StatFs(mountPoint)
                                val blockSize = stat.blockSizeLong
                                val totalBlocks = stat.blockCountLong
                                val availableBlocks = stat.availableBlocksLong

                                if (totalBlocks > 0) {
                                    val totalBytes = totalBlocks * blockSize
                                    val availableBytes = availableBlocks * blockSize
                                    val usedBytes = totalBytes - availableBytes

                                    val totalGB = totalBytes.toDouble() / (1024.0.pow(3.0))
                                    val usedGB = usedBytes.toDouble() / (1024.0.pow(3.0))
                                    val percent = ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()

                                    val totalStr = if (totalGB >= 1.0) "${df.format(totalGB)} GB" else "${df.format(totalBytes.toDouble() / (1024.0.pow(2.0)))} MB"
                                    val usedStr = if (usedGB >= 1.0) "${df.format(usedGB)} GB" else "${df.format(usedBytes.toDouble() / (1024.0.pow(2.0)))} MB"

                                    partitions.add(
                                        PartitionData(
                                            mountPoint = mountPoint,
                                            fsType = fsType,
                                            total = totalStr,
                                            used = usedStr,
                                            percent = percent,
                                            progress = percent / 100f
                                        )
                                    )
                                    seenMounts.add(mountPoint)
                                }
                            } catch (_: Exception) { }
                        }
                    }
                }
                reader.close()
            }
        } catch (_: Exception) { }
        return partitions.sortedByDescending { it.percent }
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

                var cycles = if (Build.VERSION.SDK_INT >= 34) intent.getIntExtra("android.os.extra.CYCLE_COUNT", -1) else -1
                if (cycles == -1) cycles = findValueInFiles(listOf("/sys/class/power_supply/battery/cycle_count", "/sys/class/power_supply/battery/battery_cycle")).toIntOrNull() ?: 0

                var cap = findValueInFiles(listOf("/sys/class/power_supply/battery/charge_full_design", "/sys/class/power_supply/battery/batt_capacity_max")).toIntOrNull()?.div(1000) ?: 0
                if (cap <= 0) {
                    try {
                        val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
                        val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(context)
                        cap = (powerProfileClass.getMethod("getBatteryCapacity").invoke(powerProfile) as Double).toInt()
                    } catch (_: Exception) {}
                }

                val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                val voltageStr = String.format(java.util.Locale.US, "%.1f V", if (voltageMv > 100) voltageMv / 1000f else voltageMv.toFloat())

                val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val chargeCounter = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000
                val estimatedCapacity = if (percent > 0) (chargeCounter * 100) / percent else 0

                trySend(
                    BatteryData("$percent%", status, voltageStr, "$temp°C", tech, if (cap > 0) "$cap mAh" else "Unknown", if (cycles > 0) "$cycles" else "—", percent, estimatedCapacity)
                )
            }
        }

        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.flowOn(Dispatchers.IO)

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
        } catch (_: Throwable) {
            false
        }
    }

    fun getBootloaderStatus(): String {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val alias = "SiliconBootloaderCheck"
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }

            val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
            val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                .setAttestationChallenge("silicon_challenge".toByteArray())
                .setDigests(KeyProperties.DIGEST_SHA256)

            keyPairGenerator.initialize(builder.build())
            keyPairGenerator.generateKeyPair()

            val certificateChain = keyStore.getCertificateChain(alias)
            val cert = certificateChain[0] as X509Certificate

            val attestationExtensionOid = "1.3.6.1.4.1.11129.2.1.17"
            val extensionValue = cert.getExtensionValue(attestationExtensionOid)

            if (extensionValue != null) {
                when (parseDeviceLockedFromAsn1(extensionValue)) {
                    true -> "Locked"
                    false -> "Unlocked"
                    null -> "Unknown"
                }
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    data class ThrottlingResult(
        val history: List<Float>,
        val maxPerf: Float,
        val minPerf: Float,
        val avgPerf: Float
    )

    fun runThrottlingTest(): Flow<ThrottlingResult> = flow {
        val coreCount = Runtime.getRuntime().availableProcessors()
        val opsCounter = AtomicLong(0)

        coroutineScope {
            val workers = List(coreCount) {
                launch(Dispatchers.Default) {
                    var dummy = 0.0
                    while (isActive) {
                        for (i in 0..1000) {
                            dummy += kotlin.math.sqrt(i.toDouble())
                        }
                        opsCounter.addAndGet(1000)
                    }
                }
            }

            val history = mutableListOf<Float>()
            var maxPerf = 0f
            var minPerf = Float.MAX_VALUE
            var sumPerf = 0f
            var ticks = 0
            var lastOps = opsCounter.get()

            while (isActive) {
                delay(1000)
                val currentOps = opsCounter.get()
                val delta = currentOps - lastOps
                lastOps = currentOps

                val currentPerf = delta / 1_000_000f
                history.add(currentPerf)
                if (history.size > 60) history.removeAt(0)

                if (currentPerf > maxPerf) maxPerf = currentPerf
                if (currentPerf < minPerf && ticks > 0) minPerf = currentPerf

                ticks++
                sumPerf += currentPerf
                val avgPerf = sumPerf / ticks

                emit(
                    ThrottlingResult(
                        history = history.toList(),
                        maxPerf = maxPerf,
                        minPerf = if (minPerf == Float.MAX_VALUE) 0f else minPerf,
                        avgPerf = avgPerf
                    )
                )
            }
        }
    }

    private fun parseDeviceLockedFromAsn1(extensionValue: ByteArray): Boolean? {
        try {
            val rootOfTrustTag = byteArrayOf(0xBF.toByte(), 0x85.toByte(), 0x40.toByte())
            for (i in 0 until extensionValue.size - 5) {
                if (extensionValue[i] == rootOfTrustTag[0] &&
                    extensionValue[i + 1] == rootOfTrustTag[1] &&
                    extensionValue[i + 2] == rootOfTrustTag[2]
                ) {
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
                        for (j in 0 until numBytes) {
                            keyLen = (keyLen shl 8) or (extensionValue[offset++].toInt() and 0xFF)
                        }
                    }
                    offset += keyLen

                    if (extensionValue[offset++] == 0x01.toByte()) {
                        val boolLen = extensionValue[offset++].toInt() and 0xFF
                        if (boolLen == 1) {
                            return extensionValue[offset].toInt() != 0
                        }
                    }
                }
            }
        } catch (_: Exception) { }
        return null
    }
}