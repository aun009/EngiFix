package com.example.auth.presentation.features.contest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedContest by remember { mutableStateOf<ContestItem?>(null) }
    var selectedPlatform by remember { mutableStateOf<String>("") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val viewModel = remember {
        val appContext = context.applicationContext
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            ContestEntryPoint::class.java
        )
        val notificationHelper = NotificationHelper(context)
        ContestViewModel(hiltEntryPoint.getContestRepository(), notificationHelper)
    }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
    ) {
        // Top App Bar with back button and refresh
        TopAppBar(
            title = {
                Text(
                    "Contests",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.fetchContests() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1C1C1E),
                titleContentColor = Color.White
            )
        )

        when (uiState) {
            is ContestUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1C1C1E)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6C5CE7))
                }
            }

            is ContestUiState.Success -> {
                val todayContests = (uiState as ContestUiState.Success).todayContests
                val tomorrowContests = (uiState as ContestUiState.Success).tomorrowContests
                val totalContests = todayContests.size + tomorrowContests.size
                
                println("📱 UI: Showing Today: ${todayContests.size}, Tomorrow: ${tomorrowContests.size}, Total: $totalContests")

                // Simple pull-to-refresh using LazyColumn scroll state
                val listState = rememberLazyListState()
                val isRefreshing = uiState is ContestUiState.Loading
                var wasScrolledDown by remember { mutableStateOf(false) }
                
                // Track if user was scrolled down
                LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
                    wasScrolledDown = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
                }
                
                // Detect when user scrolls back to top after being scrolled down (pull to refresh)
                val isScrolledToTop by remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                    }
                }
                
                // Trigger refresh when user scrolls to top after being scrolled down
                LaunchedEffect(isScrolledToTop) {
                    if (isScrolledToTop && wasScrolledDown && !isRefreshing) {
                        delay(300) // Small delay to ensure user intended to refresh
                        if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
                            viewModel.fetchContests()
                            wasScrolledDown = false
                        }
                    }
                }
                
                if (totalContests == 0) {
                    println("📱 UI: No contests for today/tomorrow, showing NoContestsScreen")
                    NoContestsScreen()
                } else {
                    println("📱 UI: Showing ${todayContests.size} today + ${tomorrowContests.size} tomorrow contests")
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1C1C1E))) {
                        Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1C1C1E))) {
                            TabRow(
                                selectedTabIndex = selectedTabIndex,
                                containerColor = Color(0xFF1C1C1E),
                                contentColor = Color(0xFF6C5CE7),
                                indicator = { tabPositions ->
                                    TabRowDefaults.Indicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                        color = Color(0xFF6C5CE7)
                                    )
                                }
                            ) {
                                Tab(
                                    selected = selectedTabIndex == 0,
                                    onClick = { selectedTabIndex = 0 },
                                    text = {
                                        Text(
                                            "Today (${todayContests.size})",
                                            color = if (selectedTabIndex == 0) Color(0xFF6C5CE7) else Color(0xFF888888)
                                        )
                                    }
                                )
                                Tab(
                                    selected = selectedTabIndex == 1,
                                    onClick = { selectedTabIndex = 1 },
                                    text = {
                                        Text(
                                            "Tomorrow (${tomorrowContests.size})",
                                            color = if (selectedTabIndex == 1) Color(0xFF6C5CE7) else Color(0xFF888888)
                                        )
                                    }
                                )
                            }
                            
                            val activeContests = if (selectedTabIndex == 0) todayContests else tomorrowContests
                            
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
                            ) {
                                if (activeContests.isNotEmpty()) {
                                    item {
                                        DateSectionCard(
                                            title = if (selectedTabIndex == 0) "Today's Contests" else "Tomorrow's Contests",
                                            contestCount = activeContests.size,
                                            sectionColor = if (selectedTabIndex == 0) Color(0xFF6C5CE7) else Color(0xFF4A5568),
                                            iconColor = Color(0xFFFF6B9D),
                                            onContestClick = { uiModel ->
                                                selectedContest = uiModel.raw
                                                selectedPlatform = uiModel.platformName
                                            },
                                            contests = activeContests
                                        )
                                    }
                                } else {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "No contests scheduled.",
                                                color = Color.Gray,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Show refresh indicator at top when refreshing
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 16.dp)
                            )
                        }
                    }
                }
            }

            is ContestUiState.Error -> {
                val msg = (uiState as ContestUiState.Error).message
                println("❌ ERROR STATE: $msg")
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1C1C1E)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Something went wrong",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            msg,
                            color = Color(0xFFAAAAAA),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = { viewModel.fetchContests() },
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6C5CE7)),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry", color = Color(0xFF6C5CE7))
                        }
                    }
                }
            }
        }
    }
    
    // Show contest detail screen when a contest is selected
    selectedContest?.let { contest ->
        ContestDetailScreen(
            contest = contest,
            platformColor = PlatformColors.getColorForPlatform(selectedPlatform),
            platformName = selectedPlatform,
            onBackClick = {
                selectedContest = null
                selectedPlatform = ""
            },
            onOpenContest = {
                // Open the contest URL in browser
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(contest.href))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Handle error if browser can't be opened
                    println("Error opening contest: ${e.message}")
                }
            }
        )
    }
}
