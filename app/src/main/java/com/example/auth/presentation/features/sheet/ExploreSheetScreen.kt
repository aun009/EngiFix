package com.example.auth.presentation.features.sheet

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.auth.data.local.entity.ExploreSheetEntity
import com.valentinilk.shimmer.shimmer

// ── Category filter options ───────────────────────────────────────────────────
private val categories = listOf("All", "DSA", "SQL", "System Design")

// ── Sheet accent colours (stable, index-mapped) ───────────────────────────────
private val sheetAccents = listOf(
    Color(0xFF6C5CE7),   // purple   — NeetCode 150
    Color(0xFF00B894),   // teal     — Striver A2Z
    Color(0xFF0984E3),   // blue     — Blind 75
    Color(0xFFE17055),   // coral    — SQL 50
    Color(0xFFFDAB33),   // amber    — System Design
    Color(0xFFFF4757),   // red      — Love Babbar
)

private val sheetEmojis = listOf("⚡", "🔥", "🎯", "🗄️", "🏗️", "💪")

@Composable
fun ExploreSheetScreen(
    onSheetClick: (String) -> Unit = {}
) {
    val viewModel: SheetListViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is SheetListUiState.Loading -> {
                ScreenHeader(sheets = emptyList(), selectedCategory = selectedCategory, onCategoryChange = {})
                SheetListShimmer()
            }

            is SheetListUiState.Success -> {
                val filtered = if (selectedCategory == "All") state.sheets
                else state.sheets.filter { it.category == selectedCategory }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // ── Sticky header: title + stats + filter ──
                    item {
                        ScreenHeader(
                            sheets         = state.sheets,
                            selectedCategory = selectedCategory,
                            onCategoryChange = { selectedCategory = it }
                        )
                    }

                    // ── Sheet cards ────────────────────────────
                    items(filtered, key = { it.id }) { sheet ->
                        val index = state.sheets.indexOf(sheet)
                        SheetCard(
                            sheet  = sheet,
                            accent = sheetAccents.getOrElse(index) { MaterialTheme.colorScheme.primary },
                            emoji  = sheetEmojis.getOrElse(index) { "📋" },
                            onClick = { onSheetClick(sheet.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            is SheetListUiState.Empty -> {
                ScreenHeader(sheets = emptyList(), selectedCategory = selectedCategory, onCategoryChange = {})
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📂", style = MaterialTheme.typography.displayMedium)
                        Spacer(Modifier.height(12.dp))
                        Text("No sheets found", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            is SheetListUiState.Error -> {
                ScreenHeader(sheets = emptyList(), selectedCategory = selectedCategory, onCategoryChange = {})
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// ─── Header: title + aggregate stats + category chips ────────────────────────

@Composable
private fun ScreenHeader(
    sheets: List<ExploreSheetEntity>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 20.dp)
    ) {
        // Title row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DSA Sheets",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Pick a sheet and start solving",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Stats pills (only when we have data)
        if (sheets.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatPill(
                    value = sheets.size.toString(),
                    label = "Sheets",
                    color = MaterialTheme.colorScheme.primary
                )
                StatPill(
                    value = sheets.sumOf { it.totalQuestions }.toString(),
                    label = "Questions",
                    color = Color(0xFF00B894)
                )
                StatPill(
                    value = sheets.map { it.category }.distinct().size.toString(),
                    label = "Topics",
                    color = Color(0xFFE17055)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Category filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    animationSpec = tween(200), label = "chip_color"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(200), label = "chip_text"
                )
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategoryChange(cat) },
                    label = {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
    }
}

// ─── Stat pill ────────────────────────────────────────────────────────────────

@Composable
private fun StatPill(value: String, label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Sheet Card ───────────────────────────────────────────────────────────────

@Composable
private fun SheetCard(
    sheet: ExploreSheetEntity,
    accent: Color,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Accent top bar ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(accent, accent.copy(alpha = 0.3f))
                        )
                    )
            )

            // ── Card body ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji avatar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = accent.copy(alpha = 0.12f),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 24.sp)
                    }
                }

                Spacer(Modifier.width(14.dp))

                // Text block
                Column(modifier = Modifier.weight(1f)) {
                    // Category badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accent.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = sheet.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = sheet.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = sheet.description,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ── Footer: question count + start button ──────────────────────────
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Question count
                    Text(
                        text = "${sheet.totalQuestions} questions",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Start button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accent
                ) {
                    Text(
                        text = "Start →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// ─── Shimmer placeholder ──────────────────────────────────────────────────────

@Composable
private fun SheetListShimmer() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(vertical = 6.dp)
                    .shimmer()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}