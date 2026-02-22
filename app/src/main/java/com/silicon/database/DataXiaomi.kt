package com.silicon.ui.components.database

import com.silicon.ui.components.DeviceManager

// -- codename data
object DataXiaomi {

    fun getSpecs(codeName: String): List<DeviceManager.CameraLensInfo>? {
        return when (codeName.lowercase()) {
            "land" -> redmi3s // redmi 3s
            "rolex" -> redmi4a // redmi 4a
            "santoni" -> redmi4x // redmi 4x
            "mido" -> redmiNote4 // redmi note 4 / 4x
            "whyred" -> redmiNote5Pro // redmi note 5 pro
            "lavender" -> redmiNote7 // redmi note 7
            "ginkgo", "willow" -> redmiNote8 // redmi note 8 / 8t
            "merlin" -> redmiNote9 // redmi note 9
            "joyeuse", "curtana", "excalibur", "gram" -> redmiNote9Pro // redmi note 9 pro
            "mojito", "sunny" -> redmiNote10 // redmi note 10
            "rosemary", "secret" -> redmiNote10s // redmi note 10s
            "sweet", "sweetin" -> redmiNote10Pro // redmi note 10 pro
            "spes", "spesn" -> redmiNote11 // redmi note 11

            "beryllium" -> pocoF1 // poco f1
            "alioth", "aliothin" -> pocoF3 // poco f3
            "munch", "munchin" -> pocoF4 // poco f4
            "vayu", "bhima" -> pocoX3Pro // poco x3 pro
            "surya", "karna" -> pocoX3Nfc // poco x3 nfc
            "veux", "peux" -> pocoX4Pro // poco x4 pro 5g

            "sagit" -> mi6 // mi 6
            "dipper" -> mi8 // mi 8
            "davinci", "davinciin" -> mi9t // mi 9t
            "umi" -> mi10 // mi 10
            "cmi" -> mi10Pro // mi 10 pro
            "apollo" -> mi10t // mi 10t / pro
            "lisa" -> mi11Lite5GNE // mi 11 lite 5g ne
            "renoir" -> mi11Lite5G // mi 11 lite 5g
            "venus" -> mi11 // mi 11
            "star", "mars" -> mi11Ultra // mi 11 ultra
            "haydn", "haydnin" -> mi11i // mi 11i
            "cupid" -> xiaomi12 // xiaomi 12
            "zeus" -> xiaomi12Pro // xiaomi 12 pro
            "psyche" -> xiaomi12x // xiaomi 12x

            else -> null
        }
    }

    // -- camera data
    // redmi series
    private val redmi3s = listOf(
        DeviceManager.CameraLensInfo("Main", "13 MP", "f/2.0", "Unknown", "4160 x 3120", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "5 MP", "f/2.2", "Unknown", "2560 x 1920", "Unknown", false)
    )

    private val redmi4a = listOf(
        DeviceManager.CameraLensInfo("Main", "13 MP", "f/2.2", "Unknown", "4160 x 3120", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "5 MP", "f/2.2", "Unknown", "2560 x 1920", "Unknown", false)
    )

    private val redmi4x = listOf(
        DeviceManager.CameraLensInfo("Main", "13 MP", "f/2.0", "Unknown", "4160 x 3120", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "5 MP", "f/2.2", "Unknown", "2560 x 1920", "Unknown", false)
    )

    private val redmiNote4 = listOf(
        DeviceManager.CameraLensInfo("Main", "13 MP", "f/2.0", "Unknown", "4160 x 3120", "1/3.1\"", false),
        DeviceManager.CameraLensInfo("Front", "5 MP", "f/2.0", "Unknown", "2560 x 1920", "Unknown", false)
    )

