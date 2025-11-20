package com.example.auth.presentation.authentication

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.NavController
import com.example.auth.presentation.authentication.AuthViewModel
import com.example.auth.presentation.components.ButtonEx
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

//    val googleSignInOptions = remember {
//        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken("312610679080-npefae8u3eiko4sae9pi24j05hcl6j8b.apps.googleusercontent.com")
//            .build()
//    }
//
//    val googleSignInClient = remember {
//        GoogleSignIn.getClient(context, googleSignInOptions)
//    }
//
//    val launcher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//
//    ) {
//        result ->
//        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//
//        try {
//            val account = task.result
//            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//            Firebase.auth.signInWithCredential(credential)
//                .addOnCompleteListener { task -> {
//                    if (task.isSuccessful) {
//                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
//                        navController.navigate("home") {
//                            popUpTo("register_screen") {
//                                inclusive = true
//                            }
//                        }
//                    }
//                    else {
//                        Toast.makeText(context, "Login Failed", Toast.LENGTH_SHORT).show()
//                    }
//                } }
//
//        }
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF18181C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(40.dp))

            IconButton(
                onClick = { navController.navigate("first_screen") },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Enter contact details",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // EMAIL
            Text(
                text = "Email",
                color = Color(0xFFD7D7D7),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            TextField(
                value = authViewModel.email,
                onValueChange = { authViewModel.email = it },
                placeholder = { Text("you@example.com", color = Color(0x88FFFFFF)) },
                colors = textFieldColors(),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF22232D),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(start = 8.dp, end = 8.dp)
            )
            Text(
                text = emailError ?: "",
                color = Color(0xFFEF5350),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
            )

//            Spacer(Modifier.height(5.dp))

            // PHONE (optional)
            Text(
                text = "Phone (optional)",
                color = Color(0xFFD7D7D7),
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF22232D), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "IN +91",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(Modifier.width(10.dp))
                TextField(
                    value = authViewModel.phoneNumber,
                    onValueChange = { authViewModel.phoneNumber = it },
                    placeholder = { Text("10-digit phone number", color = Color(0x88FFFFFF)) },
                    colors = textFieldColors(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent)
                )
            }

            Text(
                text = phoneError ?: "",
                color = Color(0xFFEF5350),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
            )

            Spacer(Modifier.height(17.dp))

            ButtonEx(
                text = "Next",
                onClick = {
                    // validate email (required)
                    val email = authViewModel.email.trim()
                    val phone = authViewModel.phoneNumber.trim()

                    var ok = true

                    if (email.isBlank()) {
                        emailError = "Enter your email."
                        ok = false
                    } else if (!email.contains("@") || email.length < 5) {
                        emailError = "Enter a valid email."
                        ok = false
                    } else {
                        emailError = null
                    }

                    // phone is optional: if user entered, validate digits & length
                    if (phone.isNotBlank()) {
                        if (phone.length != 10 || phone.any { !it.isDigit() }) {
                            phoneError = "Enter a valid 10-digit number."
                            ok = false
                        } else {
                            phoneError = null
                        }
                    } else {
                        phoneError = null
                    }

                    if (ok) {
                        // Keep phone in ViewModel so later when registering you can save it to Firestore
                        navController.navigate("ask_name_screen")
                    }
                },
                containerColor = Color(0xFF5865F2),
                textFontWeight = FontWeight.Medium
            )

//            Spacer(Modifier.height(32.dp))
//            Text(
//                text = "Or",
//                color = Color(0xFFB3B3B3),
//                fontSize = 14.sp,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.fillMaxWidth()
//            )
//            Spacer(Modifier.height(24.dp))
//            GoogleButton()
        }
    }
}


@Composable
fun GoogleButton() {
    OutlinedButton(
        onClick = { /* TODO: Handle Google Sign In */ },
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(50.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Companion.Transparent,
            contentColor = Color.Companion.White
        ),
        border = BorderStroke(1.dp, Color(0xFF22232D))
    ) {
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Google Logo (using a simple colored circle as placeholder)
            Box(
                modifier = Modifier.Companion
                    .size(20.dp)
                    .background(
                        Color.Companion.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text(
                    "G",
                    color = Color(0xFF4285F4),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Companion.Bold
                )
            }

            Spacer(Modifier.Companion.width(12.dp))

            Text(
                "Continue with Google",
                color = Color.Companion.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Companion.Medium
            )
        }
    }
}

