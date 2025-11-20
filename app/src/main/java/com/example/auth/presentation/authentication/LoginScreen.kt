package com.example.auth.presentation.authentication

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.auth.presentation.components.ButtonEx

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController,
                authViewModel: AuthViewModel
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var resetError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Box(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(Color(0xFF18181C))
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.Companion.height(40.dp))

            // Back arrow icon
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.Companion.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Companion.White
                )
            }

            Spacer(Modifier.Companion.height(40.dp))

            // Title
            Text(
                text = "Welcome back!",
                fontWeight = FontWeight.Companion.Bold,
                fontSize = 32.sp,
                color = Color.Companion.White,
                modifier = Modifier.Companion.padding(start = 8.dp)
            )

            Spacer(Modifier.Companion.height(8.dp))

            // Subtitle
            Text(
                text = "We're so excited to see you again!",
                fontSize = 16.sp,
                color = Color(0xFFB3B3B3),
                modifier = Modifier.Companion.padding(start = 8.dp)
            )

            Spacer(Modifier.Companion.height(32.dp))

            // Email or Phone Number field
            Text(
                text = "Email or Phone Number",
                color = Color(0xFFD7D7D7),
                fontSize = 14.sp,
                fontWeight = FontWeight.Companion.Medium,
                modifier = Modifier.Companion.padding(start = 4.dp, bottom = 8.dp)
            )

            TextField(
                value = authViewModel.email.ifEmpty { authViewModel.phoneNumber }, // 👈 pick email or phone
                onValueChange = { input ->
                    if (input.contains("@")) {
                        authViewModel.email = input
                    } else {
                        authViewModel.phoneNumber = input
                    }
                },
                placeholder = {
                    Text(
                        "Enter your email or phone",
                        color = Color(0x88FFFFFF),
                        fontSize = 16.sp
                    )
                },
                colors = textFieldColors1(),
                singleLine = true,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .background(Color(0xFF22232D), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Email)
            )

            Spacer(Modifier.Companion.height(16.dp))

            TextField(
                value = authViewModel.passwordSignIn, // 👈 comes from ViewModel
                onValueChange = { authViewModel.passwordSignIn = it },
                placeholder = {
                    Text(
                        "Enter your password",
                        color = Color(0x88FFFFFF),
                        fontSize = 16.sp
                    )
                },
                visualTransformation = if (passwordVisible) VisualTransformation.Companion.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Close else Icons.Default.Done,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color(0xFFB3B3B3)
                        )
                    }
                },
                colors = textFieldColors1(),
                singleLine = true,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .background(
                        Color(0xFF22232D),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Password)
            )


            Spacer(Modifier.Companion.height(16.dp))

            // Forgot password
            Text(
                text = "Forgot your password?",
                color = Color(0xFF5865F2),
                fontSize = 14.sp,
                modifier = Modifier.Companion
                    .padding(start = 4.dp)
                    .clickable {
                        // Open dialog and pre-fill with current email if available
                        resetEmail = authViewModel.email
                        resetError = null
                        showResetDialog = true
                    }
            )

            Spacer(Modifier.Companion.height(32.dp))

            // Login Button
//            Button(
//                onClick = { /* TODO: Handle Login */ },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp),
//                shape = RoundedCornerShape(25.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5865F2))
//            ) {
//                Text(
//                    "Log In",
//                    color = Color.White,
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }

            ButtonEx(
                text = "Log In",
                onClick = {
                    authViewModel.loginUser { ok, err ->
                        if (ok) {
                            navController.navigate("home") {
                                popUpTo("login_screen") { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context.applicationContext, err, Toast.LENGTH_LONG).show()
                            Log.e("LoginScreen", "Login error: $err")
                        }
                    }

                },
                containerColor = Color(0xFF5865F2),
                textFontWeight = FontWeight.Companion.Medium
            )


            Spacer(Modifier.Companion.height(24.dp))

            // Or divider
            Text(
                text = "Or",
                color = Color(0xFFB3B3B3),
                fontSize = 14.sp,
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.fillMaxWidth()
            )

            Spacer(Modifier.Companion.height(24.dp))

            // Google Sign In Button


            Spacer(Modifier.Companion.weight(1f))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset password") },
            text = {
                Column {
                    Text(
                        text = "Enter your account email and we'll send you a reset link.",
                        fontSize = 14.sp,
                        color = Color(0xFFB3B3B3)
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        singleLine = true,
                        placeholder = {
                            Text("Enter your email", color = Color(0x88FFFFFF))
                        },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    if (resetError != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = resetError ?: "",
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val email = resetEmail.trim()
                        if (email.isBlank() || !email.contains("@")) {
                            resetError = "Enter a valid email."
                            return@TextButton
                        }

                        authViewModel.sendPasswordResetEmail(email) { ok, err ->
                            if (ok) {
                                Toast.makeText(
                                    context,
                                    "Password reset email sent. Check your inbox.",
                                    Toast.LENGTH_LONG
                                ).show()
                                showResetDialog = false
                            } else {
                                resetError = err ?: "Failed to send reset email."
                            }
                        }
                    }
                ) {
                    Text("Send link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Reuse the same textFieldColors function from your RegisterScreen
@Composable
fun textFieldColors1() = TextFieldDefaults.colors(
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