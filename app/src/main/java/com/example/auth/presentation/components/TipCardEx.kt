package com.example.auth.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TipCardEx(
    title: String,
    description: String,
    icon: String = "🚀",
    backgroundColor: Color = Color(0xFF242426),
    accentColor: Color = Color(0xFF555555), // Subdued accent color
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "card_scale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor)
                .align(Alignment.CenterStart)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji icon in a pill
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 22.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFFAAAAAA),
                    lineHeight = 18.sp
                )
            }

            // Arrow indicator
            Text(
                text = "›",
                fontSize = 24.sp,
                color = Color(0xFF555555), // Subdued arrow color
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF18181C)
@Composable
fun TipCardExPreview() {
    TipCardEx(
        title = "Contests",
        description = "Compete today — See Today & Tomorrow's rounds",
        icon = "🏆"
    )
}