@Composable
fun TabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabTitles = listOf("Phone", "Email")

    // Calculate the indicator offset - using fraction of container width
    val indicatorOffset by animateDpAsState(
        targetValue = if (selectedTab == 0) 4.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "indicator_animation"
    )

    Box(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .height(48.dp)
            .background(
                Color(0xFF22232D),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
            )
            .padding(4.dp)
    ) {
        // Tab content - this helps us measure the actual width
        Row(
            modifier = Modifier.Companion
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            tabTitles.forEachIndexed { i, title ->
                Box(
                    modifier = Modifier.Companion
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(i) },
                    contentAlignment = Alignment.Companion.Center
                ) {
                    // Animated indicator - positioned within each tab
                    if (selectedTab == i) {
                        Box(
                            modifier = Modifier.Companion
                                .fillMaxSize()
                                .background(
                                    Color(0xFF4343D7),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                                )
                                .zIndex(0f)
                        )
                    }

                    Text(
                        text = title,
                        fontWeight = FontWeight.Companion.Medium,
                        fontSize = 16.sp,
                        color = if (selectedTab == i) Color.Companion.White else Color(0xFFB3B3B3),
                        modifier = Modifier.Companion.zIndex(1f)
                    )
                }
            }
        }
    }
}

// Cleaner TextField colors for dark theme: for TextField
@Composable
fun textFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = Color.Companion.White,
    unfocusedTextColor = Color.Companion.White,
    disabledTextColor = Color.Companion.Gray,
    errorTextColor = Color.Companion.Red,
    focusedContainerColor = Color.Companion.Transparent,
    unfocusedContainerColor = Color.Companion.Transparent,
    disabledContainerColor = Color.Companion.Transparent,
    errorContainerColor = Color.Companion.Transparent,
    cursorColor = Color.Companion.White,
    errorCursorColor = Color.Companion.Red,
    focusedIndicatorColor = Color.Companion.Transparent,
    unfocusedIndicatorColor = Color.Companion.Transparent,
    disabledIndicatorColor = Color.Companion.Transparent,
    errorIndicatorColor = Color.Companion.Transparent,
    focusedLeadingIconColor = Color.Companion.White,
    unfocusedLeadingIconColor = Color.Companion.White.copy(alpha = 0.7f),
    disabledLeadingIconColor = Color.Companion.Gray,
    errorLeadingIconColor = Color.Companion.Red,
    focusedTrailingIconColor = Color.Companion.White,
    unfocusedTrailingIconColor = Color.Companion.White.copy(alpha = 0.7f),
    disabledTrailingIconColor = Color.Companion.Gray,
    errorTrailingIconColor = Color.Companion.Red,
    focusedLabelColor = Color.Companion.White,
    unfocusedLabelColor = Color.Companion.White.copy(alpha = 0.7f),
    disabledLabelColor = Color.Companion.Gray,
    errorLabelColor = Color.Companion.Red,
    focusedPlaceholderColor = Color.Companion.White.copy(alpha = 0.7f),
    unfocusedPlaceholderColor = Color.Companion.White.copy(alpha = 0.7f),
    disabledPlaceholderColor = Color.Companion.Gray,
    errorPlaceholderColor = Color.Companion.Red,
    focusedSupportingTextColor = Color.Companion.White,
    unfocusedSupportingTextColor = Color.Companion.White.copy(alpha = 0.7f),
    disabledSupportingTextColor = Color.Companion.Gray,
    errorSupportingTextColor = Color.Companion.Red,
    focusedPrefixColor = Color.Companion.White,
    unfocusedPrefixColor = Color.Companion.White.copy(alpha = 0.7f),
    disabledPrefixColor = Color.Companion.Gray,
    errorPrefixColor = Color.Companion.Red,
    focusedSuffixColor = Color.Companion.White,
    unfocusedSuffixColor = Color.Companion.White.copy(alpha = 0.7f),
    disabledSuffixColor = Color.Companion.Gray,
    errorSuffixColor = Color.Companion.Red,
)