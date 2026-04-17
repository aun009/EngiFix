package com.example.auth.presentation.features.jobs

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.auth.data.local.entity.JobEntity
import com.valentinilk.shimmer.shimmer

// ─── Badge config ─────────────────────────────────────────────────────────────

private data class BadgeStyle(val label: String, val bg: Color, val text: Color)

@Composable
private fun JobEntity.badgeStyle(): BadgeStyle {
    val cs = MaterialTheme.colorScheme
    return when (category) {
        "Internship" -> BadgeStyle("Internship", Color(0xFF00B894).copy(0.15f), Color(0xFF00B894))
        "Trainee"    -> BadgeStyle("Trainee",   Color(0xFFFDAB33).copy(0.18f), Color(0xFFE58C00))
        "Fresher"    -> BadgeStyle("Fresher",   Color(0xFFA29BFE).copy(0.2f),  Color(0xFF6C5CE7))
        "Part-time"  -> BadgeStyle("Part-time", Color(0xFFFF6B81).copy(0.15f), Color(0xFFD63031))
        else         -> BadgeStyle("Full-time", cs.primaryContainer,           cs.onPrimaryContainer)
    }
}

// Avatar color — deterministic per company
private val AVATAR_PALETTE = listOf(
    Color(0xFF6C5CE7), Color(0xFF00B894), Color(0xFF0984E3),
    Color(0xFFE17055), Color(0xFFFDAB33), Color(0xFFFF4757),
    Color(0xFF00CEC9), Color(0xFFA29BFE),
)
private fun avatarColor(company: String) =
    AVATAR_PALETTE[(company.firstOrNull()?.code ?: 0) % AVATAR_PALETTE.size]

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun JobsScreen(onBackClick: () -> Unit = {}) {
    val viewModel: JobsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val filter  by viewModel.filter.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopBar(uiState = uiState, onRefresh = viewModel::refresh)
        FilterSection(filter = filter, viewModel = viewModel)
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
        ContentArea(uiState = uiState, filter = filter, viewModel = viewModel)
    }
}

// ─── Top bar ─────────────────────────────────────────────────────────────────

@Composable
private fun TopBar(uiState: JobsUiState, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 22.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Opportunities",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-0.5).sp
            )
            val sub = when (uiState) {
                is JobsUiState.Success  -> "${uiState.jobs.size} active listings"
                is JobsUiState.Scraping -> "Fetching fresh listings…"
                else                    -> "Jobs & Internships"
            }
            Text(
                text = sub,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Normal
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, "Refresh", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

// ─── Filter section ───────────────────────────────────────────────────────────

@Composable
private fun FilterSection(filter: JobFilter, viewModel: JobsViewModel) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        FilterRow(
            label   = "Type",
            options = TYPE_OPTIONS,
            selected = filter.type,
            activeColor = MaterialTheme.colorScheme.primary,
            onSelect = viewModel::setTypeFilter
        )
        Spacer(Modifier.height(6.dp))
        FilterRow(
            label   = "Batch",
            options = BATCH_OPTIONS,
            selected = filter.batch,
            activeColor = Color(0xFF00B894),
            onSelect = viewModel::setBatchFilter
        )
    }
}

@Composable
private fun FilterRow(
    label: String,
    options: List<String>,
    selected: String,
    activeColor: Color,
    onSelect: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 20.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(38.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(end = 20.dp)
        ) {
            items(options) { opt ->
                val isSelected = opt == selected
                val chipBg by animateColorAsState(
                    if (isSelected) activeColor else MaterialTheme.colorScheme.surface,
                    tween(160), label = "cbg"
                )
                val chipText by animateColorAsState(
                    if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    tween(160), label = "ctx"
                )
                Surface(
                    onClick = { onSelect(opt) },
                    shape = CircleShape,
                    color = chipBg,
                    modifier = if (!isSelected)
                        Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    else Modifier
                ) {
                    Text(
                        text = opt,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = chipText,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// ─── Content area ─────────────────────────────────────────────────────────────

@Composable
private fun ContentArea(
    uiState: JobsUiState,
    filter: JobFilter,
    viewModel: JobsViewModel
) {
    when (uiState) {
        is JobsUiState.Loading,
        is JobsUiState.Scraping -> ScrapingState()

        is JobsUiState.Success -> {
            if (uiState.isRefreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (uiState.jobs.isEmpty()) {
                EmptyFilter(filter)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.jobs, key = { it.id }) { job ->
                        JobCard(job)
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        is JobsUiState.Empty -> ScrapingState()

        is JobsUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("⚠️", style = MaterialTheme.typography.displaySmall)
                    Text(uiState.message, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                    Button(onClick = viewModel::refresh) { Text("Retry") }
                }
            }
        }
    }
}

// ─── Job Card — premium design ────────────────────────────────────────────────

@Composable
private fun JobCard(job: JobEntity) {
    val context = LocalContext.current
    val accent  = avatarColor(job.company)
    val badge   = job.badgeStyle()

    Card(
        onClick = {
            if (job.applyUrl.isNotBlank())
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(job.applyUrl)))
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Card header ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Company avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = job.company.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accent
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    // Company + badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = job.company,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        // Category badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badge.bg
                        ) {
                            Text(
                                text = badge.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badge.text,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Role — the most important text
                    Text(
                        text = job.role,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 20.sp
                    )
                }
            }

            // ── Meta tags row ─────────────────────────────────────────────────
            if (job.location.isNotBlank() || job.batchYears.isNotBlank() || job.salary.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (job.location.isNotBlank()) MetaTag(job.location.take(22))
                    if (job.batchYears.isNotBlank()) MetaTag("Batch ${job.batchYears.take(12)}")
                    if (job.salary.isNotBlank()) MetaTag(job.salary.take(12))
                }
            }

            // ── Apply footer ──────────────────────────────────────────────────
            if (job.applyUrl.isNotBlank()) {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Apply Now →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaTag(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ─── States ───────────────────────────────────────────────────────────────────

@Composable
private fun ScrapingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Fetching jobs from TheJobCompany…",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "This takes about 30 seconds on first launch.\nYour data is cached after.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            // Shimmer cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .shimmer()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFilter(filter: JobFilter) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("🔍", fontSize = 40.sp)
            Text(
                text = "No ${filter.type} roles\nfor batch ${filter.batch}",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Try a different filter",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
