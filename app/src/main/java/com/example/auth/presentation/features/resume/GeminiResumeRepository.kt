package com.example.auth.presentation.features.resume

import android.content.Context
import android.net.Uri
import com.example.auth.BuildConfig
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ResumeChatMessage(
    val fromUser: Boolean,
    val text: String
)

class GeminiResumeRepository(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(70, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    suspend fun analyzeResume(
        resumeUri: Uri,
        experienceLevel: String,
        targetRole: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val resumeText = extractPdfText(resumeUri)
            require(resumeText.isNotBlank()) {
                "I could not read text from this PDF. Try an ATS-style PDF with selectable text."
            }

            val prompt = buildPrompt(
                resumeText = resumeText.take(MAX_RESUME_CHARS),
                experienceLevel = experienceLevel,
                targetRole = targetRole.ifBlank { "software engineering internships and early-career roles" }
            )

            generateText(prompt = prompt, temperature = 0.35, maxOutputTokens = 900)
        }.recoverCatching { error ->
            buildOfflineInsights(
                resumeText = extractPdfText(resumeUri).take(MAX_RESUME_CHARS),
                experienceLevel = experienceLevel,
                targetRole = targetRole,
                reason = error.message.orEmpty()
            )
        }
    }

    suspend fun extractResumeText(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            extractPdfText(uri).also { text ->
                require(text.isNotBlank()) {
                    "I could not read text from this PDF. Try an ATS-style PDF with selectable text."
                }
            }
        }
    }

    suspend fun chatWithResume(
        resumeText: String,
        messages: List<ResumeChatMessage>,
        experienceLevel: String,
        targetRole: String
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val prompt = buildChatPrompt(
                resumeText = resumeText.take(MAX_RESUME_CHARS),
                messages = messages.takeLast(10),
                experienceLevel = experienceLevel,
                targetRole = targetRole.ifBlank { "software engineering internships and early-career roles" }
            )
            generateText(prompt = prompt, temperature = 0.45, maxOutputTokens = 850)
        }.recoverCatching {
            buildOfflineChatResponse(messages.lastOrNull()?.text.orEmpty(), targetRole)
        }
    }

    private fun extractPdfText(uri: Uri): String {
        PDFBoxResourceLoader.init(context.applicationContext)
        return context.contentResolver.openInputStream(uri)?.use { input ->
            PDDocument.load(input).use { document ->
                PDFTextStripper().getText(document)
                    .replace(Regex("[ \\t]+"), " ")
                    .replace(Regex("\\n{3,}"), "\n\n")
                    .trim()
            }
        }.orEmpty()
    }

    private fun buildPrompt(
        resumeText: String,
        experienceLevel: String,
        targetRole: String
    ): String = """
        You are an engineering career mentor. Analyze this resume for an Indian engineering student.

        Experience level: $experienceLevel
        Target role: $targetRole

        Return concise, practical insights in this exact structure:
        1. Profile snapshot: 2 bullets about current strengths.
        2. Skill gaps to learn next: 4 bullets, each with why it matters.
        3. Resume fixes: 4 bullets focused on measurable impact and ATS clarity.
        4. Best-fit opportunities: 4 bullets naming role types or internship tracks to apply for.
        5. 7-day action plan: 5 bullets with specific tasks.

        Keep the tone direct, supportive, and useful. Do not roast the student.

        Resume text:
        $resumeText
    """.trimIndent()

    private fun buildChatPrompt(
        resumeText: String,
        messages: List<ResumeChatMessage>,
        experienceLevel: String,
        targetRole: String
    ): String {
        val history = messages.joinToString("\n\n") { message ->
            "${if (message.fromUser) "Student" else "Mentor"}: ${message.text}"
        }

        val resumeContext = resumeText.ifBlank {
            "No resume is attached yet. Give general career guidance and ask for a resume only when it would materially improve the answer."
        }

        return """
            You are EngiFix Career AI, a direct but supportive engineering career mentor.
            Answer like a useful chat assistant, not like a report. Keep answers compact, specific, and actionable.

            Student context:
            - Experience level: $experienceLevel
            - Target role: $targetRole

            Resume context:
            $resumeContext

            Conversation:
            $history

            Reply to the latest student message. If they ask where to apply, suggest role types, search keywords, and concrete next actions. If they ask what to learn, prioritize only the highest-impact skills.
        """.trimIndent()
    }

    private fun generateText(
        prompt: String,
        temperature: Double,
        maxOutputTokens: Int
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        require(apiKey.isNotBlank()) {
            "Gemini API key is missing. Add GEMINI_API_KEY to local.properties."
        }

        val requestJson = JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(JSONObject().put("text", prompt))
                    )
                )
            )
            .put(
                "generationConfig",
                JSONObject()
                    .put("temperature", temperature)
                    .put("topP", 0.9)
                    .put("maxOutputTokens", maxOutputTokens)
            )

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")
            .addHeader("x-goog-api-key", apiKey)
            .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(parseGeminiError(responseText) ?: "Gemini request failed: ${response.code}")
            }
            return parseGeminiText(responseText).ifBlank {
                throw IllegalStateException("Gemini returned an empty response.")
            }
        }
    }

    private fun parseGeminiText(body: String): String {
        val json = JSONObject(body)
        val candidates = json.optJSONArray("candidates") ?: return ""
        if (candidates.length() == 0) return ""
        val parts = candidates
            .optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts") ?: return ""

        return buildString {
            for (index in 0 until parts.length()) {
                val text = parts.optJSONObject(index)?.optString("text").orEmpty()
                if (text.isNotBlank()) {
                    if (isNotEmpty()) append("\n")
                    append(text.trim())
                }
            }
        }.trim()
    }

    private fun parseGeminiError(body: String): String? =
        runCatching {
            JSONObject(body).optJSONObject("error")?.optString("message")
        }.getOrNull()?.takeIf { it.isNotBlank() }

    private fun buildOfflineInsights(
        resumeText: String,
        experienceLevel: String,
        targetRole: String,
        reason: String
    ): String {
        val lower = resumeText.lowercase()
        val hasProjects = "project" in lower
        val hasMetrics = Regex("\\d+%|\\d+ users|\\d+ms|\\d+ seconds|\\d+ requests").containsMatchIn(lower)
        val hasInternship = "intern" in lower || "experience" in lower
        val hasGitHub = "github" in lower
        val role = targetRole.ifBlank { "software engineering internships" }

        return buildString {
            appendLine("Gemini was unavailable, so here is an on-device fallback review.")
            if (reason.isNotBlank()) appendLine("Reason: $reason")
            appendLine()
            appendLine("1. Profile snapshot")
            appendLine("- Level: $experienceLevel, targeting $role.")
            appendLine("- Current signal: ${if (hasProjects) "project work is visible" else "projects need to be made more visible"}; ${if (hasGitHub) "GitHub is present" else "add a GitHub or portfolio link"}." )
            appendLine()
            appendLine("2. Skill gaps to learn next")
            appendLine("- Build one production-style project with auth, database, deployment, and testing.")
            appendLine("- Add data structures practice proof through solved counts, contest ratings, or selected hard problems.")
            appendLine("- Learn role-specific tools: Android/Kotlin for mobile, React/Node for web, or Python/ML pipelines for AI roles.")
            appendLine("- Add Git, API design, debugging, and basic system design vocabulary.")
            appendLine()
            appendLine("3. Resume fixes")
            appendLine("- Rewrite bullets as action + tech + measurable result.")
            appendLine("- ${if (hasMetrics) "Keep the metrics and make them easier to scan." else "Add numbers: users, latency, accuracy, rank, downloads, or time saved."}")
            appendLine("- Keep sections ATS-friendly: Education, Skills, Projects, Experience, Achievements.")
            appendLine("- ${if (hasInternship) "Put internship impact above college activities." else "Apply for internships with project-heavy proof until experience grows."}")
            appendLine()
            appendLine("4. Best-fit opportunities")
            appendLine("- Software engineering intern roles at startups that value shipped projects.")
            appendLine("- Open-source contribution programs and campus ambassador technical tracks.")
            appendLine("- Product engineering internships where Android, backend, or full-stack skills are requested.")
            appendLine("- Hackathons and fellowship projects that convert into portfolio proof.")
            appendLine()
            appendLine("5. 7-day action plan")
            appendLine("- Day 1: Pick one target role and reorder the resume around it.")
            appendLine("- Day 2: Rewrite every project bullet with impact.")
            appendLine("- Day 3: Add GitHub links and a clean README for your best project.")
            appendLine("- Day 4: Fill skill gaps with one focused mini-feature.")
            appendLine("- Day 5-7: Apply to 20 matching roles and track responses.")
        }.trim()
    }

    private fun buildOfflineChatResponse(question: String, targetRole: String): String {
        val lower = question.lowercase()
        return when {
            "apply" in lower || "opportun" in lower || "job" in lower || "intern" in lower ->
                "Start with $targetRole searches on LinkedIn, Wellfound, Internshala, company career pages, and college placement groups. Apply only after your resume has one strong project bullet, one measurable achievement, and a GitHub link. Track 20 applications with role, company, date, status, and follow-up."
            "learn" in lower || "skill" in lower ->
                "Prioritize one stack deeply instead of collecting many tools. For $targetRole, pick: core language, DSA basics, one framework, Git/GitHub, API/database fundamentals, and deployment. Build one project that proves all of it."
            "project" in lower ->
                "Build a project with login, database, external API, clean README, screenshots, and a live link if possible. A repo alone is useful, but a live demo makes your profile much easier to trust."
            else ->
                "I could not reach Gemini, but the practical move is: clarify your target role, rewrite resume bullets with measurable impact, add one strongest project with GitHub proof, and apply in a tracked weekly routine."
        }
    }

    private companion object {
        const val MAX_RESUME_CHARS = 18_000
    }
}
