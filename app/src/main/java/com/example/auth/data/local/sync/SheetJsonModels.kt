package com.example.auth.data.local.sync

import com.google.gson.annotations.SerializedName

/** Top-level JSON structure from dsa_sheets.json */
data class SheetJsonFile(
    @SerializedName("version") val version: Int = 0,
    @SerializedName("sheets")  val sheets: List<SheetJson>? = emptyList()
)

data class SheetJson(
    @SerializedName("id")              val id: String? = "",
    @SerializedName("title")           val title: String? = "",
    @SerializedName("description")     val description: String? = "",
    @SerializedName("totalQuestions")  val totalQuestions: Int = 0,
    @SerializedName("category")        val category: String? = "",
    @SerializedName("questions")       val questions: List<QuestionJson>? = emptyList()
)

data class QuestionJson(
    @SerializedName("id")         val id: String? = "",
    @SerializedName("topic")      val topic: String? = "",
    @SerializedName("title")      val title: String? = "",
    @SerializedName("difficulty") val difficulty: String? = "",
    @SerializedName("problemUrl") val problemUrl: String? = ""
)
