package com.silicon.ui.components.database

import com.silicon.ui.components.DeviceManager

// -- codename data
object DataSamsung {

    fun getSpecs(codeName: String): List<DeviceManager.CameraLensInfo>? {
        return when (codeName.lowercase()) {
            "herolte", "g930", "g930f" -> s7 // s7
            "hero2lte", "g935", "g935f" -> s7 // s7e

            "dreamlte", "g950", "g950f" -> s8 // s8
            "dream2lte", "g955", "g955f" -> s8 // s8+

            "starlte", "g960", "g960f" -> s9 // s9
            "star2lte", "g965", "g965f" -> s9Plus // s9+

            "beyond0", "beyond0lte", "g970", "g970f" -> s10e // s10e
            "beyond1", "beyond1lte", "g973", "g973f" -> s10 // s10
            "beyond2", "beyond2lte", "g975", "g975f" -> s10 // s10+

            "x1", "x1s", "x1q", "g980", "g981" -> s20 // s20
            "y2", "y2s", "y2q", "g985", "g986" -> s20 // s20+
            "z3", "z3s", "z3q", "g988" -> s20Ultra // s20 ultra
            "r8q", "g780", "g781" -> s20FE // s20 fe

            "o1", "o1s", "o1q", "g991" -> s21 // s21
            "t2", "t2s", "t2q", "g996" -> s21 // s21+
            "p3", "p3s", "p3q", "g998" -> s21Ultra // s21 ultra
            "r9q", "r9q2", "r9s", "g990" -> s21FE // s21 fe

            "r0", "r0q", "s901" -> s22 // s22
            "g0", "g0q", "s906" -> s22 // s22+
            "b0", "b0q", "s908" -> s22Ultra // s22 ultra

            "dm1q", "s911" -> s23 // s23
            "dm2q", "s916" -> s23 // s23+
            "dm3q", "s918" -> s23Ultra // s23 ultra
            "r11s", "s711" -> s23FE // s23 fe

            "e1s", "e1q", "s921" -> s24 // s24
            "e2s", "e2q", "s926" -> s24 // s24+
            "e3s", "e3q", "s928" -> s24Ultra // s24 ultra
            "r12s", "s721" -> s24FE // s24 fe

            else -> null
        }
    }

    // -- camera data

