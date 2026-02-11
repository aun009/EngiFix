package com.example.auth.presentation.features.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreSheetScreen(
    onBackClick : () -> Unit = {}
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Explore Sheets", style = MaterialTheme.typography.headlineSmall) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        "Back"
                    )
                }
            },

            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
        )

        // load here dsa sheets from the firebase i guess, first of all create  a new file there with the sheets there, in that we will arrange there like , which user is following the which sheet there , if he is following there one list show it upword there , if he follows there 2 then 2 , and also progress bar of it , like how much he completed there

        // show them, like on this screen , so like there, ongoing or subscribed sheets, show them on the main screen,
        // and show other screen onn the below of it like the explore all the sheets there
    }

}