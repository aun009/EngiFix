package com.example.auth.presentation.authentication

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.auth.presentation.authentication.AuthViewModel
import com.example.auth.presentation.components.ButtonEx

@Composable
fun UserNameAndPassScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    var passwordVisible by remember { mutableStateOf(false) }
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

            // Back arrow icon (updated for AutoMirrored)
            IconButton(
                onClick = { navController.navigate("ask_name_screen") },
                modifier = Modifier.Companion.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.Companion.height(16.dp))

            // Title
            Text(
                text = "Next, Create an account",
                fontWeight = FontWeight.Companion.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                textAlign = TextAlign.Companion.Center
            )

            Spacer(Modifier.Companion.height(18.dp))

            // Tab Row with animation

            Spacer(Modifier.Companion.height(12.dp))


            // Username input
            Text(
                text = "Username",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.Companion.padding(start = 4.dp, bottom = 4.dp)
            )

            TextField(
                value = authViewModel.userName,
                onValueChange = { value ->
                    authViewModel.userName = value
                        .lowercase()
                        .filter { it.isLetterOrDigit() || it == '_' || it == '.' }
                        .take(20)
                },
                placeholder = { Text("choose_a_handle", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = textFieldColors(),
                singleLine = true,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                    .padding(start = 8.dp, end = 8.dp)
            )

            Spacer(Modifier.Companion.height(16.dp))

            TextField(
                value = authViewModel.passwordSignUp, // 👈 comes from ViewModel
                onValueChange = { authViewModel.passwordSignUp = it },
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

            Spacer(Modifier.Companion.height(24.dp))

            // Next Button
//            Button(
//                onClick = { /* TODO: Handle Next */ },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp),
//                shape = RoundedCornerShape(25.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9EB0))
//            ) {
//                Text("Next", color = Color.White, fontSize = 16.sp)
//            }

            ButtonEx(
                text = "Sign Up",
                onClick = {
                    Log.d("AuthDebug", "SignUp button clicked")

                    Log.d("AuthDebug", "Username entered: ${authViewModel.userName.isNotBlank()}")
                    Log.d("AuthDebug", "Password length: ${authViewModel.passwordSignUp.length}")
                    Log.d("AuthDebug", "Email from previous screen: ${authViewModel.email}")
                    Log.d("AuthDebug", "Phone from previous screen: ${authViewModel.phoneNumber}")

                    // Check empty fields before calling Firebase
                    if (!authViewModel.userName.matches(Regex("^[a-z0-9_.]{3,20}$"))) {
                        Log.e("AuthDebug", "Username is invalid")
                        Toast.makeText(context, "Username must be 3-20 letters, numbers, _ or .", Toast.LENGTH_SHORT).show()
                        return@ButtonEx
                    }

                    if (authViewModel.passwordSignUp.length < 6) {
                        Log.e("AuthDebug", "Password too short: ${authViewModel.passwordSignUp.length}")
                        Toast.makeText(context, "Password must be 6+ characters", Toast.LENGTH_SHORT).show()
                        return@ButtonEx
                    }

                    Log.d("AuthDebug", "Calling registerUser() in ViewModel...")

                    authViewModel.registerUser { ok, err ->
                        Log.d("AuthDebug", "registerUser() callback → ok=$ok err=$err")

                        if (ok) {
                            Log.d("AuthDebug", "Signup success → navigating to login_screen")

                            navController.navigate("login_screen") {
                                popUpTo("first_screen") {
                                    inclusive = false
                                }
                            }
                        } else {
                            Log.e("AuthDebug", "Signup failed inside callback")

                            Toast.makeText(context, err ?: "Unknown error", Toast.LENGTH_LONG).show()
                            Log.e("RegisterScreen", "SignUp error: $err")
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                textFontWeight = FontWeight.Medium
            )



            Spacer(Modifier.Companion.height(24.dp))

        }
    }
}
