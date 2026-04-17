package com.example.auth.presentation.features.sheet

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.auth.data.local.entity.DsaQuestionEntity
import com.valentinilk.shimmer.shimmer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SheetDetailScreen(
    sheetId: String,
    onBackClick: () -> Unit
) {
    val viewModel: SheetDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = (uiState as? SheetDetailUiState.Success)?.data?.sheet?.title ?: "Details"
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
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
                    val progress = if (data.totalCount > 0)
                        data.completedCount.toFloat() / data.totalCount.toFloat() else 0f

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {

                        // ─── Sticky Progress Header ───────────────────
                        stickyHeader(key = "progress_header") {
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
                                    Text(
                                        text = "${data.completedCount} / ${data.totalCount} solved",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(Modifier.height(6.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            }
                        }

                        // ─── Topic Groups ─────────────────────────────
                        data.groupedQuestions.forEach { (topic, questions) ->

                            // Collapsible topic header
                            stickyHeader(key = "topic_$topic") {
                                TopicSectionHeader(
                                    topic = topic,
                                    totalCount = questions.size,
                                    doneCount = questions.count { it.isCompleted }
                                )
                            }

                            // Questions under this topic
                            items(questions, key = { it.id }) { question ->
                                DsaQuestionRow(
                                    question = question,
                                    onToggle = { isChecked ->
                                        viewModel.toggleCompletion(question.id, isChecked)
                                    }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 52.dp, end = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }

                is SheetDetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚠️", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// ─── Topic Section Header ─────────────────────────────────────────────────────

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = topic,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        // Completion mini-badge
        Surface(
            shape = RoundedCornerShape(50),
            color = if (doneCount == totalCount && totalCount > 0)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline
        ) {
            Text(
                text = "$doneCount/$totalCount",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (doneCount == totalCount && totalCount > 0)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}

// ─── Question Row (compact, uniform font) ─────────────────────────────────────

@Composable
private fun DsaQuestionRow(
    question: DsaQuestionEntity,
    onToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(question.problemUrl))
                context.startActivity(intent)
            }
            .padding(start = 4.dp, end = 16.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = question.isCompleted,
            onCheckedChange = onToggle,
            modifier = Modifier.size(40.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )

        // Question title — fixed font size for uniform rows
        Text(
            text = question.title,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal,
            color = if (question.isCompleted)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onSurface,
            textDecoration = if (question.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        DifficultyBadge(difficulty = question.difficulty)
    }
}

// ─── Difficulty badge pill ─────────────────────────────────────────────────────

@Composable
private fun DifficultyBadge(difficulty: String) {
    val (containerColor, contentColor) = when (difficulty.lowercase()) {
        "easy"   -> MaterialTheme.colorScheme.tertiaryContainer   to MaterialTheme.colorScheme.onTertiaryContainer
        "medium" -> MaterialTheme.colorScheme.secondaryContainer  to MaterialTheme.colorScheme.onSecondaryContainer
        else     -> MaterialTheme.colorScheme.errorContainer      to MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = containerColor
    ) {
        Text(
            text = difficulty,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

// ─── Loading skeleton ─────────────────────────────────────────────────────────

@Composable
private fun DetailShimmer() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress bar skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .shimmer()
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )
        Spacer(Modifier.height(16.dp))

        // Topic header skeletons + rows
        repeat(3) {
            // Topic header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .shimmer()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(Modifier.height(4.dp))

            // Question rows under topic
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
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(13.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
