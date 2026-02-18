package com.silicon.ui.components.database

import com.silicon.ui.components.DeviceManager

// -- codename data
object DataPixel {
    fun getSpecs(codeName: String): List<DeviceManager.CameraLensInfo>? {
        return when (codeName.lowercase()) {
            "tokay" -> pixel9 // pixel 9
            "caiman" -> pixel9Pro // pixel 9 pro
            "komodo" -> pixel9Pro // pixel 9 pro xl
            "comet" -> pixel9ProFold // pixel 9 pro fold

            "shiba" -> pixel8 // pixel 8
            "husky" -> pixel8Pro // pixel 8 pro
            "akita" -> pixel8a // pixel 8a

            "panther" -> pixel7 // pixel 7
            "cheetah" -> pixel7Pro // pixel 7 pro
            "lynx" -> pixel7a // pixel 7a

            "oriole" -> pixel6 // pixel 6
            "raven" -> pixel6Pro // pixel 6 pro
            "bluejay" -> pixel6a // pixel 6a

            "redfin" -> pixel5 // pixel 5
            "barbet" -> pixel5a // pixel 5a

            "flame" -> pixel4 // pixel 4
            "coral" -> pixel4 // pixel 4 xl
            "sunfish" -> pixel4a // pixel 4a
            "bramble" -> pixel4a5g // pixel 4a 5g

            "blueline" -> pixel3 // pixel 3
            "crosshatch" -> pixel3 // pixel 3 xl
            "sargo" -> pixel3a // pixel 3a
            "bonito" -> pixel3a // pixel 3a xl

            "felix" -> pixelFold // pixel fold

            else -> null
        }
    }