    private val redmiNote5Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/2.2", "Unknown", "4000 x 3000", "1/2.9\"", false),
        DeviceManager.CameraLensInfo("Depth", "5 MP", "f/2.0", "Unknown", "2560 x 1920", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.2", "Unknown", "5120 x 3840", "1/2.8\"", false)
    )

    private val redmiNote7 = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.8", "Unknown", "8000 x 6000", "1/2.0\"", false),
        DeviceManager.CameraLensInfo("Depth", "5 MP", "f/2.2", "Unknown", "2560 x 1920", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "13 MP", "f/2.0", "Unknown", "4160 x 3120", "1/3.1\"", false)
    )

    private val redmiNote8 = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.8", "26mm", "8000 x 6000", "1/2.0\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "13mm", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Macro", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Depth", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Front", "13 MP", "f/2.0", "Unknown", "4160 x 3120", "1/3.1\"", false)
    )

    private val redmiNote9 = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.79", "26mm", "8000 x 6000", "1/2.0\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "118˚", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Macro", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "1.75µm", false),
        DeviceManager.CameraLensInfo("Depth", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Front", "13 MP", "f/2.25", "29mm", "4160 x 3120", "1/3.06\"", false)
    )

    private val redmiNote9Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "64 MP", "f/1.89", "26mm", "9248 x 6936", "1/1.72\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "119˚", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Macro", "5 MP", "f/2.4", "Unknown", "2592 x 1944", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Depth", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.5", "Unknown", "4608 x 3456", "1/3.06\"", false)
    )

    private val redmiNote10 = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.79", "26mm", "8000 x 6000", "1/2.0\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "118˚", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Macro", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Depth", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "13 MP", "f/2.5", "Unknown", "4160 x 3120", "1/3.06\"", false)
    )

    private val redmiNote10s = listOf(
        DeviceManager.CameraLensInfo("Main", "64 MP", "f/1.8", "26mm", "9248 x 6936", "1/1.97\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "118˚", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Macro", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Depth", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "13 MP", "f/2.5", "Unknown", "4160 x 3120", "1/3.06\"", false)
    )

    private val redmiNote10Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "108 MP", "f/1.9", "26mm", "12000 x 9000", "1/1.52\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "118˚", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Macro", "5 MP", "f/2.4", "50mm", "2592 x 1944", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Depth", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.5", "Unknown", "4608 x 3456", "1/3.06\"", false)
    )

    private val redmiNote11 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "26mm", "8192 x 6144", "1/2.76\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "118˚", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Macro", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Depth", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "13 MP", "f/2.4", "Unknown", "4160 x 3120", "1/3.1\"", false)
    )

    // poco series
    private val pocoF1 = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.9", "Unknown", "4032 x 3024", "1/2.55\"", false),
        DeviceManager.CameraLensInfo("Depth", "5 MP", "f/2.0", "Unknown", "2560 x 1920", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.0", "Unknown", "5120 x 3840", "1/2.8\"", false)
    )

    private val pocoF3 = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.79", "26mm", "8000 x 6000", "1/2.0\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "119˚", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Macro", "5 MP", "f/2.4", "50mm", "2592 x 1944", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.5", "Unknown", "5120 x 3840", "1/3.4\"", false)
    )

    private val pocoF4 = listOf(
        DeviceManager.CameraLensInfo("Main", "64 MP", "f/1.8", "Unknown", "9248 x 6936", "1/2.0\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "119˚", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Macro", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.5", "Unknown", "5120 x 3840", "1/3.06\"", false)
    )

    private val pocoX3Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.79", "Unknown", "8000 x 6000", "1/2.0\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "119˚", "3264 x 2448", "Unknown", false),
        DeviceManager.CameraLensInfo("Macro", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Depth", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.2", "Unknown", "5120 x 3840", "1/3.4\"", false)
    )

    private val pocoX3Nfc = listOf(
        DeviceManager.CameraLensInfo("Main", "64 MP", "f/1.89", "Unknown", "9248 x 6936", "1/1.73\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "13 MP", "f/2.2", "119˚", "4160 x 3120", "1/3.06\"", false),
        DeviceManager.CameraLensInfo("Macro", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Depth", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.2", "Unknown", "5120 x 3840", "1/3.4\"", false)
    )

    private val pocoX4Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "108 MP", "f/1.9", "26mm", "12000 x 9000", "1/1.52\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "118˚", "3264 x 2448", "Unknown", false),
        DeviceManager.CameraLensInfo("Macro", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.5", "Unknown", "4608 x 3456", "1/3.06\"", false)
    )

    // mi series
    private val mi6 = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.8", "27mm", "4032 x 3024", "1/2.9\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "12 MP", "f/2.6", "52mm", "4032 x 3024", "1/2.9\"", false),
        DeviceManager.CameraLensInfo("Front", "8 MP", "Unknown", "Unknown", "3264 x 2448", "Unknown", false)
    )

    private val mi8 = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.8", "Unknown", "4032 x 3024", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "12 MP", "f/2.4", "Unknown", "4032 x 3024", "1/3.4\"", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.0", "Unknown", "5120 x 3840", "1/3.0\"", false)
    )

    private val mi9t = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.75", "26mm", "8000 x 6000", "1/2.0\"", false),
        DeviceManager.CameraLensInfo("Telephoto", "8 MP", "f/2.4", "53mm", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "13 MP", "f/2.4", "12mm", "4160 x 3120", "1/3.1\"", false),
        DeviceManager.CameraLensInfo("Front (Pop-up)", "20 MP", "f/2.2", "Unknown", "5120 x 3840", "1/3.4\"", false)
    )

    private val mi10 = listOf(
        DeviceManager.CameraLensInfo("Main", "108 MP", "f/1.7", "Unknown", "12000 x 9000", "1/1.33\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "13 MP", "f/2.4", "12mm", "4160 x 3120", "Unknown", false),
        DeviceManager.CameraLensInfo("Macro", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Depth", "2 MP", "f/2.4", "Unknown", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.0", "Unknown", "5120 x 3840", "1/3.0\"", false)
    )

    private val mi10t = listOf(
        DeviceManager.CameraLensInfo("Main", "64 MP", "f/1.9", "26mm", "9248 x 6936", "1/1.73\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "13 MP", "f/2.4", "123˚", "4160 x 3120", "1/3.06\"", false),
        DeviceManager.CameraLensInfo("Macro", "5 MP", "f/2.4", "Unknown", "2592 x 1944", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.2", "27mm", "5120 x 3840", "1/3.4\"", false)
    )

    private val mi10Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "108 MP", "f/1.69", "25mm", "12000 x 9000", "1/1.33\"", true),
        DeviceManager.CameraLensInfo("Telephoto (Short)", "12 MP", "f/2.0", "50mm", "4000 x 3000", "1/2.55\"", false),
        DeviceManager.CameraLensInfo("Telephoto (Long)", "8 MP", "f/2.0", "94mm", "3264 x 2448", "1/4.0\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "20 MP", "f/2.2", "15mm", "5184 x 3880", "1/2.8\"", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.0", "Unknown", "5120 x 3840", "1/3.0\"", false)
    )

    private val mi11 = listOf(
        DeviceManager.CameraLensInfo("Main", "108 MP", "f/1.85", "26mm", "12000 x 9000", "1/1.33\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "13 MP", "f/2.4", "123˚", "4160 x 3120", "1/3.06\"", false),
        DeviceManager.CameraLensInfo("Macro", "5 MP", "f/2.4", "50mm", "2592 x 1944", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.2", "27mm", "5120 x 3840", "1/3.4\"", false)
    )

    private val mi11i = listOf(
        DeviceManager.CameraLensInfo("Main", "108 MP", "f/1.75", "26mm", "12000 x 9000", "1/1.52\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "119˚", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Macro", "5 MP", "f/2.4", "50mm", "2592 x 1944", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.5", "Unknown", "5120 x 3840", "1/3.4\"", false)
    )

    private val mi11Lite5G = listOf(
        DeviceManager.CameraLensInfo("Main", "64 MP", "f/1.79", "26mm", "9248 x 6936", "1/1.97\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "119˚", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Macro", "5 MP", "f/2.4", "Unknown", "2592 x 1944", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.2", "27mm", "5120 x 3840", "1/3.4\"", false)
    )

    private val mi11Lite5GNE = listOf(
        DeviceManager.CameraLensInfo("Main", "64 MP", "f/1.79", "26mm", "9248 x 6936", "1/1.97\"", false),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "119˚", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Macro", "5 MP", "f/2.4", "50mm", "2592 x 1944", "1/5.0\"", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.2", "27mm", "5120 x 3840", "1/3.4\"", false)
    )

    private val mi11Ultra = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.95", "24mm", "8192 x 6144", "1/1.12\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "48 MP", "f/4.1", "120mm", "8000 x 6000", "1/2.0\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "48 MP", "f/2.2", "12mm", "8000 x 6000", "1/2.0\"", false),
        DeviceManager.CameraLensInfo("Front", "20 MP", "f/2.2", "27mm", "5120 x 3840", "1/3.4\"", false)
    )

    private val xiaomi12 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.88", "26mm", "8192 x 6144", "1/1.56\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "13 MP", "f/2.4", "12mm", "4160 x 3120", "1/3.06\"", false),
        DeviceManager.CameraLensInfo("Macro", "5 MP", "f/2.4", "50mm", "2592 x 1944", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "32 MP", "f/2.45", "26mm", "6528 x 4896", "Unknown", false)
    )

    private val xiaomi12Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.9", "24mm", "8192 x 6144", "1/1.28\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "50 MP", "f/1.9", "48mm", "8192 x 6144", "Unknown", false),
        DeviceManager.CameraLensInfo("Ultrawide", "50 MP", "f/2.2", "115˚", "8192 x 6144", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "32 MP", "f/2.45", "26mm", "6528 x 4896", "Unknown", false)
    )

    private val xiaomi12x = xiaomi12 // 12X & 12 had the same camera
}