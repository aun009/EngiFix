package com.example.auth.presentation.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun EngiFixIntroOverlay(
    modifier: Modifier = Modifier
) {
    val motion = rememberMotionPolicy()
    var visible by remember { mutableStateOf(true) }
    val interactionSource = remember { MutableInteractionSource() }
    val progress by animateFloatAsState(
        targetValue = if (visible && motion.enabled) 1f else 0f,
        animationSpec = tween(motion.duration(1450), easing = FastOutSlowInEasing),
        label = "intro_progress"
    )

    LaunchedEffect(motion.enabled) {
        delay(if (motion.enabled) 1850L else 220L)
        visible = false
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(motion.duration(260))),
        exit = fadeOut(tween(motion.duration(360))),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { visible = false }
                )
        ) {
            if (motion.enabled) {
                IntroCanvas()
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }

            TextButton(
                onClick = { visible = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 42.dp, end = 16.dp)
            ) {
                Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = slideInVertically(
                        animationSpec = tween(motion.duration(650), easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 4 }
                    ) + fadeIn(tween(motion.duration(650))),
                    exit = slideOutVertically(
                        animationSpec = tween(motion.duration(220)),
                        targetOffsetY = { -it / 6 }
                    ) + fadeOut(tween(motion.duration(220)))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
                        ) {
                            Text(
                                text = "ENGINEERING CAREER OS",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "EngiFix",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Practice / Rank / Apply",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IntroChip("DSA")
                            IntroChip("Resume")
                            IntroChip("Peers")
                        }
                        Spacer(Modifier.height(18.dp))
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(180.dp)
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroChip(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun IntroCanvas() {
    val transition = rememberInfiniteTransition(label = "intro_motion")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "intro_drift"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "intro_pulse"
    )

    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFFF7F4EC),
                    Color(0xFFFFFCF5),
                    Color(0xFFEAF3EF),
                    Color(0xFFFFE0D0)
                )
            )
        )

        val gridColor = Color(0x1A161616)
        val step = 36.dp.toPx()
        var x = -step + (drift * step)
        while (x < size.width + step) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += step
        }
        var y = -step + (drift * step)
        while (y < size.height + step) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += step
        }

        rotate(degrees = -12f + drift * 24f, pivot = center) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0x00C75F3A),
                        Color(0xCCC75F3A),
                        Color(0x66287C7A),
                        Color(0x00C75F3A)
                    )
                ),
                topLeft = Offset(size.width * 0.14f, size.height * 0.46f),
                size = Size(size.width * 0.72f * pulse, 4.dp.toPx()),
                cornerRadius = CornerRadius(100f, 100f)
            )
        }

        val nodes = listOf(
            Offset(size.width * 0.18f, size.height * 0.34f),
            Offset(size.width * 0.38f, size.height * 0.27f),
            Offset(size.width * 0.67f, size.height * 0.36f),
            Offset(size.width * 0.82f, size.height * 0.26f)
        )

        nodes.zipWithNext().forEachIndexed { index, pair ->
            drawLine(
                color = if (index % 2 == 0) Color(0x66287C7A) else Color(0x66C75F3A),
                start = pair.first,
                end = pair.second,
                strokeWidth = 3.dp.toPx()
            )
        }

        nodes.forEachIndexed { index, offset ->
            val nodePulse = 1f + (pulse - 1f) * (0.5f + index * 0.12f)
            drawCircle(
                color = Color.White.copy(alpha = 0.88f),
                radius = 18.dp.toPx() * nodePulse,
                center = offset
            )
            drawCircle(
                color = if (index % 2 == 0) Color(0xFF287C7A) else Color(0xFFC75F3A),
                radius = 18.dp.toPx() * nodePulse,
                center = offset,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = if (index % 2 == 0) Color(0xFF287C7A) else Color(0xFFC75F3A),
                radius = 5.dp.toPx() * nodePulse,
                center = offset
            )
        }

        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(size.width * 0.16f, size.height * 0.68f),
            size = Size(size.width * 0.68f, 64.dp.toPx()),
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
