package com.example.auth.data

data class Mentor(
    val id: String,
    val name: String,
    val skills: List<String>,
    val title: String,
    val description: String,
    val price: String,
    val imageUrl: String,
    val rating: Float,
    val totalReviews: Int,
    val aboutMe : String
)