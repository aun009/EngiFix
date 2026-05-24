package com.example.auth.presentation.animation

import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext

@Stable
data class MotionPolicy(
    val enabled: Boolean,
    val fast: Int,
    val normal: Int,
    val expressive: Int
) {
    fun duration(millis: Int): Int = if (enabled) millis else 0
}

@Composable
fun rememberMotionPolicy(): MotionPolicy {
    val context = LocalContext.current
    val animatorScale = remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
        }.getOrDefault(1f)
    }

    val enabled = animatorScale > 0f
    val scale = animatorScale.coerceIn(0.5f, 1.6f)

    return remember(enabled, scale) {
        MotionPolicy(
            enabled = enabled,
            fast = (140 * scale).toInt(),
            normal = (260 * scale).toInt(),
            expressive = (620 * scale).toInt()
        )
    }
}

fun MotionPolicy.standardTween(): AnimationSpec<Float> =
    tween(durationMillis = normal)

fun MotionPolicy.springy(): AnimationSpec<Float> =
    if (enabled) {
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    } else {
        tween(durationMillis = 0)
    }

@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.975f
): Modifier {
    val motion = rememberMotionPolicy()
    val pressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (pressed && motion.enabled) pressedScale else 1f,
        animationSpec = motion.springy(),
        label = "press_scale"
    )

    return scale(animatedScale)
}

@Composable
fun Modifier.liftOnPress(
    interactionSource: MutableInteractionSource
): Modifier {
    val motion = rememberMotionPolicy()
    val pressed by interactionSource.collectIsPressedAsState()
    val lift by animateFloatAsState(
        targetValue = if (pressed && motion.enabled) 6f else 0f,
        animationSpec = motion.standardTween(),
        label = "press_lift"
    )

    return graphicsLayer {
        translationY = lift
    }
}
