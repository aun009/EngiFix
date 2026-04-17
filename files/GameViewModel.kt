package com.campuslastday

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class GameViewModel : ViewModel() {

    // ─── State ────────────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    // Tracks which dialog index we're on per location
    private val dialogIndexMap = mutableMapOf<LocationId, Int>()

    // Canvas bounds — set once when the Canvas is measured
    private var canvasWidth = 1000f
    private var canvasHeight = 1000f

    // Map coordinate space is always 0..1000f
    // Actual screen coords are scaled by (screenSize / 1000f)
    private var scaleX = 1f
    private var scaleY = 1f

    private var countdownJob: Job? = null

    init {
        startCountdown()
    }

    // ─── Canvas scaling ───────────────────────────────────────────────────────

    fun onCanvasSizeChanged(width: Float, height: Float) {
        canvasWidth = width
        canvasHeight = height
        scaleX = width / 1000f
        scaleY = height / 1000f
    }

    // Convert screen drag delta → map coordinate delta
    private fun screenDeltaToMap(delta: Offset): Offset =
        Offset(delta.x / scaleX, delta.y / scaleY)

    // ─── Event handler ────────────────────────────────────────────────────────

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.PlayerDragged      -> handleDrag(event.delta)
            is GameEvent.LocationEntered    -> handleLocationEntered(event.location)
            is GameEvent.DialogDismissed    -> handleDialogDismissed(event.locationId)
            is GameEvent.GameEndAcknowledged -> Unit // navigate away — handled in UI
        }
    }

    // ─── Movement + proximity ─────────────────────────────────────────────────

    private fun handleDrag(screenDelta: Offset) {
        if (_state.value.phase != GamePhase.EXPLORING) return

        val mapDelta = screenDeltaToMap(screenDelta)
        val current = _state.value.player.position

        // Clamp within map bounds (50f padding from edges)
        val newPos = Offset(
            x = (current.x + mapDelta.x).coerceIn(50f, 950f),
            y = (current.y + mapDelta.y).coerceIn(50f, 950f)
        )

        _state.update { it.copy(player = it.player.copy(position = newPos)) }

        // Check proximity to every location
        checkProximity(newPos)
    }

    private fun checkProximity(playerPos: Offset) {
        val state = _state.value
        if (state.phase != GamePhase.EXPLORING) return

        for (location in CampusData.locations) {
            val dist = distance(playerPos, location.center)
            if (dist < location.radius && location.id !in state.player.visitedLocations) {
                // Player just walked into an unvisited location
                _state.update {
                    it.copy(
                        player = it.player.copy(
                            currentLocation = location.id,
                            visitedLocations = it.player.visitedLocations + location.id,
                            confidence = (it.player.confidence + location.confidenceDelta)
                                .coerceIn(0, 100)
                        )
                    )
                }
                triggerDialog(location)
                break
            }
        }
    }

    private fun distance(a: Offset, b: Offset): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }

    // ─── Dialog ───────────────────────────────────────────────────────────────

    private fun triggerDialog(location: CampusLocation) {
        val index = dialogIndexMap.getOrDefault(location.id, 0)
        val dialog = location.dialogs.getOrNull(index) ?: return

        _state.update {
            it.copy(
                phase = GamePhase.IN_DIALOG,
                activeDialog = Pair(location, dialog)
            )
        }
    }

    private fun handleLocationEntered(location: CampusLocation) {
        // Re-entering a visited location shows next dialog in sequence
        val index = dialogIndexMap.getOrDefault(location.id, 0)
        val nextIndex = (index + 1) % location.dialogs.size
        val dialog = location.dialogs[nextIndex]

        dialogIndexMap[location.id] = nextIndex

        _state.update {
            it.copy(
                phase = GamePhase.IN_DIALOG,
                activeDialog = Pair(location, dialog)
            )
        }
    }

    private fun handleDialogDismissed(locationId: LocationId) {
        // Advance dialog index for next visit
        val location = CampusData.locationMap[locationId] ?: return
        val current = dialogIndexMap.getOrDefault(locationId, 0)
        dialogIndexMap[locationId] = (current + 1) % location.dialogs.size

        _state.update {
            it.copy(
                phase = GamePhase.EXPLORING,
                activeDialog = null
            )
        }
    }

    // ─── Countdown ────────────────────────────────────────────────────────────

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_state.value.secondsRemaining > 0) {
                delay(1000L)
                _state.update { it.copy(secondsRemaining = it.secondsRemaining - 1) }
            }
            // Time's up → Game Over
            _state.update {
                it.copy(
                    phase = GamePhase.GAME_OVER,
                    isGameOver = true
                )
            }
        }
    }

    // ─── Helpers (used by UI) ─────────────────────────────────────────────────

    fun formattedTime(): String {
        val s = _state.value.secondsRemaining
        val h = s / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        return "%02d:%02d:%02d".format(h, m, sec)
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
