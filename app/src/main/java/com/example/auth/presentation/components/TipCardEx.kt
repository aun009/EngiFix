package com.example.auth.presentation.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TipCardEx(title : String, description : String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },   // ✅ clickable
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF262626)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text(description, fontSize = 16.sp, color = Color.LightGray)
        }
    }

}

@Preview(showSystemUi = true)
@Composable
fun TipCardExPreview() {
    val title = "Sample Title"
    val description = "Sample Description"
    val icon = Icon.createWithContentUri("content://com.example.auth/icon")
    TipCardEx(title, description)
}