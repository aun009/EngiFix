package com.campuslastday

import androidx.compose.ui.geometry.Offset

// ─── Locations ────────────────────────────────────────────────────────────────

enum class LocationId {
    LIBRARY, PLACEMENT_CELL, H_MESS, CSE_DEPT, GROUND, HOSTEL_ROOM
}

data class CampusLocation(
    val id: LocationId,
    val name: String,
    val center: Offset,          // canvas coordinates (0..1000f range, scaled at draw time)
    val radius: Float = 60f,
    val confidenceDelta: Int,    // +/- applied on entry
    val emoji: String,
    val dialogs: List<DialogNode>
)

// ─── Dialog ───────────────────────────────────────────────────────────────────

data class DialogNode(
    val speaker: String,
    val line: String,
    val response: String? = null // player's only response option (null = just "Ok")
)

// ─── Player ───────────────────────────────────────────────────────────────────

data class PlayerState(
    val position: Offset = Offset(500f, 700f), // starts near main gate
    val confidence: Int = 50,                  // 0..100
    val visitedLocations: Set<LocationId> = emptySet(),
    val currentLocation: LocationId? = null
)

// ─── Game ─────────────────────────────────────────────────────────────────────

data class GameState(
    val player: PlayerState = PlayerState(),
    val secondsRemaining: Int = 6 * 60 * 60,   // 6 hours
    val activeDialog: Pair<CampusLocation, DialogNode>? = null,
    val isGameOver: Boolean = false,
    val phase: GamePhase = GamePhase.EXPLORING
)

enum class GamePhase { EXPLORING, IN_DIALOG, GAME_OVER }

// ─── Events (UI → ViewModel) ──────────────────────────────────────────────────

sealed class GameEvent {
    data class PlayerDragged(val delta: Offset) : GameEvent()
    data class LocationEntered(val location: CampusLocation) : GameEvent()
    data class DialogDismissed(val locationId: LocationId) : GameEvent()
    object GameEndAcknowledged : GameEvent()
}
