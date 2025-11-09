package com.example.auth.presentation.features.contest

data class ContestResult(
    val meta: Meta,
    val objects: List<ContestItem>
)

data class Meta(
    val limit: Int,
    val next: String?,
    val offset: Int,
    val previous: String?,
    val total_count: Int?
)

data class ContestItem(
    val id: Int,
    val event: String,
    val start: String,
    val end: String,
    val duration: String,
    val resource: String,
    val resource_id: Int,
    val host: String,
    val href: String
)
