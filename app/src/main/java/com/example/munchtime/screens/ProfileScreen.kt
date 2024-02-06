package com.example.munchtime.screens

import android.service.autofill.UserData
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    userData: com.example.munchtime.auth.UserData?,
    onSignOut : () -> Unit
) {
    Column {
        if (userData?.userId!=null){
            Text(text = userData.userId)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSignOut) {
            Text(text = "Sign Out")
        }
    }

}