package com.example.auth.presentation.features.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.auth.data.local.SheetProgress
import com.valentinilk.shimmer.shimmer
import kotlin.math.roundToInt

private enum class SheetSort(val label: String) {
    Title("Title"),
    Progress("Progress"),
    Unsolved("Unsolved")
}

private val sheetAccents = listOf(
    Color(0xFF3DDC97),
    Color(0xFFFFB020),
    Color(0xFF62A8FF),
    Color(0xFFFF6B6B),
    Color(0xFFA78BFA),
    Color(0xFF4DD0E1)
)

@Composable
fun ExploreSheetScreen(
    onSheetClick: (String) -> Unit = {}
) {
    val viewModel: SheetListViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    var query by remember { mutableStateOf("") }
    var selectedSort by remember { mutableStateOf(SheetSort.Title) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val state = uiState) {
            is SheetListUiState.Loading -> {
                Column {
                    ScreenHeader(
                        sheets = emptyList(),
                        selectedCategory = selectedCategory,
                        selectedSort = selectedSort,
                        query = query,
                        isRefreshing = true,
                        syncError = null,
                        onCategoryChange = {},
                        onSortChange = {},
                        onQueryChange = {},
                        onRefresh = {}
                    )
                    SheetListShimmer()
                }
            }

            is SheetListUiState.Success -> {
                val categories = remember(state.sheets) {
                    listOf("All") + state.sheets.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
                }
                LaunchedEffect(categories) {
                    if (selectedCategory !in categories) selectedCategory = "All"
                }

                val filtered by remember(state.sheets, selectedCategory, selectedSort, query) {
                    derivedStateOf {
                        state.sheets
                            .asSequence()
                            .filter { selectedCategory == "All" || it.category == selectedCategory }
                            .filter {
                                val needle = query.trim()
                                needle.isBlank() ||
                                    it.title.contains(needle, ignoreCase = true) ||
                                    it.description.contains(needle, ignoreCase = true) ||
                                    it.category.contains(needle, ignoreCase = true)
                            }
                            .sortedWith(sheetComparator(selectedSort))
                            .toList()
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        ScreenHeader(
                            sheets = state.sheets,
                            selectedCategory = selectedCategory,
                            selectedSort = selectedSort,
                            query = query,
                            isRefreshing = state.isRefreshing,
                            syncError = state.syncError,
                            onCategoryChange = { selectedCategory = it },
                            onSortChange = { selectedSort = it },
                            onQueryChange = { query = it },
                            onRefresh = viewModel::refreshSheets
                        )
                    }

                    if (filtered.isEmpty()) {
                        item {
                            EmptyState(
                                title = "No matching sheets",
                                message = "Try a different search or clear the active filters.",
                                actionLabel = "Clear filters",
                                onAction = {
                                    query = ""
                                    selectedCategory = "All"
                                }
                            )
                        }
                    } else {
                        items(filtered, key = { it.id }) { sheet ->
                            val index = state.sheets.indexOfFirst { it.id == sheet.id }.coerceAtLeast(0)
                            SheetCard(
                                sheet = sheet,
                                accent = sheetAccents[index % sheetAccents.size],
                                icon = iconForCategory(sheet.category),
                                onClick = { onSheetClick(sheet.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            is SheetListUiState.Empty -> {
                Column {
                    ScreenHeader(
                        sheets = emptyList(),
                        selectedCategory = selectedCategory,
                        selectedSort = selectedSort,
                        query = query,
                        isRefreshing = state.isRefreshing,
                        syncError = state.syncError,
                        onCategoryChange = {},
                        onSortChange = { selectedSort = it },
                        onQueryChange = { query = it },
                        onRefresh = viewModel::refreshSheets
                    )
                    EmptyState(
                        title = "No sheets available",
                        message = "The bundled sheet data could not be loaded yet.",
                        actionLabel = "Retry",
                        onAction = viewModel::refreshSheets
                    )
                }
            }

            is SheetListUiState.Error -> {
                EmptyState(
                    title = "Sheets failed to load",
                    message = state.message,
                    actionLabel = "Retry",
                    onAction = viewModel::refreshSheets
                )
            }
        }
    }
}

@Composable
private fun ScreenHeader(
    sheets: List<SheetProgress>,
    selectedCategory: String,
    selectedSort: SheetSort,
    query: String,
    isRefreshing: Boolean,
    syncError: String?,
    onCategoryChange: (String) -> Unit,
    onSortChange: (SheetSort) -> Unit,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val categories = remember(sheets) {
        listOf("All") + sheets.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Practice Sheets",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isRefreshing) "Refreshing the latest question set" else "Track DSA, SQL, and design prep in one place",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onRefresh, enabled = !isRefreshing) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh sheets",
                    tint = if (isRefreshing) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                )
            }
            SortButton(selectedSort = selectedSort, onSortChange = onSortChange)
        }

        if (sheets.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(sheets.size.toString(), "Sheets", MaterialTheme.colorScheme.primary)
                StatPill(sheets.sumOf { it.displayQuestionCount }.toString(), "Questions", Color(0xFF3DDC97))
                StatPill(sheets.sumOf { it.completedQuestions }.toString(), "Solved", Color(0xFFFFB020))
            }
        }

        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true,
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            placeholder = { Text("Search sheets") },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
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
                    leadingIcon = if (isSelected) {
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
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = Color.Transparent
                    )
                )
            }
        }

        AnimatedVisibility(visible = syncError != null) {
            Text(
                text = syncError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
    }
}

@Composable
private fun SortButton(
    selectedSort: SheetSort,
    onSortChange: (SheetSort) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.FilterList, contentDescription = "Sort sheets")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SheetSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = { Text(sort.label) },
                    leadingIcon = if (sort == selectedSort) {
                        { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                    } else null,
                    onClick = {
                        onSortChange(sort)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StatPill(value: String, label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.14f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SheetCard(
    sheet: SheetProgress,
    accent: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = sheet.progress.coerceIn(0f, 1f)
    val percent = (progress * 100).roundToInt()
    val unsolved = (sheet.displayQuestionCount - sheet.completedQuestions).coerceAtLeast(0)

    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accent.copy(alpha = 0.14f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(26.dp))
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accent.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = sheet.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "$percent%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = sheet.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = sheet.description,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(10.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    MetaText(Icons.Default.CheckCircle, "${sheet.completedQuestions} solved")
                    MetaText(Icons.Default.PlayArrow, "$unsolved left")
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = accent
                )
            }
        }
    }
}

@Composable
private fun MetaText(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyState(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
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
private fun SheetListShimmer() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(152.dp)
                    .padding(vertical = 6.dp)
                    .shimmer()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

private fun sheetComparator(sort: SheetSort): Comparator<SheetProgress> =
    when (sort) {
        SheetSort.Title -> compareBy<SheetProgress, String>(String.CASE_INSENSITIVE_ORDER) { it.title }
        SheetSort.Progress -> compareByDescending<SheetProgress> { it.progress }
            .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
        SheetSort.Unsolved -> compareByDescending<SheetProgress> {
            (it.displayQuestionCount - it.completedQuestions).coerceAtLeast(0)
        }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
    }

private fun iconForCategory(category: String): ImageVector =
    when (category.lowercase()) {
        "sql" -> Icons.Default.Storage
        "system design" -> Icons.Default.FilterList
        else -> Icons.Default.MenuBook
    }
