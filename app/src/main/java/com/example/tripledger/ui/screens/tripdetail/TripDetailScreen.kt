package com.example.tripledger.ui.screens.tripdetail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tripledger.data.remote.TripPhotoResponse
import com.example.tripledger.data.remote.TripResponse
import com.example.tripledger.util.uriToMultipart
import com.example.tripledger.viewmodel.TripViewModel
import kotlinx.coroutines.launch

private const val API_BASE_URL = "http://10.0.2.2:8000"

@Composable
fun TripDetailScreen(
    trip: TripResponse,
    tripViewModel: TripViewModel = viewModel(),
    onExpensesClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var photos by remember {
        mutableStateOf<List<TripPhotoResponse>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var message by remember {
        mutableStateOf<String?>(null)
    }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->

            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                message = "Uploading photo..."

                val photoPart = uriToMultipart(
                    context = context,
                    uri = uri
                )

                if (photoPart == null) {
                    message = "Unsupported image format"
                    return@launch
                }

                val result = tripViewModel.uploadTripPhoto(
                    tripId = trip.id,
                    photo = photoPart
                )

                result
                    .onSuccess {
                        message = "Memory photo added!"

                        tripViewModel
                            .getTripPhotos(trip.id)
                            .onSuccess { updatedPhotos ->
                                photos = updatedPhotos
                            }
                    }
                    .onFailure { exception ->
                        message =
                            exception.message
                                ?: "Photo upload failed"
                    }
            }
        }

    LaunchedEffect(trip.id) {
        isLoading = true

        tripViewModel
            .getTripPhotos(trip.id)
            .onSuccess { loadedPhotos ->
                photos = loadedPhotos
            }

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = trip.title,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = trip.destination,
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "${trip.start_date} → ${trip.end_date}",
            style = MaterialTheme.typography.bodyLarge
        )

        if (!trip.description.isNullOrBlank()) {
            Text(
                text = trip.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onExpensesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Trip Expenses")
        }

        Text(
            text = "Memory Gallery",
            style = MaterialTheme.typography.titleLarge
        )

        Button(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts
                            .PickVisualMedia
                            .ImageOnly
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Memory Photo")
        }

        message?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (isLoading) {

            CircularProgressIndicator(
                modifier = Modifier.size(32.dp)
            )

        } else if (photos.isEmpty()) {

            Text(
                text = "No memory photos yet.",
                style = MaterialTheme.typography.bodyMedium
            )

        } else {

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    vertical = 8.dp
                ),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = photos,
                    key = { photo ->
                        photo.id
                    }
                ) { photo ->

                    TripMemoryItem(
                        photo = photo,
                        tripViewModel = tripViewModel,
                        onDeleted = {
                            photos =
                                photos.filter {
                                    it.id != photo.id
                                }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TripMemoryItem(
    photo: TripPhotoResponse,
    tripViewModel: TripViewModel,
    onDeleted: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val imageUrl =
        "$API_BASE_URL${photo.photo_url}"

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Trip memory",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            val result =
                                tripViewModel
                                    .deleteTripPhoto(
                                        photo.id
                                    )

                            result.onSuccess {
                                onDeleted()
                            }
                        }
                    }
                ) {
                    Text("✕")
                }
            }
        }
    }
}