package com.example.auth.presentation.inApp.profilescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.auth.presentation.authentication.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // User data state
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var userNotFound by remember { mutableStateOf(false) }

    // Coding platforms state
    val platforms = remember { mutableStateListOf<CodingPlatform>() }

    // Load user data
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val document = db.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (document.exists()) {
                    userName = document.getString("userName") ?: ""
                    email = document.getString("email") ?: ""
                    displayName = document.getString("displayName") ?: ""
                    phoneNumber = document.getString("phoneNumber") ?: ""

                    // Load coding platforms if they exist
                    val platformsData = document.get("codingPlatforms") as? List<Map<String, String>>
                    platformsData?.forEach { platformMap ->
                        platforms.add(
                            CodingPlatform(
                                name = platformMap["name"] ?: "",
                                username = platformMap["username"] ?: "",
                                stats = platformMap["stats"] ?: "N/A",
                                statsLabel = platformMap["statsLabel"] ?: "",
                                icon = Icons.Default.Star
                            )
                        )
                    }
                } else {
                    userNotFound = true
                }
            } catch (e: Exception) {
                userNotFound = true
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
            userNotFound = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            userNotFound -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Please log in to view profile",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Profile Header
                    ProfileHeader(
                        userName = userName,
                        email = email,
                        displayName = displayName
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Personal Info Card
//                    PersonalInfoCard(
//                        email = email,
//                        phoneNumber = phoneNumber,
//                        displayName = displayName
//                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Coding Platforms Section
                    CodingPlatformsSection(
                        platforms = platforms,
                        onPlatformAdded = { platformName, username ->
                            scope.launch {
                                // Add to local list
                                platforms.add(
                                    CodingPlatform(
                                        platformName,
                                        username,
                                        "N/A",
                                        "Loading...",
                                        Icons.Default.Star
                                    )
                                )

                                // Save to Firestore
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                if (currentUser != null) {
                                    val db = FirebaseFirestore.getInstance()
                                    val platformsList = platforms.map {
                                        mapOf(
                                            "name" to it.name,
                                            "username" to it.username,
                                            "stats" to it.stats,
                                            "statsLabel" to it.statsLabel
                                        )
                                    }
                                    db.collection("users")
                                        .document(currentUser.uid)
                                        .update("codingPlatforms", platformsList)
                                }
                            }
                        },
                        onPlatformRemoved = { platform ->
                            scope.launch {
                                platforms.remove(platform)

                                // Update Firestore
                                val currentUser = FirebaseAuth.getInstance().currentUser
                                if (currentUser != null) {
                                    val db = FirebaseFirestore.getInstance()
                                    val platformsList = platforms.map {
                                        mapOf(
                                            "name" to it.name,
                                            "username" to it.username,
                                            "stats" to it.stats,
                                            "statsLabel" to it.statsLabel
                                        )
                                    }
                                    db.collection("users")
                                        .document(currentUser.uid)
                                        .update("codingPlatforms", platformsList)
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Card
//                    StatsCard()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Settings Section
                    SettingsSection()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Logout Button
                    LogoutButton(
                        onLogout = {
                            scope.launch {
                                FirebaseAuth.getInstance().signOut()
                                onNavigateToLogin()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    userName: String,
    email: String,
    displayName: String
) {
    val firstLetter = if (userName.isNotEmpty()) userName.first().uppercase() else "U"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF212139),  // top color
                            Color(0xFF7B3EFF)   // bottom color
                        )
                    )),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstLetter,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color(0xFFE0E0E0),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username
            Text(
                text = displayName.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }.ifEmpty { "User" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )


            // Display Name
//            if (displayName.isNotEmpty() && displayName != userName) {
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = displayName,
//                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
//                )
//            }

            // Email
            if (email.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun PersonalInfoCard(
    email: String,
    phoneNumber: String,
    displayName: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoRow("Display Name", displayName.ifEmpty { "Not set" })
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            InfoRow("Email", email.ifEmpty { "Not set" })
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            InfoRow("Phone", phoneNumber.ifEmpty { "Not set" })
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CodingPlatformsSection(
    platforms: List<CodingPlatform>,
    onPlatformAdded: (String, String) -> Unit,
    onPlatformRemoved: (CodingPlatform) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Coding Profiles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            FilledIconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Platform",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (platforms.isEmpty()) {
            EmptyPlatformsCard(onAddClick = { showAddDialog = true })
        } else {
            platforms.forEach { platform ->
                PlatformCard(
                    platform = platform,
                    onRemove = { onPlatformRemoved(platform) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showAddDialog) {
        AddPlatformDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { platformName, username ->
                onPlatformAdded(platformName, username)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PlatformCard(
    platform: CodingPlatform,
    onRemove: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Platform Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (platform.name) {
                            "GitHub" -> Color(0xFF6e5494)
                            "LeetCode" -> Color(0xFFffa116)
                            "Codeforces" -> Color(0xFF1f8acb)
                            "CodeChef" -> Color(0xFF5B4638)
                            "HackerRank" -> Color(0xFF2EC866)
                            "AtCoder" -> Color(0xFF000000)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = platform.name.first().toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = platform.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${platform.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${platform.stats} ${platform.statsLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View Profile") },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.ExitToApp, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Refresh") },
                        onClick = { showMenu = false },
                        leadingIcon = { Icon(Icons.Default.Refresh, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onRemove()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPlatformsCard(onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Profiles Added",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Add your coding platform profiles to showcase your skills",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Platform")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlatformDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var selectedPlatform by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val platforms = listOf("GitHub", "LeetCode", "Codeforces", "CodeChef", "HackerRank", "AtCoder")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Coding Platform", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedPlatform,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Platform") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(expanded, { expanded = false }) {
                        platforms.forEach { platform ->
                            DropdownMenuItem(
                                text = { Text(platform) },
                                onClick = {
                                    selectedPlatform = platform
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("Enter your username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(selectedPlatform, username) },
                enabled = selectedPlatform.isNotEmpty() && username.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

//@Composable
//private fun StatsCard() {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//    ) {
//        Column(modifier = Modifier.padding(20.dp)) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    imageVector = Icons.Default.Star,
//                    contentDescription = null,
//                    tint = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.size(24.dp)
//                )
//                Spacer(modifier = Modifier.width(12.dp))
//                Text(
//                    text = "Account Statistics",
//                    style = MaterialTheme.typography.titleLarge,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            InfoRow("Member Since", "2024")
//            Divider(modifier = Modifier.padding(vertical = 12.dp))
//            InfoRow("Contests Participated", "0")
//            Divider(modifier = Modifier.padding(vertical = 12.dp))
//            InfoRow("Profile Views", "0")
//        }
//    }
//}

@Composable
private fun SettingsSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                SettingsItem(Icons.Default.Notifications, "Notifications", "Manage notification preferences")
                Divider()
                SettingsItem(Icons.Default.Lock, "Privacy & Security", "Control your privacy settings")
                Divider()
                SettingsItem(Icons.Default.Info, "Help & Support", "Get help and contact support")
                Divider()
                SettingsItem(Icons.Default.Info, "About", "App version and information")
            }
        }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    ) {
        Icon(Icons.Default.ExitToApp, null, Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Logout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
    }
}

// Data class
data class CodingPlatform(
    val name: String,
    val username: String,
    val stats: String,
    val statsLabel: String,
    val icon: ImageVector
)