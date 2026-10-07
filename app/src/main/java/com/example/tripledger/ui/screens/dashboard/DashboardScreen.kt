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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tripledger.data.remote.TripResponse
import com.example.tripledger.ui.components.BottomNavigationBar
import com.example.tripledger.viewmodel.TripViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

private data class DashboardStats(
    val tripCount: Int = 0,
    val totalSpent: Double = 0.0,
    val placesVisited: Int = 0,
    val recentTrips: List<TripResponse> = emptyList()
)

@Composable
fun DashboardScreen(
    navController: NavController
) {
    val tripViewModel: TripViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    var showContent by remember {
        mutableStateOf(false)
    }

    var stats by remember {
        mutableStateOf(DashboardStats())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var hasError by remember {
        mutableStateOf(false)
    }

    var refreshKey by remember {
        mutableStateOf(0)
    }

    suspend fun loadDashboard() {
        isLoading = true
        hasError = false

        tripViewModel.loadTrips()

        // Read the trips directly from the ViewModel after its state updates.
        // A short polling loop avoids changing the existing TripViewModel API.
        var trips: List<TripResponse> = emptyList()
        repeat(40) {
            val state = tripViewModel.tripState.value

            when (state) {
                is com.example.tripledger.viewmodel.TripState.Success -> {
                    trips = state.trips
                    return@repeat
                }

                is com.example.tripledger.viewmodel.TripState.Error -> {
                    hasError = true
                    return@repeat
                }

                else -> delay(100)
            }
        }

        if (trips.isEmpty() && hasError) {
            stats = DashboardStats()
            isLoading = false
            return
        }

        var totalSpent = 0.0
        var placesVisited = 0
        var analyticsFailed = false

        for (trip in trips) {
            tripViewModel.getExpenseAnalytics(trip.id)
                .onSuccess { analytics ->
                    totalSpent += analytics.total_expenses
                }
                .onFailure {
                    analyticsFailed = true
                }

            tripViewModel.getTripPlaces(trip.id)
                .onSuccess { places ->
                    placesVisited += places.size
                }
                .onFailure {
                    analyticsFailed = true
                }
        }

        hasError = analyticsFailed

        val recentTrips = trips
            .sortedWith(
                compareByDescending<TripResponse> {
                    parseDateOrMin(it.start_date)
                }.thenByDescending {
                    parseDateOrMin(it.end_date)
                }
            )
            .take(3)

        stats = DashboardStats(
            tripCount = trips.size,
            totalSpent = totalSpent,
            placesVisited = placesVisited,
            recentTrips = recentTrips
        )

        isLoading = false
    }

    LaunchedEffect(refreshKey) {
        loadDashboard()
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 16.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
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
            }

            item {
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

                        if (isLoading) {
                            DashboardLoadingCard()
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {

                                OverviewCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Flight,
                                    title = "Trips",
                                    value = stats.tripCount.toString()
                                )

                                OverviewCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.AccountBalanceWallet,
                                    title = "Spent",
                                    value = formatCurrency(stats.totalSpent)
                                )
                            }

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            OverviewCard(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Default.Place,
                                title = "Places Visited",
                                value = stats.placesVisited.toString()
                            )

                            if (hasError) {
                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Text(
                                    text = "Some dashboard details could not be loaded.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(
                    visible = showContent && !isLoading,
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

                        if (stats.recentTrips.isEmpty()) {
                            EmptyTripsCard()
                        }
                    }
                }
            }

            if (!isLoading) {
                items(
                    items = stats.recentTrips,
                    key = { trip -> trip.id }
                ) { trip ->
                    RecentTripCard(
                        trip = trip,
                        onClick = {
                            navController.navigate("tripDetail/${trip.id}")
                        }
                    )
                }
            }

            if (!isLoading && hasError) {
                item {
                    Button(
                        onClick = {
                            refreshKey++
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(
                            modifier = Modifier.size(8.dp)
                        )
                        Text("Refresh Dashboard")
                    }
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
private fun DashboardLoadingCard() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp)
            )

            Spacer(
                modifier = Modifier.size(12.dp)
            )

            Text(
                text = "Loading your travel statistics..."
            )
        }
    }
}

@Composable
private fun RecentTripCard(
    trip: TripResponse,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Flight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(25.dp)
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
                    text = trip.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text = trip.destination,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "${trip.start_date}  →  ${trip.end_date}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primaryContainer
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

private fun formatCurrency(amount: Double): String {
    return NumberFormat
        .getCurrencyInstance(Locale("en", "IN"))
        .format(amount)
}

private fun parseDateOrMin(value: String): LocalDate {
    return try {
        LocalDate.parse(value)
    } catch (_: Exception) {
        LocalDate.MIN
    }
}
