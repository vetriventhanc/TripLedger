package com.example.tripledger.ui.screens.trips

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripledger.data.remote.TripResponse
import com.example.tripledger.viewmodel.TripState
import com.example.tripledger.viewmodel.TripViewModel


@Composable
fun TripsScreen(
    onTripClick: (Int) -> Unit,
    tripViewModel: TripViewModel = viewModel()
) {
    val tripState by tripViewModel.tripState.collectAsStateWithLifecycle()

    var showCreateForm by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        tripViewModel.loadTrips()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "My Trips",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = {
                showCreateForm = !showCreateForm
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (showCreateForm) {
                    "Cancel"
                } else {
                    "Add Trip"
                }
            )
        }

        if (showCreateForm) {
            CreateTripForm(
                onCreateTrip = { title, destination, startDate, endDate, description ->

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
                Text(
                    text = "Your trips will appear here."
                )
            }

            is TripState.Loading -> {
                CircularProgressIndicator()
            }

            is TripState.Success -> {

                if (state.trips.isEmpty()) {

                    Text(
                        text = "No trips yet. Add your first trip!"
                    )

                } else {

                    TripList(
                        trips = state.trips,
                        onTripClick = onTripClick,
                        onDeleteTrip = { tripId ->
                            tripViewModel.deleteTrip(tripId)
                        }
                    )
                }
            }

            is TripState.Created -> {

                LaunchedEffect(state.trip.id) {
                    tripViewModel.loadTrips()
                }

                Text(
                    text = "Trip created successfully!"
                )
            }

            is TripState.Deleted -> {

                LaunchedEffect(Unit) {
                    tripViewModel.loadTrips()
                }

                Text(
                    text = "Trip deleted successfully!"
                )
            }

            is TripState.Error -> {

                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )

                OutlinedButton(
                    onClick = {
                        tripViewModel.loadTrips()
                    }
                ) {
                    Text("Retry")
                }
            }
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
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        items(
            items = trips,
            key = { trip -> trip.id }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = trip.title,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = trip.destination,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "${trip.start_date} → ${trip.end_date}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (!trip.description.isNullOrBlank()) {

                Text(
                    text = trip.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Trip")
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Trip")
            }
        }
    }
}


@Composable
private fun CreateTripForm(
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

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
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {

                onCreateTrip(
                    title,
                    destination,
                    startDate,
                    endDate,
                    description.ifBlank {
                        null
                    }
                )
            },
            enabled =
                title.isNotBlank() &&
                        destination.isNotBlank() &&
                        startDate.isNotBlank() &&
                        endDate.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Trip")
        }
    }
}