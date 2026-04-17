package com.example.auth.presentation.features.toolkit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Data ─────────────────────────────────────────────────────────────────────

private data class QuickLink(
    val emoji: String,
    val label: String,
    val url: String,
    val accent: Color
)

private val SECTIONS = listOf(
    "DSA & Competitive Programming" to listOf(
        QuickLink("🧩", "LeetCode",    "https://leetcode.com",               Color(0xFFffa116)),
        QuickLink("⚡", "Codeforces",  "https://codeforces.com",             Color(0xFF1f8acb)),
        QuickLink("👨‍🍳","CodeChef",    "https://www.codechef.com",           Color(0xFF5B4638)),
        QuickLink("🌏", "HackerRank",  "https://www.hackerrank.com",         Color(0xFF2EC866)),
        QuickLink("🏔️", "AtCoder",     "https://atcoder.jp",                 Color(0xFF888888)),
        QuickLink("🔰", "GfG",         "https://www.geeksforgeeks.org",      Color(0xFF2F8D46)),
    ),
    "Resumes & Career" to listOf(
        QuickLink("📄", "Overleaf",    "https://www.overleaf.com",           Color(0xFF4CAF50)),
        QuickLink("💼", "LinkedIn",    "https://www.linkedin.com",           Color(0xFF0077B5)),
        QuickLink("🚀", "Internshala", "https://internshala.com",            Color(0xFF0C6EFD)),
        QuickLink("🌟", "Naukri",      "https://www.naukri.com",             Color(0xFF1A73E8)),
        QuickLink("🏢", "Wellfound",   "https://wellfound.com",              Color(0xFF2D3748)),
        QuickLink("📝", "FlowCV",      "https://flowcv.com",                 Color(0xFF6C5CE7)),
    ),
    "Interview Prep" to listOf(
        QuickLink("🎯", "InterviewBit","https://www.interviewbit.com",       Color(0xFF4F46E5)),
        QuickLink("🗣️", "Pramp",       "https://www.pramp.com",              Color(0xFFE91E63)),
        QuickLink("🏗️", "Sys Design",  "https://github.com/donnemartin/system-design-primer", Color(0xFF6e5494)),
        QuickLink("👁️", "Blind",       "https://www.teamblind.com",          Color(0xFF3B82F6)),
        QuickLink("💡", "NeetCode",    "https://neetcode.io",                Color(0xFF00BCD4)),
        QuickLink("📊", "Codolio",     "https://codolio.com",                Color(0xFF7C3AED)),
    ),
    "Learning Platforms" to listOf(
        QuickLink("🎓", "NPTEL",       "https://nptel.ac.in",                Color(0xFF1565C0)),
        QuickLink("🌐", "CS50",        "https://cs50.harvard.edu",           Color(0xFFB71C1C)),
        QuickLink("📚", "Coursera",    "https://www.coursera.org",           Color(0xFF0056D2)),
        QuickLink("🗺️", "Roadmap.sh",  "https://roadmap.sh",                 Color(0xFF00B4D8)),
        QuickLink("📖", "MIT OCW",     "https://ocw.mit.edu",                Color(0xFFA8200D)),
        QuickLink("🧠", "freeCodeCamp","https://www.freecodecamp.org",       Color(0xFF006400)),
    ),
    "Dev Tools" to listOf(
        QuickLink("🐙", "GitHub",      "https://github.com",                 Color(0xFF333333)),
        QuickLink("🌊", "StackOverflow","https://stackoverflow.com",         Color(0xFFF58025)),
        QuickLink("📋", "DevDocs",     "https://devdocs.io",                 Color(0xFF7E57C2)),
        QuickLink("☁️", "Replit",      "https://replit.com",                 Color(0xFF5F4B8B)),
        QuickLink("⚙️", "Vercel",      "https://vercel.com",                 Color(0xFF000000)),
        QuickLink("🔬", "Postman",     "https://www.postman.com",            Color(0xFFFF6C37)),
    ),
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolkitScreen(onBackClick: () -> Unit = {}) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Student Toolkit",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            "30+ essential tools, one tap away",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SECTIONS.forEach { (category, links) ->
                Column(modifier = Modifier.padding(bottom = 20.dp)) {
                    Text(
                        text = category,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 20.dp, bottom = 10.dp, top = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(links) { link ->
                            ToolkitChip(link = link) {
                                try { uriHandler.openUri(link.url) } catch (_: Exception) {}
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Chip ─────────────────────────────────────────────────────────────────────

@Composable
private fun ToolkitChip(link: QuickLink, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .width(88.dp)
            .border(1.dp, link.accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(link.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(link.emoji, fontSize = 18.sp)
            }
            Text(
                text = link.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
