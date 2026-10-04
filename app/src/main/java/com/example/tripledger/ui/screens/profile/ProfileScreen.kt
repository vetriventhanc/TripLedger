package com.example.tripledger.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Profile",
            fontSize = 28.sp
        )

        Text(
            text = "Your TripLedger account will appear here.",
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Button(
            onClick = onLogout,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = "Logout"
            )
        }
    }
}