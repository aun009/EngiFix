package com.example.auth.presentation.features.contest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.ui.graphics.Color
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

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar with back button and refresh
        TopAppBar(
            title = { Text("Contests", style = MaterialTheme.typography.headlineSmall) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.fetchContests() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        when (uiState) {
            is ContestUiState.Loading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ContestUiState.Success -> {
                val contestsByPlatform = (uiState as ContestUiState.Success).contestsByPlatform
                
                println("📱 UI: Received ${contestsByPlatform.values.sumOf { it.size }} total contests from ${contestsByPlatform.size} platforms")
                contestsByPlatform.forEach { (platform, contests) ->
                    println("   Platform: $platform - ${contests.size} contests")
                }

                // Filter contests for today and tomorrow only
                val (todayContests, tomorrowContests) = filterContestsTodayAndTomorrow(contestsByPlatform)
                val totalContests = todayContests.size + tomorrowContests.size
                
                println("📱 UI: After filtering - Today: ${todayContests.size}, Tomorrow: ${tomorrowContests.size}, Total: $totalContests")

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
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
                        ) {
                            // Today section
                            if (todayContests.isNotEmpty()) {
                                item {
                                    DateSectionCard(
                                        title = "Today",
                                        contestCount = todayContests.size,
                                        sectionColor = Color(0xFF6C5CE7), // Purple for Today
                                        iconColor = Color(0xFFFF6B9D), // Pink icon color
                                        onContestClick = { contest ->
                                            selectedContest = contest
                                            selectedPlatform = when (contest.resource) {
                                                "codeforces.com" -> "Codeforces"
                                                "codechef.com" -> "CodeChef"
                                                "atcoder.jp" -> "AtCoder"
                                                "leetcode.com" -> "LeetCode"
                                                else -> contest.resource
                                            }
                                        },
                                        contests = todayContests
                                    )
                                }
                            }
                            
                            // Tomorrow section
                            if (tomorrowContests.isNotEmpty()) {
                                item {
                                    DateSectionCard(
                                        title = "Tomorrow",
                                        contestCount = tomorrowContests.size,
                                        sectionColor = Color(0xFF4A5568), // Dark gray for Tomorrow
                                        iconColor = Color(0xFFFF6B9D), // Pink icon color
                                        onContestClick = { contest ->
                                            selectedContest = contest
                                            selectedPlatform = when (contest.resource) {
                                                "codeforces.com" -> "Codeforces"
                                                "codechef.com" -> "CodeChef"
                                                "atcoder.jp" -> "AtCoder"
                                                "leetcode.com" -> "LeetCode"
                                                else -> contest.resource
                                            }
                                        },
                                        contests = tomorrowContests
                                    )
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
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Error: $msg", 
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Check Logcat for details. Search for: 'Contest' or 'Filtering'",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
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
