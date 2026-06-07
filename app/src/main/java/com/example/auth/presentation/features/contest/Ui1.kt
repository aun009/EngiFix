package com.example.auth.presentation.features.contest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
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
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.example.auth.presentation.components.EngiFixBackground
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContestScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedContest by remember { mutableStateOf<ContestUiModel?>(null) }
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

    EngiFixBackground {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar with back button and refresh
        TopAppBar(
            title = {
                Text(
                    "Contests",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                IconButton(onClick = { viewModel.fetchContests() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        when (uiState) {
            is ContestUiState.Loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TabRow(
                                selectedTabIndex = selectedTabIndex,
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.primary,
                                indicator = { tabPositions ->
                                    TabRowDefaults.Indicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            ) {
                                Tab(
                                    selected = selectedTabIndex == 0,
                                    onClick = { selectedTabIndex = 0 },
                                    text = {
                                        Text(
                                            "Today (${todayContests.size})",
                                            color = if (selectedTabIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                                Tab(
                                    selected = selectedTabIndex == 1,
                                    onClick = { selectedTabIndex = 1 },
                                    text = {
                                        Text(
                                            "Tomorrow (${tomorrowContests.size})",
                                            color = if (selectedTabIndex == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                                            sectionColor = if (selectedTabIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                            iconColor = if (selectedTabIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                            onContestClick = { uiModel ->
                                                selectedContest = uiModel
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
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Something went wrong",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            msg,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = { viewModel.fetchContests() },
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text("Retry", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
    
    // Show contest detail screen when a contest is selected
    selectedContest?.let { uiModel ->
        BackHandler {
            selectedContest = null
        }

        ContestDetailScreen(
            contest = uiModel,
            platformColor = PlatformColors.getColorForPlatform(uiModel.platformName),
            onBackClick = {
                selectedContest = null
            },
            onOpenContest = {
                // Open the contest URL in browser
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uiModel.raw.href))
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Handle error if browser can't be opened
                    println("Error opening contest: ${e.message}")
                }
            }
        )
    }
    }
}