    // s7 series
    private val s7 = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.7", "26mm", "4032 x 3024", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Front", "5 MP", "f/1.7", "22mm", "2560 x 1440", "1/4.1\"", false)
    )

    // s8 series
    private val s8 = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.7", "26mm", "4032 x 3024", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Front", "8 MP", "f/1.7", "25mm", "3264 x 2448", "1/3.6\"", true) // AF included
    )

    // s9 series
    private val s9 = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.5-2.4", "26mm", "4032 x 3024", "1/2.55\"", true), // Variable aperture
        DeviceManager.CameraLensInfo("Front", "8 MP", "f/1.7", "25mm", "3264 x 2448", "1/3.6\"", true)
    )

    private val s9Plus = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.5-2.4", "26mm", "4032 x 3024", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "12 MP", "f/2.4", "52mm (2x)", "4032 x 3024", "1/3.6\"", true),
        DeviceManager.CameraLensInfo("Front", "8 MP", "f/1.7", "25mm", "3264 x 2448", "1/3.6\"", true)
    )

    // s10 series
    private val s10e = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.5-2.4", "26mm", "4032 x 3024", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "16 MP", "f/2.2", "12mm", "4608 x 3456", "1/3.1\"", false),
        DeviceManager.CameraLensInfo("Front", "10 MP", "f/1.9", "26mm", "3840 x 2160", "1/3.0\"", true)
    )

    private val s10 = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.5-2.4", "26mm", "4032 x 3024", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "12 MP", "f/2.4", "52mm (2x)", "4032 x 3024", "1/3.6\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "16 MP", "f/2.2", "12mm", "4608 x 3456", "1/3.1\"", false),
        DeviceManager.CameraLensInfo("Front", "10 MP", "f/1.9", "26mm", "3840 x 2160", "1/3.0\"", true)
    )

    // s20 series
    private val s20 = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.8", "26mm", "4000 x 3000", "1/1.76\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "64 MP", "f/2.0", "29mm (3x Hybrid)", "9248 x 6936", "1/1.72\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/2.55\"", false),
        DeviceManager.CameraLensInfo("Front", "10 MP", "f/2.2", "26mm", "3840 x 2160", "1/3.2\"", true)
    )

    private val s20Ultra = listOf(
        DeviceManager.CameraLensInfo("Main", "108 MP", "f/1.8", "26mm", "12000 x 9000", "1/1.33\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "48 MP", "f/3.5", "103mm (4x)", "8000 x 6000", "1/2.0\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/2.55\"", false),
        DeviceManager.CameraLensInfo("Front", "40 MP", "f/2.2", "26mm", "7296 x 5472", "1/2.8\"", true)
    )

    private val s20FE = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.8", "26mm", "4000 x 3000", "1/1.76\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "8 MP", "f/2.4", "76mm (3x)", "3264 x 2448", "1/4.5\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/3.0\"", false),
        DeviceManager.CameraLensInfo("Front", "32 MP", "f/2.2", "26mm", "6528 x 4896", "1/2.74\"", false)
    )

    // s21 series
    private val s21 = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.8", "26mm", "4000 x 3000", "1/1.76\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "64 MP", "f/2.0", "29mm (3x Hybrid)", "9248 x 6936", "1/1.72\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/2.55\"", false),
        DeviceManager.CameraLensInfo("Front", "10 MP", "f/2.2", "26mm", "3840 x 2160", "1/3.24\"", true)
    )

    private val s21Ultra = listOf(
        DeviceManager.CameraLensInfo("Main", "108 MP", "f/1.8", "24mm", "12000 x 9000", "1/1.33\"", true),
        DeviceManager.CameraLensInfo("Telephoto (Short)", "10 MP", "f/2.4", "72mm (3x)", "3840 x 2160", "1/3.24\"", true),
        DeviceManager.CameraLensInfo("Telephoto (Long)", "10 MP", "f/4.9", "240mm (10x)", "3840 x 2160", "1/3.24\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Front", "40 MP", "f/2.2", "26mm", "7296 x 5472", "1/2.8\"", true)
    )

    private val s21FE = listOf(
        DeviceManager.CameraLensInfo("Main", "12 MP", "f/1.8", "26mm", "4000 x 3000", "1/1.76\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "8 MP", "f/2.4", "76mm (3x)", "3264 x 2448", "1/4.5\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/3.0\"", false),
        DeviceManager.CameraLensInfo("Front", "32 MP", "f/2.2", "26mm", "6528 x 4896", "1/2.74\"", false)
    )

    // s22 series
    private val s22 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "23mm", "8192 x 6144", "1/1.56\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "10 MP", "f/2.4", "70mm (3x)", "3840 x 2160", "1/3.94\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/2.55\"", false),
        DeviceManager.CameraLensInfo("Front", "10 MP", "f/2.2", "26mm", "3840 x 2160", "1/3.24\"", true)
    )

    private val s22Ultra = listOf(
        DeviceManager.CameraLensInfo("Main", "108 MP", "f/1.8", "23mm", "12000 x 9000", "1/1.33\"", true),
        DeviceManager.CameraLensInfo("Telephoto (Short)", "10 MP", "f/2.4", "70mm (3x)", "3840 x 2160", "1/3.52\"", true),
        DeviceManager.CameraLensInfo("Telephoto (Long)", "10 MP", "f/4.9", "230mm (10x)", "3840 x 2160", "1/3.52\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Front", "40 MP", "f/2.2", "26mm", "7296 x 5472", "1/2.82\"", true)
    )

    // s23 series
    private val s23 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "24mm", "8192 x 6144", "1/1.56\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "10 MP", "f/2.4", "70mm (3x)", "3840 x 2160", "1/3.94\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/2.55\"", false),
        DeviceManager.CameraLensInfo("Front", "12 MP", "f/2.2", "26mm", "4000 x 3000", "1/3.24\"", true)
    )

    private val s23Ultra = listOf(
        DeviceManager.CameraLensInfo("Main", "200 MP", "f/1.7", "24mm", "16320 x 12240", "1/1.3\"", true),
        DeviceManager.CameraLensInfo("Telephoto (Short)", "10 MP", "f/2.4", "70mm (3x)", "3840 x 2160", "1/3.52\"", true),
        DeviceManager.CameraLensInfo("Telephoto (Long)", "10 MP", "f/4.9", "230mm (10x)", "3840 x 2160", "1/3.52\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Front", "12 MP", "f/2.2", "26mm", "4000 x 3000", "1/3.24\"", true)
    )

    private val s23FE = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "24mm", "8192 x 6144", "1/1.56\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "8 MP", "f/2.4", "75mm (3x)", "3264 x 2448", "1/4.4\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/3.0\"", false),
        DeviceManager.CameraLensInfo("Front", "10 MP", "f/2.4", "26mm", "3840 x 2160", "1/3.0\"", false)
    )

    // s24 series
    private val s24 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "24mm", "8192 x 6144", "1/1.56\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "10 MP", "f/2.4", "67mm (3x)", "3840 x 2160", "1/3.94\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/2.55\"", false),
        DeviceManager.CameraLensInfo("Front", "12 MP", "f/2.2", "26mm", "4000 x 3000", "1/3.24\"", true)
    )

    private val s24Ultra = listOf(
        DeviceManager.CameraLensInfo("Main", "200 MP", "f/1.7", "24mm", "16320 x 12240", "1/1.3\"", true),
        DeviceManager.CameraLensInfo("Telephoto (Short)", "10 MP", "f/2.4", "67mm (3x)", "3840 x 2160", "1/3.52\"", true),
        DeviceManager.CameraLensInfo("Telephoto (Long)", "50 MP", "f/3.4", "111mm (5x)", "8192 x 6144", "1/2.52\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Front", "12 MP", "f/2.2", "26mm", "4000 x 3000", "1/3.24\"", true)
    )

    private val s24FE = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "24mm", "8192 x 6144", "1/1.56\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "8 MP", "f/2.4", "75mm (3x)", "3264 x 2448", "1/4.4\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "13mm", "4000 x 3000", "1/3.0\"", false),
        DeviceManager.CameraLensInfo("Front", "10 MP", "f/2.4", "26mm", "3840 x 2160", "1/3.0\"", false)
    )
}