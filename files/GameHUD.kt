package com.campuslastday

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Colors ───────────────────────────────────────────────────────────────────

private val ColorBackground    = Color(0xCC1A1A2E)  // semi-transparent dark navy
private val ColorText          = Color(0xFFF0F0FF)
private val ColorMuted         = Color(0xFF8888AA)
private val ColorConfHigh      = Color(0xFF4CB08F)  // teal-green
private val ColorConfMid       = Color(0xFFE9A84C)  // amber
private val ColorConfLow       = Color(0xFFE05C5C)  // red
private val ColorClockNormal   = Color(0xFF5B9BD5)
private val ColorClockUrgent   = Color(0xFFE05C5C)

// ─── Top HUD ──────────────────────────────────────────────────────────────────

/**
 * Fixed overlay at the top of the screen.
 * Shows: countdown timer on left, confidence bar on right.
 */
@Composable
fun GameHUD(
    secondsRemaining: Int,
    confidence: Int,
    currentLocation: LocationId?,
    modifier: Modifier = Modifier
) {
    val isUrgent = secondsRemaining < 3600 // less than 1 hour = urgent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ColorBackground, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Left: Countdown ─────────────────────────────────────────────
        CountdownClock(secondsRemaining = secondsRemaining, isUrgent = isUrgent)

        // ── Center: Current location tag ────────────────────────────────
        currentLocation?.let { locId ->
            val loc = CampusData.locationMap[locId]
            if (loc != null) {
                Text(
                    text = "${loc.emoji} ${loc.name}",
                    fontSize = 11.sp,
                    color = ColorMuted,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Right: Confidence bar ────────────────────────────────────────
        ConfidenceBar(confidence = confidence)
    }
}

// ─── Countdown ────────────────────────────────────────────────────────────────

@Composable
fun CountdownClock(secondsRemaining: Int, isUrgent: Boolean) {
    val h = secondsRemaining / 3600
    val m = (secondsRemaining % 3600) / 60
    val s = secondsRemaining % 60
    val label = "%02d:%02d:%02d".format(h, m, s)

    val color by animateColorAsState(
        targetValue = if (isUrgent) ColorClockUrgent else ColorClockNormal,
        animationSpec = tween(600),
        label = "clockColor"
    )

    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = "Placements in",
            fontSize = 10.sp,
            color = ColorMuted,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = label,
            fontSize = 20.sp,
            color = color,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

// ─── Confidence bar ───────────────────────────────────────────────────────────

@Composable
fun ConfidenceBar(confidence: Int) {
    val progress by animateFloatAsState(
        targetValue = confidence / 100f,
        animationSpec = tween(500),
        label = "confidenceProgress"
    )

    val barColor by animateColorAsState(
        targetValue = when {
            confidence >= 60 -> ColorConfHigh
            confidence >= 30 -> ColorConfMid
            else             -> ColorConfLow
        },
        animationSpec = tween(600),
        label = "confidenceColor"
    )

    val label = when {
        confidence >= 80 -> "Confident 💪"
        confidence >= 60 -> "Okay-ish 🤔"
        confidence >= 40 -> "Nervous 😅"
        confidence >= 20 -> "Panicking 😨"
        else             -> "Send help 🆘"
    }

    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = ColorMuted,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .width(110.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = Color.White.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}
