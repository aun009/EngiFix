package com.example.auth.presentation.features.project

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.presentation.components.EngiFixBackground
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectCanvasScreen(onBackClick: () -> Unit) {
    var selectedTool by remember { mutableStateOf(CanvasTool.Select) }
    val defaultInk = MaterialTheme.colorScheme.onSurface
    val paletteColors = remember(defaultInk) { canvasColors(defaultInk) }
    var selectedColor by remember(defaultInk) { mutableStateOf(defaultInk) }
    var strokeWidth by remember { mutableFloatStateOf(5f) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var elements by remember { mutableStateOf<List<SketchElement>>(emptyList()) }
    var activePoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedElementId by remember { mutableStateOf<Long?>(null) }
    var draggingElementId by remember { mutableStateOf<Long?>(null) }
    var nextElementId by remember { mutableLongStateOf(1L) }
    var pendingTextPoint by remember { mutableStateOf<Offset?>(null) }
    var textDraft by remember { mutableStateOf("") }
    var selectionHintPending by remember { mutableStateOf(false) }
    var selectionDebounceToken by remember { mutableLongStateOf(0L) }

    val currentElements by rememberUpdatedState(elements)
    val currentPan by rememberUpdatedState(pan)
    val currentZoom by rememberUpdatedState(zoom)
    val currentSelectedElementId by rememberUpdatedState(selectedElementId)

    fun queueSelectToolAfterIdle() {
        selectionHintPending = true
        selectionDebounceToken++
    }

    fun resetSelectionDebounce() {
        if (selectionHintPending) {
            selectionHintPending = false
            selectionDebounceToken++
        }
    }

    LaunchedEffect(selectionDebounceToken, selectionHintPending) {
        if (!selectionHintPending) return@LaunchedEffect
        delay(SELECT_TOOL_DEBOUNCE_MS)
        selectedTool = CanvasTool.Select
        selectionHintPending = false
    }

    fun addElement(tool: CanvasTool, points: List<Offset>, text: String = "") {
        val cleanPoints = points.sanitizeFor(tool)
        if (tool != CanvasTool.Text && cleanPoints.isTooSmallShape()) return
        if (tool == CanvasTool.Text && text.trim().isBlank()) return

        val element = SketchElement(
            id = nextElementId++,
            tool = tool,
            points = cleanPoints,
            color = selectedColor,
            strokeWidth = strokeWidth,
            text = text.trim()
        )
        elements = elements + element
        selectedElementId = element.id
        queueSelectToolAfterIdle()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Canvas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        elements = elements.dropLast(1)
                        selectedElementId = null
                    }, enabled = elements.isNotEmpty()) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = {
                        elements = if (selectedElementId != null) {
                            elements.filterNot { it.id == selectedElementId }
                        } else {
                            emptyList()
                        }
                        selectedElementId = null
                    }, enabled = elements.isNotEmpty()) {
                        Icon(Icons.Default.Delete, contentDescription = if (selectedElementId != null) "Delete selected" else "Clear")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        EngiFixBackground(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CanvasToolbar(
                    selectedTool = selectedTool,
                    selectedColor = selectedColor,
                    paletteColors = paletteColors,
                    strokeWidth = strokeWidth,
                    onToolSelected = {
                        selectedTool = it
                        selectionHintPending = false
                        if (it != CanvasTool.Select) selectedElementId = null
                    },
                    onColorSelected = { selectedColor = it },
                    onStrokeWidthChange = { strokeWidth = it }
                )

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(selectedTool) {
                                    detectTapGestures { tap ->
                                        resetSelectionDebounce()
                                        val worldTap = tap.toWorld(currentPan, currentZoom)
                                        when (selectedTool) {
                                            CanvasTool.Text -> {
                                                pendingTextPoint = worldTap
                                                textDraft = ""
                                            }
                                            CanvasTool.Select -> selectedElementId = currentElements.hitTest(worldTap, currentZoom)
                                            else -> Unit
                                        }
                                    }
                                }
                                .pointerInput(selectedTool) {
                                    detectDragGestures(
                                        onDragStart = { point ->
                                            resetSelectionDebounce()
                                            val worldPoint = point.toWorld(currentPan, currentZoom)
                                            when (selectedTool) {
                                                CanvasTool.Select -> {
                                                    val hitId = currentElements.hitTest(worldPoint, currentZoom)
                                                    selectedElementId = hitId
                                                    draggingElementId = hitId
                                                }
                                                CanvasTool.Hand -> Unit
                                                CanvasTool.Text -> Unit
                                                else -> activePoints = listOf(worldPoint)
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            when (selectedTool) {
                                                CanvasTool.Select -> {
                                                    val id = draggingElementId
                                                        ?: currentSelectedElementId
                                                        ?: return@detectDragGestures
                                                    val delta = dragAmount / currentZoom
                                                    selectedElementId = id
                                                    elements = currentElements.map { if (it.id == id) it.moveBy(delta) else it }
                                                }
                                                CanvasTool.Hand -> pan = currentPan + dragAmount
                                                CanvasTool.Text -> Unit
                                                else -> {
                                                    val nextPoint = change.position.toWorld(currentPan, currentZoom)
                                                    activePoints = if (selectedTool == CanvasTool.Pen) {
                                                        activePoints + nextPoint
                                                    } else {
                                                        listOf(activePoints.firstOrNull() ?: nextPoint, nextPoint)
                                                    }
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            if (activePoints.size > 1) {
                                                addElement(selectedTool, activePoints)
                                            }
                                            activePoints = emptyList()
                                            draggingElementId = null
                                        },
                                        onDragCancel = {
                                            activePoints = emptyList()
                                            draggingElementId = null
                                        }
                                    )
                                }
                        ) {
                            elements.forEach { element ->
                                drawSketchElement(
                                    element = element,
                                    zoom = zoom,
                                    pan = pan,
                                    selected = element.id == selectedElementId,
                                    dragging = element.id == draggingElementId
                                )
                            }
                            if (activePoints.size > 1) {
                                drawSketchElement(
                                    element = SketchElement(
                                        id = -1L,
                                        tool = selectedTool,
                                        points = activePoints,
                                        color = selectedColor.copy(alpha = 0.72f),
                                        strokeWidth = strokeWidth
                                    ),
                                    zoom = zoom,
                                    pan = pan,
                                    selected = false,
                                    dragging = false
                                )
                            }
                        }

                        ZoomControls(
                            zoom = zoom,
                            onZoomOut = { zoom = (zoom - 0.15f).coerceIn(MIN_ZOOM, MAX_ZOOM) },
                            onZoomIn = { zoom = (zoom + 0.15f).coerceIn(MIN_ZOOM, MAX_ZOOM) },
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                        )

                        if (selectionHintPending) {
                            SelectionDelayHint(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    pendingTextPoint?.let { point ->
        ModalBottomSheet(
            onDismissRequest = { pendingTextPoint = null },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Add text", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = textDraft,
                    onValueChange = { if (it.length <= 80) textDraft = it },
                    placeholder = { Text("Type on canvas") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { pendingTextPoint = null },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            addElement(CanvasTool.Text, listOf(point), textDraft)
                            pendingTextPoint = null
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CanvasToolbar(
    selectedTool: CanvasTool,
    selectedColor: Color,
    paletteColors: List<Color>,
    strokeWidth: Float,
    onToolSelected: (CanvasTool) -> Unit,
    onColorSelected: (Color) -> Unit,
    onStrokeWidthChange: (Float) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ToolButton(CanvasTool.Select, Icons.Default.SelectAll, selectedTool, onToolSelected)
                ToolButton(CanvasTool.Hand, Icons.Default.PanTool, selectedTool, onToolSelected)
                ToolButton(CanvasTool.Pen, Icons.Default.Edit, selectedTool, onToolSelected)
                ToolButton(CanvasTool.Rectangle, Icons.Default.CheckBoxOutlineBlank, selectedTool, onToolSelected)
                ToolButton(CanvasTool.Ellipse, Icons.Default.RadioButtonUnchecked, selectedTool, onToolSelected)
                ToolButton(CanvasTool.Arrow, Icons.AutoMirrored.Filled.ArrowForward, selectedTool, onToolSelected)
                ToolButton(CanvasTool.Text, Icons.Default.TextFields, selectedTool, onToolSelected)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                CanvasPalette(selectedColor, paletteColors, onColorSelected)
                Spacer(Modifier.weight(1f))
                Text(
                    selectedTool.label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Stroke", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(10.dp))
                Slider(
                    value = strokeWidth,
                    onValueChange = onStrokeWidthChange,
                    valueRange = 2f..12f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ToolButton(
    tool: CanvasTool,
    icon: ImageVector,
    selectedTool: CanvasTool,
    onToolSelected: (CanvasTool) -> Unit
) {
    if (tool == selectedTool) {
        FilledIconButton(onClick = { onToolSelected(tool) }, modifier = Modifier.size(34.dp)) {
            Icon(icon, contentDescription = tool.label, modifier = Modifier.size(18.dp))
        }
    } else {
        OutlinedIconButton(onClick = { onToolSelected(tool) }, modifier = Modifier.size(34.dp)) {
            Icon(icon, contentDescription = tool.label, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun CanvasPalette(
    selectedColor: Color,
    colors: List<Color>,
    onColorSelected: (Color) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color, CircleShape)
                    .border(
                        width = if (color == selectedColor) 3.dp else 1.dp,
                        color = if (color == selectedColor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

@Composable
private fun ZoomControls(
    zoom: Float,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onZoomOut, modifier = Modifier.size(32.dp), enabled = zoom > MIN_ZOOM) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom out", modifier = Modifier.size(17.dp))
            }
            Text("${(zoom * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onZoomIn, modifier = Modifier.size(32.dp), enabled = zoom < MAX_ZOOM) {
                Icon(Icons.Default.Add, contentDescription = "Zoom in", modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun SelectionDelayHint(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(0.86f),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                "Select tool will activate after a short pause",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

private fun DrawScope.drawSketchElement(
    element: SketchElement,
    zoom: Float,
    pan: Offset,
    selected: Boolean,
    dragging: Boolean
) {
    val screenPoints = element.points.map { it.toScreen(pan, zoom) }
    val scaledStroke = (element.strokeWidth * zoom).coerceAtLeast(1f)
    val stroke = Stroke(width = scaledStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)

    when (element.tool) {
        CanvasTool.Pen -> {
            if (screenPoints.size < 2) return
            val path = Path().apply {
                moveTo(screenPoints.first().x, screenPoints.first().y)
                screenPoints.drop(1).forEach { point -> lineTo(point.x, point.y) }
            }
            drawPath(path = path, color = element.color, style = stroke)
        }
        CanvasTool.Rectangle -> {
            val bounds = screenPoints.bounds() ?: return
            drawRoundRect(
                color = element.color,
                topLeft = bounds.first,
                size = bounds.second,
                cornerRadius = CornerRadius(14f * zoom, 14f * zoom),
                style = stroke
            )
        }
        CanvasTool.Ellipse -> {
            val bounds = screenPoints.bounds() ?: return
            drawOval(color = element.color, topLeft = bounds.first, size = bounds.second, style = stroke)
        }
        CanvasTool.Arrow -> drawArrow(screenPoints, element.color, scaledStroke)
        CanvasTool.Text -> drawTextElement(element, screenPoints.firstOrNull() ?: return, zoom)
        CanvasTool.Select,
        CanvasTool.Hand -> Unit
    }

    if (selected) {
        element.selectionBounds(zoom, pan)?.let { (topLeft, size) ->
            if (dragging) {
                drawRoundRect(
                    color = Color(0x335E6AD2),
                    topLeft = topLeft - Offset(3f, 3f),
                    size = Size(size.width + 6f, size.height + 6f),
                    cornerRadius = CornerRadius(10f, 10f),
                    style = Stroke(width = 6f)
                )
            }
            drawRoundRect(
                color = Color(0xFF5E6AD2),
                topLeft = if (dragging) topLeft - Offset(2f, 2f) else topLeft,
                size = if (dragging) Size(size.width + 4f, size.height + 4f) else size,
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = if (dragging) 3f else 2f)
            )
        }
    }
}

private fun DrawScope.drawTextElement(element: SketchElement, point: Offset, zoom: Float) {
    val textSize = (20f + element.strokeWidth * 2.2f) * zoom
    drawIntoCanvas { canvas ->
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.color.toArgb()
            this.textSize = textSize
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
        }
        element.text.lineSequence().forEachIndexed { index, line ->
            canvas.nativeCanvas.drawText(line, point.x, point.y + index * textSize * 1.25f, paint)
        }
    }
}

private fun DrawScope.drawArrow(points: List<Offset>, color: Color, strokeWidth: Float) {
    if (points.size < 2) return
    val start = points.first()
    val end = points.last()
    drawLine(color, start, end, strokeWidth = strokeWidth, cap = StrokeCap.Round)

    val angle = atan2((end.y - start.y), (end.x - start.x))
    val headLength = (strokeWidth * 4.6f).coerceAtLeast(18f)
    val left = Offset(
        x = end.x - headLength * cos(angle - PI.toFloat() / 6f),
        y = end.y - headLength * sin(angle - PI.toFloat() / 6f)
    )
    val right = Offset(
        x = end.x - headLength * cos(angle + PI.toFloat() / 6f),
        y = end.y - headLength * sin(angle + PI.toFloat() / 6f)
    )
    drawLine(color, end, left, strokeWidth = strokeWidth, cap = StrokeCap.Round)
    drawLine(color, end, right, strokeWidth = strokeWidth, cap = StrokeCap.Round)
}

private fun List<Offset>.bounds(): Pair<Offset, Size>? {
    if (size < 2) return null
    val start = first()
    val end = last()
    val left = min(start.x, end.x)
    val top = min(start.y, end.y)
    return Offset(left, top) to Size(abs(end.x - start.x), abs(end.y - start.y))
}

private fun SketchElement.selectionBounds(zoom: Float, pan: Offset): Pair<Offset, Size>? {
    val worldBounds = worldBounds() ?: return null
    val topLeft = worldBounds.first.toScreen(pan, zoom) - Offset(8f, 8f)
    val size = Size(worldBounds.second.width * zoom + 16f, worldBounds.second.height * zoom + 16f)
    return topLeft to size
}

private fun SketchElement.worldBounds(): Pair<Offset, Size>? {
    if (tool == CanvasTool.Text) {
        val anchor = points.firstOrNull() ?: return null
        val textSize = 20f + strokeWidth * 2.2f
        val lines = text.lineSequence().toList().ifEmpty { listOf(text) }
        val width = lines.maxOf { it.length }.coerceAtLeast(1) * textSize * 0.58f
        val height = lines.size * textSize * 1.25f
        return anchor + Offset(0f, -textSize) to Size(width, height + textSize * 0.35f)
    }

    if (points.isEmpty()) return null
    val left = points.minOf { it.x }
    val top = points.minOf { it.y }
    val right = points.maxOf { it.x }
    val bottom = points.maxOf { it.y }
    return Offset(left, top) to Size((right - left).coerceAtLeast(1f), (bottom - top).coerceAtLeast(1f))
}

private fun List<SketchElement>.hitTest(point: Offset, zoom: Float): Long? {
    val padding = 18f / zoom
    return asReversed().firstOrNull { element ->
        val bounds = element.worldBounds() ?: return@firstOrNull false
        point.x in (bounds.first.x - padding)..(bounds.first.x + bounds.second.width + padding) &&
                point.y in (bounds.first.y - padding)..(bounds.first.y + bounds.second.height + padding)
    }?.id
}

private fun SketchElement.moveBy(delta: Offset): SketchElement =
    copy(points = points.map { it + delta })

private fun List<Offset>.sanitizeFor(tool: CanvasTool): List<Offset> {
    if (tool == CanvasTool.Pen || size <= 2) return this
    return listOf(first(), last())
}

private fun List<Offset>.isTooSmallShape(): Boolean {
    if (size < 2) return true
    val bounds = bounds() ?: return true
    return bounds.second.width < 3f && bounds.second.height < 3f
}

private fun Offset.toWorld(pan: Offset, zoom: Float): Offset =
    (this - pan) / zoom

private fun Offset.toScreen(pan: Offset, zoom: Float): Offset =
    this * zoom + pan

private enum class CanvasTool(val label: String) {
    Select("Select / move"),
    Hand("Pan"),
    Pen("Draw"),
    Rectangle("Rectangle"),
    Ellipse("Circle"),
    Arrow("Arrow"),
    Text("Text")
}

private data class SketchElement(
    val id: Long,
    val tool: CanvasTool,
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val text: String = ""
)

private const val MIN_ZOOM = 0.45f
private const val MAX_ZOOM = 2.5f
private const val SELECT_TOOL_DEBOUNCE_MS = 2400L

private fun canvasColors(defaultInk: Color) = listOf(
    defaultInk,
    Color(0xFFC75F3A),
    Color(0xFF287C7A),
    Color(0xFF5E6AD2),
    Color(0xFFE0A82E)
)
