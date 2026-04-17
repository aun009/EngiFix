package com.example.auth.presentation.features.mentorship

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.data.Mentor
import com.example.auth.presentation.components.MentorCardEx
import com.example.auth.presentation.components.MentorListShimmer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorshipScreen(
    onBackClick: () -> Unit = {},
    onMentorClick: (Mentor) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    // Simulate a 1.5s loading state for shimmer demonstration
    // In a real ViewModel-backed screen, replace with UiState.Loading sealed class.
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(1500L)
        isLoading = false
    }

    // Sample mentor data
    val mentors = remember {
        listOf(
            Mentor(
                id = "1",
                name = "Aman Dhattarwal",
                skills = listOf("C++", "DSA", "System Design"),
                title = "Ex-Microsoft | Founder",
                description = "Guiding students towards product-based companies with structured roadmaps.",
                price = "₹500",
                imageUrl = "",
                rating = 4.9f,
                totalReviews = 127,
                aboutMe = "Passionate educator and engineer helping thousands of students land their dream jobs."
            ),
            Mentor(
                id = "2",
                name = "Anjali Singh",
                skills = listOf("Android", "Kotlin", "Compose"),
                title = "SDE II at Google",
                description = "Specializing in native Android development with 5+ years of experience.",
                price = "₹800",
                imageUrl = "",
                rating = 4.8f,
                totalReviews = 89,
                aboutMe = "I love architecting scalable Android applications and mentoring junior developers."
            ),
            Mentor(
                id = "3",
                name = "Rahul Kumar",
                skills = listOf("React", "Node.js", "MongoDB"),
                title = "Full Stack Engineer at Flipkart",
                description = "Expert in MERN stack. I'll help you build scalable web applications from scratch.",
                price = "₹600",
                imageUrl = "",
                rating = 4.7f,
                totalReviews = 156,
                aboutMe = "I build web platforms that scale to millions of users daily."
            ),
            Mentor(
                id = "4",
                name = "Kunal Kushwaha",
                skills = listOf("DevOps", "Kubernetes", "Open Source"),
                title = "Developer Advocate",
                description = "Master cloud native technologies and open source contributions.",
                price = "₹450",
                imageUrl = "",
                rating = 5.0f,
                totalReviews = 203,
                aboutMe = "CNCF Ambassador focusing on community building and cloud-native architecture."
            ),
            Mentor(
                id = "5",
                name = "Shruti Balasa",
                skills = listOf("UI/UX", "Tailwind CSS", "Frontend"),
                title = "Lead Frontend Engineer",
                description = "Creating beautiful and accessible user interfaces. Let's make the web look great.",
                price = "₹550",
                imageUrl = "",
                rating = 4.9f,
                totalReviews = 94,
                aboutMe = "Design-oriented frontend developer with a focus on CSS architecture."
            ),
            Mentor(
                id = "6",
                name = "Sandeep Jain",
                skills = listOf("Algorithms", "Java", "Backend"),
                title = "Founder of GeeksforGeeks",
                description = "Master competitive programming and core backend engineering principles.",
                price = "₹1200",
                imageUrl = "",
                rating = 4.8f,
                totalReviews = 112,
                aboutMe = "Dedicated to making computer science education accessible to everyone."
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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Find Your Mentor",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    ),
                    scrollBehavior = scrollBehavior
                )
                // Search Box inside the header space roughly (but not pinned to TopAppBar so it hides under it, or pin it to TopBar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f).height(56.dp),
                        placeholder = { Text("Search mentors...", color = Color(0xFF666666)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF666666)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6C5CE7),
                            unfocusedBorderColor = Color(0xFF2C2C2E),
                            focusedContainerColor = Color(0xFF2C2C2E),
                            unfocusedContainerColor = Color(0xFF2C2C2E),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    IconButton(
                        onClick = { /* Handle filter */ },
                        modifier = Modifier
                            .size(56.dp)
                            .background(color = Color(0xFF2C2C2E), shape = RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                MentorListShimmer()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(top = 12.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Top Mentors",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${filteredMentors.size} available",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
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
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Try a different search term",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                onClick = { onMentorClick(mentor) }
                            )
                        }
                    }
                }
            } // end else (not loading)

            // Scroll to top FAB
            val isScrolledDown by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
            androidx.compose.animation.AnimatedVisibility(
                visible = isScrolledDown,
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
                    containerColor = Color(0xFF6C5CE7),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
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