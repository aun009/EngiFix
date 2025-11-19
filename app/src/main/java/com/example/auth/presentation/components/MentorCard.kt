package com.example.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MentorCardEx(
    name: String,
    skills: List<String>,
    title: String,
    description: String,
    price: String,
    imageUrl: String,
    rating: Float = 4.9f,
    totalReviews: Int = 127,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with Profile
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placeholder for profile image
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF667EEA),
                                    Color(0xFF764BA2)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "$rating",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = " ($totalReviews)",
                            fontSize = 14.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Skills chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                skills.take(3).forEach { skill ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE8F0FE),
                        modifier = Modifier
                    ) {
                        Text(
                            text = skill,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1967D2),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Title
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF1A1A1A),
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(8.dp))

            // Description
            Text(
                text = description,
                color = Color(0xFF5F6368),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Spacer(Modifier.height(24.dp))

            // Divider with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFE0E0E0),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(Modifier.height(20.dp))

            // Price and CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = price,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = "per session",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1967D2)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Book Session",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun MentorCardExPreview() {
    MentorCardEx(
        name = "Jane Doe",
        skills = listOf("Android", "Kotlin", "Jetpack Compose", "Architecture"),
        title = "Senior Android Developer at Google",
        description = "I will help you master Jetpack Compose and prepare for technical interviews. Let's build amazing apps together!",
        price = "$50",
        imageUrl = "",
        rating = 4.9f,
        totalReviews = 127,
        onClick = {}
    )
}