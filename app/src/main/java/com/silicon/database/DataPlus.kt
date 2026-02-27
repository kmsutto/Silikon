package com.silicon.ui.components.database

import com.silicon.ui.components.DeviceManager

// -- codename data
object DataPlus {

    fun getSpecs(codeName: String): List<DeviceManager.CameraLensInfo>? {
        return when (codeName.lowercase()) {
            "dodge", "pjz110", "cph2649", "cph2655", "cph2653" -> oneplus13 // oneplus 13
            "pagani", "cph2723", "pkx110" -> oneplus13T // oneplus 13t

            "pineapple", "cph2581", "pjd110" -> oneplus12 // oneplus 12
            "cayenne", "cph2585", "cph2609", "ace3" -> oneplus12R // oneplus 12r / ace 3

            "salami", "cph2449", "phb110" -> oneplus11 // oneplus 11
            "udon", "cph2487", "phk110" -> oneplus11R // oneplus 11r / ace 2

            "nebulapro", "ne2210", "ne2211", "ne2213", "ne2215" -> oneplus10Pro // oneplus 10 pro
            "ovaltine", "cph2413", "cph2415", "cph2417" -> oneplus10T // oneplus 10t
            "pickle", "cph2411", "cph2423", "pgkm10" -> oneplus10R // oneplus 10r / ace

            "lemonadep", "le2120", "le2121", "le2123", "le2125" -> oneplus9Pro // oneplus 9 pro
            "lemonade", "le2110", "le2111", "le2113", "le2115" -> oneplus9 // oneplus 9
            "martini", "mt2110", "mt2111" -> oneplus9RT // oneplus 9rt
            "lemonades", "le2100", "le2101" -> oneplus9R // oneplus 9r

            "instantnoodlep", "in2020", "in2021", "in2023", "in2025" -> oneplus8Pro // oneplus 8 pro
            "kebab", "kb2000", "kb2001", "kb2003", "kb2005" -> oneplus8T // oneplus 8t
            "instantnoodle", "in2010", "in2011", "in2013", "in2015" -> oneplus8 // oneplus 8

            "hotdog", "hd1910", "hd1911", "hd1913" -> oneplus7TPro // oneplus 7t pro
            "hotdogb", "hd1900", "hd1901", "hd1903", "hd1905" -> oneplus7T // oneplus 7t
            "guacamole", "gm1910", "gm1911", "gm1913", "gm1917" -> oneplus7Pro // oneplus 7 pro
            "guacamoleb", "gm1900", "gm1901", "gm1903" -> oneplus7 // oneplus 7

            "denniz", "dn2101", "dn2103" -> nord2 // nord 2
            "vitamin", "cph2491", "cph2493" -> nord3 // nord 3
            "audi", "cph2611", "cph2619" -> nord4 // nord 4 / ace 3V
            "avicii", "ac2001", "ac2003" -> nord1 // nord

            "ace2pro", "pja110" -> ace2Pro // ace 2 pro

            else -> null
        }
    }

