package com.example.auth.presentation.authentication

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.auth.presentation.authentication.AuthViewModel
import com.example.auth.presentation.components.ButtonEx

@Composable
fun AskFirstName(
    navController: NavController,
    authViewModel: AuthViewModel
) {
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
                onClick = { navController.navigate("register_screen") },
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
                text = "What's your name?",
                fontWeight = FontWeight.Companion.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                textAlign = TextAlign.Companion.Center
            )

            Spacer(Modifier.Companion.height(20.dp))

            // Tab Row with animation

            Spacer(Modifier.Companion.height(12.dp))


            // Email input
            Text(
                text = "Display Name",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.Companion.padding(start = 4.dp, bottom = 4.dp)
            )

            TextField(
                value = authViewModel.displayName,
                onValueChange = { authViewModel.displayName = it },
                placeholder = { Text("Name", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = textFieldColors(),
                singleLine = true,
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                    .padding(start = 8.dp, end = 8.dp)
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
                text = "Next",
                onClick = { navController.navigate("username_pass_screen") },
                containerColor = MaterialTheme.colorScheme.primary,
                textFontWeight = FontWeight.Companion.Medium
            )


            Spacer(Modifier.Companion.height(32.dp))

        }
    }
}
