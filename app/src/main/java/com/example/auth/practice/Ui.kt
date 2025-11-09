package com.example.auth.practice

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun UserScreen(userViewModel: UserViewModel = viewModel()) {
    val users by userViewModel.userList
    val error by userViewModel.errorMessage

    // Fetch users only once when screen starts
    LaunchedEffect(Unit) {
        userViewModel.fetchUsers()
    }

    if (error.isNotEmpty()) {
        Text(text = "Error: $error", color = MaterialTheme.colorScheme.error)
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(users) { user ->
                Text(text = "${user.company} (${user.email})")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
