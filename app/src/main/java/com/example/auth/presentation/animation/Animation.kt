package com.example.auth.presentation.animation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Star class with observable state for x and y
class Star(
    initialX: Float,
    initialY: Float,
    val radius: Float,
    val speed: Float
) {
    var x by mutableStateOf(initialX)
    var y by mutableStateOf(initialY)
}

// Shooting star class
class ShootingStar(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val duration: Float
) {
    var progress by mutableStateOf(0f)
    var isActive by mutableStateOf(false)

    val currentX: Float get() = startX + (endX - startX) * progress
    val currentY: Float get() = startY + (endY - startY) * progress

    // Calculate the angle for the tail direction
    val angle: Float get() = atan2(endY - startY, endX - startX)

    fun start() {
        isActive = true
        progress = 0f
    }

    fun update(deltaProgress: Float) {
        if (isActive) {
            progress += deltaProgress
            if (progress >= 1f) {
                isActive = false
            }
        }
    }
}

@Composable
fun AnimatedStarsBackground() {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val screenWidthPx = configuration.screenWidthDp * density
    val screenHeightPx = configuration.screenHeightDp * density
    val starCount = 30

    val stars = remember {
        mutableStateListOf<Star>().apply {
            repeat(starCount) {
                add(
                    Star(
                        initialX = Random.Default.nextFloat() * screenWidthPx,
                        initialY = Random.Default.nextFloat() * screenHeightPx,
                        radius = Random.Default.nextFloat() * 4 + 3,
                        speed = Random.Default.nextFloat() * 3 + 1
                    )
                )
            }
        }
    }

    val shootingStar = remember {
        ShootingStar(
            startX = screenWidthPx + 50f,
            startY = -50f,
            endX = -50f,
            endY = screenHeightPx + 50f,
            duration = 1.5f
        )
    }

    var lastShootingStarTime by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            val currentTime = System.currentTimeMillis()

            // Update regular stars
            stars.forEach { star ->
                star.y += star.speed
                if (star.y > screenHeightPx) {
                    star.y = 0f
                    star.x = Random.Default.nextFloat() * screenWidthPx
                }
            }

            // Trigger shooting star every 6-12 seconds randomly
            if (currentTime - lastShootingStarTime > Random.Default.nextLong(
                    6000,
                    12000
                ) && !shootingStar.isActive
            ) {
                shootingStar.start()
                lastShootingStarTime = currentTime
            }

            // Update shooting star
            if (shootingStar.isActive) {
                shootingStar.update(0.016f / shootingStar.duration) // 60fps delta
            }

            delay(16) // roughly 60fps
        }
    }

    Canvas(modifier = Modifier.Companion.fillMaxSize()) {
        // Draw realistic 5-pointed stars
        stars.forEach { star ->
            drawRealisticStar(
                center = Offset(star.x, star.y),
                outerRadius = star.radius,
                innerRadius = star.radius * 0.4f,
                color = Color.Companion.White
            )
        }

        // Draw shooting star
        if (shootingStar.isActive) {
            drawShootingStar(shootingStar)
        }
    }
}

// Function to draw a realistic 5-pointed star
// Function to draw a realistic 5-pointed star
fun DrawScope.drawRealisticStar(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    color: Color
) {
    val path = Path()
    val numPoints = 5
    val angleStep = (2 * PI / numPoints).toFloat()
    val startAngle = -PI.toFloat() / 2 // Start from top

    // Calculate star points
    val points = mutableListOf<Offset>()

    for (i in 0 until numPoints * 2) {
        val angle = startAngle + i * angleStep / 2
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + cos(angle) * radius
        val y = center.y + sin(angle) * radius
        points.add(Offset(x, y))
    }

    // Create path for the star - FIXED VERSION
    if (points.isNotEmpty()) {
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)  // Fixed: using points[i].x and points[i].y
        }
        path.close()
    }

    // Draw the star
    drawPath(
        path = path,
        color = color
    )
}


// Extension function to draw a realistic shooting star with streak
fun DrawScope.drawShootingStar(shootingStar: ShootingStar) {
    val headX = shootingStar.currentX
    val headY = shootingStar.currentY

    // Calculate tail direction (opposite to movement)
    val tailLength = 120f
    val tailX = headX - cos(shootingStar.angle) * tailLength
    val tailY = headY - sin(shootingStar.angle) * tailLength

    // Create multiple layers for the shooting star effect

    // 1. Outer glow (widest, most transparent)
    drawLine(
        brush = Brush.Companion.linearGradient(
            colors = listOf(
                Color(0x1A87CEEB), // Very transparent light blue
                Color.Companion.Transparent
            ),
            start = Offset(headX, headY),
            end = Offset(tailX, tailY)
        ),
        start = Offset(headX, headY),
        end = Offset(tailX, tailY),
        strokeWidth = 20f,
        cap = StrokeCap.Companion.Round
    )

    // 2. Middle glow
    drawLine(
        brush = Brush.Companion.linearGradient(
            colors = listOf(
                Color(0x4087CEEB), // Semi-transparent light blue
                Color.Companion.Transparent
            ),
            start = Offset(headX, headY),
            end = Offset(tailX * 0.7f + headX * 0.3f, tailY * 0.7f + headY * 0.3f)
        ),
        start = Offset(headX, headY),
        end = Offset(tailX * 0.7f + headX * 0.3f, tailY * 0.7f + headY * 0.3f),
        strokeWidth = 12f,
        cap = StrokeCap.Companion.Round
    )

    // 3. Inner bright streak
    drawLine(
        brush = Brush.Companion.linearGradient(
            colors = listOf(
                Color(0xFF4A90E2), // Bright blue
                Color(0x8087CEEB)  // Fading light blue
            ),
            start = Offset(headX, headY),
            end = Offset(tailX * 0.5f + headX * 0.5f, tailY * 0.5f + headY * 0.5f)
        ),
        start = Offset(headX, headY),
        end = Offset(tailX * 0.5f + headX * 0.5f, tailY * 0.5f + headY * 0.5f),
        strokeWidth = 6f,
        cap = StrokeCap.Companion.Round
    )

    // 4. Core bright line
    drawLine(
        color = Color(0xFF6BB6FF), // Very bright blue
        start = Offset(headX, headY),
        end = Offset(tailX * 0.3f + headX * 0.7f, tailY * 0.3f + headY * 0.7f),
        strokeWidth = 3f,
        cap = StrokeCap.Companion.Round
    )

    // 5. Bright head
    drawCircle(
        color = Color(0xFF87CEEB), // Light blue glow
        center = Offset(headX, headY),
        radius = 8f
    )

    drawCircle(
        color = Color(0xFFFFFFFF), // White hot center
        center = Offset(headX, headY),
        radius = 4f
    )
}