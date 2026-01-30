package com.silicon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.silicon.ui.components.AppAnimations
import com.silicon.ui.screens.AndroidScreen
import com.silicon.ui.screens.HardwareScreen
import com.silicon.ui.screens.HomeScreen
import com.silicon.ui.theme.SiliconTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            SiliconTheme(dynamicColor = true) {
                val navController = rememberNavController()
                var currentTab by remember { mutableIntStateOf(0) }

                Scaffold(
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { Text("Silicon", fontWeight = FontWeight.Medium) },
                            navigationIcon = {
                                IconButton(onClick = {}) { Icon(Icons.Default.Menu, contentDescription = "Menu") }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar(tonalElevation = 8.dp) {
                            val items = listOf("Dashboard", "Hardware", "Android")
                            val selectedIcons = listOf(Icons.Filled.Dashboard, Icons.Filled.Memory, Icons.Filled.PhoneAndroid)
                            val unselectedIcons = listOf(Icons.Outlined.Dashboard, Icons.Outlined.Memory, Icons.Outlined.PhoneAndroid)

                            items.forEachIndexed { index, item ->
                                val isSelected = currentTab == index
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            if (isSelected) selectedIcons[index] else unselectedIcons[index],
                                            contentDescription = item
                                        )
                                    },
                                    label = { Text(item) },
                                    selected = isSelected,
                                    onClick = {
                                        if (currentTab != index) {
                                            currentTab = index
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
                ) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = "Dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(
                            "Dashboard",
                            // Используем enterTransition
                            enterTransition = { AppAnimations.enterTransition(0, initialState.destination.route?.toTabIndex() ?: 0) },
                            // Используем exitTransition
                            exitTransition = { AppAnimations.exitTransition(targetState.destination.route?.toTabIndex() ?: 0, 0) }
                        ) { HomeScreen() }

                        composable(
                            "Hardware",
                            enterTransition = { AppAnimations.enterTransition(1, initialState.destination.route?.toTabIndex() ?: 0) },
                            exitTransition = { AppAnimations.exitTransition(targetState.destination.route?.toTabIndex() ?: 0, 1) }
                        ) { HardwareScreen() }

                        composable(
                            "Android",
                            enterTransition = { AppAnimations.enterTransition(2, initialState.destination.route?.toTabIndex() ?: 0) },
                            exitTransition = { AppAnimations.exitTransition(targetState.destination.route?.toTabIndex() ?: 0, 2) }
                        ) { AndroidScreen() }
                    }
                }
            }
        }
    }

    private fun String.toTabIndex(): Int = when(this) {
        "Dashboard" -> 0
        "Hardware" -> 1
        "Android" -> 2
        else -> 0
    }
}