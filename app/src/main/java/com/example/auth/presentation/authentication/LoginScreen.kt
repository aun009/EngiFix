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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)

        ) {
            Spacer(Modifier.Companion.height(26.dp))

            // Back arrow icon
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.Companion.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.Companion.height(24.dp))

            // Title
            Text(
                text = "Welcome back!",
                fontWeight = FontWeight.Companion.Bold,
                fontSize = 29.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.Companion.padding(start = 8.dp)
            )

            Spacer(Modifier.Companion.height(8.dp))

            // Subtitle
            Text(
                text = "We're so excited to see you again!",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.Companion.padding(start = 8.dp)
            )

            Spacer(Modifier.Companion.height(24.dp))

            // Email or Phone Number field
            Text(
                text = "Email or Phone Number",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                },
                colors = textFieldColors1(),
                singleLine = true,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                },
                visualTransformation = if (passwordVisible) VisualTransformation.Companion.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Close else Icons.Default.Done,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = textFieldColors1(),
                singleLine = true,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Companion.Password)
            )


            Spacer(Modifier.Companion.height(16.dp))

            // Forgot password
            Text(
                text = "Forgot your password?",
                color = MaterialTheme.colorScheme.primary,
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

            Spacer(Modifier.Companion.height(24.dp))

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
                containerColor = MaterialTheme.colorScheme.primary,
                textFontWeight = FontWeight.Companion.Medium
            )


            Text(
                text = "Create a new account",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 24.dp)
                    .clickable { navController.navigate("register_screen") }
            )
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    TextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        singleLine = true,
                        placeholder = {
                            Text("Enter your email", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
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
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    disabledTextColor = Color.Companion.Gray,
    errorTextColor = MaterialTheme.colorScheme.error,
    focusedContainerColor = Color.Companion.Transparent,
    unfocusedContainerColor = Color.Companion.Transparent,
    disabledContainerColor = Color.Companion.Transparent,
    errorContainerColor = Color.Companion.Transparent,
    cursorColor = MaterialTheme.colorScheme.primary,
    errorCursorColor = MaterialTheme.colorScheme.error,
    focusedIndicatorColor = Color.Companion.Transparent,
    unfocusedIndicatorColor = Color.Companion.Transparent,
    disabledIndicatorColor = Color.Companion.Transparent,
    errorIndicatorColor = Color.Companion.Transparent,
    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLeadingIconColor = Color.Companion.Gray,
    errorLeadingIconColor = MaterialTheme.colorScheme.error,
    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledTrailingIconColor = Color.Companion.Gray,
    errorTrailingIconColor = MaterialTheme.colorScheme.error,
    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLabelColor = Color.Companion.Gray,
    errorLabelColor = MaterialTheme.colorScheme.error,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledPlaceholderColor = Color.Companion.Gray,
    errorPlaceholderColor = MaterialTheme.colorScheme.error,
    focusedSupportingTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledSupportingTextColor = Color.Companion.Gray,
    errorSupportingTextColor = MaterialTheme.colorScheme.error,
    focusedPrefixColor = MaterialTheme.colorScheme.onSurface,
    unfocusedPrefixColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledPrefixColor = Color.Companion.Gray,
    errorPrefixColor = MaterialTheme.colorScheme.error,
    focusedSuffixColor = MaterialTheme.colorScheme.onSurface,
    unfocusedSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledSuffixColor = Color.Companion.Gray,
    errorSuffixColor = MaterialTheme.colorScheme.error,
)
