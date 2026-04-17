package com.campuslastday

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Colors ───────────────────────────────────────────────────────────────────

private val SheetBg     = Color(0xFF1A1A2E)
private val SheetText   = Color(0xFFF0F0FF)
private val SheetMuted  = Color(0xFF8888AA)
private val ButtonBg    = Color(0xFF3A3A6E)
private val ButtonText  = Color(0xFFD0D0FF)

// ─── NPC Dialog Sheet ─────────────────────────────────────────────────────────

/**
 * Animated bottom sheet that slides up when a dialog is triggered.
 * Shows speaker name, their line, and a single player response button.
 */
@Composable
fun NPCDialogSheet(
    dialogPair: Pair<CampusLocation, DialogNode>?,
    onDismiss: (LocationId) -> Unit
) {
    AnimatedVisibility(
        visible = dialogPair != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        dialogPair?.let { (location, dialog) ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = SheetBg,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // ── Drag handle ─────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(2.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Location tag ────────────────────────────────────
                    Text(
                        text = "${location.emoji}  ${location.name}",
                        fontSize = 11.sp,
                        color = SheetMuted,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Speaker name ────────────────────────────────────
                    Text(
                        text = dialog.speaker,
                        fontSize = 13.sp,
                        color = Color(0xFF9B8FFF),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Dialog line ─────────────────────────────────────
                    Text(
                        text = "\"${dialog.line}\"",
                        fontSize = 15.sp,
                        color = SheetText,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Player response button ───────────────────────────
                    Button(
                        onClick = { onDismiss(location.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonBg,
                            contentColor = ButtonText
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = dialog.response ?: "Ok.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
