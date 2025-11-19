package com.example.auth.presentation.features.mentorship

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.data.Mentor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorDetailScreen(
    mentor: Mentor,
    onBackClick: () -> Unit = {},
    onGetAccessClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFF1C1C1E)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1C1E))
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // Header Section with Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF3D2E5C),
                                    Color(0xFF2A1F3D)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Back Button
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        // Best Deal Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFB24592).copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "Best Deal",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFF6EC7),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Mentor Info Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "1:1 DSA Mentorship",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    lineHeight = 34.sp
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            // Profile Image
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF667EEA),
                                                Color(0xFF764BA2)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mentor.name.take(1),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Mentor Name
                        Text(
                            text = mentor.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // About Me Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1E))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "About Me",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Parse and display about me with highlighted keywords
                    Text(
                        text = buildAnnotatedString {
                            val keywords = listOf(
                                "Java", "Full Stack", "High-Level", "Low-Level", "HLD/LLD",
                                "design patterns", "problem-solving", "competitive programming",
                                "Google Kickstart", "Facebook Hacker Cup", "MP Coding Contest",
                                "Amazon", "MakeMyTrip", "USA-based", "Spring Boot", "Hibernate",
                                "Microservices", "RESTful APIs", "Apache Kafka", "Apache Camel",
                                "Singleton", "Factory", "Observer", "Builder", "Docker", "Kubernetes",
                                "Jenkins", "CI/CD", "AWS", "GCP", "Terraform", "Ansible", "JFrog",
                                "Prometheus", "Grafana", "React", "Angular", "Next.js", "HTML",
                                "CSS", "JavaScript", "MySQL", "PostgreSQL", "MongoDB", "Redis"
                            )

                            var remainingText = mentor.aboutMe
                            while (remainingText.isNotEmpty()) {
                                val nextKeyword = keywords
                                    .mapNotNull { keyword ->
                                        val index = remainingText.indexOf(keyword, ignoreCase = true)
                                        if (index >= 0) index to keyword else null
                                    }
                                    .minByOrNull { it.first }

                                if (nextKeyword == null) {
                                    append(remainingText)
                                    break
                                }

                                val (index, keyword) = nextKeyword
                                append(remainingText.substring(0, index))

                                withStyle(
                                    style = SpanStyle(
                                        color = Color(0xFFB8B8FF),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                ) {
                                    append(remainingText.substring(index, index + keyword.length))
                                }

                                remainingText = remainingText.substring(index + keyword.length)
                            }
                        },
                        fontSize = 15.sp,
                        color = Color(0xFFCCCCCC),
                        lineHeight = 24.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    // Skills Section
                    Text(
                        text = "Skills & Expertise",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Skills Grid
                    val skillsChunked = mentor.skills.chunked(3)
                    skillsChunked.forEach { rowSkills ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowSkills.forEach { skill ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF2C2C2E)
                                ) {
                                    Text(
                                        text = skill,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(100.dp)) // Space for bottom button
                }
            }

            // Bottom Price and CTA Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = Color(0xFF1C1C1E),
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1E))
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Price
                    Column {
                        Text(
                            text = "₹${mentor.price.replace("$", "")}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "per session",
                            fontSize = 13.sp,
                            color = Color(0xFF888888)
                        )
                    }

                    // Get Access Button
                    Button(
                        onClick = onGetAccessClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        contentPadding = PaddingValues(horizontal = 40.dp, vertical = 16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = "Get Access!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
fun MentorDetailScreenPreview() {

    val sampleMentor = Mentor(
        id = "1",
        name = "Himanshu Singour",
        skills = listOf("Java", "Spring Boot", "Microservices", "AWS", "Docker", "Kubernetes"),
        title = "Senior Software Engineer at Google",
        description = "Expert in Java and cloud technologies",
        price = "₹1,399",
        imageUrl = "",
        rating = 4.9f,
        totalReviews = 127,
        aboutMe = "I'm a passionate Java Full Stack Developer with expertise in High-Level and Low-Level Design (HLD/LLD), design patterns, and strong problem-solving abilities, alongside experience in competitive programming. I have secured impressive positions in various competitions, including Google Kickstart, Facebook Hacker Cup, and MP Coding Contest, and I've successfully cracked PBCs like Amazon and MakeMyTrip, along with two remote job offers from USA-based companies."
    )

    MentorDetailScreen(
        mentor = sampleMentor,

    )
}