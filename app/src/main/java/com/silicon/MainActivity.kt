package com.silicon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.*
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
import androidx.navigation.compose.rememberNavController
import com.silicon.ui.components.AppAnimations
import com.silicon.ui.screens.AndroidScreen
import com.silicon.ui.screens.HardwareScreen
import com.silicon.ui.screens.HomeScreen
import com.silicon.ui.theme.SiliconTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            SiliconTheme(dynamicColor = true) {
                val navController = rememberNavController()
                var currentTab by remember { mutableIntStateOf(0) }

                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                val haptic = LocalHapticFeedback.current

                Scaffold(
                    modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        LargeTopAppBar(
                            title = {
                                Text(
                                    "Silicon",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            scrollBehavior = scrollBehavior,
                            colors = TopAppBarDefaults.largeTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                scrolledContainerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.background,
                            tonalElevation = 0.dp
                        ) {
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
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

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
                            enterTransition = { AppAnimations.enterTransition(0, initialState.destination.route?.toTabIndex() ?: 0) },
                            exitTransition = { AppAnimations.exitTransition(targetState.destination.route?.toTabIndex() ?: 0, 0) }
                        ) {
                            HomeScreen(paddingValues = PaddingValues(0.dp))
                        }

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