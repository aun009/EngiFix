package com.example.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.data.preferences.AppSettingsRepository
import com.example.auth.presentation.authentication.AskFirstName
import com.example.auth.presentation.authentication.AuthViewModel
import com.example.auth.presentation.authentication.FirstScreen
import com.example.auth.presentation.authentication.LoginScreen
import com.example.auth.presentation.authentication.RegisterScreen
import com.example.auth.presentation.authentication.UserNameAndPassScreen
import com.example.auth.presentation.features.contest.ContestNotificationService
import com.example.auth.presentation.features.contest.ContestScreen
import com.example.auth.presentation.features.aptitude.AptitudeGameScreen
import com.example.auth.presentation.features.jobs.JobsScreen
import com.example.auth.presentation.features.sheet.ExploreSheetScreen
import com.example.auth.presentation.features.sheet.SheetDetailScreen
import com.example.auth.presentation.inApp.homescreen.HomeScreen
import com.example.auth.presentation.inApp.profilescreen.ProfileScreen
import com.example.auth.presentation.inApp.profilescreen.SettingsScreen
import com.example.auth.presentation.animation.EngiFixIntroOverlay
import com.example.auth.presentation.animation.rememberMotionPolicy
import com.example.auth.presentation.inApp.profilescreen.ConnectScreen
import com.example.auth.presentation.features.referral.AlumniReferralScreen
import com.example.auth.ui.theme.AuthTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// ─── Bottom nav tab definition ────────────────────────────────────────────────

private data class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab("home",         "Home",    Icons.Filled.Home,                    Icons.Outlined.Home),
    BottomTab("sheets",       "Sheets",  Icons.AutoMirrored.Filled.MenuBook,   Icons.AutoMirrored.Outlined.MenuBook),
    BottomTab("profile",      "Profile", Icons.Filled.Person,                  Icons.Outlined.Person),
)

