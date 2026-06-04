package com.example.auth.presentation.features.resume

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.auth.presentation.components.EngiFixBackground
import com.example.auth.presentation.components.IconTile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumeRoastScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember(context) { GeminiResumeRepository(context.applicationContext) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var resumeText by remember { mutableStateOf("") }
    var selectedExperience by remember { mutableStateOf("Fresher (0-1 yr)") }
    var targetRole by remember { mutableStateOf("Android / software engineering intern") }
    var isBusy by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var messages by remember { mutableStateOf(defaultResumeMessages()) }
    var isChatInputFocused by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val collapseSetupPanel = isKeyboardVisible && isChatInputFocused

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        runCatching {
            withContext(Dispatchers.IO) {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .await()
            }
        }.onSuccess { doc ->
            resumeText = doc.getString("resumeText").orEmpty()
            val savedMessages = parseStoredResumeMessages(doc.get("resumeChatMessages"))
            if (savedMessages.isNotEmpty()) {
                messages = savedMessages
            } else if (resumeText.isNotBlank()) {
                messages = listOf(
                    ResumeChatMessage(
                        fromUser = false,
                        text = "Your saved resume is attached. Ask me about roles, skills, projects, applications, or resume bullets."
                    )
                )
            }
        }
    }

    fun saveConversation(nextResumeText: String, nextMessages: List<ResumeChatMessage>) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        coroutineScope.launch {
            persistResumeConversation(
                uid = uid,
                resumeText = nextResumeText,
                messages = nextMessages
            )
        }
    }

    fun askAi(question: String) {
        if (question.isBlank() || isBusy) return
        val nextMessages = messages + ResumeChatMessage(fromUser = true, text = question.trim())
        messages = nextMessages
        input = ""
        isBusy = true
        errorText = null
        saveConversation(resumeText, nextMessages)
        coroutineScope.launch {
            repository
                .chatWithResume(resumeText, nextMessages, selectedExperience, targetRole)
                .onSuccess { answer ->
                    val updatedMessages = nextMessages + ResumeChatMessage(fromUser = false, text = answer)
                    messages = updatedMessages
                    saveConversation(resumeText, updatedMessages)
                }
                .onFailure { error ->
                    errorText = error.message ?: "Could not reach AI right now."
                    messages = nextMessages
                    saveConversation(resumeText, nextMessages)
                }
            isBusy = false
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        selectedUri = uri
        isBusy = true
        errorText = null
        coroutineScope.launch {
            repository.extractResumeText(uri)
                .onSuccess { text ->
                    resumeText = text
                    val introQuestion = "Analyze my resume for $targetRole. Tell me what to learn, what to fix, and where I should apply."
                    val starter = listOf(ResumeChatMessage(fromUser = true, text = introQuestion))
                    messages = starter
                    saveConversation(text, starter)
                    repository.chatWithResume(text, starter, selectedExperience, targetRole)
                        .onSuccess { answer ->
                            val updatedMessages = starter + ResumeChatMessage(fromUser = false, text = answer)
                            messages = updatedMessages
                            saveConversation(text, updatedMessages)
                        }
                        .onFailure { error ->
                            errorText = error.message ?: "Could not analyze this resume."
                            val fallbackMessages = listOf(
                                ResumeChatMessage(
                                    fromUser = false,
                                    text = "I read the PDF, but AI analysis failed. You can still ask questions and I will use fallback guidance."
                                )
                            )
                            messages = fallbackMessages
                            saveConversation(text, fallbackMessages)
                        }
                }
                .onFailure { error ->
                    errorText = error.message ?: "Could not read this PDF."
                }
            isBusy = false
        }
    }

    LaunchedEffect(messages.size, isBusy) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    val experienceLevels = listOf("Fresher (0-1 yr)", "Mid (1-3 yrs)", "Experienced (3+ yrs)")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Career AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedUri = null
                            resumeText = ""
                            input = ""
                            errorText = null
                            messages = defaultResumeMessages()
                            saveConversation("", messages)
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        EngiFixBackground(modifier = Modifier.padding(paddingValues)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                Column(Modifier.weight(1f)) {
                    if (!collapseSetupPanel) {
                        ResumeChatSetupPanel(
                            hasResume = resumeText.isNotBlank(),
                            selectedExperience = selectedExperience,
                            targetRole = targetRole,
                            experienceLevels = experienceLevels,
                            isBusy = isBusy,
                            onExperienceChange = { selectedExperience = it },
                            onTargetRoleChange = { targetRole = it },
                            onUploadClick = { filePickerLauncher.launch("application/pdf") }
                        )
                    }

                    if (errorText != null && !collapseSetupPanel) {
                        Text(
                            text = errorText.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages) { message ->
                            ResumeMessageBubble(message)
                        }
                        if (isBusy) {
                            item {
                                ResumeThinkingBubble()
                            }
                        }
                    }
                }

                ChatInputBar(
                    enabled = !isBusy,
                    input = input,
                    isBusy = isBusy,
                    onInputChange = { input = it },
                    onAttach = { filePickerLauncher.launch("application/pdf") },
                    onSend = { askAi(input) },
                    onFocusChange = { isChatInputFocused = it }
                )
            }
        }
    }
}

