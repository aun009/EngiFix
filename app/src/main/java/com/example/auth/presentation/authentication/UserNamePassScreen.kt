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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
            .background(Color(0xFF18181C))
    ) {
        Column(
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.Companion.height(40.dp)) // Added more top padding

            // Back arrow icon (updated for AutoMirrored)
            IconButton(
                onClick = { navController.navigate("ask_name_screen") },
                modifier = Modifier.Companion.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Companion.White
                )
            }

            Spacer(Modifier.Companion.height(20.dp)) // Increased spacing

            // Title
            Text(
                text = "Next, Create an account",
                fontWeight = FontWeight.Companion.Bold,
                fontSize = 24.sp,
                color = Color.Companion.White,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                textAlign = TextAlign.Companion.Center
            )

            Spacer(Modifier.Companion.height(32.dp))

            // Tab Row with animation

            Spacer(Modifier.Companion.height(32.dp))


            // Email input
            Text(
                text = "Username",
                color = Color(0xFFD7D7D7),
                fontSize = 14.sp,
                modifier = Modifier.Companion.padding(start = 4.dp, bottom = 4.dp)
            )

            TextField(
                value = authViewModel.userName,
                onValueChange = { authViewModel.userName = it },
                placeholder = { Text("username", color = Color(0x88FFFFFF)) },
                colors = textFieldColors(),
                singleLine = true,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .background(Color(0xFF22232D), shape = RoundedCornerShape(20.dp))
                    .padding(start = 8.dp, end = 8.dp)
            )

            Spacer(Modifier.Companion.height(16.dp))

            TextField(
                value = authViewModel.passwordSignUp, // 👈 comes from ViewModel
                onValueChange = { authViewModel.passwordSignUp = it },
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

            Spacer(Modifier.Companion.height(32.dp))

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
                    authViewModel.registerUser { ok, err ->
                        if (ok) {
                            // e.g., go to login or home
                            navController.navigate("login_screen") {
                                popUpTo("first_screen") { inclusive = false }
                            }
                        } else {
                            // show error (Snackbar/Toast/log)
                            Toast.makeText(context.applicationContext, err, Toast.LENGTH_LONG).show()
                            Log.e("RegisterScreen", "SignUp error: $err")
                        }
                    }
                },
                containerColor = Color(0xFF5865F2),
                textFontWeight = FontWeight.Companion.Medium
            )



            Spacer(Modifier.Companion.height(32.dp))

        }
    }
}