// Routes where the bottom bar should be HIDDEN (detail / flow screens)
private val routesWithoutBottomBar = setOf(
    "first_screen", "register_screen", "login_screen",
    "ask_name_screen", "username_pass_screen",
    "sheet_detail/{sheetId}",
    "settings",
    "ui",          // full-screen from home cards, keep bar hidden for now
    "alumni_referral"
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        startContestNotificationService()

        setContent {
            val appSettingsRepository = remember {
                AppSettingsRepository(applicationContext)
            }
            val isDarkTheme by appSettingsRepository.isDarkTheme.collectAsState(initial = false)
            val settingsScope = rememberCoroutineScope()

            AuthTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Show bottom bar only for the 4 main tabs
                val showBottomBar = currentRoute in bottomTabs.map { it.route }

                Box(Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            if (showBottomBar) {
                                AppBottomNav(
                                    currentRoute = currentRoute,
                                    onTabSelected = { route ->
                                        if (route != currentRoute) {
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                        ) {
                            val startDest = remember {
                                if (authViewModel.currentUser != null) "home" else "first_screen"
                            }
                            NavHost(
                                navController = navController,
                                startDestination = startDest,
                                enterTransition = {
                                    slideInHorizontally(initialOffsetX = { it }) + fadeIn(animationSpec = tween(300))
                                },
                                exitTransition = {
                                    slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(animationSpec = tween(300))
                                },
                                popEnterTransition = {
                                    slideInHorizontally(initialOffsetX = { -it }) + fadeIn(animationSpec = tween(300))
                                },
                                popExitTransition = {
                                    slideOutHorizontally(targetOffsetX = { it }) + fadeOut(animationSpec = tween(300))
                                }
                            ) {
                                // ── Auth flow ──────────────────────────────
                                composable("first_screen") {
                                    FirstScreen(navController)
                                }
                                composable("register_screen") {
                                    RegisterScreen(navController, authViewModel)
                                }
                                composable("login_screen") {
                                    LoginScreen(navController, authViewModel)
                                }
                                composable("ask_name_screen") {
                                    AskFirstName(navController, authViewModel)
                                }
                                composable("username_pass_screen") {
                                    UserNameAndPassScreen(navController, authViewModel)
                                }

                                // ── Main tabs (bottom bar visible) ─────────
                                composable(
                                    route = "home",
                                    enterTransition = { EnterTransition.None },
                                    exitTransition = { ExitTransition.None },
                                    popEnterTransition = { EnterTransition.None },
                                    popExitTransition = { ExitTransition.None }
                                ) {
                                    HomeScreen(navController)
                                }
                                composable(
                                    route = "sheets",
                                    enterTransition = { EnterTransition.None },
                                    exitTransition = { ExitTransition.None },
                                    popEnterTransition = { EnterTransition.None },
                                    popExitTransition = { ExitTransition.None }
                                ) {
                                    ExploreSheetScreen(
                                        onSheetClick = { sheetId ->
                                            navController.navigate("sheet_detail/$sheetId")
                                        }
                                    )
                                }
                                composable(
                                    route = "profile",
                                    enterTransition = { EnterTransition.None },
                                    exitTransition = { ExitTransition.None },
                                    popEnterTransition = { EnterTransition.None },
                                    popExitTransition = { ExitTransition.None }
                                ) {
                                    ProfileScreen(
                                        navController = navController,
                                        viewModel = authViewModel,
                                        isDarkTheme = isDarkTheme,
                                        onThemeChange = { enabled ->
                                            settingsScope.launch {
                                                 appSettingsRepository.setDarkTheme(enabled)
                                            }
                                        },
                                        onNavigateToLogin = {
                                            navController.navigate("first_screen") {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    inclusive = true
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                                composable("connect") {
                                    ConnectScreen(onBackClick = { navController.navigateUp() })
                                }
                                composable("settings") {
                                    SettingsScreen(
                                        navController = navController,
                                        isDarkTheme = isDarkTheme,
                                        onThemeChange = { enabled ->
                                            settingsScope.launch {
                                                appSettingsRepository.setDarkTheme(enabled)
                                            }
                                        },
                                        onNavigateToLogin = {
                                            navController.navigate("first_screen") {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    inclusive = true
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }

                                // ── Detail screens (bottom bar hidden) ─────
                                composable("sheet_detail/{sheetId}") { backStackEntry ->
                                    val sheetId = backStackEntry.arguments
                                        ?.getString("sheetId") ?: return@composable
                                    SheetDetailScreen(
                                        sheetId = sheetId,
                                        onBackClick = { navController.navigateUp() }
                                    )
                                }
                                // ── Feature screens (navigated from home cards, bottom bar hidden) ──
                                composable("internships") {
                                    JobsScreen(onBackClick = { navController.navigateUp() })
                                }
                                composable("ui") {
                                    ContestScreen(onBackClick = { navController.navigateUp() })
                                }
                                composable("game") {
                                    AptitudeGameScreen(
                                        onBackClick = { navController.navigateUp() }
                                    )
                                }
                                composable("alumni_referral") {
                                    AlumniReferralScreen(
                                        onBackClick = { navController.navigateUp() }
                                    )
                                }
                            }
                        }
                    }

                    EngiFixIntroOverlay(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(10f)
                    )
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun startContestNotificationService() {
        startService(Intent(this, ContestNotificationService::class.java))
    }
}

// ─── Persistent Bottom Navigation Bar ────────────────────────────────────────

@Composable
private fun AppBottomNav(
    currentRoute: String?,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        bottomTabs.forEach { tab ->
            val isSelected = currentRoute == tab.route
            val motion = rememberMotionPolicy()
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.12f else 1f,
                animationSpec = tween(motion.fast),
                label = "${tab.route}_icon_scale"
            )
            val indicatorAlpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.72f,
                animationSpec = tween(motion.fast),
                label = "${tab.route}_indicator_alpha"
            )
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        modifier = Modifier.graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = indicatorAlpha
                    )
                )
            )
        }
    }
}
