package com.example.auth.presentation.inApp.profilescreen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.auth.data.local.DsaDao
import com.example.auth.data.repository.CodingPlatformStatsRepository
import com.example.auth.presentation.authentication.AuthViewModel
import com.example.auth.presentation.components.IconTile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

// ── Platform accent colours ───────────────────────────────────────────────────
private fun platformColor(name: String) = when (name.lowercase()) {
    "leetcode"   -> Color(0xFFffa116)
    "codeforces" -> Color(0xFF1f8acb)
    "codechef"   -> Color(0xFF5B4638)
    "hackerrank" -> Color(0xFF2EC866)
    "atcoder"    -> Color(0xFF888888)
    "github"     -> Color(0xFF333333)
    else         -> Color(0xFFC75F3A)
}

// ── Avatar gradient ──────────────────────────────────────────────────────────
private val avatarGradient = Brush.linearGradient(
    listOf(Color(0xFFC75F3A), Color(0xFF287C7A))
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
    val collegeRepository = remember { CollegeDirectoryRepository() }

    // ── User state ────────────────────────────────────────────────────────────
    var userName    by remember { mutableStateOf("") }
    var email       by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var bio         by remember { mutableStateOf("") }
    var photoUri    by remember { mutableStateOf<Uri?>(null) }   // local pick
    var photoUrl    by remember { mutableStateOf("") }           // Firestore URL
    var photoLocalUri by remember { mutableStateOf("") }         // persisted on this device
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoAdjuster by remember { mutableStateOf(false) }
    var isPhotoSaving by remember { mutableStateOf(false) }
    var headline    by remember { mutableStateOf("") }
    var collegeName by remember { mutableStateOf("") }
    var collegeKey  by remember { mutableStateOf("") }
    var collegeCity by remember { mutableStateOf("") }
    var collegeState by remember { mutableStateOf("") }
    var branch      by remember { mutableStateOf("") }
    var graduationYear by remember { mutableStateOf("") }
    var goalRole    by remember { mutableStateOf("") }
    var skills      by remember { mutableStateOf<List<String>>(emptyList()) }
    var projects    by remember { mutableStateOf<List<ProfileProject>>(emptyList()) }
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

    var showUsernameEdit by remember { mutableStateOf(false) }
    var editingUsername by remember { mutableStateOf("") }
    var usernameUpdatedAt by remember { mutableLongStateOf(0L) }
    var usernameEditError by remember { mutableStateOf<String?>(null) }

    // ── Student profile edit state ─────────────────────────────────────────────
    var showAcademicEdit by remember { mutableStateOf(false) }
    var editingHeadline by remember { mutableStateOf("") }
    var editingCollege by remember { mutableStateOf("") }
    var editingCollegeKey by remember { mutableStateOf("") }
    var editingCollegeCity by remember { mutableStateOf("") }
    var editingCollegeState by remember { mutableStateOf("") }
    var editingBranch by remember { mutableStateOf("") }
    var editingGraduationYear by remember { mutableStateOf("") }
    var editingGoalRole by remember { mutableStateOf("") }

    var showSkillsEdit by remember { mutableStateOf(false) }
    var editingSkillsText by remember { mutableStateOf("") }

    var showProjectEdit by remember { mutableStateOf(false) }
    var editingProjectId by remember { mutableStateOf<String?>(null) }
    var editingProjectTitle by remember { mutableStateOf("") }
    var editingProjectDescription by remember { mutableStateOf("") }
    var editingProjectRepoUrl by remember { mutableStateOf("") }
    var editingProjectLiveUrl by remember { mutableStateOf("") }
    var editingProjectTech by remember { mutableStateOf("") }

    var collegeRank by remember { mutableStateOf<Int?>(null) }
    var collegePeerCount by remember { mutableIntStateOf(0) }

    // ── Logout confirm ─────────────────────────────────────────────────────────
    var showLogout by remember { mutableStateOf(false) }

    // ── Image picker ───────────────────────────────────────────────────────────
    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        pendingPhotoUri = uri
        showPhotoAdjuster = true
    }

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
            photoLocalUri = loadLocalProfilePhoto(context, currentUser.uid)
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
                    usernameUpdatedAt = doc.getLong("usernameUpdatedAt") ?: 0L
                    email       = doc.getString("email")       ?: ""
                    displayName = doc.getString("displayName") ?: ""
                    bio         = doc.getString("bio")         ?: ""
                    photoUrl    = doc.getString("photoUrl")    ?: ""
                    headline    = doc.getString("headline")    ?: ""
                    collegeName = doc.getString("collegeName") ?: ""
                    collegeKey  = doc.getString("collegeNameNormalized")
                        ?: doc.getString("collegeKey")
                        ?: collegeName.normalizedCollegeKey()
                    collegeCity = doc.getString("collegeCity") ?: ""
                    collegeState = doc.getString("collegeState") ?: ""
                    branch      = doc.getString("branch")      ?: ""
                    graduationYear = doc.getString("graduationYear") ?: ""
                    goalRole    = doc.getString("goalRole")    ?: ""
                    skills      = parseSkills(doc.get("skills"))
                    projects    = parseProjects(doc.get("projects"))

                    val platformsData = (doc.get("codingPlatforms") as? List<*>)
                        ?.mapNotNull { it as? Map<*, *> }
                    platformsData?.forEach { pm ->
                        val pName  = pm["name"] as? String ?: return@forEach
                        val pUser  = pm["username"] as? String ?: ""
                        val pStats = pm["stats"] as? String ?: "N/A"
                        val pLabel = pm["statsLabel"] as? String ?: ""
                        val pImg   = (pm["profileImageUrl"] as? String)?.takeIf { it.isNotEmpty() }
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

    val platformSignal = platforms.joinToString("|") { "${it.name}:${it.username}:${it.stats}:${it.statsLabel}" }
    val flexScore = remember(dsaSolved, dsaTotal, platformSignal, bio, collegeName, branch, goalRole, skills) {
        calculateFlexScore(
            dsaSolved = dsaSolved,
            dsaTotal = dsaTotal,
            platforms = platforms,
            bio = bio,
            collegeName = collegeName,
            branch = branch,
            goalRole = goalRole,
            skills = skills
        )
    }
    val profileCompletion = remember(displayName, bio, photoLocalUri, photoUrl, collegeName, branch, graduationYear, goalRole, skills, projects, platformSignal) {
        calculateProfileCompletion(
            displayName = displayName,
            bio = bio,
            hasPhoto = photoLocalUri.isNotBlank() || photoUrl.isNotBlank(),
            collegeName = collegeName,
            branch = branch,
            graduationYear = graduationYear,
            goalRole = goalRole,
            skills = skills,
            projectCount = projects.size,
            platformCount = platforms.size
        )
    }

    LaunchedEffect(isLoading, userNotFound, flexScore, collegeName, collegeKey, profileCompletion) {
        if (isLoading || userNotFound) return@LaunchedEffect
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return@LaunchedEffect
        val normalizedCollege = collegeKey.ifBlank { collegeName.normalizedCollegeKey() }

        try {
            withContext(Dispatchers.IO) {
                FirebaseFirestore.getInstance().collection("users").document(currentUser.uid)
                    .update(
                        mapOf(
                            "profileScore" to flexScore,
                            "profileCompletion" to profileCompletion,
                            "collegeNameNormalized" to normalizedCollege,
                            "collegeKey" to normalizedCollege
                        )
                    ).await()
            }
        } catch (_: Exception) {}

        if (normalizedCollege.isBlank()) {
            collegeRank = null
            collegePeerCount = 0
            return@LaunchedEffect
        }

        try {
            val docs = withContext(Dispatchers.IO) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .whereEqualTo("collegeNameNormalized", normalizedCollege)
                    .get()
                    .await()
            }
            val scores = docs.documents.map { it.getLong("profileScore")?.toInt() ?: 0 }
            collegePeerCount = scores.size
            collegeRank = scores.count { it > flexScore } + 1
        } catch (_: Exception) {
            collegeRank = null
            collegePeerCount = 0
        }
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

    fun saveUsername(newUsername: String) {
        val cleanUsername = newUsername.trim().lowercase(Locale.ROOT)
        val normalizedUsername = cleanUsername.normalizedUsernameKey()
        if (!cleanUsername.isValidUsername()) {
            usernameEditError = "Use 3-20 letters, numbers, underscores or dots."
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        scope.launch {
            try {
                val now = System.currentTimeMillis()
                withContext(Dispatchers.IO) {
                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("users").document(uid)
                    val usernameRef = db.collection("usernames").document(normalizedUsername)

                    db.runTransaction { transaction ->
                        val userSnapshot = transaction.get(userRef)
                        val storedUsername = userSnapshot.getString("userName").orEmpty()
                        val storedNormalized = userSnapshot.getString("userNameNormalized")
                            ?: storedUsername.normalizedUsernameKey()
                        val lastUpdated = userSnapshot.getLong("usernameUpdatedAt") ?: 0L
                        val changed = storedNormalized != normalizedUsername

                        if (changed && lastUpdated > 0L && now - lastUpdated < USERNAME_UPDATE_INTERVAL_MS) {
                            throw IllegalStateException(usernameCooldownMessage(lastUpdated, now))
                        }

                        val ownerSnapshot = transaction.get(usernameRef)
                        val ownerUid = ownerSnapshot.getString("uid")
                        if (ownerSnapshot.exists() && ownerUid != uid) {
                            throw IllegalStateException("Username already taken.")
                        }

                        if (storedNormalized.isNotBlank() && storedNormalized != normalizedUsername) {
                            transaction.delete(db.collection("usernames").document(storedNormalized))
                        }

                        transaction.set(
                            usernameRef,
                            mapOf("uid" to uid, "userName" to cleanUsername, "updatedAt" to now)
                        )
                        transaction.update(
                            userRef,
                            mapOf(
                                "userName" to cleanUsername,
                                "userNameNormalized" to normalizedUsername,
                                "usernameUpdatedAt" to now
                            )
                        )
                    }.await()
                }
                userName = cleanUsername
                usernameUpdatedAt = now
                usernameEditError = null
                showUsernameEdit = false
                Toast.makeText(context, "Username updated", Toast.LENGTH_SHORT).show()
            } catch (error: Exception) {
                usernameEditError = error.message ?: "Could not update username."
            }
        }
    }

    fun saveAcademicProfile(
        newHeadline: String,
        newCollege: String,
        newCollegeKey: String,
        newCollegeCity: String,
        newCollegeState: String,
        newBranch: String,
        newGraduationYear: String,
        newGoalRole: String
    ) {
        val resolvedCollegeKey = newCollegeKey.ifBlank { newCollege.normalizedCollegeKey() }
        headline = newHeadline
        collegeName = newCollege
        collegeKey = resolvedCollegeKey
        collegeCity = newCollegeCity
        collegeState = newCollegeState
        branch = newBranch
        graduationYear = newGraduationYear
        goalRole = newGoalRole
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                        .update(
                            mapOf(
                                "headline" to newHeadline,
                                "collegeName" to newCollege,
                                "collegeNameNormalized" to resolvedCollegeKey,
                                "collegeKey" to resolvedCollegeKey,
                                "collegeCity" to newCollegeCity,
                                "collegeState" to newCollegeState,
                                "branch" to newBranch,
                                "graduationYear" to newGraduationYear,
                                "goalRole" to newGoalRole
                            )
                        ).await()
                }
            } catch (_: Exception) {}
        }
    }

    fun saveSkills(newSkills: List<String>) {
        skills = newSkills
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                        .update("skills", newSkills).await()
                }
            } catch (_: Exception) {}
        }
    }

    fun saveProject(project: ProfileProject) {
        val nextProjects = if (editingProjectId == null) {
            projects + project
        } else {
            projects.map { if (it.id == project.id) project else it }
        }
        projects = nextProjects
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                        .update("projects", nextProjects.map { it.toFirestoreMap() }).await()
                }
            } catch (_: Exception) {}
        }
    }

    fun removeProject(projectId: String) {
        val nextProjects = projects.filterNot { it.id == projectId }
        projects = nextProjects
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                        .update("projects", nextProjects.map { it.toFirestoreMap() }).await()
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
                    IconTile(Icons.Default.Person, MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
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
                    headline    = headline,
                    bio         = bio,
                    photoUri    = photoUri,
                    photoLocalUri = photoLocalUri,
                    photoUrl    = photoUrl,
                    dsaSolved   = dsaSolved,
                    dsaTotal    = dsaTotal,
                    platformCount = platforms.size,
                    onPickPhoto  = { pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )},
                    onEditName   = { editingName = displayName; showNameEdit = true },
                    onEditBio    = { editingBio = bio; showBioEdit = true },
                    onOpenConnect = { navController?.navigate("connect") },
                    onShareProfile = {
                        shareProfile(
                            context = context,
                            displayName = displayName.ifBlank { userName },
                            userName = userName,
                            collegeName = collegeName,
                            branch = branch,
                            goalRole = goalRole,
                            flexScore = flexScore,
                            skills = skills
                        )
                    }
                )

                Spacer(Modifier.height(18.dp))

                ProfileScoreSection(
                    flexScore = flexScore,
                    profileCompletion = profileCompletion,
                    collegeRank = collegeRank,
                    collegePeerCount = collegePeerCount,
                    collegeName = collegeName
                )

                Spacer(Modifier.height(14.dp))

                AccountSettingsSection(
                    username = userName,
                    usernameUpdatedAt = usernameUpdatedAt,
                    onEditPhoto = { pickMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    ) },
                    onEditName = { editingName = displayName; showNameEdit = true },
                    onEditUsername = {
                        editingUsername = userName
                        usernameEditError = null
                        showUsernameEdit = true
                    },
                    onEditBio = { editingBio = bio; showBioEdit = true },
                    onLogout = { showLogout = true }
                )

                Spacer(Modifier.height(14.dp))

                StudentIdentitySection(
                    collegeName = collegeName,
                    branch = branch,
                    graduationYear = graduationYear,
                    goalRole = goalRole,
                    onEdit = {
                        editingHeadline = headline
                        editingCollege = collegeName
                        editingCollegeKey = collegeKey
                        editingCollegeCity = collegeCity
                        editingCollegeState = collegeState
                        editingBranch = branch
                        editingGraduationYear = graduationYear
                        editingGoalRole = goalRole
                        showAcademicEdit = true
                    }
                )

                Spacer(Modifier.height(14.dp))

                SkillTagsSection(
                    skills = skills,
                    onEdit = {
                        editingSkillsText = skills.joinToString(", ")
                        showSkillsEdit = true
                    }
                )

                Spacer(Modifier.height(22.dp))

                GithubProjectsSection(
                    projects = projects,
                    onAdd = {
                        editingProjectId = null
                        editingProjectTitle = ""
                        editingProjectDescription = ""
                        editingProjectRepoUrl = ""
                        editingProjectLiveUrl = ""
                        editingProjectTech = ""
                        showProjectEdit = true
                    },
                    onEdit = { project ->
                        editingProjectId = project.id
                        editingProjectTitle = project.title
                        editingProjectDescription = project.description
                        editingProjectRepoUrl = project.repoUrl
                        editingProjectLiveUrl = project.liveUrl
                        editingProjectTech = project.tech.joinToString(", ")
                        showProjectEdit = true
                    },
                    onRemove = { project -> removeProject(project.id) }
                )

                Spacer(Modifier.height(22.dp))

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

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ── Bio edit dialog ───────────────────────────────────────────────────────
    if (showBioEdit) {
        ProfileEditBottomSheet(
            title = "Edit bio",
            subtitle = "Make it short, specific, and useful for people who want to connect.",
            icon = Icons.Default.Edit,
            onDismiss = { showBioEdit = false },
            onSave = { saveBio(editingBio.trim()); showBioEdit = false }
        ) {
            OutlinedTextField(
                value = editingBio,
                onValueChange = { if (it.length <= 220) editingBio = it },
                placeholder = { Text("Example: Android developer exploring Firebase, DSA and open-source projects.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6,
                shape = RoundedCornerShape(8.dp),
                supportingText = {
                    Text("${editingBio.length}/220", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                }
            )
        }
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
                    shape = RoundedCornerShape(8.dp)
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
            shape = RoundedCornerShape(8.dp)
        )
    }

    if (showUsernameEdit) {
        AlertDialog(
            onDismissRequest = { showUsernameEdit = false },
            title = { Text("Edit username", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editingUsername,
                        onValueChange = { value ->
                            editingUsername = value
                                .lowercase(Locale.ROOT)
                                .filter { it.isLetterOrDigit() || it == '_' || it == '.' }
                                .take(20)
                            usernameEditError = null
                        },
                        label = { Text("Username") },
                        prefix = { Text("@") },
                        singleLine = true,
                        isError = usernameEditError != null,
                        supportingText = {
                            Text(usernameEditError ?: "You can update username once every 7 days.")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { saveUsername(editingUsername) }) {
                    Text("Save", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUsernameEdit = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(8.dp)
        )
    }

    if (showPhotoAdjuster && pendingPhotoUri != null) {
        ProfilePhotoAdjustDialog(
            sourceUri = pendingPhotoUri!!,
            isSaving = isPhotoSaving,
            onDismiss = {
                if (!isPhotoSaving) {
                    showPhotoAdjuster = false
                    pendingPhotoUri = null
                }
            },
            onSave = { zoom, offsetX, offsetY ->
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@ProfilePhotoAdjustDialog
                scope.launch {
                    isPhotoSaving = true
                    val savedUri = prepareAdjustedProfilePhoto(
                        context = context,
                        uid = uid,
                        sourceUri = pendingPhotoUri!!,
                        zoom = zoom,
                        offsetX = offsetX,
                        offsetY = offsetY
                    )
                    if (savedUri != null) {
                        photoLocalUri = savedUri
                        photoUri = Uri.parse(savedUri)
                        val backendUrl = uploadProfilePhotoToBackend(uid, Uri.parse(savedUri))
                        if (backendUrl.isNotBlank()) photoUrl = backendUrl
                    }
                    isPhotoSaving = false
                    showPhotoAdjuster = false
                    pendingPhotoUri = null
                }
            }
        )
    }

    if (showAcademicEdit) {
        ProfileEditBottomSheet(
            title = "Student identity",
            subtitle = "Pick your college from suggestions so ranking and peer discovery stay clean.",
            icon = Icons.Default.School,
            onDismiss = { showAcademicEdit = false },
            onSave = {
                saveAcademicProfile(
                    newHeadline = editingHeadline.trim(),
                    newCollege = editingCollege.trim(),
                    newCollegeKey = editingCollegeKey.ifBlank { editingCollege.normalizedCollegeKey() },
                    newCollegeCity = editingCollegeCity.trim(),
                    newCollegeState = editingCollegeState.trim(),
                    newBranch = editingBranch.trim(),
                    newGraduationYear = editingGraduationYear.trim(),
                    newGoalRole = editingGoalRole.trim()
                )
                showAcademicEdit = false
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = editingHeadline,
                    onValueChange = { if (it.length <= 80) editingHeadline = it },
                    label = { Text("Headline") },
                    placeholder = { Text("Android dev | DSA | Open source") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                CollegeAutocompleteField(
                    value = editingCollege,
                    repository = collegeRepository,
                    onValueChange = {
                        editingCollege = it
                        editingCollegeKey = it.normalizedCollegeKey()
                        editingCollegeCity = ""
                        editingCollegeState = ""
                    },
                    onCollegeSelected = { college ->
                        editingCollege = college.name
                        editingCollegeKey = college.normalizedName
                        editingCollegeCity = college.city
                        editingCollegeState = college.state
                    }
                )
                OutlinedTextField(
                    value = editingBranch,
                    onValueChange = { editingBranch = it },
                    label = { Text("Branch") },
                    placeholder = { Text("CSE, ECE, Mechanical...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editingGraduationYear,
                        onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) editingGraduationYear = it },
                        label = { Text("Batch") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = editingGoalRole,
                        onValueChange = { editingGoalRole = it },
                        label = { Text("Goal") },
                        placeholder = { Text("SDE Intern") },
                        singleLine = true,
                        modifier = Modifier.weight(1.35f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }

    if (showSkillsEdit) {
        ProfileEditBottomSheet(
            title = "Skills",
            subtitle = "Add searchable skills. Keep the strongest ones first.",
            icon = Icons.Default.Code,
            onDismiss = { showSkillsEdit = false },
            onSave = {
                saveSkills(
                    editingSkillsText
                        .split(",", "\n")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .distinctBy { it.lowercase(Locale.ROOT) }
                        .take(12)
                )
                showSkillsEdit = false
            }
        ) {
            OutlinedTextField(
                value = editingSkillsText,
                onValueChange = { editingSkillsText = it },
                label = { Text("Comma separated skills") },
                placeholder = { Text("Kotlin, Firebase, DSA, React") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }

    if (showProjectEdit) {
        ProfileEditBottomSheet(
            title = if (editingProjectId == null) "Add project" else "Edit project",
            subtitle = "Repo link is enough. Add a live link when the project is deployed.",
            icon = Icons.Default.AccountTree,
            onDismiss = { showProjectEdit = false },
            onSave = {
                if (editingProjectTitle.isNotBlank() && editingProjectRepoUrl.isNotBlank()) {
                    saveProject(
                        ProfileProject(
                            id = editingProjectId ?: UUID.randomUUID().toString(),
                            title = editingProjectTitle.trim(),
                            description = editingProjectDescription.trim(),
                            repoUrl = editingProjectRepoUrl.trim(),
                            liveUrl = editingProjectLiveUrl.trim(),
                            tech = parseSkills(editingProjectTech)
                        )
                    )
                    showProjectEdit = false
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = editingProjectTitle,
                    onValueChange = { editingProjectTitle = it },
                    label = { Text("Project title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = editingProjectRepoUrl,
                    onValueChange = { editingProjectRepoUrl = it },
                    label = { Text("GitHub repo URL") },
                    placeholder = { Text("https://github.com/user/repo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = editingProjectLiveUrl,
                    onValueChange = { editingProjectLiveUrl = it },
                    label = { Text("Live link optional") },
                    placeholder = { Text("https://your-project.vercel.app") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = editingProjectDescription,
                    onValueChange = { if (it.length <= 160) editingProjectDescription = it },
                    label = { Text("Short description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = editingProjectTech,
                    onValueChange = { editingProjectTech = it },
                    label = { Text("Tech stack") },
                    placeholder = { Text("Kotlin, Firebase, Jetpack Compose") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                if (editingProjectId != null) {
                    OutlinedButton(
                        onClick = {
                            editingProjectId?.let(::removeProject)
                            showProjectEdit = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.42f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Remove project")
                    }
                }
            }
        }
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
                        if (navController != null) {
                            navController.navigate("first_screen") {
                                popUpTo("profile") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            onNavigateToLogin()
                        }
                    }
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogout = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(8.dp)
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

@Composable
private fun ProfilePhotoAdjustDialog(
    sourceUri: Uri,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (zoom: Float, offsetX: Float, offsetY: Float) -> Unit
) {
    var zoom by remember(sourceUri) { mutableFloatStateOf(1.15f) }
    var offsetX by remember(sourceUri) { mutableFloatStateOf(0f) }
    var offsetY by remember(sourceUri) { mutableFloatStateOf(0f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust photo", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = sourceUri,
                        contentDescription = "Selected profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                translationX = offsetX * size.width * 0.18f
                                translationY = offsetY * size.height * 0.18f
                            }
                    )
                }

                AdjustmentSlider("Zoom", zoom, 1f, 2.4f) { zoom = it }
                AdjustmentSlider("Left / right", offsetX, -1f, 1f) { offsetX = it }
                AdjustmentSlider("Up / down", offsetY, -1f, 1f) { offsetY = it }
            }
        },
        confirmButton = {
            TextButton(enabled = !isSaving, onClick = { onSave(zoom, offsetX, offsetY) }) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        Slider(value = value, onValueChange = onValueChange, valueRange = min..max)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileEditBottomSheet(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(icon, MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(18.dp))
            content()
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CollegeAutocompleteField(
    value: String,
    repository: CollegeDirectoryRepository,
    onValueChange: (String) -> Unit,
    onCollegeSelected: (CollegeSuggestion) -> Unit
) {
    var suggestions by remember { mutableStateOf<List<CollegeSuggestion>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        val query = value.trim()
        if (query.length < 2) {
            suggestions = emptyList()
            return@LaunchedEffect
        }
        delay(320L)
        isSearching = true
        suggestions = repository.searchColleges(query).getOrDefault(emptyList())
        isSearching = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("College") },
            placeholder = { Text("Start typing your college name") },
            singleLine = true,
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.School, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        suggestions.take(5).forEach { college ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCollegeSelected(college) },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(college.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    if (college.subtitle.isNotBlank()) {
                        Text(college.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
        }
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
    headline: String,
    bio: String,
    photoUri: Uri?,
    photoLocalUri: String,
    photoUrl: String,
    dsaSolved: Int,
    dsaTotal: Int,
    platformCount: Int,
    onPickPhoto: () -> Unit,
    onEditName: () -> Unit,
    onEditBio: () -> Unit,
    onOpenConnect: () -> Unit,
    onShareProfile: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Gradient banner ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFFE0D0), Color(0xFFD7EFEC), Color(0xFFE3ECD9)))
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
                        val imageModel: Any? = photoUri
                            ?: photoLocalUri.takeIf { it.isNotBlank() }
                            ?: photoUrl.takeIf { it.isNotBlank() }
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

                val showUsername = userName.isNotBlank() && !userName.equals(email, ignoreCase = true)
                val showEmail = email.isNotBlank() && !email.equals(userName, ignoreCase = true)
                if (showUsername) {
                    Text("@$userName", fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (headline.isNotBlank()) {
                    Text(headline, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 3.dp))
                }
                if (showEmail) {
                    Text(email, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenConnect,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Find peers", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onShareProfile,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("Share", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
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
                icon    = Icons.Default.CheckCircle,
                value   = "$dsaSolved",
                label   = "Solved",
                modifier = Modifier.weight(1f)
            )
            StatPill(
                icon    = Icons.AutoMirrored.Filled.TrendingUp,
                value   = if (dsaTotal > 0) "${(dsaSolved * 100f / dsaTotal).toInt()}%" else "0%",
                label   = "Progress",
                modifier = Modifier.weight(1f)
            )
            StatPill(
                icon    = Icons.Default.AccountCircle,
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
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Tap 'Add Bio' to write something about yourself...",
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
private fun StatPill(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProfileScoreSection(
    flexScore: Int,
    profileCompletion: Int,
    collegeRank: Int?,
    collegePeerCount: Int,
    collegeName: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Flex score",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        flexScore.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                IconTile(Icons.Default.EmojiEvents, MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (profileCompletion / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MiniMetric(
                    value = if (collegeRank != null) "#$collegeRank" else "--",
                    label = if (collegeName.isBlank()) "Set college" else "College rank",
                    modifier = Modifier.weight(1f)
                )
                MiniMetric(
                    value = if (collegePeerCount > 0) "$collegePeerCount" else "--",
                    label = "College peers",
                    modifier = Modifier.weight(1f)
                )
                MiniMetric(
                    value = "$profileCompletion%",
                    label = "Profile ready",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AccountSettingsSection(
    username: String,
    usernameUpdatedAt: Long,
    onEditPhoto: () -> Unit,
    onEditName: () -> Unit,
    onEditUsername: () -> Unit,
    onEditBio: () -> Unit,
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            SettingsActionRow(Icons.Default.CameraAlt, "Profile photo", "Crop and adjust", onEditPhoto)
            SettingsActionRow(Icons.Default.Person, "Display name", "Edit name", onEditName)
            SettingsActionRow(
                Icons.Default.AlternateEmail,
                "Username",
                if (usernameUpdatedAt > 0L) usernameCooldownSummary(usernameUpdatedAt) else "@${username.ifBlank { "username" }}",
                onEditUsername
            )
            SettingsActionRow(Icons.Default.Edit, "Bio", "Edit about", onEditBio)
            SettingsActionRow(Icons.AutoMirrored.Filled.ExitToApp, "Logout", "Sign out", onLogout, danger = true)
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    val contentColor = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = contentColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun StudentIdentitySection(
    collegeName: String,
    branch: String,
    graduationYear: String,
    goalRole: String,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Student Identity", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit student profile", modifier = Modifier.size(17.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            IdentityRow(Icons.Default.School, "College", collegeName.ifBlank { "Add college" })
            IdentityRow(Icons.Default.Badge, "Branch", branch.ifBlank { "Add branch" })
            IdentityRow(Icons.Default.CalendarMonth, "Batch", graduationYear.ifBlank { "Add batch" })
            IdentityRow(Icons.Default.TrackChanges, "Goal", goalRole.ifBlank { "Add target role" })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillTagsSection(
    skills: List<String>,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Skills", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Text(if (skills.isEmpty()) "Add" else "Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
            if (skills.isEmpty()) {
                Text(
                    "Add skills to make your profile easier to discover.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    skills.forEach { skill ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                        ) {
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GithubProjectsSection(
    projects: List<ProfileProject>,
    onAdd: () -> Unit,
    onEdit: (ProfileProject) -> Unit,
    onRemove: (ProfileProject) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("GitHub Projects", fontWeight = FontWeight.Bold, fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onBackground)
            FilledTonalIconButton(onClick = onAdd, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add project", modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(12.dp))

        if (projects.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconTile(Icons.Default.AccountTree, MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))
                    Text("Showcase shipped work", fontWeight = FontWeight.Bold)
                    Text(
                        "Add a GitHub repo. If there is a live demo, it gets its own action.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    FilledTonalButton(onClick = onAdd, shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add project")
                    }
                }
            }
        } else {
            projects.forEach { project ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.Top) {
                            IconTile(Icons.Default.AccountTree, MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(project.title, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                if (project.description.isNotBlank()) {
                                    Text(
                                        project.description,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp,
                                        modifier = Modifier.padding(top = 3.dp)
                                    )
                                }
                            }
                            var showMenu by remember(project.id) { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Project menu")
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        onClick = { showMenu = false; onEdit(project) },
                                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Remove", color = MaterialTheme.colorScheme.error) },
                                        onClick = { showMenu = false; onRemove(project) },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }

                        if (project.tech.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                project.tech.take(6).forEach { tech ->
                                    Surface(shape = RoundedCornerShape(7.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                        Text(tech, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { openUrlSafely(uriHandler, project.repoUrl) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Repo")
                            }
                            if (project.liveUrl.isNotBlank()) {
                                Button(
                                    onClick = { openUrlSafely(uriHandler, project.liveUrl) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Live")
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun MiniMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun IdentityRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.width(56.dp))
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Accent left bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
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
                                platform.statsLabel == "Loading..." -> "Loading..."
                                platform.statsLabel.startsWith(platform.stats) -> platform.statsLabel
                                platform.stats != "N/A" -> "${platform.stats} - ${platform.statsLabel}"
                                else -> platform.statsLabel.ifEmpty { "-" }
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
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconTile(Icons.Default.AccountCircle, MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text("Link your coding profiles",
                fontWeight = FontWeight.Bold, fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("Track LeetCode, Codeforces, GitHub and more",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            FilledTonalButton(onClick = onAddClick, shape = RoundedCornerShape(8.dp)) {
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

    data class PlatformOption(val name: String, val icon: ImageVector, val color: Color)
    val allPlatforms = listOf(
        PlatformOption("LeetCode",   Icons.Default.Code, Color(0xFFffa116)),
        PlatformOption("Codeforces", Icons.Default.Bolt, Color(0xFF1f8acb)),
        PlatformOption("CodeChef",   Icons.Default.Restaurant, Color(0xFF5B4638)),
        PlatformOption("GitHub",     Icons.Default.AccountTree, Color(0xFF333333)),
        PlatformOption("HackerRank", Icons.Default.Terminal, Color(0xFF2EC866)),
        PlatformOption("AtCoder",    Icons.Default.Functions, Color(0xFF888888))
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
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) opt.color else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(
                                if (isSelected) opt.color.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { selectedPlatform = opt.name; errorMessage = null }
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(opt.icon, contentDescription = null, tint = opt.color, modifier = Modifier.size(22.dp))
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
                shape = RoundedCornerShape(8.dp)
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
                shape = RoundedCornerShape(8.dp),
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
        shape = RoundedCornerShape(8.dp),
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

private suspend fun prepareAdjustedProfilePhoto(
    context: Context,
    uid: String,
    sourceUri: Uri,
    zoom: Float,
    offsetX: Float,
    offsetY: Float
): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.filesDir, "profile_photos").apply { mkdirs() }
            val destination = File(directory, "avatar_$uid.jpg")
            val sourceBitmap = loadBitmapFromUri(context, sourceUri) ?: return@runCatching null
            val outputBitmap = cropProfileBitmap(sourceBitmap, zoom, offsetX, offsetY)
            destination.outputStream().use { output ->
                outputBitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
            }
            if (outputBitmap != sourceBitmap) outputBitmap.recycle()
            sourceBitmap.recycle()

            Uri.fromFile(destination).toString().also { savedUri ->
                context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putString("photo_$uid", savedUri)
                    .apply()
            }
        }.getOrNull()
    }

private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? =
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                decoder.isMutableRequired = false
            }
        } else {
            context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
        }?.copy(Bitmap.Config.ARGB_8888, false)
    }.getOrNull()

private fun cropProfileBitmap(source: Bitmap, zoom: Float, offsetX: Float, offsetY: Float): Bitmap {
    val safeZoom = zoom.coerceIn(1f, 2.4f)
    val minSide = minOf(source.width, source.height).toFloat()
    val cropSide = (minSide / safeZoom).coerceAtLeast(1f)
    val maxOffsetX = ((source.width - cropSide) / 2f).coerceAtLeast(0f)
    val maxOffsetY = ((source.height - cropSide) / 2f).coerceAtLeast(0f)
    val centerX = source.width / 2f + offsetX.coerceIn(-1f, 1f) * maxOffsetX
    val centerY = source.height / 2f + offsetY.coerceIn(-1f, 1f) * maxOffsetY
    val left = (centerX - cropSide / 2f).coerceIn(0f, source.width - cropSide)
    val top = (centerY - cropSide / 2f).coerceIn(0f, source.height - cropSide)
    val src = Rect(left.toInt(), top.toInt(), (left + cropSide).toInt(), (top + cropSide).toInt())
    val output = Bitmap.createBitmap(720, 720, Bitmap.Config.ARGB_8888)
    Canvas(output).drawBitmap(source, src, Rect(0, 0, output.width, output.height), Paint(Paint.ANTI_ALIAS_FLAG))
    return output
}

private suspend fun uploadProfilePhotoToBackend(uid: String, sourceUri: Uri): String =
    withContext(Dispatchers.IO) {
        runCatching {
            val ref = FirebaseStorage.getInstance()
                .reference
                .child("profile_photos/$uid/avatar_${System.currentTimeMillis()}.jpg")
            ref.putFile(sourceUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("photoUrl", downloadUrl)
                .await()
            downloadUrl
        }.getOrDefault("")
    }

private fun loadLocalProfilePhoto(context: Context, uid: String): String =
    context.getSharedPreferences(PROFILE_PREFS, Context.MODE_PRIVATE)
        .getString("photo_$uid", "")
        .orEmpty()

private fun parseSkills(value: Any?): List<String> = when (value) {
    is List<*> -> value.mapNotNull { it?.toString()?.trim() }.filter { it.isNotBlank() }
    is String -> value.split(",", "\n").map { it.trim() }.filter { it.isNotBlank() }
    else -> emptyList()
}.distinctBy { it.lowercase(Locale.ROOT) }.take(12)

private fun normalizeCollege(value: String): String =
    value.normalizedCollegeKey()

private fun parseProjects(value: Any?): List<ProfileProject> =
    (value as? List<*>)?.mapNotNull { item ->
        val map = item as? Map<*, *> ?: return@mapNotNull null
        val id = map["id"] as? String ?: UUID.randomUUID().toString()
        val title = map["title"] as? String ?: return@mapNotNull null
        val repoUrl = map["repoUrl"] as? String ?: ""
        ProfileProject(
            id = id,
            title = title,
            description = map["description"] as? String ?: "",
            repoUrl = repoUrl,
            liveUrl = map["liveUrl"] as? String ?: "",
            tech = parseSkills(map["tech"])
        )
    }.orEmpty()

private fun openUrlSafely(uriHandler: androidx.compose.ui.platform.UriHandler, rawUrl: String) {
    val clean = rawUrl.trim()
    if (clean.isBlank()) return
    val url = if (clean.startsWith("http://") || clean.startsWith("https://")) clean else "https://$clean"
    runCatching { uriHandler.openUri(url) }
}

private fun calculateFlexScore(
    dsaSolved: Int,
    dsaTotal: Int,
    platforms: List<CodingPlatform>,
    bio: String,
    collegeName: String,
    branch: String,
    goalRole: String,
    skills: List<String>
): Int {
    val dsaProgress = if (dsaTotal > 0) (dsaSolved * 100 / dsaTotal).coerceIn(0, 100) else 0
    val dsaScore = (dsaSolved * 4 + dsaProgress * 2).coerceAtMost(520)
    val platformScore = platforms.sumOf { platform ->
        val numeric = Regex("\\d+").find(platform.stats)?.value?.toIntOrNull()
        when {
            numeric != null && numeric >= 1000 -> ((numeric - 900) / 8).coerceIn(45, 180)
            numeric != null -> (numeric / 3).coerceIn(25, 120)
            platform.stats != "N/A" && platform.stats != "Error" -> 45
            else -> 25
        }
    }.coerceAtMost(280)
    val profileScore =
        (if (bio.length >= 40) 40 else 0) +
        (if (collegeName.isNotBlank()) 35 else 0) +
        (if (branch.isNotBlank()) 25 else 0) +
        (if (goalRole.isNotBlank()) 35 else 0) +
        (skills.size * 12).coerceAtMost(90)

    return (dsaScore + platformScore + profileScore).coerceIn(0, 999)
}

private fun calculateProfileCompletion(
    displayName: String,
    bio: String,
    hasPhoto: Boolean,
    collegeName: String,
    branch: String,
    graduationYear: String,
    goalRole: String,
    skills: List<String>,
    projectCount: Int,
    platformCount: Int
): Int {
    var score = 0
    if (displayName.isNotBlank()) score += 10
    if (bio.length >= 40) score += 15
    if (hasPhoto) score += 10
    if (collegeName.isNotBlank()) score += 15
    if (branch.isNotBlank()) score += 10
    if (graduationYear.isNotBlank()) score += 8
    if (goalRole.isNotBlank()) score += 12
    if (skills.size >= 3) score += 10
    if (projectCount > 0) score += 5
    if (platformCount > 0) score += 5
    return score.coerceIn(0, 100)
}

private fun shareProfile(
    context: Context,
    displayName: String,
    userName: String,
    collegeName: String,
    branch: String,
    goalRole: String,
    flexScore: Int,
    skills: List<String>
) {
    val message = buildString {
        appendLine("${displayName.ifBlank { "EngiFix student" }} on EngiFix")
        if (userName.isNotBlank()) appendLine("@$userName")
        if (collegeName.isNotBlank() || branch.isNotBlank()) {
            appendLine(listOf(branch, collegeName).filter { it.isNotBlank() }.joinToString(" - "))
        }
        if (goalRole.isNotBlank()) appendLine("Goal: $goalRole")
        appendLine("Flex score: $flexScore")
        if (skills.isNotEmpty()) appendLine("Skills: ${skills.joinToString(", ")}")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, "Share EngiFix profile"))
}

private fun String.normalizedUsernameKey(): String =
    trim().lowercase(Locale.ROOT).replace(Regex("[^a-z0-9_.]"), "")

private fun String.isValidUsername(): Boolean =
    matches(Regex("^[a-z0-9_.]{3,20}$"))

private fun usernameCooldownMessage(lastUpdated: Long, now: Long): String {
    val remainingMillis = (USERNAME_UPDATE_INTERVAL_MS - (now - lastUpdated)).coerceAtLeast(0L)
    val days = (remainingMillis + DAY_MS - 1L) / DAY_MS
    return "You can update username once a week. Try again in $days day${if (days == 1L) "" else "s"}."
}

private fun usernameCooldownSummary(lastUpdated: Long): String {
    val now = System.currentTimeMillis()
    val remainingMillis = USERNAME_UPDATE_INTERVAL_MS - (now - lastUpdated)
    if (remainingMillis <= 0L) return "Available now"
    val days = (remainingMillis + DAY_MS - 1L) / DAY_MS
    return "Available in $days day${if (days == 1L) "" else "s"}"
}

private const val DAY_MS = 24L * 60L * 60L * 1000L
private const val USERNAME_UPDATE_INTERVAL_MS = 7L * DAY_MS
private const val PROFILE_PREFS = "engifix_profile"

data class ProfileProject(
    val id: String,
    val title: String,
    val description: String,
    val repoUrl: String,
    val liveUrl: String,
    val tech: List<String>
) {
    fun toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "repoUrl" to repoUrl,
        "liveUrl" to liveUrl,
        "tech" to tech
    )
}

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
