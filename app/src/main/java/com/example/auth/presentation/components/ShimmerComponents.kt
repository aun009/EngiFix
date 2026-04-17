package com.example.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer

/**
 * A single "MentorCard" shimmer skeleton.
 * Display several of these stacked when Mentorship data is loading.
 */
@Composable
fun MentorCardShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shimmer()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(20.dp)
        ) {
            // Header row: avatar + name/title
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    // Name bar
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                    Spacer(Modifier.height(8.dp))
                    // Title bar
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Skill chips row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Description lines
            repeat(2) { line ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (line == 1) 0.7f else 1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.outline)
                )
                Spacer(Modifier.height(6.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Price + CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.outline)
                )
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        }
    }
}

/**
 * Full screen mentor loading state — shows 3 stacked card skeletons.
 */
@Composable
fun MentorListShimmer() {
    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
    ) {
        items(3) {
            MentorCardShimmer()
        }
    }
}

/**
 * A single "ContestCard" shimmer skeleton.
 */
@Composable
fun ContestCardShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shimmer()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(20.dp)
        ) {
            // Platform header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.outline)
                )
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(MaterialTheme.colorScheme.outline)
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.outline)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.outline)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.outline)
            )

            Spacer(Modifier.height(20.dp))

            // Start / End time row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MaterialTheme.colorScheme.outline)
                    )
                }
            }
        }
    }
}

/**
 * Full screen contest loading state — shows 4 stacked card skeletons.
 */
@Composable
fun ContestListShimmer() {
    LazyColumn(
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
    ) {
        items(4) {
            ContestCardShimmer()
        }
    }
}
