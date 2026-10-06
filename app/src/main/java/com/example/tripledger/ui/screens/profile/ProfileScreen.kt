package com.example.tripledger.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {

    var showContent by remember {
        mutableStateOf(false)
    }

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(450)
                ) +
                        slideInVertically(
                            initialOffsetY = { -35 },
                            animationSpec = tween(450)
                        )
        ) {

            ProfileHeader()
        }

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = 100
                    )
                ) +
                        scaleIn(
                            initialScale = 0.95f,
                            animationSpec = tween(
                                durationMillis = 450,
                                delayMillis = 100
                            )
                        )
        ) {

            ProfileAccountCard()
        }

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = 180
                    )
                ) +
                        slideInVertically(
                            initialOffsetY = { 30 },
                            animationSpec = tween(
                                durationMillis = 450,
                                delayMillis = 180
                            )
                        )
        ) {

            ProfileTravelCard()
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = 260
                    )
                ) +
                        slideInVertically(
                            initialOffsetY = { 40 },
                            animationSpec = tween(
                                durationMillis = 450,
                                delayMillis = 260
                            )
                        )
        ) {

            LogoutSection(
                onLogoutClick = {
                    showLogoutDialog = true
                }
            )
        }
    }

    if (showLogoutDialog) {

        LogoutConfirmationDialog(
            onDismiss = {
                showLogoutDialog = false
            },
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            }
        )
    }
}

@Composable
private fun ProfileHeader() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme
                    .colorScheme
                    .primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Card(
                modifier = Modifier.size(62.dp),
                shape =
                    MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .primary
                ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
            ) {

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Person,
                        contentDescription =
                            "Profile",
                        modifier =
                            Modifier.size(34.dp),
                        tint =
                            MaterialTheme
                                .colorScheme
                                .onPrimary
                    )
                }
            }

            Spacer(
                modifier = Modifier.size(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Profile",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onPrimaryContainer
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text =
                        "Manage your TripLedger account",
                    fontSize = 14.sp,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onPrimaryContainer
                            .copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun ProfileAccountCard() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(14.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Default.AccountCircle,
                    contentDescription =
                        "Account",
                    modifier =
                        Modifier.size(26.dp),
                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )

                Spacer(
                    modifier = Modifier.size(10.dp)
                )

                Text(
                    text = "Account",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            ProfileInfoRow(
                icon = Icons.Default.Person,
                title = "Profile",
                value =
                    "Your TripLedger account"
            )

            ProfileInfoRow(
                icon = Icons.Default.Info,
                title = "Account status",
                value = "Active"
            )
        }
    }
}

@Composable
private fun ProfileTravelCard() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme
                    .colorScheme
                    .secondaryContainer
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Luggage,
                    contentDescription =
                        "Travel",
                    modifier =
                        Modifier.size(27.dp),
                    tint =
                        MaterialTheme
                            .colorScheme
                            .onSecondaryContainer
                )

                Spacer(
                    modifier = Modifier.size(10.dp)
                )

                Text(
                    text = "Your Travel Journal",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSecondaryContainer
                )
            }

            Text(
                text =
                    "Keep your trips, memories and expenses organized in one place.",
                fontSize = 14.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSecondaryContainer
                        .copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(21.dp),
            tint =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier = Modifier.size(10.dp)
        )

        Column {

            Text(
                text = title,
                fontSize = 12.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LogoutSection(
    onLogoutClick: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "Account Actions",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text =
                    "Sign out of your TripLedger account.",
                fontSize = 13.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            OutlinedButton(
                onClick = onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Logout,
                    contentDescription = null,
                    modifier =
                        Modifier.size(19.dp)
                )

                Text(
                    text = "  Logout",
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,

        icon = {
            Icon(
                imageVector =
                    Icons.Default.Logout,
                contentDescription = null,
                modifier =
                    Modifier.size(30.dp),
                tint =
                    MaterialTheme
                        .colorScheme
                        .error
            )
        },

        title = {
            Text(
                text = "Logout?",
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Text(
                text =
                    "Are you sure you want to logout from TripLedger?"
            )
        },

        confirmButton = {

            Button(
                onClick = onConfirm,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
            ) {
                Text("Logout")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}