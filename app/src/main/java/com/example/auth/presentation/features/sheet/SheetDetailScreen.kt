package com.example.auth.presentation.features.sheet

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.auth.data.local.entity.DsaQuestionEntity
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val difficultyFilters = listOf("All", "Easy", "Medium", "Hard")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SheetDetailScreen(
    sheetId: String,
    onBackClick: () -> Unit
) {
    val viewModel: SheetDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var query by remember(sheetId) { mutableStateOf("") }
    var selectedDifficulty by remember(sheetId) { mutableStateOf("All") }
    var hideSolved by remember(sheetId) { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? SheetDetailUiState.Success)?.data?.sheet?.title ?: "Sheet details"
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val refreshing = (uiState as? SheetDetailUiState.Success)?.data?.isRefreshing == true
                    IconButton(onClick = viewModel::refreshSheet, enabled = !refreshing) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh sheet",
                            tint = if (refreshing) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SheetDetailUiState.Loading -> DetailShimmer()

                is SheetDetailUiState.Success -> {
                    val data = state.data
                    val allQuestions = remember(data.groupedQuestions) {
                        data.groupedQuestions.values.flatten()
                    }
                    val filteredQuestions by remember(allQuestions, query, selectedDifficulty, hideSolved) {
                        derivedStateOf {
                            val needle = query.trim()
                            allQuestions.filter { question ->
                                val matchesSearch = needle.isBlank() ||
                                    question.title.contains(needle, ignoreCase = true) ||
                                    question.topic.contains(needle, ignoreCase = true)
                                val matchesDifficulty = selectedDifficulty == "All" ||
                                    question.difficulty.equals(selectedDifficulty, ignoreCase = true)
                                val matchesSolved = !hideSolved || !question.isCompleted
                                matchesSearch && matchesDifficulty && matchesSolved
                            }.groupBy { it.topic.ifBlank { "General" } }
                        }
                    }
                    val progress = if (data.totalCount > 0) {
                        (data.completedCount.toFloat() / data.totalCount.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        stickyHeader(key = "progress_header") {
                            DetailHeader(
                                completedCount = data.completedCount,
                                totalCount = data.totalCount,
                                progress = progress,
                                query = query,
                                selectedDifficulty = selectedDifficulty,
                                hideSolved = hideSolved,
                                syncError = data.syncError,
                                onQueryChange = { query = it },
                                onDifficultyChange = { selectedDifficulty = it },
                                onHideSolvedChange = { hideSolved = it }
                            )
                        }

                        if (allQuestions.isEmpty()) {
                            item {
                                EmptyDetailState(
                                    title = "No questions in this sheet",
                                    message = "Refresh to pull the latest bundled or remote data.",
                                    actionLabel = "Refresh",
                                    onAction = viewModel::refreshSheet
                                )
                            }
                        } else if (filteredQuestions.isEmpty()) {
                            item {
                                EmptyDetailState(
                                    title = "No questions match",
                                    message = "Clear search, difficulty, or solved filters.",
                                    actionLabel = "Clear filters",
                                    onAction = {
                                        query = ""
                                        selectedDifficulty = "All"
                                        hideSolved = false
                                    }
                                )
                            }
                        } else {
                            filteredQuestions.forEach { (topic, questions) ->
                                stickyHeader(key = "topic_$topic") {
                                    TopicSectionHeader(
                                        topic = topic,
                                        totalCount = questions.size,
                                        doneCount = questions.count { it.isCompleted }
                                    )
                                }

                                items(questions, key = { it.id }) { question ->
                                    DsaQuestionRow(
                                        question = question,
                                        onToggle = { isChecked ->
                                            viewModel.toggleCompletion(question.id, isChecked)
                                        },
                                        onOpen = {
                                            val opened = openProblemUrl(context, question.problemUrl)
                                            if (!opened) {
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("This question link is missing or invalid.")
                                                }
                                            }
                                        }
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant,
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }

                is SheetDetailUiState.Error -> {
                    EmptyDetailState(
                        title = "Sheet not available",
                        message = state.message,
                        actionLabel = "Retry",
                        onAction = viewModel::refreshSheet,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(
    completedCount: Int,
    totalCount: Int,
    progress: Float,
    query: String,
    selectedDifficulty: String,
    hideSolved: Boolean,
    syncError: String?,
    onQueryChange: (String) -> Unit,
    onDifficultyChange: (String) -> Unit,
    onHideSolvedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$completedCount / $totalCount solved",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(progress * 100).roundToInt()}% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)) {
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            placeholder = { Text("Search questions or topics") },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(difficultyFilters) { difficulty ->
                val selected = difficulty == selectedDifficulty
                FilterChip(
                    selected = selected,
                    onClick = { onDifficultyChange(difficulty) },
                    label = { Text(difficulty, fontSize = 13.sp) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hide solved", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Switch(checked = hideSolved, onCheckedChange = onHideSolvedChange)
        }
        if (syncError != null) {
            Text(
                text = syncError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
    }
}

@Composable
private fun TopicSectionHeader(
    topic: String,
    totalCount: Int,
    doneCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = topic,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (doneCount == totalCount && totalCount > 0)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface
        ) {
            Text(
                text = "$doneCount/$totalCount",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (doneCount == totalCount && totalCount > 0)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun DsaQuestionRow(
    question: DsaQuestionEntity,
    onToggle: (Boolean) -> Unit,
    onOpen: () -> Unit
) {
    val hasLink = question.problemUrl.isLikelyWebUrl()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = question.isCompleted,
            onCheckedChange = onToggle,
            modifier = Modifier.size(44.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = question.title,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
                color = if (question.isCompleted)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurface,
                textDecoration = if (question.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = question.topic.ifBlank { "General" },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))
        DifficultyBadge(difficulty = question.difficulty)
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onOpen, enabled = hasLink, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (hasLink) Icons.Default.Link else Icons.Default.LinkOff,
                contentDescription = if (hasLink) "Open problem" else "Missing problem link",
                tint = if (hasLink) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: String) {
    val normalized = difficulty.ifBlank { "Medium" }
    val (containerColor, contentColor) = when (normalized.lowercase()) {
        "easy" -> Color(0xFF3DDC97).copy(alpha = 0.16f) to Color(0xFF3DDC97)
        "medium" -> Color(0xFFFFB020).copy(alpha = 0.18f) to Color(0xFFFFB020)
        "hard" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = containerColor
    ) {
        Text(
            text = normalized,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyDetailState(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(14.dp).size(30.dp)
                )
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAction, shape = RoundedCornerShape(8.dp)) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun DetailShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .shimmer()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.height(16.dp))

        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .shimmer()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.height(6.dp))

            repeat(4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .shimmer(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(22.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun openProblemUrl(context: Context, rawUrl: String): Boolean {
    if (!rawUrl.isLikelyWebUrl()) return false
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(rawUrl.trim()))
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: IllegalArgumentException) {
        false
    }
}

private fun String.isLikelyWebUrl(): Boolean {
    val uri = runCatching { Uri.parse(trim()) }.getOrNull() ?: return false
    return uri.scheme == "http" || uri.scheme == "https"
}