    // -- camera data
    // pixel 9 series
    private val pixel9 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.68", "25mm", "8160 x 6120", "1/1.31\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "48 MP", "f/1.7", "12mm", "8000 x 6000", "1/2.55\"", false),
        DeviceManager.CameraLensInfo("Front", "10.5 MP", "f/2.2", "20mm", "3840 x 2880", "1/3.1\"", true) // AF included
    )

    private val pixel9Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.68", "25mm", "8160 x 6120", "1/1.31\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "48 MP", "f/2.8", "113mm (5x)", "8000 x 6000", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "48 MP", "f/1.7", "12mm", "8000 x 6000", "1/2.55\"", false),
        DeviceManager.CameraLensInfo("Front", "42 MP", "f/2.2", "17mm", "7680 x 5760", "Unknown", true)
    )

    // pixel 8 series
    private val pixel8 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.68", "25mm", "8160 x 6120", "1/1.31\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "12mm", "4032 x 3024", "1/2.9\"", false),
        DeviceManager.CameraLensInfo("Front", "10.5 MP", "f/2.2", "20mm", "3840 x 2880", "1/3.1\"", false)
    )

    private val pixel8Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.68", "25mm", "8160 x 6120", "1/1.31\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "48 MP", "f/2.8", "113mm (5x)", "8000 x 6000", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "48 MP", "f/1.95", "12mm", "8000 x 6000", "1/2.0\"", false),
        DeviceManager.CameraLensInfo("Front", "10.5 MP", "f/2.2", "20mm", "3840 x 2880", "1/3.1\"", true)
    )

    private val pixel8a = listOf(
        DeviceManager.CameraLensInfo("Main", "64 MP", "f/1.89", "26mm", "9248 x 6936", "1/1.73\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "13 MP", "f/2.2", "14mm", "4160 x 3120", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "13 MP", "f/2.2", "20mm", "4160 x 3120", "Unknown", false)
    )

    // pixel 7 series
    private val pixel7 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.85", "25mm", "8160 x 6120", "1/1.31\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "114˚", "4032 x 3024", "1/2.9\"", false),
        DeviceManager.CameraLensInfo("Front", "10.8 MP", "f/2.2", "21mm", "3840 x 2880", "1/3.1\"", false)
    )

    private val pixel7Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.85", "25mm", "8160 x 6120", "1/1.31\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "48 MP", "f/3.5", "120mm (5x)", "8000 x 6000", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "126˚", "4032 x 3024", "1/2.9\"", false), // Autofocus added
        DeviceManager.CameraLensInfo("Front", "10.8 MP", "f/2.2", "21mm", "3840 x 2880", "1/3.1\"", false)
    )

    private val pixel7a = listOf(
        DeviceManager.CameraLensInfo("Main", "64 MP", "f/1.89", "26mm", "9248 x 6936", "1/1.73\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "13 MP", "f/2.2", "14mm", "4160 x 3120", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "13 MP", "f/2.2", "20mm", "4160 x 3120", "Unknown", false)
    )

    // pixel 6 series
    private val pixel6 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.85", "25mm", "8160 x 6120", "1/1.31\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "16mm", "4032 x 3024", "1/2.9\"", false), // Исправил 114 град на 16mm
        DeviceManager.CameraLensInfo("Front", "8 MP", "f/2.0", "24mm", "3264 x 2448", "Unknown", false)
    )

    private val pixel6Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.85", "25mm", "8160 x 6120", "1/1.31\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "48 MP", "f/3.5", "104mm (4x)", "8000 x 6000", "1/2.0\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "16mm", "4032 x 3024", "1/2.9\"", false),
        DeviceManager.CameraLensInfo("Front", "11.1 MP", "f/2.2", "20mm", "3840 x 2880", "Unknown", false)
    )

    private val pixel6a = listOf(
        DeviceManager.CameraLensInfo("Main", "12.2 MP", "f/1.7", "27mm", "4032 x 3024", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "12 MP", "f/2.2", "16mm", "4032 x 3024", "1/2.9\"", false),
        DeviceManager.CameraLensInfo("Front", "8 MP", "f/2.0", "24mm", "3264 x 2448", "Unknown", false)
    )

    // pixel 5 series
    private val pixel5 = listOf(
        DeviceManager.CameraLensInfo("Main", "12.2 MP", "f/1.7", "27mm", "4032 x 3024", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "16 MP", "f/2.2", "16mm", "4608 x 3456", "1.0µm", false),
        DeviceManager.CameraLensInfo("Front", "8 MP", "f/2.0", "24mm", "3264 x 2448", "1/4.0\"", false)
    )
    private val pixel5a = pixel5 // 5a & 5 had the same camera

    // pixel 4 series
    private val pixel4 = listOf(
        DeviceManager.CameraLensInfo("Main", "12.2 MP", "f/1.7", "27mm", "4032 x 3024", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "16 MP", "f/2.4", "50mm (2x)", "4608 x 3456", "1.0µm", true),
        DeviceManager.CameraLensInfo("Front", "8 MP", "f/2.0", "22mm", "3264 x 2448", "1/4.0\"", false)
    )

    private val pixel4a = listOf(
        DeviceManager.CameraLensInfo("Main", "12.2 MP", "f/1.7", "27mm", "4032 x 3024", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Front", "8 MP", "f/2.0", "24mm", "3264 x 2448", "1/4.0\"", false)
    )
    private val pixel4a5g = pixel5 // 4a & 5G had the same camera

    // pixel 3 series
    private val pixel3 = listOf(
        DeviceManager.CameraLensInfo("Main", "12.2 MP", "f/1.8", "28mm", "4032 x 3024", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Front (Wide)", "8 MP", "f/1.8", "28mm", "3264 x 2448", "Unknown", true), // AF
        DeviceManager.CameraLensInfo("Front (Ultra)", "8 MP", "f/2.2", "19mm", "3264 x 2448", "Unknown", false)
    )

    private val pixel3a = listOf(
        DeviceManager.CameraLensInfo("Main", "12.2 MP", "f/1.8", "28mm", "4032 x 3024", "1/2.55\"", true),
        DeviceManager.CameraLensInfo("Front", "8 MP", "f/2.0", "24mm", "3264 x 2448", "Unknown", false)
    )

    // pixel fold series

    private val pixelFold = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.7", "25mm", "8000 x 6000", "1/2.0\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "10.8 MP", "f/3.05", "112mm (5x)", "4000 x 3000", "1/3.1\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "10.8 MP", "f/2.2", "12mm", "4000 x 3000", "1/3.0\"", false),
        DeviceManager.CameraLensInfo("Front (Cover)", "9.5 MP", "f/2.2", "24mm", "3552 x 2664", "Unknown", false),
        DeviceManager.CameraLensInfo("Front (Inner)", "8 MP", "f/2.0", "24mm", "3264 x 2448", "Unknown", false)
    )

    private val pixel9ProFold = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.7", "25mm", "8000 x 6000", "1/2.0\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "10.8 MP", "f/3.1", "112mm (5x)", "4000 x 3000", "1/3.2\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "10.5 MP", "f/2.2", "12mm", "4032 x 3024", "1/3.4\"", false),
        DeviceManager.CameraLensInfo("Front (Cover)", "10 MP", "f/2.2", "23mm", "3840 x 2880", "Unknown", false),
        DeviceManager.CameraLensInfo("Front (Inner)", "10 MP", "f/2.2", "23mm", "3840 x 2880", "Unknown", false)
    )
}