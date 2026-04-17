package com.example.auth.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF242426)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3A3A3C)),
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
                        .background(Color(0xFF3A3A3C)), // Minimal profile circle
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF1DA1F2),
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
                            color = Color.White
                        )
                        Text(
                            text = " ($totalReviews reviews)",
                            fontSize = 14.sp,
                            color = Color(0xFFAAAAAA)
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
                        color = Color(0xFF333333),
                        modifier = Modifier
                    ) {
                        Text(
                            text = skill,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE0E0E0),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Title
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.White,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(8.dp))

            // Description
            Text(
                text = description,
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 4.dp),
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(24.dp))

            // Divider with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF3A3A3C)) // Solid simple divider
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
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Starts from",
                        fontSize = 12.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF3A3A3C), // Simple dark button
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Button(
                        onClick = onClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = "View Services",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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