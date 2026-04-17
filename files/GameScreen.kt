package com.campuslastday

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ─── Root Screen ──────────────────────────────────────────────────────────────

/**
 * Entry point composable.
 * Wire this to your NavHost destination.
 *
 *   composable("game") { GameScreen() }
 */
@Composable
fun GameScreen(
    vm: GameViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Map canvas (full screen base layer) ──────────────────────────
        MapCanvas(
            playerPos         = state.player.position,
            locations         = CampusData.locations,
            visitedLocations  = state.player.visitedLocations,
            onCanvasReady     = vm::onCanvasSizeChanged,
            onDrag            = { delta -> vm.onEvent(GameEvent.PlayerDragged(delta)) },
            modifier          = Modifier.fillMaxSize()
        )

        // ── HUD (top overlay) ────────────────────────────────────────────
        GameHUD(
            secondsRemaining  = state.secondsRemaining,
            confidence        = state.player.confidence,
            currentLocation   = state.player.currentLocation,
            modifier          = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        // ── NPC Dialog (bottom sheet overlay) ───────────────────────────
        NPCDialogSheet(
            dialogPair = state.activeDialog,
            onDismiss  = { locationId ->
                vm.onEvent(GameEvent.DialogDismissed(locationId))
            }
        )

        // ── Mini instruction (fades out after player first moves) ────────
        var showTip by remember { mutableStateOf(true) }
        LaunchedEffect(state.player.position) {
            // Hide tip once player has moved from starting position
            if (state.player.visitedLocations.isNotEmpty()) showTip = false
        }
        AnimatedVisibility(
            visible = showTip,
            enter   = fadeIn(),
            exit    = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 160.dp)
        ) {
            Text(
                text       = "Drag to walk around campus",
                fontSize   = 13.sp,
                color      = Color(0xAA3A3A6E),
                fontWeight = FontWeight.Medium
            )
        }

        // ── Game Over overlay ────────────────────────────────────────────
        AnimatedVisibility(
            visible  = state.isGameOver,
            enter    = fadeIn(),
            modifier = Modifier.fillMaxSize()
        ) {
            GameOverScreen(
                confidence = state.player.confidence,
                visited    = state.player.visitedLocations.size,
                onRestart  = { /* restart logic — reset ViewModel state */ }
            )
        }
    }
}

// ─── Game Over ────────────────────────────────────────────────────────────────

@Composable
fun GameOverScreen(
    confidence: Int,
    visited: Int,
    onRestart: () -> Unit
) {
    val message = when {
        confidence >= 80 -> "You walked in confident.\nThat's already half the battle."
        confidence >= 50 -> "Nervous but ready.\nYou showed up. That's what matters."
        confidence >= 25 -> "Anxious, exhausted, and still here.\nThat's bravery."
        else             -> "You showed up.\nThat's already more than most."
    }

    val emoji = when {
        confidence >= 80 -> "💪"
        confidence >= 50 -> "🎯"
        confidence >= 25 -> "😤"
        else             -> "🫂"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF01A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text     = emoji,
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text       = "You walk into the hall.",
                fontSize   = 14.sp,
                color      = Color(0xFF8888AA),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text      = message,
                fontSize  = 20.sp,
                color     = Color(0xFFF0F0FF),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text     = "Locations visited: $visited / ${CampusData.locations.size}",
                fontSize = 13.sp,
                color    = Color(0xFF6666AA)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onRestart,
                shape   = RoundedCornerShape(12.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3A3A6E),
                    contentColor   = Color(0xFFD0D0FF)
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
            ) {
                Text("Play again", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
