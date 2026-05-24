package com.example.auth.data.local

data class SheetProgress(
    val id: String,
    val title: String,
    val description: String,
    val totalQuestions: Int,
    val category: String,
    val imageUrl: String,
    val availableQuestions: Int,
    val completedQuestions: Int
) {
    val displayQuestionCount: Int
        get() = availableQuestions.takeIf { it > 0 } ?: totalQuestions

    val progress: Float
        get() = if (displayQuestionCount > 0) {
            completedQuestions.toFloat() / displayQuestionCount.toFloat()
        } else {
            0f
        }
}
