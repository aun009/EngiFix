package com.campuslastday

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

// ─── Color palette ────────────────────────────────────────────────────────────

private val ColorGrass      = Color(0xFFD4EDDA)
private val ColorPath       = Color(0xFFE9D8B0)
private val ColorPlayer     = Color(0xFF3A3A6E)
private val ColorPlayerRing = Color(0xFF6C6CB4)
private val ColorProximity  = Color(0x336C6CB4)

private val locationColors = mapOf(
    LocationId.LIBRARY       to Color(0xFF5B9BD5),
    LocationId.PLACEMENT_CELL to Color(0xFFE9A84C),
    LocationId.H_MESS        to Color(0xFF4CB08F),
    LocationId.CSE_DEPT      to Color(0xFF9B59B6),
    LocationId.GROUND        to Color(0xFF27AE60),
    LocationId.HOSTEL_ROOM   to Color(0xFFE05C5C)
)

// ─── Composable ───────────────────────────────────────────────────────────────

/**
 * The game world canvas.
 *
 * Coordinate system: all location data lives in 0..1000f map space.
 * This composable scales everything to actual screen pixels via [scaleX/scaleY].
 *
 * Usage:
 *   MapCanvas(
 *       playerPos   = viewModel.state.player.position,
 *       locations   = CampusData.locations,
 *       onCanvasReady = viewModel::onCanvasSizeChanged,
 *       onDrag      = { delta -> viewModel.onEvent(GameEvent.PlayerDragged(delta)) },
 *       modifier    = Modifier.fillMaxSize()
 *   )
 */
@Composable
fun MapCanvas(
    playerPos: Offset,
    locations: List<CampusLocation>,
    visitedLocations: Set<LocationId>,
    onCanvasReady: (width: Float, height: Float) -> Unit,
    onDrag: (delta: Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    // Scale factors — recalculated when canvas size changes
    var scaleX = remember { 1f }
    var scaleY = remember { 1f }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    // Pass raw screen delta — ViewModel handles the scale conversion
                    onDrag(dragAmount)
                }
            }
    ) {
        // ── On first draw / resize, report canvas size to ViewModel ────────
        scaleX = size.width / 1000f
        scaleY = size.height / 1000f
        onCanvasReady(size.width, size.height)

        // ── Ground (grass base) ────────────────────────────────────────────
        drawRect(color = ColorGrass, size = size)

        // ── Paths between locations (visual roads) ─────────────────────────
        drawMapPaths(scaleX, scaleY)

        // ── Location circles ───────────────────────────────────────────────
        for (location in locations) {
            val screenCenter = location.center.toScreen(scaleX, scaleY)
            val screenRadius = location.radius * ((scaleX + scaleY) / 2f)
            val color = locationColors[location.id] ?: Color.Gray
            val visited = location.id in visitedLocations

            // Proximity ring (shows before player enters)
            drawCircle(
                color = ColorProximity,
                radius = screenRadius * 1.4f,
                center = screenCenter
            )

            // Location body
            drawCircle(
                color = if (visited) color.copy(alpha = 0.5f) else color,
                radius = screenRadius,
                center = screenCenter
            )

            // Border ring
            drawCircle(
                color = color.copy(alpha = 0.9f),
                radius = screenRadius,
                center = screenCenter,
                style = Stroke(width = 2.dp.toPx())
            )

            // Visited tick mark overlay
            if (visited) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = screenRadius * 0.6f,
                    center = screenCenter
                )
            }

            // Location label
            drawLocationLabel(screenCenter, location.name, location.emoji, scaleX)
        }

        // ── Player dot ────────────────────────────────────────────────────
        val screenPlayerPos = playerPos.toScreen(scaleX, scaleY)
        val playerRadius = 14.dp.toPx()

        // Shadow
        drawCircle(
            color = Color.Black.copy(alpha = 0.15f),
            radius = playerRadius + 3f,
            center = screenPlayerPos.copy(y = screenPlayerPos.y + 4f)
        )

        // Body
        drawCircle(
            color = ColorPlayer,
            radius = playerRadius,
            center = screenPlayerPos
        )

        // Pulse ring
        drawCircle(
            color = ColorPlayerRing,
            radius = playerRadius,
            center = screenPlayerPos,
            style = Stroke(width = 2.dp.toPx())
        )

        // White dot (eye feel)
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = 4.dp.toPx(),
            center = screenPlayerPos.copy(
                x = screenPlayerPos.x - 3f,
                y = screenPlayerPos.y - 3f
            )
        )
    }
}

// ─── Draw helpers ─────────────────────────────────────────────────────────────

/**
 * Draws simple dashed paths between locations — gives the map a campus road feel.
 */
private fun DrawScope.drawMapPaths(scaleX: Float, scaleY: Float) {
    val pathColor = ColorPath

    fun line(from: Offset, to: Offset) {
        drawLine(
            color = pathColor,
            start = from.toScreen(scaleX, scaleY),
            end = to.toScreen(scaleX, scaleY),
            strokeWidth = 12.dp.toPx()
        )
    }

    // Horizontal spine
    line(Offset(180f, 220f), Offset(820f, 220f))
    // Vertical spine
    line(Offset(500f, 180f), Offset(500f, 600f))
    // Side verticals
    line(Offset(180f, 220f), Offset(180f, 550f))
    line(Offset(820f, 220f), Offset(820f, 550f))
    // Bottom cross
    line(Offset(180f, 550f), Offset(820f, 550f))
    // Gate path
    line(Offset(500f, 600f), Offset(500f, 900f))
}

/**
 * Draws a text label + emoji below each location circle.
 * Uses Android's native canvas for text because Compose Canvas
 * doesn't have a built-in text draw API (without TextMeasurer).
 */
private fun DrawScope.drawLocationLabel(
    center: Offset,
    name: String,
    emoji: String,
    scaleX: Float
) {
    val labelY = center.y + 52.dp.toPx()
    val fontSize = (11f * scaleX.coerceIn(0.8f, 1.4f)).sp.toPx()

    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#3A3A6E")
            textSize = 28f * scaleX.coerceIn(0.7f, 1.2f)
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        // Emoji
        canvas.nativeCanvas.drawText(emoji, center.x, center.y + 14f, paint)

        // Name
        paint.textSize = 22f * scaleX.coerceIn(0.7f, 1.2f)
        paint.color = android.graphics.Color.parseColor("#2C2C4A")
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        canvas.nativeCanvas.drawText(name, center.x, labelY, paint)
    }
}

// ─── Extension ────────────────────────────────────────────────────────────────

/**
 * Converts map coordinates (0..1000f) to screen coordinates.
 */
fun Offset.toScreen(scaleX: Float, scaleY: Float): Offset =
    Offset(x * scaleX, y * scaleY)

// Needed for fontSize calc above — avoids importing all of TextUnit
private val Float.sp: androidx.compose.ui.unit.TextUnit
    get() = androidx.compose.ui.unit.TextUnit(this, androidx.compose.ui.unit.TextUnitType.Sp)

private fun Float.sp.toPx(): Float = this.value * 2.5f // approx sp→px for canvas
