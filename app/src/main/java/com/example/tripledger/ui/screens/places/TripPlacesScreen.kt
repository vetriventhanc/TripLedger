package com.example.tripledger.ui.screens.places

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripledger.data.remote.TripPlaceResponse
import com.example.tripledger.viewmodel.TripViewModel
import kotlinx.coroutines.launch

@Composable
fun TripPlacesScreen(
    tripId: Int,
    tripViewModel: TripViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()

    var places by remember {
        mutableStateOf<List<TripPlaceResponse>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var showAddDialog by remember {
        mutableStateOf(false)
    }

    var showContent by remember {
        mutableStateOf(false)
    }

    fun loadPlaces() {
        scope.launch {
            isLoading = true
            errorMessage = null

            tripViewModel
                .getTripPlaces(tripId)
                .onSuccess { result ->
                    places = result
                    isLoading = false
                }
                .onFailure { exception ->
                    errorMessage =
                        exception.message
                            ?: "Failed to load places"
                    isLoading = false
                }
        }
    }

    LaunchedEffect(tripId) {
        showContent = false
        loadPlaces()
        showContent = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
            PlacesHeader(
                placeCount = places.size
            )
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
                        slideInVertically(
                            initialOffsetY = { 30 },
                            animationSpec = tween(
                                durationMillis = 450,
                                delayMillis = 100
                            )
                        )
        ) {

            Button(
                onClick = {
                    showAddDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {

                Icon(
                    imageVector =
                        Icons.Default.AddLocationAlt,
                    contentDescription = null
                )

                Text(
                    text = "  Add Place",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        when {
            isLoading -> {
                LoadingPlacesCard()
            }

            errorMessage != null -> {
                PlacesErrorCard(
                    message = errorMessage!!,
                    onRetry = {
                        loadPlaces()
                    }
                )
            }

            places.isEmpty() -> {
                EmptyPlacesCard(
                    onAddPlace = {
                        showAddDialog = true
                    }
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    items(
                        items = places,
                        key = { place ->
                            place.id
                        }
                    ) { place ->

                        PlaceCard(
                            place = place,
                            onDelete = {

                                scope.launch {
                                    tripViewModel
                                        .deletePlace(
                                            tripId = tripId,
                                            placeId = place.id
                                        )
                                        .onSuccess {
                                            places =
                                                places.filter {
                                                    it.id != place.id
                                                }
                                        }
                                }
                            }
                        )
                    }

                    item {
                        Spacer(
                            modifier = Modifier.height(80.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {

        AddPlaceDialog(
            onDismiss = {
                showAddDialog = false
            },
            onSave = {
                    name,
                    location,
                    visitDate,
                    notes ->

                showAddDialog = false

                scope.launch {
                    tripViewModel
                        .createPlace(
                            tripId = tripId,
                            name = name,
                            location = location,
                            visitDate = visitDate,
                            notes = notes,
                            photoUrl = null
                        )
                        .onSuccess {
                            loadPlaces()
                        }
                        .onFailure { exception ->
                            errorMessage =
                                exception.message
                                    ?: "Failed to add place"
                        }
                }
            }
        )
    }
}

@Composable
private fun PlacesHeader(
    placeCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer
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
                modifier = Modifier.size(58.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.primary
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
                            Icons.Default.Place,
                        contentDescription =
                            "Trip places",
                        tint =
                            MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(30.dp)
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
                    text = "Places & Locations",
                    fontSize = 25.sp,
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
                        if (placeCount == 0) {
                            "Remember the places you visited"
                        } else {
                            "$placeCount places saved from this journey"
                        },
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
private fun PlaceCard(
    place: TripPlaceResponse,
    onDelete: () -> Unit
) {

    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(place.id) {
        kotlinx.coroutines.delay(70)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter =
            fadeIn(
                animationSpec = tween(400)
            ) +
                    scaleIn(
                        initialScale = 0.95f,
                        animationSpec = tween(400)
                    ),
        exit = fadeOut(
            animationSpec = tween(220)
        )
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Card(
                        modifier = Modifier.size(46.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
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
                                    "Place location",
                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary,
                                modifier =
                                    Modifier.size(25.dp)
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.size(12.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = place.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(3.dp)
                        )

                        Text(
                            text = place.location,
                            fontSize = 14.sp,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDelete
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Delete,
                            contentDescription =
                                "Delete place",
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                    }
                }

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.CalendarMonth,
                        contentDescription =
                            "Visit date",
                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary,
                        modifier =
                            Modifier.size(19.dp)
                    )

                    Spacer(
                        modifier = Modifier.size(7.dp)
                    )

                    Text(
                        text = place.visit_date,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (!place.notes.isNullOrBlank()) {

                    Text(
                        text = place.notes,
                        fontSize = 14.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AddPlaceDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        location: String,
        visitDate: String,
        notes: String?
    ) -> Unit
) {

    var name by remember {
        mutableStateOf("")
    }

    var location by remember {
        mutableStateOf("")
    }

    var visitDate by remember {
        mutableStateOf("")
    }

    var notes by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = "Add Place",
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(9.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = {
                        Text("Place name")
                    },
                    placeholder = {
                        Text("Example: Marina Beach")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = {
                        location = it
                        errorMessage = null
                    },
                    label = {
                        Text("Location")
                    },
                    placeholder = {
                        Text("Example: Chennai, Tamil Nadu")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = visitDate,
                    onValueChange = {
                        visitDate = it
                        errorMessage = null
                    },
                    label = {
                        Text("Visit date")
                    },
                    placeholder = {
                        Text("YYYY-MM-DD")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                    },
                    label = {
                        Text("Notes")
                    },
                    placeholder = {
                        Text("What did you experience here?")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                errorMessage?.let { message ->

                    Text(
                        text = message,
                        color =
                            MaterialTheme
                                .colorScheme
                                .error,
                        fontSize = 13.sp
                    )
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    when {

                        name.isBlank() -> {
                            errorMessage =
                                "Enter a place name"
                        }

                        location.isBlank() -> {
                            errorMessage =
                                "Enter a location"
                        }

                        visitDate.isBlank() -> {
                            errorMessage =
                                "Enter a visit date"
                        }

                        !isValidDate(visitDate) -> {
                            errorMessage =
                                "Use date format YYYY-MM-DD"
                        }

                        else -> {

                            onSave(
                                name.trim(),
                                location.trim(),
                                visitDate.trim(),
                                notes
                                    .trim()
                                    .takeIf {
                                        it.isNotBlank()
                                    }
                            )
                        }
                    }
                }
            ) {

                Text(
                    text = "Save Place",
                    fontWeight = FontWeight.SemiBold
                )
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

@Composable
private fun LoadingPlacesCard() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            CircularProgressIndicator(
                modifier = Modifier.size(28.dp)
            )

            Spacer(
                modifier = Modifier.size(12.dp)
            )

            Text(
                text = "Loading places...",
                fontWeight = FontWeight.Medium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlacesErrorCard(
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint =
                        MaterialTheme
                            .colorScheme
                            .onErrorContainer,
                    modifier =
                        Modifier.size(25.dp)
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text(
                    text = "Unable to load places",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onErrorContainer
                )
            }

            Text(
                text = message,
                color =
                    MaterialTheme
                        .colorScheme
                        .onErrorContainer
            )

            OutlinedButton(
                onClick = onRetry
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Refresh,
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

@Composable
private fun EmptyPlacesCard(
    onAddPlace: () -> Unit
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
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Icon(
                imageVector =
                    Icons.Default.Place,
                contentDescription =
                    "Places",
                modifier = Modifier.size(45.dp),
                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Text(
                text = "No places added yet",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "Save the places that made your journey memorable.",
                fontSize = 14.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Button(
                onClick = onAddPlace,
                shape = MaterialTheme.shapes.large
            ) {

                Icon(
                    imageVector =
                        Icons.Default.AddLocationAlt,
                    contentDescription = null
                )

                Text("  Add First Place")
            }
        }
    }
}

private fun isValidDate(value: String): Boolean {
    return Regex(
        "^\\d{4}-\\d{2}-\\d{2}$"
    ).matches(value)
}