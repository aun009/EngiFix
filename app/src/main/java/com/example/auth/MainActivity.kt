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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.data.Mentor
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
import com.example.auth.presentation.features.mentorship.MentorDetailScreen
import com.example.auth.presentation.features.mentorship.MentorshipScreen
import com.example.auth.presentation.features.sheet.ExploreSheetScreen
import com.example.auth.presentation.features.sheet.SheetDetailScreen
import com.example.auth.presentation.features.toolkit.ToolkitScreen
import com.example.auth.presentation.inApp.homescreen.HomeScreen
import com.example.auth.presentation.inApp.profilescreen.ProfileScreen
import com.example.auth.presentation.features.resume.ResumeRoastScreen
import com.example.auth.ui.theme.AuthTheme
import dagger.hilt.android.AndroidEntryPoint

// ─── Bottom nav tab definition ────────────────────────────────────────────────

private data class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab("home",         "Home",    Icons.Filled.Home,                    Icons.Outlined.Home),
    BottomTab("sheets",       "Sheets",  Icons.Filled.MenuBook,                Icons.Outlined.MenuBook),
    BottomTab("resume_roast", "Roast",   Icons.Filled.SentimentVeryDissatisfied, Icons.Outlined.SentimentVeryDissatisfied),
    BottomTab("profile",      "Profile", Icons.Filled.Person,                  Icons.Outlined.Person),
)

// Routes where the bottom bar should be HIDDEN (detail / flow screens)
private val routesWithoutBottomBar = setOf(
    "first_screen", "register_screen", "login_screen",
    "ask_name_screen", "username_pass_screen",
    "mentor_detail", "sheet_detail/{sheetId}",
    "mentor", "ui"          // these are full-screen from home cards, keep bar for now
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
            AuthTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Show bottom bar only for the 4 main tabs
                val showBottomBar = currentRoute in bottomTabs.map { it.route }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (showBottomBar) {
                            AppBottomNav(
                                currentRoute = currentRoute,
                                onTabSelected = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
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
                        NavHost(
                            navController = navController,
                            startDestination = "first_screen"
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
                            composable("home") {
                                HomeScreen(navController)
                            }
                            composable("sheets") {
                                ExploreSheetScreen(
                                    onSheetClick = { sheetId ->
                                        navController.navigate("sheet_detail/$sheetId")
                                    }
                                )
                            }
                            composable("resume_roast") {
                                ResumeRoastScreen(navController)
                            }
                            composable("profile") {
                                ProfileScreen(
                                    navController = navController,
                                    viewModel = authViewModel
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
                            composable("mentor_detail") {
                                val mentor = navController.previousBackStackEntry
                                    ?.savedStateHandle?.get<Mentor>("selected_mentor")
                                mentor?.let {
                                    MentorDetailScreen(
                                        mentor = it,
                                        onBackClick = { navController.navigateUp() },
                                        onGetAccessClick = { navController.navigateUp() },
                                        razorpayKeyId = "YOUR_RAZORPAY_KEY_ID"
                                    )
                                }
                            }

                            // ── Feature screens (navigated from home cards, bottom bar hidden) ──
                            composable("internships") {
                                JobsScreen(onBackClick = { navController.navigateUp() })
                            }
                            composable("ui") {
                                ContestScreen(onBackClick = { navController.navigateUp() })
                            }
                            composable("mentor") {
                                MentorshipScreen(
                                    onBackClick = { navController.navigateUp() },
                                    onMentorClick = { mentor ->
                                        navController.currentBackStackEntry
                                            ?.savedStateHandle?.set("selected_mentor", mentor)
                                        navController.navigate("mentor_detail")
                                    }
                                )
                            }
                            composable("game") {
                                AptitudeGameScreen(
                                    onBackClick = { navController.navigateUp() }
                                )
                            }
                            composable("toolkit") {
                                ToolkitScreen(
                                    onBackClick = { navController.navigateUp() }
                                )
                            }
                        }
                    }
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
        tonalElevation = 0.dp
    ) {
        bottomTabs.forEach { tab ->
            val isSelected = currentRoute == tab.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label
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
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
