package com.example.auth.presentation.authentication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.auth.presentation.animation.rememberMotionPolicy
import com.example.auth.presentation.components.EngiFixBackground
import com.example.auth.presentation.components.Eyebrow

@Composable
fun FirstScreen(navController: NavController) {
    val motion = rememberMotionPolicy()
    var contentVisible by remember { mutableStateOf(false) }
    var pointerX by remember { mutableFloatStateOf(0.5f) }
    var pointerY by remember { mutableFloatStateOf(0.5f) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    EngiFixBackground {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(motion.duration(620))) +
                    slideInVertically(
                        animationSpec = tween(motion.duration(620), easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 6 }
                    ),
            modifier = Modifier.fillMaxSize()
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val position = event.changes.firstOrNull()?.position ?: continue
                                pointerX = (position.x / size.width).coerceIn(0f, 1f)
                                pointerY = (position.y / size.height).coerceIn(0f, 1f)
                            }
                        }
                    }
            ) {
                val iconShiftX = ((pointerX - 0.5f) * 18f).dp
                val iconShiftY = ((pointerY - 0.5f) * 18f).dp

                FloatingHeroIcon(
                    icon = Icons.Default.AutoAwesome,
                    tint = Color(0xFF7B61FF),
                    baseX = 22.dp,
                    baseY = 94.dp,
                    shiftX = iconShiftX,
                    shiftY = iconShiftY
                )
                FloatingHeroIcon(
                    icon = Icons.Default.BarChart,
                    tint = Color(0xFF5E6AD2),
                    baseX = maxWidth - 66.dp,
                    baseY = 58.dp,
                    shiftX = iconShiftX,
                    shiftY = iconShiftY
                )
                FloatingHeroIcon(
                    icon = Icons.Default.ViewInAr,
                    tint = Color(0xFF1D7CF2),
                    baseX = 26.dp,
                    baseY = maxHeight * 0.51f,
                    shiftX = iconShiftX,
                    shiftY = iconShiftY
                )
                FloatingHeroIcon(
                    icon = Icons.Default.RocketLaunch,
                    tint = Color(0xFFA765F4),
                    baseX = maxWidth - 66.dp,
                    baseY = maxHeight * 0.46f,
                    shiftX = iconShiftX,
                    shiftY = iconShiftY
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(44.dp))
                    Eyebrow(text = "SMARTER TOOLS • BIGGER IMPACT", icon = Icons.Default.AutoAwesome)
                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = buildAnnotatedString {
                            append("Everything You\nNeed to ")
                            withStyle(SpanStyle(color = Color(0xFF246BFD))) {
                                append("Create,\nGrow & Scale")
                            }
                        },
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 40.sp,
                            lineHeight = 45.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Practice, opportunities, contests, and progress in one calm workspace built for engineering students.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 23.sp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )

                    Spacer(Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(0.84f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            onClick = { navController.navigate("register_screen") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Register",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            onClick = { navController.navigate("login_screen") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Log In",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            onClick = {
                                navController.navigate("home") {
                                    popUpTo("first_screen") { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Continue as Guest",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FloatingHeroIcon(
    icon: ImageVector,
    tint: Color,
    baseX: Dp,
    baseY: Dp,
    shiftX: Dp,
    shiftY: Dp
) {
    val animatedX by animateDpAsState(
        targetValue = baseX + shiftX,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "hero_icon_x"
    )
    val animatedY by animateDpAsState(
        targetValue = baseY + shiftY,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "hero_icon_y"
    )

    Surface(
        modifier = Modifier
            .offset(x = animatedX, y = animatedY)
            .size(52.dp)
            .graphicsLayer {
                shadowElevation = 12f
                shape = RoundedCornerShape(16.dp)
                clip = false
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.72f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(tint.copy(alpha = 0.16f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
