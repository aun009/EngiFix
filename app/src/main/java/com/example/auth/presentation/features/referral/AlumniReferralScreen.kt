package com.example.auth.presentation.features.referral

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.presentation.components.ButtonEx
import com.example.auth.presentation.components.EngiFixBackground
import com.example.auth.presentation.components.IconTile
import java.net.URLEncoder
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumniReferralScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var collegeName by remember { mutableStateOf("") }
    var cityName by remember { mutableStateOf("") }          // NEW
    var companyName by remember { mutableStateOf("") }
    var switchJustPlaced by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alumni Referral Finder", fontWeight = FontWeight.Bold) },
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
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile(icon = Icons.Default.Search, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "LinkedIn Alumni Search",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Find verified alumni working at your target company using exact-phrase LinkedIn search.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // College Name
                OutlinedTextField(
                    value = collegeName,
                    onValueChange = { collegeName = it },
                    label = { Text("College / University Name") },
                    placeholder = { Text("e.g. VJTI, IIT Bombay") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // City (NEW) — helps disambiguate common college names
                OutlinedTextField(
                    value = cityName,
                    onValueChange = { cityName = it },
                    label = { Text("College City (Optional)") },
                    placeholder = { Text("e.g. Mumbai, Delhi, Pune") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Company Name
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("Target Company") },
                    placeholder = { Text("e.g. Google, Microsoft") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Just Placed toggle
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recent Hires Only",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Targets profiles with 'joined', 'started', or 'new role' keywords.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                            checked = switchJustPlaced,
                            onCheckedChange = { switchJustPlaced = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Button
                ButtonEx(
                    text = "Search Alumni on LinkedIn",
                    onClick = {
                        if (collegeName.isBlank() || companyName.isBlank()) {
                            Toast.makeText(context, "Please enter college and company", Toast.LENGTH_SHORT).show()
                        } else {
                            launchAlumniSearch(
                                context = context,
                                collegeName = collegeName.trim(),
                                cityName = cityName.trim(),
                                companyName = companyName.trim(),
                                switchJustPlaced = switchJustPlaced
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    textColor = Color.White,
                    textFontWeight = FontWeight.Bold
                )

                // Disclaimer
                Text(
                    text = "Tip: If your college name is common (e.g. 'Government Engineering College'), filling the City field helps LinkedIn find the right alumni.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

fun launchAlumniSearch(
    context: Context,
    collegeName: String,
    cityName: String,
    companyName: String,
    switchJustPlaced: Boolean
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()

    // Build the LinkedIn Boolean query properly
    val queryBuilder = StringBuilder()

    // 1. Exact college name (quoted)
    queryBuilder.append("\"$collegeName\"")

    // 2. City (optional) — quoted to disambiguate colleges with common names
    if (cityName.isNotBlank()) {
        queryBuilder.append(" \"$cityName\"")
    }

    // 3. Exact company name (quoted)
    queryBuilder.append(" \"$companyName\"")

    // 4. Just Placed filter — uses terms people actually write in LinkedIn headlines
    if (switchJustPlaced) {
        // LinkedIn supports OR operator in keyword search
        queryBuilder.append(" (joined OR started OR \"new role\" OR \"excited to join\")")
        queryBuilder.append(" $currentYear")
    }

    val linkedinQuery = queryBuilder.toString()
    val encodedQuery = URLEncoder.encode(linkedinQuery, "UTF-8")

    // LinkedIn People Search URL format (verified structure)
    val linkedinUrl = "https://www.linkedin.com/search/results/people/?keywords=$encodedQuery"

    // Attempt 1: Open directly in LinkedIn Android app
    val linkedinIntent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl)).apply {
        setPackage("com.linkedin.android")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val isLinkedInInstalled = linkedinIntent.resolveActivity(context.packageManager) != null

    if (isLinkedInInstalled) {
        context.startActivity(linkedinIntent)
    } else {
        // Attempt 2: Fallback to browser with the SAME LinkedIn URL
        // DO NOT use Google "site:" search — it is broken for LinkedIn profiles.
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
    }
}
