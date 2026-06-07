package com.example.auth.presentation.authentication

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavController
import com.example.auth.presentation.components.ButtonEx

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
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
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.Companion.height(40.dp))

            // Title
            Text(
                text = "Welcome back!",
                fontWeight = FontWeight.Companion.Bold,
                fontSize = 32.sp,
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

            Spacer(Modifier.Companion.height(32.dp))

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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 16.sp
                    )
                },
                colors = textFieldColors1(),
                singleLine = true,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 16.sp
                    )
                },
                visualTransformation = if (passwordVisible) VisualTransformation.Companion.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
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
                        MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
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

            Spacer(Modifier.Companion.height(32.dp))

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
                textColor = MaterialTheme.colorScheme.onPrimary,
                textFontWeight = FontWeight.Companion.Medium
            )

            Spacer(Modifier.Companion.height(24.dp))

            // Or divider
            Text(
                text = "Or",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontSize = 14.sp,
                textAlign = TextAlign.Companion.Center,
                modifier = Modifier.Companion.fillMaxWidth()
            )

            Spacer(Modifier.Companion.height(24.dp))

            Spacer(Modifier.Companion.weight(1f))
        }
    }

    if (showResetDialog) {
        ModalBottomSheet(
            onDismissRequest = { showResetDialog = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Reset password", color = MaterialTheme.colorScheme.onSurface, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Enter your account email and we'll send you a reset link.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextField(
                    value = resetEmail,
                    onValueChange = { resetEmail = it },
                    singleLine = true,
                    placeholder = {
                        Text("Enter your email", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                    },
                    colors = textFieldColors1(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
                if (resetError != null) {
                    Text(
                        text = resetError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showResetDialog = false },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Text("Cancel", fontSize = 16.sp)
                    }
                    Button(
                        onClick = {
                            val email = resetEmail.trim()
                            if (email.isBlank() || !email.contains("@")) {
                                resetError = "Enter a valid email."
                                return@Button
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
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Send link", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

// Reuse the same textFieldColors function from your RegisterScreen
@Composable
fun textFieldColors1() = textFieldColors()
