package com.example.auth.presentation.inApp.homescreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.auth.presentation.components.TipCardEx
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavHostController) {
    TopAndMidHomeScreen(navController)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TopAndMidHomeScreen(navController: NavHostController) {
    val scrollState = rememberScrollState()

    var formattedTime by remember { mutableStateOf("") }
    var formattedDay  by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val timeFmt = DateTimeFormatter.ofPattern("hh:mm a")
        val dayFmt  = DateTimeFormatter.ofPattern("EEEE, d MMM")
        while (true) {
            val now = LocalDateTime.now()
            formattedTime = now.format(timeFmt)
            formattedDay  = now.format(dayFmt)
            kotlinx.coroutines.delay(1000L)
        }
    }

    var firstName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
            val doc = FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
            val name = doc.getString("displayName") ?: doc.getString("userName")
                       ?: FirebaseAuth.getInstance().currentUser?.displayName
            firstName = name?.split(" ")?.firstOrNull()
                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: ""
        } catch (_: Exception) {}
    }

    val greeting = when (LocalDateTime.now().hour) {
        in 5..11  -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else      -> "Good night"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF18181C))
            .verticalScroll(scrollState)
    ) {
        // ── Hero Banner ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E22))
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (firstName.isNotEmpty()) "$greeting, $firstName 👋"
                                   else "$greeting 👋",
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Let's build something great today",
                            fontSize = 14.sp,
                            color = Color(0xFFAAAAAA)
                        )
                    }
                    Box {
                        IconButton(onClick = {}) {
                            Icon(
                                Icons.Default.Notifications, null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF6B6B))
                                .align(Alignment.TopEnd)
                                .offset(x = 0.dp, y = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))

                // Live clock
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = formattedTime,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = formattedDay,
                                fontSize = 13.sp,
                                color = Color(0xFFBBBBFF)
                            )
                        }
                        Text(text = "🗓️", fontSize = 32.sp)
                    }
                }
            }
        }

        // ── Feature Cards ─────────────────────────────────────────────────────
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Explore",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        TipCardEx(
            title = "Jobs & Internships",
            description = "Discover the latest openings tailored for you",
            icon = "💼",
            backgroundColor = Color(0xFF242426),
            accentColor = Color(0xFF4DABF7)
        ) { navController.navigate("internships") }

        TipCardEx(
            title = "Contests",
            description = "Today & Tomorrow's coding battles — don't miss out",
            icon = "🏆",
            backgroundColor = Color(0xFF242426),
            accentColor = Color(0xFFFF9F43)
        ) { navController.navigate("ui") }

        TipCardEx(
            title = "Mentorship",
            description = "1:1 guidance from top engineers at FAANG & beyond",
            icon = "🎯",
            backgroundColor = Color(0xFF242426),
            accentColor = Color(0xFF6C5CE7)
        ) { navController.navigate("mentor") }

        // ── NEW: Student Toolkit card ─────────────────────────────────────────
        TipCardEx(
            title = "Student Toolkit",
            description = "30+ essential sites — DSA, resumes, interviews & more",
            icon = "🛠️",
            backgroundColor = Color(0xFF242426),
            accentColor = Color(0xFF00B894)
        ) { navController.navigate("toolkit") }

        // ── NEW: Cognitive Aptitude Game ──────────────────────────────────────
        TipCardEx(
            title = "Cognitive Assessment",
            description = "Test your numerical reasoning with fast-paced equation puzzles",
            icon = "🧮",
            backgroundColor = Color(0xFF242426),
            accentColor = Color(0xFFFFB800)
        ) { navController.navigate("game") }

        Spacer(Modifier.height(24.dp))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getCurrentDateTime(): LocalDateTime = LocalDateTime.now()
