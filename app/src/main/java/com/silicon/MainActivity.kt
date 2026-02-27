package com.silicon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.silicon.ui.components.AppAnimations
import com.silicon.ui.screens.AndroidScreen
import com.silicon.ui.screens.AboutScreen
import com.silicon.ui.screens.HardwareScreen
import com.silicon.ui.screens.HomeScreen
import com.silicon.ui.theme.SiliconTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            SiliconTheme(dynamicColor = true) {
                val windowSizeClass = calculateWindowSizeClass(this)
                val isWideScreen = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "Dashboard"
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                val haptic = LocalHapticFeedback.current

                val items = listOf("Dashboard", "Hardware", "Android")
                val selectedIcons = listOf(Icons.Filled.Dashboard, Icons.Filled.Memory, Icons.Filled.PhoneAndroid)
                val unselectedIcons = listOf(Icons.Outlined.Dashboard, Icons.Outlined.Memory, Icons.Outlined.PhoneAndroid)

                Row(modifier = Modifier.fillMaxSize()) {
                    if (isWideScreen && currentRoute != "About") {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.background,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(modifier = Modifier.weight(1f))

                            items.forEachIndexed { index, item ->
                                val isSelected = currentRoute == item
                                NavigationRailItem(
                                    icon = { Icon(if (isSelected) selectedIcons[index] else unselectedIcons[index], contentDescription = item) },
                                    label = { Text(item) },
                                    selected = isSelected,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (currentRoute != item) {
                                            navController.navigate(item) {
                                                popUpTo("Dashboard") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    Scaffold(
                        modifier = Modifier.weight(1f).nestedScroll(scrollBehavior.nestedScrollConnection),
                        containerColor = MaterialTheme.colorScheme.background,
                        topBar = {
                            LargeTopAppBar(
                                title = { Text(if (currentRoute == "About") "About" else "Silicon", fontWeight = FontWeight.SemiBold) },
                                navigationIcon = {
                                    if (currentRoute == "About") {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                        }
                                    }
                                },
                                actions = {
                                    if (currentRoute != "About") {
                                        IconButton(onClick = { navController.navigate("About") }) {
                                            Icon(Icons.Outlined.Info, contentDescription = "About")
                                        }
                                    }
                                },
                                scrollBehavior = scrollBehavior,
                                colors = TopAppBarDefaults.largeTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background,
                                    scrolledContainerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        },
                        bottomBar = {
                            if (!isWideScreen && currentRoute != "About") {
                                NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
                                    items.forEachIndexed { index, item ->
                                        val isSelected = currentRoute == item
                                        NavigationBarItem(
                                            icon = { Icon(if (isSelected) selectedIcons[index] else unselectedIcons[index], contentDescription = item) },
                                            label = { Text(item) },
                                            selected = isSelected,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                if (currentRoute != item) {
                                                    navController.navigate(item) {
                                                        popUpTo("Dashboard") { saveState = true }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "Dashboard",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable(
                                "Dashboard",
                                enterTransition = { AppAnimations.enterTransition(0, initialState.destination.route?.toTabIndex() ?: 0) },
                                exitTransition = { AppAnimations.exitTransition(targetState.destination.route?.toTabIndex() ?: 0, 0) }
                            ) { HomeScreen(paddingValues = PaddingValues(0.dp), isWideScreen = isWideScreen) }

                            composable(
                                "Hardware",
                                enterTransition = { AppAnimations.enterTransition(1, initialState.destination.route?.toTabIndex() ?: 0) },
                                exitTransition = { AppAnimations.exitTransition(targetState.destination.route?.toTabIndex() ?: 0, 1) }
                            ) { HardwareScreen(isWideScreen = isWideScreen) }

                            composable(
                                "Android",
                                enterTransition = { AppAnimations.enterTransition(2, initialState.destination.route?.toTabIndex() ?: 0) },
                                exitTransition = { AppAnimations.exitTransition(targetState.destination.route?.toTabIndex() ?: 0, 2) }
                            ) { AndroidScreen(isWideScreen = isWideScreen) }

                            composable(
                                "About",
                                enterTransition = { AppAnimations.enterTransition(3, initialState.destination.route?.toTabIndex() ?: 0) },
                                exitTransition = { AppAnimations.exitTransition(targetState.destination.route?.toTabIndex() ?: 0, 3) }
                            ) { AboutScreen() }
                        }
                    }
                }
            }
        }
    }

    private fun String.toTabIndex(): Int = when(this) {
        "Dashboard" -> 0
        "Hardware" -> 1
        "Android" -> 2
        "About" -> 3
        else -> 0
    }
}