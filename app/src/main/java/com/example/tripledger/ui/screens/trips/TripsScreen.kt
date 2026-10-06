package com.example.tripledger.ui.screens.trips

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripledger.data.remote.TripResponse
import com.example.tripledger.viewmodel.TripState
import com.example.tripledger.viewmodel.TripViewModel
import kotlinx.coroutines.delay

@Composable
fun TripsScreen(
    onTripClick: (Int) -> Unit,
    tripViewModel: TripViewModel = viewModel()
) {
    val tripState by tripViewModel.tripState.collectAsStateWithLifecycle()

    var showCreateForm by remember {
        mutableStateOf(false)
    }

    var showContent by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        tripViewModel.loadTrips()
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(
                animationSpec = tween(450)
            ) + slideInVertically(
                initialOffsetY = { -35 },
                animationSpec = tween(450)
            )
        ) {
            TripsHeader(
                tripCount = when (val state = tripState) {
                    is TripState.Success -> state.trips.size
                    else -> 0
                }
            )
        }

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 450,
                    delayMillis = 100
                )
            ) + slideInVertically(
                initialOffsetY = { 30 },
                animationSpec = tween(
                    durationMillis = 450,
                    delayMillis = 100
                )
            )
        ) {

            Button(
                onClick = {
                    showCreateForm = !showCreateForm
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text(
                    text = if (showCreateForm) {
                        "Close Form"
                    } else {
                        "Add New Trip"
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = showCreateForm,
            enter = fadeIn(
                animationSpec = tween(300)
            ) + expandVertically(
                animationSpec = tween(350)
            ),
            exit = fadeOut(
                animationSpec = tween(200)
            ) + shrinkVertically(
                animationSpec = tween(300)
            )
        ) {

            CreateTripCard(
                onCreateTrip = {
                        title,
                        destination,
                        startDate,
                        endDate,
                        description ->

                    tripViewModel.createTrip(
                        title = title,
                        destination = destination,
                        startDate = startDate,
                        endDate = endDate,
                        description = description
                    )

                    showCreateForm = false
                }
            )
        }

        when (val state = tripState) {

            is TripState.Idle -> {

                LoadingTripsCard(
                    text = "Preparing your trips..."
                )
            }

            is TripState.Loading -> {

                LoadingTripsCard(
                    text = "Loading your trips..."
                )
            }

            is TripState.Success -> {

                if (state.trips.isEmpty()) {

                    EmptyTripsState(
                        onAddTrip = {
                            showCreateForm = true
                        }
                    )

                } else {

                    TripList(
                        trips = state.trips,
                        onTripClick = onTripClick,
                        onDeleteTrip = {
                            tripViewModel.deleteTrip(it)
                        }
                    )
                }
            }

            is TripState.Created -> {

                LaunchedEffect(state.trip.id) {
                    tripViewModel.loadTrips()
                }

                SuccessMessage(
                    text = "Trip created successfully!"
                )
            }

            is TripState.Updated -> {

                LaunchedEffect(state.trip.id) {
                    tripViewModel.loadTrips()
                }

                SuccessMessage(
                    text = "Trip updated successfully!"
                )
            }

            is TripState.Deleted -> {

                LaunchedEffect(Unit) {
                    tripViewModel.loadTrips()
                }

                SuccessMessage(
                    text = "Trip deleted successfully!"
                )
            }

            is TripState.Error -> {

                ErrorTripsCard(
                    message = state.message,
                    onRetry = {
                        tripViewModel.loadTrips()
                    }
                )
            }
        }
    }
}

@Composable
private fun TripsHeader(
    tripCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Card(
            modifier = Modifier.size(56.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(
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
                    imageVector = Icons.Default.Luggage,
                    contentDescription = "Trips",
                    tint =
                        MaterialTheme.colorScheme
                            .onPrimaryContainer,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(
            modifier = Modifier.size(14.dp)
        )

        Column {

            Text(
                text = "My Trips",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = when (tripCount) {
                    0 -> "Start planning your next journey"
                    1 -> "1 journey in your travel diary"
                    else -> "$tripCount journeys in your travel diary"
                },
                fontSize = 14.sp,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TripList(
    trips: List<TripResponse>,
    onTripClick: (Int) -> Unit,
    onDeleteTrip: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            top = 4.dp,
            bottom = 100.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        items(
            items = trips,
            key = { trip ->
                trip.id
            }
        ) { trip ->

            TripCard(
                trip = trip,
                onClick = {
                    onTripClick(trip.id)
                },
                onDelete = {
                    onDeleteTrip(trip.id)
                }
            )
        }
    }
}

@Composable
private fun TripCard(
    trip: TripResponse,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(trip.id) {
        delay(80)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(450)
        ) + slideInVertically(
            initialOffsetY = { 35 },
            animationSpec = tween(450)
        )
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Card(
                        modifier = Modifier.size(52.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme.colorScheme
                                    .secondaryContainer
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
                                    Icons.Default.LocationOn,
                                contentDescription =
                                    "Destination",
                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .onSecondaryContainer,
                                modifier =
                                    Modifier.size(26.dp)
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
                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(3.dp)
                        )

                        Text(
                            text = trip.destination,
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary,
                            fontWeight =
                                FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = onDelete
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Delete,
                            contentDescription =
                                "Delete trip",
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme
                                .surfaceVariant
                    ),
                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.CalendarMonth,
                            contentDescription =
                                "Trip dates",
                            modifier =
                                Modifier.size(20.dp),
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                        )

                        Spacer(
                            modifier = Modifier.size(8.dp)
                        )

                        Text(
                            text =
                                "${trip.start_date}  →  ${trip.end_date}",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,
                            fontWeight =
                                FontWeight.Medium
                        )
                    }
                }

                if (!trip.description.isNullOrBlank()) {

                    Text(
                        text = trip.description,
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {

                    Text(
                        text = "View Trip",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateTripCard(
    onCreateTrip: (
        title: String,
        destination: String,
        startDate: String,
        endDate: String,
        description: String?
    ) -> Unit
) {
    var title by remember {
        mutableStateOf("")
    }

    var destination by remember {
        mutableStateOf("")
    }

    var startDate by remember {
        mutableStateOf("")
    }

    var endDate by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf("")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme
                    .primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = "Plan a new journey",
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Add the details of your trip.",
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onPrimaryContainer
                        .copy(alpha = 0.75f)
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                },
                label = {
                    Text("Trip title")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = destination,
                onValueChange = {
                    destination = it
                },
                label = {
                    Text("Destination")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = startDate,
                onValueChange = {
                    startDate = it
                },
                label = {
                    Text("Start date")
                },
                placeholder = {
                    Text("YYYY-MM-DD")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = endDate,
                onValueChange = {
                    endDate = it
                },
                label = {
                    Text("End date")
                },
                placeholder = {
                    Text("YYYY-MM-DD")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                label = {
                    Text("Description")
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {

                    onCreateTrip(
                        title.trim(),
                        destination.trim(),
                        startDate.trim(),
                        endDate.trim(),
                        description
                            .trim()
                            .takeIf {
                                it.isNotBlank()
                            }
                    )
                },
                enabled =
                    title.isNotBlank() &&
                            destination.isNotBlank() &&
                            startDate.isNotBlank() &&
                            endDate.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text(
                    text = "Save Trip",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EmptyTripsState(
    onAddTrip: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme
                    .surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier.size(72.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme
                            .primaryContainer
                ),
                elevation = CardDefaults.cardElevation(
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
                        imageVector = Icons.Default.Luggage,
                        contentDescription = "No trips",
                        modifier = Modifier.size(38.dp),
                        tint =
                            MaterialTheme.colorScheme
                                .onPrimaryContainer
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "No trips yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "Your next adventure starts here.",
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Button(
                onClick = onAddTrip,
                shape = MaterialTheme.shapes.large
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text("Create First Trip")
            }
        }
    }
}

@Composable
private fun LoadingTripsCard(
    text: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            CircularProgressIndicator(
                modifier = Modifier.size(34.dp)
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = text,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SuccessMessage(
    text: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme
                    .secondaryContainer
        )
    ) {

        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color =
                MaterialTheme.colorScheme
                    .onSecondaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorTripsCard(
    message: String,
    onRetry: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.errorContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = "Something went wrong",
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme
                        .onErrorContainer
            )

            Text(
                text = message,
                color =
                    MaterialTheme.colorScheme
                        .onErrorContainer
            )

            TextButton(
                onClick = onRetry
            ) {

                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.size(6.dp)
                )

                Text("Retry")
            }
        }
    }
}