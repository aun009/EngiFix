package com.example.auth.presentation.inApp.homescreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import androidx.navigation.NavHostController
import com.example.auth.presentation.components.TipCardEx
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TopAndMidHomeScreen(navController: NavHostController) {

    var formattedDate by remember {
        mutableStateOf("")
    }

    // This effect will run once and update the time every second
    LaunchedEffect(true) {
        // Use the correct DateTimeFormatter from java.time.format
        val formatter = DateTimeFormatter.ofPattern("EEEE d, hh:mm a") // <-- This will now resolve correctly
        while (true) {
            formattedDate = LocalDateTime.now().format(formatter)
            kotlinx.coroutines.delay(1000L) // Wait for 1 second
        }
    }

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
                formattedDate,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFFD7D7D7),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(25.dp))

            TipCardEx("Jobs & Internships", "Find opportunities") {
                navController.navigate("internships")
            }
//            TipCardEx("Jobs", "Latest openings") {
//                navController.navigate("jobs")
//            }
            TipCardEx("Contests", "Compete today!") {
                navController.navigate("ui")
            }
            TipCardEx("Mentorship ", "None") {
                navController.navigate("mentor")
            }

            TipCardEx("TimeLine", "Events") {
                navController.navigate("timeline")
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
private fun getCurrentDateTime(): LocalDateTime {
    return LocalDateTime.now()
}
@Composable
fun BottomNavBarExample(navController: NavHostController) {
    val items = listOf(
        "home" to Icons.Filled.Home,
        "Resume Roast" to Icons.Outlined.Call,
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
                    android.util.Log.d("HomeScreen", "🔵 Navigation clicked: route = $route")
                    try {
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                        android.util.Log.d("HomeScreen", "✅ Navigation successful to: $route")
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "❌ Navigation failed to $route", e)
                        e.printStackTrace()
                    }
                }
            )
        }
    }
}
