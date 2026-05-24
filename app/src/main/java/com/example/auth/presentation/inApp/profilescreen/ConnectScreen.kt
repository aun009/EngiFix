package com.example.auth.presentation.inApp.profilescreen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.auth.presentation.components.EngiFixBackground
import com.example.auth.presentation.components.IconTile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConnectScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    var activeCollegeName by remember { mutableStateOf("") }
    var activeCollegeKey by remember { mutableStateOf("") }
    var myCollegeName by remember { mutableStateOf("") }
    var myCollegeKey by remember { mutableStateOf("") }
    var studentQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var profiles by remember { mutableStateOf<List<PeerProfile>>(emptyList()) }
    var selectedProfile by remember { mutableStateOf<PeerProfile?>(null) }

    LaunchedEffect(Unit) {
        val uid = currentUid ?: run {
            isLoading = false
            return@LaunchedEffect
        }
        val doc = withContext(Dispatchers.IO) {
            FirebaseFirestore.getInstance().collection("users").document(uid).get().await()
        }
        myCollegeName = doc.getString("collegeName").orEmpty()
        myCollegeKey = doc.getString("collegeNameNormalized")
            ?: doc.getString("collegeKey")
                    ?: myCollegeName.normalizedCollegeKey()
        activeCollegeName = myCollegeName
        activeCollegeKey = myCollegeKey
    }

    LaunchedEffect(activeCollegeKey) {
        if (activeCollegeKey.isBlank()) {
            profiles = emptyList()
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        profiles = runCatching {
            withContext(Dispatchers.IO) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("collegeNameNormalized", activeCollegeKey)
                    .limit(120L)
                    .get()
                    .await()
                    .documents
                    .map { doc ->
                        val platforms = doc.get("codingPlatforms") as? List<*> ?: emptyList<Any>()
                        val projects = doc.get("projects") as? List<*> ?: emptyList<Any>()
                        PeerProfile(
                            id = doc.id,
                            displayName = doc.getString("displayName").orEmpty(),
                            userName = doc.getString("userName").orEmpty(),
                            email = doc.getString("email").orEmpty(),
                            photoUrl = doc.getString("photoUrl").orEmpty(),
                            collegeName = doc.getString("collegeName").orEmpty(),
                            branch = doc.getString("branch").orEmpty(),
                            graduationYear = doc.getString("graduationYear").orEmpty(),
                            goalRole = doc.getString("goalRole").orEmpty(),
                            headline = doc.getString("headline").orEmpty(),
                            skills = parsePeerSkills(doc.get("skills")),
                            platformCount = platforms.size,
                            projectCount = projects.size,
                            profileScore = doc.getLong("profileScore")?.toInt() ?: 0
                        )
                    }
                    .sortedByDescending { it.profileScore }
            }
        }.getOrDefault(emptyList())
        isLoading = false
    }

    val normalizedStudentQuery = studentQuery.trim().lowercase(Locale.ROOT)
    val filteredProfiles = remember(profiles, normalizedStudentQuery) {
        if (normalizedStudentQuery.isBlank()) profiles else profiles.filter {
            it.searchBlob.contains(normalizedStudentQuery)
        }
    }
    val collegeRanks = remember(profiles) {
        profiles.sortedByDescending { it.profileScore }.mapIndexed { index, profile -> profile.id to index + 1 }.toMap()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("College Network", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        EngiFixBackground(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 14.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile(Icons.Default.Groups, MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(activeCollegeName.ifBlank { "Choose your college" }, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                Text(
                                    if (activeCollegeKey.isBlank()) "Add your college in Profile to unlock your network."
                                    else "${filteredProfiles.size} of ${profiles.size} students visible from your college",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        OutlinedTextField(
                            value = studentQuery,
                            onValueChange = { studentQuery = it },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            placeholder = { Text("Search students, usernames, skills") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            NetworkMetricChip(Icons.Default.School, myCollegeName.ifBlank { "College not set" }, Modifier.weight(1f))
                            NetworkMetricChip(Icons.Default.Person, "${profiles.size} profiles", Modifier.weight(1f))
                        }
                    }
                }

                when {
                    activeCollegeKey.isBlank() -> EmptyPeerState(
                        title = "No college selected",
                        message = "Set your college in Profile first, then students from the same college will appear here."
                    )

                    isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }

                    filteredProfiles.isEmpty() -> EmptyPeerState(
                        title = "No students found",
                        message = "This college has no matching profiles yet."
                    )

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredProfiles, key = { it.id }) { profile ->
                            PeerProfileCard(
                                profile = profile,
                                isCurrentUser = profile.id == currentUid,
                                collegeRank = collegeRanks[profile.id],
                                onViewProfile = { selectedProfile = profile },
                                onConnect = {
                                    if (profile.email.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${profile.email}"))
                                        context.startActivity(Intent.createChooser(intent, "Connect with ${profile.displayLabel}"))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedProfile?.let { profile ->
        AlertDialog(
            onDismissRequest = { selectedProfile = null },
            icon = { IconTile(Icons.Default.Person, MaterialTheme.colorScheme.primary) },
            title = { Text(profile.displayLabel, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(profile.headline.ifBlank { profile.goalRole.ifBlank { "@${profile.userName}" } })
                    PeerDetailLine("College", profile.collegeName.ifBlank { "Not added" })
                    PeerDetailLine("Branch", profile.branch.ifBlank { "Not added" })
                    PeerDetailLine("Batch", profile.graduationYear.ifBlank { "Not added" })
                    PeerDetailLine("Flex score", profile.profileScore.toString())
                    PeerDetailLine("Projects", profile.projectCount.toString())
                    if (profile.skills.isNotEmpty()) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            profile.skills.forEach { skill ->
                                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                    Text(
                                        skill,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedProfile = null
                        if (profile.email.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${profile.email}"))
                            context.startActivity(Intent.createChooser(intent, "Connect with ${profile.displayLabel}"))
                        }
                    },
                    enabled = profile.email.isNotBlank()
                ) { Text("Connect") }
            },
            dismissButton = {
                TextButton(onClick = { selectedProfile = null }) { Text("Close") }
            },
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PeerProfileCard(
    profile: PeerProfile,
    isCurrentUser: Boolean,
    collegeRank: Int?,
    onViewProfile: () -> Unit,
    onConnect: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(profile)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            profile.displayLabel,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isCurrentUser) {
                            Spacer(Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Text(
                                    "You",
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        profile.headline.ifBlank { profile.goalRole.ifBlank { "@${profile.userName}" } },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    profile.profileScore.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PeerChip(Icons.Default.Badge, profile.branch.ifBlank { "Branch" }, Modifier.weight(1f))
                PeerChip(
                    Icons.Default.EmojiEvents,
                    if (collegeRank != null) "College #$collegeRank" else "${profile.platformCount} coding profiles",
                    Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                PeerChip(Icons.Default.Work, profile.goalRole.ifBlank { "Open to roles" }, Modifier.weight(1f))
                PeerChip(Icons.Default.AccountTree, "${profile.projectCount} projects", Modifier.weight(1f))
            }

            if (profile.skills.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    profile.skills.take(5).forEach { skill ->
                        Surface(shape = RoundedCornerShape(7.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(skill, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onViewProfile, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                    Text("View")
                }
                Button(
                    onClick = onConnect,
                    modifier = Modifier.weight(1f),
                    enabled = profile.email.isNotBlank() && !isCurrentUser,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Connect")
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(profile: PeerProfile) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFFC75F3A), Color(0xFF287C7A)))),
        contentAlignment = Alignment.Center
    ) {
        if (profile.photoUrl.isNotBlank()) {
            AsyncImage(
                model = profile.photoUrl,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Text(
                profile.displayLabel.firstOrNull()?.uppercase().orEmpty(),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun NetworkMetricChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(7.dp))
            Text(text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PeerChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmptyPeerState(title: String, message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            IconTile(Icons.Default.Search, MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

@Composable
private fun PeerDetailLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

private data class PeerProfile(
    val id: String,
    val displayName: String,
    val userName: String,
    val email: String,
    val photoUrl: String,
    val collegeName: String,
    val branch: String,
    val graduationYear: String,
    val goalRole: String,
    val headline: String,
    val skills: List<String>,
    val platformCount: Int,
    val projectCount: Int,
    val profileScore: Int
) {
    val displayLabel: String = displayName.ifBlank { userName.ifBlank { "Engineer" } }
    val searchBlob: String = listOf(
        displayName,
        userName,
        email,
        collegeName,
        branch,
        graduationYear,
        goalRole,
        headline,
        skills.joinToString(" ")
    ).joinToString(" ").lowercase(Locale.ROOT)
}

private fun parsePeerSkills(value: Any?): List<String> = when (value) {
    is List<*> -> value.mapNotNull { it?.toString()?.trim() }.filter { it.isNotBlank() }
    is String -> value.split(",", "\n").map { it.trim() }.filter { it.isNotBlank() }
    else -> emptyList()
}.distinctBy { it.lowercase(Locale.ROOT) }.take(12)
