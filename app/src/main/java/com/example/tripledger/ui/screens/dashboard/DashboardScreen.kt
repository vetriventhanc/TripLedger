package com.example.tripledger.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tripledger.ui.components.BottomNavigationBar
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    navController: NavController
) {
    var showContent by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        delay(120)
        showContent = true
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("trips")
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add trip"
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp
                )
        ) {

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(
                    animationSpec = tween(500)
                ) + slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(500)
                )
            ) {

                DashboardHeader()
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        delayMillis = 100
                    )
                ) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(
                        durationMillis = 500,
                        delayMillis = 100
                    )
                )
            ) {

                Column {

                    Text(
                        text = "Travel Overview",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        OverviewCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Flight,
                            title = "Trips",
                            value = "0"
                        )

                        OverviewCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Spent",
                            value = "₹0"
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    OverviewCard(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.Place,
                        title = "Places Visited",
                        value = "0"
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        delayMillis = 250
                    )
                ) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(
                        durationMillis = 500,
                        delayMillis = 250
                    )
                )
            ) {

                Column {

                    Text(
                        text = "Recent Trips",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    EmptyTripsCard()
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Card(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primary
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
                        imageVector = Icons.Default.Explore,
                        contentDescription = "Explore",
                        tint =
                            MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.size(16.dp)
            )

            Column {

                Text(
                    text = "TripLedger",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = "Your travel companion",
                    fontSize = 14.sp,
                    color =
                        MaterialTheme.colorScheme
                            .onPrimaryContainer
                            .copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun OverviewCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Card(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme
                            .secondaryContainer
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
                        imageVector = icon,
                        contentDescription = title,
                        tint =
                            MaterialTheme.colorScheme
                                .onSecondaryContainer,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = title,
                fontSize = 13.sp,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = value,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EmptyTripsCard() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier
                    .size(64.dp)
                    .clip(
                        RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme
                            .primaryContainer
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
                        imageVector = Icons.Default.Flight,
                        contentDescription = "No trips",
                        tint =
                            MaterialTheme.colorScheme
                                .onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "No trips yet",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Start your first journey with TripLedger.",
                fontSize = 14.sp,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}