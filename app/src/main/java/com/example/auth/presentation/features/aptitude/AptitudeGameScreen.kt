package com.example.auth.presentation.features.aptitude

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// ─── Colors ───────────────────────────────────────────────────────────────────
private val BgDark = Color(0xFF14141A)
private val CardDark = Color(0xFF1F1F28)
private val AccentGold = Color(0xFFFFB800)
private val TextMain = Color(0xFFE5E5E5)
private val TextMuted = Color(0xFF808080)
private val CorrectColor = Color(0xFF4CB050)
private val ErrorColor = Color(0xFFE53935)
private val NodeBg = Color(0xFF2C2C35)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun AptitudeGameScreen(
    vm: AptitudeViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        when (state.gameState) {
            GameState.INTRO -> IntroScreen(onStart = vm::startGame, onBackClick = onBackClick)
            GameState.PLAYING -> PlayingScreen(state, vm, onQuit = { vm.quitGame(); onBackClick() })
            GameState.GAME_OVER -> GameOverScreen(state.score, onRetry = vm::startGame, onExit = onBackClick)
        }
    }
}

// ─── Intro Screen ─────────────────────────────────────────────────────────────

@Composable
private fun IntroScreen(onStart: () -> Unit, onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(AccentGold.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🧮", fontSize = 48.sp)
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            "Numerical Reasoning",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            "Find the missing number to complete the equation. " +
            "You have 60 seconds to solve as many as possible.\n\n" +
            "Based on Accenture's Cognitive Assessment.",
            fontSize = 15.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black)
        ) {
            Text("Start Test", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(16.dp))
        
        TextButton(onClick = onBackClick) {
            Text("Exit", color = TextMuted, fontSize = 15.sp)
        }
    }
}

// ─── Playing Screen ──────────────────────────────────────────────────────────

@Composable
private fun PlayingScreen(
    state: AptitudeState,
    vm: AptitudeViewModel,
    onQuit: () -> Unit
) {
    val q = state.currentQuestion ?: return

    // Flash background red on error, green on correct (very briefly)
    val bgColor by animateColorAsState(
        targetValue = when {
            state.errorFlash -> ErrorColor.copy(alpha = 0.15f)
            state.correctFlash -> CorrectColor.copy(alpha = 0.1f)
            else -> BgDark
        },
        animationSpec = tween(150)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
    ) {
        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onQuit) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quit", tint = TextMuted)
            }
            Text(
                text = "${state.secondsRemaining}s",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (state.secondsRemaining <= 10) ErrorColor else AccentGold
            )
            Text(
                "Score: ${state.score}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextMain,
                modifier = Modifier.padding(end = 16.dp)
            )
        }

        // Combo
        if (state.combo > 1) {
            Text(
                text = "${state.combo}x Combo! 🔥",
                color = AccentGold,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            Spacer(Modifier.height(20.dp))
        }

        Spacer(Modifier.weight(1f))

        // PUZZLE AREA (The 3 interconnected circles)
        Box(
            modifier = Modifier.fillMaxWidth().height(260.dp),
            contentAlignment = Alignment.Center
        ) {
            // Draw connecting lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                
                val nodeA = Offset(cx - 250f, cy - 80f)
                val nodeB = Offset(cx + 250f, cy - 80f)
                val nodeTarget = Offset(cx, cy + 120f)
                val nodeOp = Offset(cx, cy - 120f) // The operator knot

                drawLine(TextMuted.copy(alpha = 0.3f), start = nodeA, end = nodeTarget, strokeWidth = 8f, cap = StrokeCap.Round)
                drawLine(TextMuted.copy(alpha = 0.3f), start = nodeB, end = nodeTarget, strokeWidth = 8f, cap = StrokeCap.Round)
            }
            
            // Nodes map: A on left, B on right, C at bottom. Op connecting them.
            // Simplified UI Layout for nodes using absolute positioning via Box
            NodeCircle(text = q.nodeA, modifier = Modifier.align(Alignment.TopStart).offset(x = 40.dp, y = 20.dp))
            NodeCircle(text = q.nodeB, modifier = Modifier.align(Alignment.TopEnd).offset(x = (-40).dp, y = 20.dp))
            
            // Operator in middle
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 20.dp)
                	.size(48.dp)
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(2.dp, TextMuted.copy(0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(q.operator, fontSize = 24.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            }

            // Target Node (Result)
            NodeCircle(
                text = q.nodeC, 
                isTarget = true,
                modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-20).dp)
            )
        }

        Spacer(Modifier.weight(1f))

        // OPTIONS GRID
        val options = q.options
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OptionButton(text = options[0].toString(), modifier = Modifier.weight(1f)) { vm.selectOption(0) }
                OptionButton(text = options[1].toString(), modifier = Modifier.weight(1f)) { vm.selectOption(1) }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OptionButton(text = options[2].toString(), modifier = Modifier.weight(1f)) { vm.selectOption(2) }
                OptionButton(text = options[3].toString(), modifier = Modifier.weight(1f)) { vm.selectOption(3) }
            }
        }
    }
}

@Composable
private fun NodeCircle(text: String, isTarget: Boolean = false, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(if (isTarget) AccentGold.copy(0.15f) else NodeBg)
            .border(3.dp, if (isTarget) AccentGold else TextMuted.copy(0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (isTarget) AccentGold else TextMain
        )
    }
}

@Composable
private fun OptionButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .clickable(onClick = onClick)
            .border(1.dp, TextMuted.copy(0.1f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
    }
}

// ─── Game Over Screen ────────────────────────────────────────────────────────

@Composable
private fun GameOverScreen(score: Int, onRetry: () -> Unit, onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Time's Up!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AccentGold)
        Spacer(Modifier.height(16.dp))
        Text("Final Score", fontSize = 16.sp, color = TextMuted)
        Text("$score", fontSize = 64.sp, fontWeight = FontWeight.ExtraBold, color = TextMain)
        
        Spacer(Modifier.height(48.dp))
        
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black)
        ) {
            Text("Play Again", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CardDark, contentColor = TextMain)
        ) {
            Text("Back to Home", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