    // -- camera data
    // 1+ 13 series
    private val oneplus13 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.6", "23mm", "8192 x 6144", "1/1.4\"", true), // LYT-808
        DeviceManager.CameraLensInfo("Telephoto", "50 MP", "f/2.6", "73mm", "8192 x 6144", "1/1.95\"", true), // 3x Periscope
        DeviceManager.CameraLensInfo("Ultrawide", "50 MP", "f/2.0", "15mm", "8192 x 6144", "1/2.75\"", false),
        DeviceManager.CameraLensInfo("Front", "32 MP", "f/2.4", "21mm", "6528 x 4896", "1/2.74\"", false)
    )

    private val oneplus13T = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "24mm", "8192 x 6144", "1/1.56\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "50 MP", "f/2.0", "16mm", "4096 х 3072", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.4", "24mm", "4608 x 3456", "1/3.0\"", false)
    )

    // 1+ 12 series
    private val oneplus12 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.6", "23mm", "8192 x 6144", "1/1.4\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "64 MP", "f/2.6", "70mm", "9248 x 6944", "1/2.0\"", true), // 3x Periscope
        DeviceManager.CameraLensInfo("Ultrawide", "48 MP", "f/2.2", "14mm", "8000 x 6000", "1/2.0\"", false),
        DeviceManager.CameraLensInfo("Front", "32 MP", "f/2.4", "21mm", "6528 x 4896", "1/2.74\"", false)
    )

    private val oneplus12R = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "24mm", "8192 x 6144", "1/1.56\"", true), // IMX890
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "16mm", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.4", "24mm", "4608 x 3456", "1/3.0\"", false)
    )

    // 1+ 11 series
    private val oneplus11 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "24mm", "8192 x 6144", "1/1.56\"", true), // IMX890
        DeviceManager.CameraLensInfo("Telephoto", "32 MP", "f/2.0", "48mm", "6528 x 4896", "1/2.74\"", true), // 2x Zoom
        DeviceManager.CameraLensInfo("Ultrawide", "48 MP", "f/2.2", "14mm", "8000 x 6000", "1/2.0\"", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.45", "25mm", "4608 x 3456", "1/3.1\"", false)
    )

    private val oneplus11R = oneplus12R // 1+ 11r & 1+ 12r had the same camera
    private val ace2Pro = oneplus11 // 1+ ace2 pro & 1+ 11 had the same camera

    // 1+ 10 series
    private val oneplus10Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.8", "23mm", "8000 x 6000", "1/1.43\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "8 MP", "f/2.4", "77mm", "3264 x 2448", "1.0µm", true), // 3.3x
        DeviceManager.CameraLensInfo("Ultrawide", "50 MP", "f/2.2", "14mm", "8192 x 6144", "1/2.76\"", false), // 150 deg
        DeviceManager.CameraLensInfo("Front", "32 MP", "f/2.2", "22mm", "6528 x 4896", "1/2.74\"", false)
    )

    private val oneplus10T = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "24mm", "8192 x 6144", "1/1.56\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "16mm", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.4", "24mm", "4608 x 3456", "1/3.0\"", false)
    )

    private val oneplus10R = oneplus10T // 1+ 10r & ace had the same camera

    // 1+ 9 series
    private val oneplus9 = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.8", "23mm", "8000 x 6000", "1/1.43\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "50 MP", "f/2.2", "14mm", "8192 x 6144", "1/1.56\"", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.4", "24mm", "4608 x 3456", "1/3.06\"", false)
    )

    private val oneplus9Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.8", "23mm", "8000 x 6000", "1/1.43\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "8 MP", "f/2.4", "77mm", "3264 x 2448", "1.0µm", true),
        DeviceManager.CameraLensInfo("Ultrawide", "50 MP", "f/2.2", "14mm", "8192 x 6144", "1/1.56\"", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.4", "24mm", "4608 x 3456", "1/3.06\"", false)
    )

    private val oneplus9RT = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "24mm", "8192 x 6144", "1/1.56\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "16 MP", "f/2.2", "14mm", "4608 x 3456", "1/3.6\"", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.4", "24mm", "4608 x 3456", "1/3.06\"", false)
    )

    private val oneplus9R = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.7", "26mm", "8000 x 6000", "1/2.0\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "16 MP", "f/2.2", "14mm", "4608 x 3456", "1/3.6\"", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.4", "24mm", "4608 x 3456", "1/3.06\"", false)
    )

    // 1+ 8 series

    private val oneplus8Pro = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.78", "25mm", "8000 x 6000", "1/1.43\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "8 MP", "f/2.44", "74mm", "3264 x 2448", "1.0µm", true), // 3x Hybrid
        DeviceManager.CameraLensInfo("Ultrawide", "48 MP", "f/2.2", "14mm", "8000 x 6000", "1/2.0\"", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.45", "24mm", "4608 x 3456", "1/3.06\"", false)
    )

    private val oneplus8 = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.75", "25mm", "8000 x 6000", "1/2.0\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "16 MP", "f/2.2", "14mm", "4608 x 3456", "1/3.6\"", false),
        DeviceManager.CameraLensInfo("Macro", "2 MP", "f/2.4", "22mm", "1600 x 1200", "Unknown", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.45", "24mm", "4608 x 3456", "1/3.06\"", false)
    )

    private val oneplus8T = oneplus9R // 1+ 8t & 9r had the same camera

    // 1+ 7 series

    private val oneplus7TPro = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.6", "26mm", "8000 x 6000", "1/2.0\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "8 MP", "f/2.4", "78mm", "3264 x 2448", "1.0µm", true), // 3x
        DeviceManager.CameraLensInfo("Ultrawide", "16 MP", "f/2.2", "13mm", "4608 x 3456", "1/3.6\"", false),
        DeviceManager.CameraLensInfo("Front (Pop-up)", "16 MP", "f/2.0", "25mm", "4608 x 3456", "1/3.1\"", false)
    )

    private val oneplus7T = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.6", "26mm", "8000 x 6000", "1/2.0\"", true),
        DeviceManager.CameraLensInfo("Telephoto", "12 MP", "f/2.2", "51mm", "4000 x 3000", "1.0µm", false), // 2x
        DeviceManager.CameraLensInfo("Ultrawide", "16 MP", "f/2.2", "13mm", "4608 x 3456", "1/3.6\"", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.0", "25mm", "4608 x 3456", "1/3.1\"", false)
    )

    private val oneplus7 = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.7", "26mm", "8000 x 6000", "1/2.0\"", true),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.0", "25mm", "4608 x 3456", "1/3.1\"", false)
    )

    private val oneplus7Pro = oneplus7TPro // 1+ 7pro & 7tpro had the same camera

    // 1+ nord series

    private val nord4 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "26mm", "8192 x 6144", "1/1.95\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "16mm", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.4", "24mm", "4608 x 3456", "1/3.0\"", false)
    )

    private val nord3 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.8", "24mm", "8192 x 6144", "1/1.56\"", true), // IMX890
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.2", "16mm", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Front", "16 MP", "f/2.4", "24mm", "4608 x 3456", "1/3.0\"", false)
    )

    private val nord2 = listOf(
        DeviceManager.CameraLensInfo("Main", "50 MP", "f/1.88", "24mm", "8192 x 6144", "1/1.56\"", true), // IMX766
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.25", "16mm", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Front", "32 MP", "f/2.45", "24mm", "6528 x 4896", "1/2.74\"", false)
    )

    private val nord1 = listOf(
        DeviceManager.CameraLensInfo("Main", "48 MP", "f/1.75", "26mm", "8000 x 6000", "1/2.0\"", true),
        DeviceManager.CameraLensInfo("Ultrawide", "8 MP", "f/2.25", "16mm", "3264 x 2448", "1/4.0\"", false),
        DeviceManager.CameraLensInfo("Front (Main)", "32 MP", "f/2.45", "24mm", "6528 x 4896", "1/2.74\"", false),
        DeviceManager.CameraLensInfo("Front (Wide)", "8 MP", "f/2.45", "14mm", "3264 x 2448", "1/4.0\"", false)
    )
}