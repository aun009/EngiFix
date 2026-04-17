package com.example.auth.presentation.features.resume

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeRoastScreen(navController: NavController) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedExperience by remember { mutableStateOf("Fresher (0-1 yr)") }
    var isRoasting by remember { mutableStateOf(false) }
    var roastResult by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> 
        uri?.let { selectedUri = it }
    }

    val experienceLevels = listOf("Fresher (0-1 yr)", "Mid (1-3 yrs)", "Experienced (3+)")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resume Roast", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF18181C))
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (roastResult != null) {
                // Show result
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Done",
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
                        )
                        Text(
                            text = "Your Roast is Ready!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = roastResult!!,
                                color = Color(0xFFE0E0E0),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(20.dp),
                                lineHeight = 24.sp
                            )
                        }
                        
                        Spacer(Modifier.height(30.dp))
                        
                        Button(
                            onClick = {
                                selectedUri = null
                                roastResult = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Roast Another Resume")
                        }
                    }
                }
            } else {
                // Main Roast form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Get harsh, actionable feedback on your resume powered by AI.",
                        color = Color(0xFFAAAAAA),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Experience Segment Selector
                    Text(
                        text = "Select your experience level:",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        experienceLevels.forEach { level ->
                            val isSelected = selectedExperience == level
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF6C5CE7) else Color(0xFF2C2C2E))
                                    .clickable { selectedExperience = level }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = level,
                                    color = if (isSelected) Color.White else Color(0xFFAAAAAA),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // File Upload Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color(0xFF6C5CE7), RoundedCornerShape(16.dp))
                            .background(Color(0xFF1E1E24))
                            .clickable { filePickerLauncher.launch("application/pdf") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (selectedUri != null) Icons.Default.Info else Icons.Default.Add,
                                contentDescription = "Upload",
                                tint = if (selectedUri != null) Color(0xFF00C853) else Color(0xFF6C5CE7),
                                modifier = Modifier.size(48.dp).padding(bottom = 8.dp)
                            )
                            Text(
                                text = if (selectedUri != null) "PDF Selected!" else "Tap to Select PDF",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            if (selectedUri == null) {
                                Text(
                                    text = "Max size: 5MB",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Action Button
                    Button(
                        onClick = {
                            if (selectedUri != null) {
                                isRoasting = true
                                coroutineScope.launch {
                                    // Mocking an AI delay
                                    delay(2500)
                                    roastResult = "🔥 Ouch! This resume needs work.\n\n" +
                                            "1. Too Much Fluff: Your summary section is full of buzzwords like 'passionate' and 'synergy' but lacks concrete metrics.\n\n" +
                                            "2. Weak Bullet Points: 'Worked on the backend API' tells me nothing. Did you use Node? Spring? What was the scale?\n\n" +
                                            "3. Good Formatting: On the bright side, the single-column layout is very ATS-friendly.\n\n" +
                                            "Targeting $selectedExperience? You need to emphasize impact: Use the 'X by Y doing Z' formula for every bullet point."
                                    isRoasting = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = selectedUri != null && !isRoasting
                    ) {
                        if (isRoasting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Roasting via AI...", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Analyze & Roast Resume", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
