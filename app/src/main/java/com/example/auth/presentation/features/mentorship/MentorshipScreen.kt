package com.example.auth.presentation.features.mentorship

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.presentation.components.MentorCardEx
import kotlinx.coroutines.launch

data class Mentor(
    val id: String,
    val name: String,
    val skills: List<String>,
    val title: String,
    val description: String,
    val price: String,
    val imageUrl: String,
    val rating: Float,
    val totalReviews: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorshipScreen(
    onBackClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Track if user is scrolling up
    val isScrollingUp = remember { derivedStateOf {
        listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 100
    }}

    // Sample mentor data
    val mentors = remember {
        listOf(
            Mentor(
                id = "1",
                name = "Omkar Kad",
                skills = listOf("Arduino", "Raspberry Pie", "Entrapenaur"),
                title = "Founder Of SP Tech Solutions",
                description = "Brilliant Minds from the IIT Moze Collage",
                price = "2 Tea",
                imageUrl = "",
                rating = 4.9f,
                totalReviews = 127
            ),
            Mentor(
                id = "2",
                name = "John Smith",
                skills = listOf("iOS", "Swift", "SwiftUI"),
                title = "Lead iOS Engineer at Apple",
                description = "Specializing in iOS development with 10+ years of experience. Let me guide you through your iOS journey!",
                price = "$65",
                imageUrl = "",
                rating = 4.8f,
                totalReviews = 89
            ),
            Mentor(
                id = "3",
                name = "Sarah Johnson",
                skills = listOf("React", "TypeScript", "Node.js"),
                title = "Full Stack Developer at Meta",
                description = "Expert in modern web technologies. I'll help you build scalable web applications from scratch.",
                price = "$55",
                imageUrl = "",
                rating = 4.7f,
                totalReviews = 156
            ),
            Mentor(
                id = "4",
                name = "Michael Chen",
                skills = listOf("Python", "Machine Learning", "AI"),
                title = "ML Engineer at OpenAI",
                description = "Passionate about AI and machine learning. Let's explore the fascinating world of artificial intelligence together!",
                price = "$75",
                imageUrl = "",
                rating = 5.0f,
                totalReviews = 203
            ),
            Mentor(
                id = "5",
                name = "Emily Rodriguez",
                skills = listOf("UI/UX", "Figma", "Design Systems"),
                title = "Senior Product Designer at Spotify",
                description = "Creating beautiful and intuitive user experiences. I'll teach you the principles of great design.",
                price = "$45",
                imageUrl = "",
                rating = 4.9f,
                totalReviews = 94
            ),
            Mentor(
                id = "6",
                name = "David Kumar",
                skills = listOf("DevOps", "AWS", "Kubernetes"),
                title = "Cloud Architect at Amazon",
                description = "Expert in cloud infrastructure and DevOps practices. Let's build robust and scalable systems!",
                price = "$70",
                imageUrl = "",
                rating = 4.8f,
                totalReviews = 112
            )
        )
    }

    // Filter mentors based on search query
    val filteredMentors = remember(searchQuery, mentors) {
        if (searchQuery.isBlank()) {
            mentors
        } else {
            mentors.filter { mentor ->
                mentor.name.contains(searchQuery, ignoreCase = true) ||
                        mentor.title.contains(searchQuery, ignoreCase = true) ||
                        mentor.skills.any { it.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF1C1C1E)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1C1E))
                .padding(paddingValues)
        ) {
            // Main Content - Mentor Cards
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 180.dp, // Space for header and search
                    bottom = 16.dp
                )
            ) {
                // Show mentor count
                item {
                    Text(
                        text = "${filteredMentors.size} Mentor${if (filteredMentors.size != 1) "s" else ""} Available",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (filteredMentors.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No mentors found",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Try a different search term",
                                    fontSize = 15.sp,
                                    color = Color(0xFFAAAAAA)
                                )
                            }
                        }
                    }
                } else {
                    items(filteredMentors) { mentor ->
                        MentorCardEx(
                            name = mentor.name,
                            skills = mentor.skills,
                            title = mentor.title,
                            description = mentor.description,
                            price = mentor.price,
                            imageUrl = mentor.imageUrl,
                            rating = mentor.rating,
                            totalReviews = mentor.totalReviews,
                            onClick = {
                                // Handle mentor card click
                                // Navigate to mentor detail screen
                            }
                        )
                    }
                }
            }

            // Sticky Header Section (appears/disappears on scroll)
            androidx.compose.animation.AnimatedVisibility(
                visible = isScrollingUp.value,
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1C1C1E))
                ) {
                    // Header Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Find Your Mentor",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Placeholder for symmetry
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = Color.Transparent
                            )
                        }
                    }


                    // Search and Filter Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    "Search...",
                                    color = Color(0xFF666666)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF666666)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3A3A3C),
                                unfocusedBorderColor = Color(0xFF2C2C2E),
                                focusedContainerColor = Color(0xFF2C2C2E),
                                unfocusedContainerColor = Color(0xFF2C2C2E),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        // Filter Button
                        IconButton(
                            onClick = { /* Handle filter */ },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    color = Color(0xFF2C2C2E),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Filter",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Scroll to top FAB (when header is hidden)
            androidx.compose.animation.AnimatedVisibility(
                visible = !isScrollingUp.value,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = Color(0xFF667EEA),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Scroll to top"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
fun MentorshipScreenPreview() {
    MentorshipScreen()
}