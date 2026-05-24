package com.example.auth.presentation.inApp.homescreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.auth.presentation.animation.rememberMotionPolicy
import com.example.auth.presentation.components.EngiFixBackground
import com.example.auth.presentation.components.Eyebrow
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
    val motion = rememberMotionPolicy()
    var visible by remember { mutableStateOf(false) }
    val heroLift by animateFloatAsState(
        targetValue = if (scrollState.value > 16 && motion.enabled) -10f else 0f,
        animationSpec = tween(motion.fast),
        label = "home_hero_lift"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

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

    EngiFixBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(motion.duration(420))) +
                        slideInVertically(
                            animationSpec = tween(motion.duration(520), easing = FastOutSlowInEasing),
                            initialOffsetY = { it / 8 }
                        )
            ) {
                Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .graphicsLayer { translationY = heroLift }
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.74f)
                                )
                            )
                        )
                        .padding(horizontal = 18.dp)
                        .padding(top = 28.dp, bottom = 18.dp)
                ) {
                    Column {
                        Eyebrow(text = "CAREER OS")
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (firstName.isNotEmpty()) {
                                        "$greeting, ${firstName.take(18)}"
                                    } else {
                                        greeting
                                    },
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Choose the next move. The app keeps up with you.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box {
                                IconButton(onClick = {}) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-1).dp, y = 8.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.44f),
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = formattedTime.ifBlank { "--:--" },
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = formattedDay.ifBlank { "Today" },
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Your next move",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                TipCardEx(
                    title = "Jobs & Internships",
                    description = "Fresh roles, filtered for early-career engineers",
                    icon = Icons.Default.Work,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    accentColor = Color(0xFF7BA7A5)
                ) { navController.navigate("internships") }

                TipCardEx(
                    title = "Contests",
                    description = "Today and tomorrow rounds, ready to join",
                    icon = Icons.Default.EmojiEvents,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    accentColor = MaterialTheme.colorScheme.primary
                ) { navController.navigate("ui") }

                TipCardEx(
                    title = "Mentorship",
                    description = "Book focused guidance for interviews and projects",
                    icon = Icons.Default.School,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    accentColor = Color(0xFFA7B894)
                ) { navController.navigate("mentor") }

                TipCardEx(
                    title = "Peer Network",
                    description = "Find students by college, branch, skills, and goals",
                    icon = Icons.Default.Groups,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    accentColor = Color(0xFF8E6FBD)
                ) { navController.navigate("connect") }

                TipCardEx(
                    title = "Canvas",
                    description = "Sketch ideas, flows, and project diagrams on a canvas",
                    icon = Icons.Default.AccountTree,
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    accentColor = Color(0xFFFFC857)
                ) { navController.navigate("project_canvas") }

                Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