@Composable
private fun ResumeChatSetupPanel(
    hasResume: Boolean,
    selectedExperience: String,
    targetRole: String,
    experienceLevels: List<String>,
    isBusy: Boolean,
    onExperienceChange: (String) -> Unit,
    onTargetRoleChange: (String) -> Unit,
    onUploadClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(Icons.Default.AutoAwesome, MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Chat with resume",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (hasResume) "Resume attached. Ask anything."
                        else "Ask now, or attach a PDF.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                FilledTonalButton(
                    onClick = onUploadClick,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(
                        if (hasResume) Icons.Default.Description else Icons.Default.UploadFile,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (hasResume) "Replace" else "Upload", maxLines = 1)
                }
            }

            OutlinedTextField(
                value = targetRole,
                onValueChange = onTargetRoleChange,
                label = { Text("Target role") },
                leadingIcon = { Icon(Icons.Default.TrackChanges, contentDescription = null) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                experienceLevels.forEach { level ->
                    val isSelected = selectedExperience == level
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clickable(enabled = !isBusy) { onExperienceChange(level) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 5.dp)) {
                            Text(
                                text = level,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumeMessageBubble(message: ResumeChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(if (message.fromUser) 0.82f else 0.94f),
            shape = RoundedCornerShape(8.dp),
            color = if (message.fromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            border = if (message.fromUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Text(
                text = message.text,
                color = if (message.fromUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
private fun ResumeThinkingBubble() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Thinking...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    enabled: Boolean,
    input: String,
    isBusy: Boolean,
    onInputChange: (String) -> Unit,
    onAttach: () -> Unit,
    onSend: () -> Unit,
    onFocusChange: (Boolean) -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onAttach, enabled = !isBusy, modifier = Modifier.size(42.dp)) {
                Icon(Icons.Default.AttachFile, contentDescription = "Attach resume")
            }
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                placeholder = { Text(if (enabled) "Ask AI..." else "Thinking...") },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 112.dp)
                    .onFocusChanged { onFocusChange(it.isFocused) },
                minLines = 1,
                maxLines = 3,
                enabled = enabled,
                shape = RoundedCornerShape(8.dp)
            )
            FilledIconButton(
                onClick = onSend,
                enabled = enabled && input.isNotBlank(),
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

private fun defaultResumeMessages(): List<ResumeChatMessage> =
    listOf(
        ResumeChatMessage(
            fromUser = false,
            text = "Ask me anything about roles, skills, projects, applications, or resume bullets. Upload a resume whenever you want more specific guidance."
        )
    )

private suspend fun persistResumeConversation(
    uid: String,
    resumeText: String,
    messages: List<ResumeChatMessage>
) {
    runCatching {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val payload = mapOf(
                "resumeText" to resumeText.take(MAX_STORED_RESUME_CHARS),
                "resumeUpdatedAt" to if (resumeText.isBlank()) 0L else now,
                "resumeChatMessages" to messages.takeLast(MAX_STORED_MESSAGES).map {
                    mapOf(
                        "fromUser" to it.fromUser,
                        "text" to it.text.take(MAX_STORED_MESSAGE_CHARS),
                        "createdAt" to now
                    )
                }
            )
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update(payload)
                .await()
        }
    }
}

private fun parseStoredResumeMessages(value: Any?): List<ResumeChatMessage> =
    (value as? List<*>)?.mapNotNull { item ->
        val map = item as? Map<*, *> ?: return@mapNotNull null
        val text = map["text"] as? String ?: return@mapNotNull null
        ResumeChatMessage(
            fromUser = map["fromUser"] as? Boolean ?: false,
            text = text
        )
    }.orEmpty()
        .filter { it.text.isNotBlank() }
        .takeLast(MAX_STORED_MESSAGES)

private const val MAX_STORED_RESUME_CHARS = 18_000
private const val MAX_STORED_MESSAGES = 40
private const val MAX_STORED_MESSAGE_CHARS = 4_000
