package com.example.auth.presentation.inApp.profilescreen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.auth.data.local.DsaDao
import com.example.auth.data.repository.CodingPlatformStatsRepository
import com.example.auth.presentation.authentication.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── Platform accent colours ───────────────────────────────────────────────────
private fun platformColor(name: String) = when (name.lowercase()) {
    "leetcode"   -> Color(0xFFffa116)
    "codeforces" -> Color(0xFF1f8acb)
    "codechef"   -> Color(0xFF5B4638)
    "hackerrank" -> Color(0xFF2EC866)
    "atcoder"    -> Color(0xFF888888)
    "github"     -> Color(0xFF6e5494)
    else         -> Color(0xFF6C5CE7)
}

// ── Avatar gradient ──────────────────────────────────────────────────────────
private val avatarGradient = Brush.linearGradient(
    listOf(Color(0xFF6C5CE7), Color(0xFF0984E3))
)

// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController? = null,
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {}
) {
    val scope       = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context     = LocalContext.current
    val statsRepository = remember { CodingPlatformStatsRepository() }

    // ── User state ────────────────────────────────────────────────────────────
    var userName    by remember { mutableStateOf("") }
    var email       by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var bio         by remember { mutableStateOf("") }
    var photoUri    by remember { mutableStateOf<Uri?>(null) }   // local pick
    var photoUrl    by remember { mutableStateOf("") }           // Firestore URL
    var isLoading   by remember { mutableStateOf(true) }
    var userNotFound by remember { mutableStateOf(false) }

    // ── DSA stats (from Room) ─────────────────────────────────────────────────
    var dsaSolved by remember { mutableIntStateOf(0) }
    var dsaTotal  by remember { mutableIntStateOf(0) }

    // ── Coding platforms ───────────────────────────────────────────────────────
    val platforms = remember { mutableStateListOf<CodingPlatform>() }

    // ── Bio edit state ─────────────────────────────────────────────────────────
    var showBioEdit by remember { mutableStateOf(false) }
    var editingBio  by remember { mutableStateOf("") }

    // ── Name edit state ────────────────────────────────────────────────────────
    var showNameEdit  by remember { mutableStateOf(false) }
    var editingName   by remember { mutableStateOf("") }

    // ── Logout confirm ─────────────────────────────────────────────────────────
    var showLogout by remember { mutableStateOf(false) }

    // ── Image picker ───────────────────────────────────────────────────────────
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) photoUri = uri }

    // ── Load data ──────────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        // DSA stats from Room via Hilt EntryPoint
        try {
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                DsaDaoEntryPoint::class.java
            )
            val dao = hiltEntryPoint.dsaDao()
            dsaSolved = dao.getTotalSolvedGlobal()
            dsaTotal  = dao.getTotalQuestionsGlobal()
        } catch (_: Exception) {}

        // Firebase user data
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            try {
                val doc = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUser.uid)
                        .get()
                        .await()
                }
                if (doc.exists()) {
                    userName    = doc.getString("userName")    ?: ""
                    email       = doc.getString("email")       ?: ""
                    displayName = doc.getString("displayName") ?: ""
                    bio         = doc.getString("bio")         ?: ""
                    photoUrl    = doc.getString("photoUrl")    ?: ""

                    val platformsData = doc.get("codingPlatforms") as? List<Map<String, String>>
                    platformsData?.forEach { pm ->
                        val pName  = pm["name"]    ?: return@forEach
                        val pUser  = pm["username"] ?: ""
                        val pStats = pm["stats"]    ?: "N/A"
                        val pLabel = pm["statsLabel"] ?: ""
                        val pImg   = pm["profileImageUrl"]?.takeIf { it.isNotEmpty() }
                        platforms.add(CodingPlatform(pName, pUser, pStats, pLabel, Icons.Default.Star, pImg))

                        if (pStats == "N/A" || pLabel == "Loading...") {
                            scope.launch {
                                try {
                                    val s = statsRepository.fetchStats(pName, pUser)
                                    val idx = platforms.indexOfFirst { it.name == pName && it.username == pUser }
                                    if (idx >= 0) {
                                        platforms[idx] = platforms[idx].copy(
                                            stats = s.rating, statsLabel = s.statsLabel, profileImageUrl = s.profileImageUrl
                                        )
                                        saveplatformsToFirestore(currentUser.uid, platforms)
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    }
                } else userNotFound = true
            } catch (_: Exception) { userNotFound = true }
            finally { isLoading = false }
        } else { isLoading = false; userNotFound = true }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    fun saveBio(newBio: String) {
        bio = newBio
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        scope.launch {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                        .update("bio", newBio).await()
                }
            } catch (_: Exception) {}
        }
    }

    fun saveName(newName: String) {
        displayName = newName
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        scope.launch {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                        .update("displayName", newName).await()
                }
            } catch (_: Exception) {}
        }
    }

    // ── UI ─────────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            userNotFound -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)) {
                    Text("👤", fontSize = 64.sp)
                    Text("Not logged in", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 100.dp)
            ) {
                // ── 1. Profile hero card ──────────────────────────────────────
                ProfileHeroCard(
                    displayName = displayName,
                    userName    = userName,
                    email       = email,
                    bio         = bio,
                    photoUri    = photoUri,
                    photoUrl    = photoUrl,
                    dsaSolved   = dsaSolved,
                    dsaTotal    = dsaTotal,
                    platformCount = platforms.size,
                    onPickPhoto  = { pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )},
                    onEditName   = { editingName = displayName; showNameEdit = true },
                    onEditBio    = { editingBio = bio; showBioEdit = true }
                )

                Spacer(Modifier.height(20.dp))

                // ── 2. Coding profiles ────────────────────────────────────────
                CodingPlatformsSection(
                    platforms       = platforms,
                    statsRepository = statsRepository,
                    onPlatformAdded = { pName, pUser ->
                        scope.launch {
                            if (platforms.any { it.name.equals(pName, ignoreCase = true) }) return@launch
                            val np = CodingPlatform(pName, pUser, "N/A", "Loading...", Icons.Default.Star, null)
                            platforms.add(np)
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                            saveplatformsToFirestore(uid, platforms)
                            try {
                                val s = statsRepository.fetchStats(pName, pUser)
                                val idx = platforms.indexOfFirst { it.name == pName && it.username == pUser }
                                if (idx >= 0) {
                                    platforms[idx] = platforms[idx].copy(stats = s.rating, statsLabel = s.statsLabel, profileImageUrl = s.profileImageUrl)
                                    saveplatformsToFirestore(uid, platforms)
                                }
                            } catch (_: Exception) {}
                        }
                    },
                    onPlatformRemoved = { plat ->
                        scope.launch {
                            platforms.remove(plat)
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                            saveplatformsToFirestore(uid, platforms)
                        }
                    },
                    onPlatformRefresh = { plat ->
                        scope.launch {
                            val idx = platforms.indexOfFirst { it.name == plat.name && it.username == plat.username }
                            if (idx < 0) return@launch
                            platforms[idx] = platforms[idx].copy(stats = "N/A", statsLabel = "Loading...")
                            try {
                                val s = statsRepository.fetchStats(plat.name, plat.username)
                                platforms[idx] = platforms[idx].copy(stats = s.rating, statsLabel = s.statsLabel, profileImageUrl = s.profileImageUrl)
                                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                                saveplatformsToFirestore(uid, platforms)
                            } catch (_: Exception) {
                                platforms[idx] = platforms[idx].copy(stats = "Error", statsLabel = "Failed to load")
                            }
                        }
                    }
                )

                Spacer(Modifier.height(28.dp))

                // ── 3. Logout ─────────────────────────────────────────────────
                LogoutButton(onClick = { showLogout = true })

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ── Bio edit dialog ───────────────────────────────────────────────────────
    if (showBioEdit) {
        AlertDialog(
            onDismissRequest = { showBioEdit = false },
            title = { Text("Edit Bio", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editingBio,
                        onValueChange = { if (it.length <= 180) editingBio = it },
                        placeholder = { Text("Write something about yourself…") },
                        modifier = Modifier.fillMaxWidth().height(130.dp),
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        supportingText = { Text("${editingBio.length}/180",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { saveBio(editingBio); showBioEdit = false }) {
                    Text("Save", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBioEdit = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Name edit dialog ──────────────────────────────────────────────────────
    if (showNameEdit) {
        AlertDialog(
            onDismissRequest = { showNameEdit = false },
            title = { Text("Edit Name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = { saveName(editingName.trim()); showNameEdit = false }) {
                    Text("Save", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameEdit = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // ── Logout confirm dialog ─────────────────────────────────────────────────
    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("Logout?", fontWeight = FontWeight.Bold) },
            text = { Text("You will be signed out of your account.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogout = false
                    scope.launch {
                        FirebaseAuth.getInstance().signOut()
                        onNavigateToLogin()
                    }
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogout = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ── Helper ────────────────────────────────────────────────────────────────────

private suspend fun saveplatformsToFirestore(uid: String, platforms: List<CodingPlatform>) {
    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .update("codingPlatforms", platforms.map {
                mapOf("name" to it.name, "username" to it.username,
                    "stats" to it.stats, "statsLabel" to it.statsLabel,
                    "profileImageUrl" to (it.profileImageUrl ?: ""))
            }).await()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profile Hero Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeroCard(
    displayName: String,
    userName: String,
    email: String,
    bio: String,
    photoUri: Uri?,
    photoUrl: String,
    dsaSolved: Int,
    dsaTotal: Int,
    platformCount: Int,
    onPickPhoto: () -> Unit,
    onEditName: () -> Unit,
    onEditBio: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Gradient banner ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF0EA5E9)))
                )
        )

        // ── Avatar overlapping banner ─────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().offset(y = (-48).dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Avatar + edit button
                Box {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(avatarGradient)
                            .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .clickable { onPickPhoto() },
                        contentAlignment = Alignment.Center
                    ) {
                        val imageModel: Any? = photoUri ?: photoUrl.takeIf { it.isNotBlank() }
                        if (imageModel != null) {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = "Profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = (displayName.firstOrNull() ?: userName.firstOrNull() ?: 'U').uppercase(),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                    // Camera badge
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .clickable { onPickPhoto() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null,
                            modifier = Modifier.size(14.dp), tint = Color.White)
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Name row + edit
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = displayName.ifEmpty { userName.ifEmpty { "User" } },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(
                        onClick = onEditName,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Edit, "Edit name",
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (userName.isNotBlank()) {
                    Text("@$userName", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (email.isNotBlank()) {
                    Text(email, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Compensate the offset
        Spacer(Modifier.height((-36).dp))

        // ── Stats pills row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatPill(
                emoji   = "🔥",
                value   = "$dsaSolved",
                label   = "Solved",
                modifier = Modifier.weight(1f)
            )
            StatPill(
                emoji   = "📊",
                value   = if (dsaTotal > 0) "${(dsaSolved * 100f / dsaTotal).toInt()}%" else "0%",
                label   = "Progress",
                modifier = Modifier.weight(1f)
            )
            StatPill(
                emoji   = "🏆",
                value   = "$platformCount",
                label   = "Profiles",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Bio section ───────────────────────────────────────────────────────
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                Text("About Me", fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground)
                TextButton(
                    onClick = onEditBio,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(if (bio.isBlank()) "Add Bio" else "Edit",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (bio.isBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Tap 'Add Bio' to write something about yourself…",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp),
                        lineHeight = 20.sp
                    )
                }
            } else {
                Text(
                    text = bio,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// ── Stat pill ─────────────────────────────────────────────────────────────────

@Composable
private fun StatPill(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 18.sp)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Coding Platforms Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CodingPlatformsSection(
    platforms: List<CodingPlatform>,
    statsRepository: CodingPlatformStatsRepository,
    onPlatformAdded: (String, String) -> Unit,
    onPlatformRemoved: (CodingPlatform) -> Unit,
    onPlatformRefresh: (CodingPlatform) -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Coding Profiles", fontWeight = FontWeight.Bold, fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground)
            FilledTonalIconButton(
                onClick = { showAddSheet = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, "Add", modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        if (platforms.isEmpty()) {
            EmptyPlatformsCard { showAddSheet = true }
        } else {
            platforms.forEach { platform ->
                PlatformCard(
                    platform  = platform,
                    onRemove  = { onPlatformRemoved(platform) },
                    onRefresh = { onPlatformRefresh(platform) }
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }

    if (showAddSheet) {
        AddPlatformBottomSheet(
            existingPlatforms = platforms,
            onDismiss = { showAddSheet = false },
            onAdd     = { p, u -> onPlatformAdded(p, u); showAddSheet = false }
        )
    }
}

// ── Platform Card ─────────────────────────────────────────────────────────────

@Composable
private fun PlatformCard(
    platform: CodingPlatform,
    onRemove: () -> Unit,
    onRefresh: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val accent = platformColor(platform.name)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Accent left bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!platform.profileImageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = platform.profileImageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Text(platform.name.first().uppercase(), fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold, color = accent)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(platform.name, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("@${platform.username}", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    // Stats chip
                    Surface(shape = RoundedCornerShape(6.dp), color = accent.copy(alpha = 0.1f)) {
                        Text(
                            text = when {
                                platform.statsLabel == "Loading..." -> "⏳ Loading…"
                                platform.statsLabel.startsWith(platform.stats) -> platform.statsLabel
                                platform.stats != "N/A" -> "${platform.stats}  ·  ${platform.statsLabel}"
                                else -> platform.statsLabel.ifEmpty { "—" }
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("View Profile") },
                            onClick = {
                                showMenu = false
                                val url = getProfileUrl(platform.name, platform.username)
                                if (url.isNotEmpty()) uriHandler.openUri(url)
                            },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Refresh") },
                            onClick = { showMenu = false; onRefresh() },
                            leadingIcon = { Icon(Icons.Default.Refresh, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove", color = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; onRemove() },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
        }
    }
}

// ── Empty platforms card ──────────────────────────────────────────────────────

@Composable
private fun EmptyPlatformsCard(onAddClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏆", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text("Link your coding profiles",
                fontWeight = FontWeight.Bold, fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("Track LeetCode, Codeforces, GitHub and more",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            FilledTonalButton(onClick = onAddClick, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add Profile")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Platform Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlatformBottomSheet(
    existingPlatforms: List<CodingPlatform>,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPlatform by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    data class PlatformOption(val name: String, val emoji: String, val color: Color)
    val allPlatforms = listOf(
        PlatformOption("LeetCode",   "🧩", Color(0xFFffa116)),
        PlatformOption("Codeforces", "⚡", Color(0xFF1f8acb)),
        PlatformOption("CodeChef",   "👨‍🍳", Color(0xFF5B4638)),
        PlatformOption("GitHub",     "🐙", Color(0xFF6e5494)),
        PlatformOption("HackerRank", "🌏", Color(0xFF2EC866)),
        PlatformOption("AtCoder",    "🏔️", Color(0xFF888888))
    )
    val available = allPlatforms.filterNot { opt ->
        existingPlatforms.any { it.name.equals(opt.name, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Add Coding Profile", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface)
            Text("Choose a platform and enter your handle",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
            ) {
                items(available) { opt ->
                    val isSelected = selectedPlatform == opt.name
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) opt.color else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(
                                if (isSelected) opt.color.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { selectedPlatform = opt.name; errorMessage = null }
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(opt.emoji, fontSize = 22.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(opt.name, fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) opt.color else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; errorMessage = null },
                label = { Text("Your Username / Handle") },
                placeholder = { Text(if (selectedPlatform.isNotEmpty()) "e.g. tourist" else "Select a platform first") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = selectedPlatform.isNotEmpty(),
                leadingIcon = { Icon(Icons.Default.Person, null) },
                isError = errorMessage != null,
                supportingText = { errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    when {
                        selectedPlatform.isEmpty() -> errorMessage = "Please select a platform"
                        username.isBlank()         -> errorMessage = "Username cannot be empty"
                        else                       -> onAdd(selectedPlatform, username.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = available.isNotEmpty()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Add ${if (selectedPlatform.isNotEmpty()) selectedPlatform else "Platform"}",
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Logout Button
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(50.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
    ) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Logout", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Data + helpers
// ─────────────────────────────────────────────────────────────────────────────

data class CodingPlatform(
    val name: String,
    val username: String,
    val stats: String,
    val statsLabel: String,
    val icon: ImageVector,
    val profileImageUrl: String? = null
)

private fun getProfileUrl(platform: String, username: String) = when (platform.lowercase()) {
    "leetcode"   -> "https://leetcode.com/u/$username/"
    "codeforces" -> "https://codeforces.com/profile/$username"
    "codechef"   -> "https://www.codechef.com/users/$username"
    "hackerrank" -> "https://www.hackerrank.com/profile/$username"
    "atcoder"    -> "https://atcoder.jp/users/$username"
    "github"     -> "https://github.com/$username"
    else         -> ""
}