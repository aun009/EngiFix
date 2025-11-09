package com.example.auth.presentation.inApp.homescreen

import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.auth.presentation.components.TipCardEx

@Composable
fun HomeScreen(navController: NavHostController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavBarExample(navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            TopAndMidHomeScreen(navController)
        }
    }
}

@Composable
fun TopAndMidHomeScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF18181C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(40.dp))

            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "notify",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.End)
            )

            Text(
                "Hi, Bro",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(30.dp))

            Text(
                "Here tip of the day",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFFD7D7D7),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(25.dp))

            TipCardEx("Internships", "Find opportunities") {
                navController.navigate("internships")
            }
            TipCardEx("Jobs", "Latest openings") {
                navController.navigate("jobs")
            }
            TipCardEx("Contests", "Compete today!") {
                navController.navigate("ui")
            }
            TipCardEx("Study Stopwatch", "Track your focus") {
                navController.navigate("stopwatch")
            }
        }
    }
}

@Composable
fun BottomNavBarExample(navController: NavHostController) {
    val items = listOf(
        "home" to Icons.Filled.Home,
        "chat" to Icons.Outlined.Call,
        "profile" to Icons.Filled.Person
    )
    // route name + icon pair

    NavigationBar(
        containerColor = Color(0xFF202124),
        contentColor = Color.White
    ) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        items.forEach { (route, icon) ->
            NavigationBarItem(
                icon = {
                    Icon(
                        icon,
                        contentDescription = route,
                        tint = if (currentRoute == route) Color.White else Color.Gray,
                    )
                },
                label = {
                    Text(
                        route.replaceFirstChar { it.uppercase() },
                        color = if (currentRoute == route) Color.White else Color.Gray
                    )
                },
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
