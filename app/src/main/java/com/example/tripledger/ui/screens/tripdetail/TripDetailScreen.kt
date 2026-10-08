package com.example.tripledger.ui.screens.tripdetail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tripledger.data.remote.TripOverviewResponse
import com.example.tripledger.data.remote.TripPhotoResponse
import com.example.tripledger.data.remote.TripResponse
import com.example.tripledger.util.uriToMultipart
import com.example.tripledger.viewmodel.TripViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val API_BASE_URL = "http://10.0.2.2:8000"

@Composable
fun TripDetailScreen(
    trip: TripResponse,
    tripViewModel: TripViewModel = viewModel(),
    onExpensesClick: () -> Unit,
    onTimelineClick: () -> Unit,
    onPlacesClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var photos by remember {
        mutableStateOf<List<TripPhotoResponse>>(emptyList())
    }

    var tripOverview by remember {
        mutableStateOf<TripOverviewResponse?>(null)
    }

    var tripExpenseAnalytics by remember {
        mutableStateOf<com.example.tripledger.data.remote.TripExpenseAnalyticsResponse?>(null)
    }

    var placesCount by remember {
        mutableStateOf(0)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var isOverviewLoading by remember {
        mutableStateOf(true)
    }

    var message by remember {
        mutableStateOf<String?>(null)
    }

    var showEditDialog by remember {
        mutableStateOf(false)
    }
    var selectedMemory by remember {
        mutableStateOf<TripPhotoResponse?>(null)
    }

    var pendingMemoryUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var memoryCaption by remember {
        mutableStateOf("")
    }

    var memoryDate by remember {
        mutableStateOf("")
    }

    var memoryFilter by remember {
        mutableStateOf("All")
    }

    val filteredPhotos = remember(
        photos,
        memoryFilter
    ) {
        when (memoryFilter) {
            "Date" -> photos.filter {
                !it.memory_date.isNullOrBlank()
            }

            "Caption" -> photos.filter {
                !it.caption.isNullOrBlank()
            }

            else -> photos
        }
    }

    var isUploadingMemory by remember {
        mutableStateOf(false)
    }

    var showContent by remember {
        mutableStateOf(false)
    }

    var coverPhotoUrl by remember {
        mutableStateOf<String?>(trip.cover_photo)
    }

    var isUploadingCover by remember {
        mutableStateOf(false)
    }

    val coverPhotoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->

            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                isUploadingCover = true
                message = "Uploading cover photo..."

                val photoPart = uriToMultipart(
                    context = context,
                    uri = uri
                )

                if (photoPart == null) {
                    isUploadingCover = false
                    message = "Unsupported image format"
                    return@launch
                }

                val result =
                    tripViewModel.uploadTripCoverPhoto(
                        tripId = trip.id,
                        photo = photoPart
                    )

                result
                    .onSuccess { updatedTrip ->
                        coverPhotoUrl = updatedTrip.cover_photo
                        message = "Cover photo updated!"
                    }
                    .onFailure { exception ->
                        message =
                            exception.message
                                ?: "Cover photo upload failed"
                    }

                isUploadingCover = false
            }
        }

    val photoPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->

            if (uri == null) {
                return@rememberLauncherForActivityResult
            }

            pendingMemoryUri = uri
            memoryCaption = ""
            memoryDate = ""
        }


    LaunchedEffect(trip.id) {
        isLoading = true
        isOverviewLoading = true
        showContent = false

        tripViewModel
            .getTripPhotos(trip.id)
            .onSuccess { loadedPhotos ->
                photos = loadedPhotos
            }

        tripViewModel
            .getTripOverview(trip.id)
            .onSuccess { overview ->
                tripOverview = overview
            }

        tripViewModel
            .getExpenseAnalytics(trip.id)
            .onSuccess { analytics ->
                tripExpenseAnalytics = analytics
            }

        tripViewModel
            .getTripPlaces(trip.id)
            .onSuccess { places ->
                placesCount = places.size
            }

        isLoading = false
        isOverviewLoading = false

        delay(100)
        showContent = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
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

            TripHeroCard(
                trip = trip,
                coverPhotoUrl = coverPhotoUrl,
                isUploadingCover = isUploadingCover,
                onEditClick = {
                    showEditDialog = true
                },
                onCoverPhotoClick = {
                    coverPhotoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts
                                .PickVisualMedia
                                .ImageOnly
                        )
                    )
                }
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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedButton(
                        onClick = {
                            showEditDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    ) {

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Text(
                            text = " Edit",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onExpensesClick,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Text(
                            text = " Expenses",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    OutlinedButton(
                        onClick = onTimelineClick,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    ) {

                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Text(
                            text = " Timeline",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = onPlacesClick,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large
                    ) {

                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Text(
                            text = " Places",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = 150
                    )
                ) +
                        slideInVertically(
                            initialOffsetY = { 25 },
                            animationSpec = tween(
                                durationMillis = 450,
                                delayMillis = 150
                            )
                        )
        ) {

            if (isOverviewLoading) {
                TripOverviewLoading()
            } else {
                tripOverview?.let { overview ->
                    TripOverviewCard(
                        overview = overview
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = 160
                    )
                ) +
                        slideInVertically(
                            initialOffsetY = { 30 },
                            animationSpec = tween(
                                durationMillis = 450,
                                delayMillis = 160
                            )
                        )
        ) {

            TripFinancialSummaryCard(
                overview = tripOverview,
                analytics = tripExpenseAnalytics,
                placesCount = placesCount
            )
        }

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = 170
                    )
                ) +
                        slideInVertically(
                            initialOffsetY = { 32 },
                            animationSpec = tween(
                                durationMillis = 450,
                                delayMillis = 170
                            )
                        )
        ) {
            SmartTripSummaryCard(
                trip = trip,
                overview = tripOverview,
                analytics = tripExpenseAnalytics,
                placesCount = placesCount,
                memoryCount = photos.size
            )
        }

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = 175
                    )
                ) +
                        slideInVertically(
                            initialOffsetY = { 34 },
                            animationSpec = tween(
                                durationMillis = 450,
                                delayMillis = 175
                            )
                        )
        ) {
            TripActivityInsightsCard(
                trip = trip,
                analytics = tripExpenseAnalytics,
                memoryCount = photos.size,
                placesCount = placesCount
            )
        }

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = 185
                    )
                ) +
                        slideInVertically(
                            initialOffsetY = { 36 },
                            animationSpec = tween(
                                durationMillis = 450,
                                delayMillis = 185
                            )
                        )
        ) {
            TripCompletionSummaryCard(
                trip = trip,
                analytics = tripExpenseAnalytics,
                memoryCount = photos.size,
                placesCount = placesCount
            )
        }

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = 195
                    )
                ) +
                        slideInVertically(
                            initialOffsetY = { 38 },
                            animationSpec = tween(
                                durationMillis = 450,
                                delayMillis = 195
                            )
                        )
        ) {
            TripShareSummaryCard(
                trip = trip,
                overview = tripOverview,
                analytics = tripExpenseAnalytics,
                placesCount = placesCount,
                memoryCount = photos.size
            )
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
                            initialOffsetY = { 35 },
                            animationSpec = tween(
                                durationMillis = 450,
                                delayMillis = 180
                            )
                        )
        ) {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint =
                            MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Text(
                        text = " Memory Gallery",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (photos.isNotEmpty()) {

                        Text(
                            text = "  ${photos.size}",
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }

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
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.AddPhotoAlternate,
                        contentDescription = null
                    )

                    Text(
                        text = " Add Memory Photo",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (photos.isNotEmpty()) {

                    Text(
                        text = "Filter memories",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {

                        listOf(
                            "All",
                            "Date",
                            "Caption"
                        ).forEach { filter ->

                            if (memoryFilter == filter) {
                                Button(
                                    onClick = {
                                        memoryFilter = filter
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Text(filter)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        memoryFilter = filter
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Text(filter)
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = message != null,
            enter =
                fadeIn(
                    animationSpec = tween(250)
                ) +
                        expandVertically(
                            animationSpec = tween(250)
                        ),
            exit =
                fadeOut(
                    animationSpec = tween(200)
                ) +
                        shrinkVertically(
                            animationSpec = tween(200)
                        )
        ) {

            message?.let {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme
                                .colorScheme
                                .secondaryContainer
                    )
                ) {

                    Text(
                        text = it,
                        modifier = Modifier.padding(14.dp),
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSecondaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (isLoading) {

            LoadingGalleryState()

        } else if (photos.isEmpty()) {

            EmptyGalleryState(
                onAddPhoto = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts
                                .PickVisualMedia
                                .ImageOnly
                        )
                    )
                }
            )

        } else if (filteredPhotos.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No matching memories",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text =
                            when (memoryFilter) {
                                "Date" ->
                                    "No memories have a memory date yet."
                                "Caption" ->
                                    "No memories have a caption yet."
                                else ->
                                    "No memories match this filter."
                            },
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = {
                            memoryFilter = "All"
                        },
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Show All")
                    }
                }
            }

        } else {

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                filteredPhotos
                    .chunked(2)
                    .forEach { rowPhotos ->

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp)
                        ) {

                            rowPhotos.forEach { photo ->

                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    TripMemoryItem(
                                        photo = photo,
                                        tripViewModel = tripViewModel,
                                        onPhotoClick = {
                                            selectedMemory = photo
                                        },
                                        onDeleted = {

                                            photos =
                                                photos.filter {
                                                    it.id != photo.id
                                                }

                                            tripOverview =
                                                tripOverview?.copy(
                                                    memory_count =
                                                        photos.size
                                                )

                                            message =
                                                "Memory photo deleted"
                                        }
                                    )
                                }
                            }

                            if (rowPhotos.size == 1) {

                                Spacer(
                                    modifier =
                                        Modifier.weight(1f)
                                )
                            }
                        }
                    }
            }
        }
    }

    selectedMemory?.let { photo ->
        MemoryPhotoViewer(
            photo = photo,
            tripViewModel = tripViewModel,
            onDismiss = {
                selectedMemory = null
            },
            onDeleted = {
                photos = photos.filter { it.id != photo.id }
                tripOverview = tripOverview?.copy(
                    memory_count = photos.size
                )
                selectedMemory = null
                message = "Memory photo deleted"
            }
        )
    }

    if (pendingMemoryUri != null) {

        AlertDialog(
            onDismissRequest = {
                if (!isUploadingMemory) {
                    pendingMemoryUri = null
                    memoryCaption = ""
                    memoryDate = ""
                }
            },
            title = {
                Text(
                    text = "Add Memory"
                )
            },
            text = {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Add an optional caption to this memory photo.",
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = memoryCaption,
                        onValueChange = {
                            if (it.length <= 500) {
                                memoryCaption = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Caption")
                        },
                        placeholder = {
                            Text("e.g. Sunset at Marina Beach")
                        },
                        supportingText = {
                            Text("${memoryCaption.length}/500")
                        },
                        singleLine = false,
                        maxLines = 4,
                        enabled = !isUploadingMemory
                    )

                    OutlinedTextField(
                        value = memoryDate,
                        onValueChange = {
                            if (it.length <= 10) {
                                memoryDate = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("Memory Date")
                        },
                        placeholder = {
                            Text("YYYY-MM-DD")
                        },
                        supportingText = {
                            Text("Optional • YYYY-MM-DD")
                        },
                        singleLine = true,
                        enabled = !isUploadingMemory
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingMemoryUri
                            ?: return@Button

                        scope.launch {
                            isUploadingMemory = true
                            message = "Uploading memory..."

                            val photoPart =
                                uriToMultipart(
                                    context = context,
                                    uri = uri
                                )

                            if (photoPart == null) {
                                isUploadingMemory = false
                                message = "Unsupported image format"
                                return@launch
                            }

                            val caption =
                                memoryCaption.trim()
                                    .takeIf { it.isNotEmpty() }

                            val enteredMemoryDate =
                                memoryDate.trim()
                                    .takeIf { it.isNotEmpty() }

                            if (
                                enteredMemoryDate != null &&
                                !Regex("\\d{4}-\\d{2}-\\d{2}")
                                    .matches(enteredMemoryDate)
                            ) {
                                isUploadingMemory = false
                                message = "Memory date must use YYYY-MM-DD"
                                return@launch
                            }

                            val result =
                                tripViewModel.uploadTripPhoto(
                                    tripId = trip.id,
                                    photo = photoPart,
                                    caption = caption,
                                    memoryDate = enteredMemoryDate
                                )

                            result
                                .onSuccess {
                                    message = "Memory photo added!"

                                    tripViewModel
                                        .getTripPhotos(trip.id)
                                        .onSuccess { updatedPhotos ->
                                            photos = updatedPhotos
                                        }

                                    tripViewModel
                                        .getTripOverview(trip.id)
                                        .onSuccess { updatedOverview ->
                                            tripOverview = updatedOverview
                                        }

                                    pendingMemoryUri = null
                                    memoryCaption = ""
                                }
                                .onFailure { exception ->
                                    message =
                                        exception.message
                                            ?: "Photo upload failed"
                                }

                            isUploadingMemory = false
                        }
                    },
                    enabled =
                        !isUploadingMemory
                ) {
                    if (isUploadingMemory) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Upload Memory")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isUploadingMemory) {
                            pendingMemoryUri = null
                            memoryCaption = ""
                            memoryDate = ""
                        }
                    },
                    enabled = !isUploadingMemory
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {

        EditTripDialog(
            trip = trip,
            onDismiss = {
                showEditDialog = false
            },
            onSave = {
                    title,
                    destination,
                    startDate,
                    endDate,
                    description ->

                tripViewModel.updateTrip(
                    tripId = trip.id,
                    title = title,
                    destination = destination,
                    startDate = startDate,
                    endDate = endDate,
                    description = description,
                    coverPhoto = trip.cover_photo
                )

                showEditDialog = false
                message = "Trip updated successfully!"

                scope.launch {

                    delay(400)

                    tripViewModel
                        .getTripOverview(trip.id)
                        .onSuccess { updatedOverview ->
                            tripOverview = updatedOverview
                        }
                }
            }
        )
    }
}

@Composable
private fun TripFinancialSummaryCard(
    overview: TripOverviewResponse?,
    analytics: com.example.tripledger.data.remote.TripExpenseAnalyticsResponse?,
    placesCount: Int
) {
    val durationDays = overview?.duration_days ?: 0
    val totalExpenses = analytics?.total_expenses
        ?: overview?.total_expenses
        ?: 0.0

    val averageDailySpend =
        if (durationDays > 0) {
            totalExpenses / durationDays
        } else {
            0.0
        }

    val averageExpense = analytics?.average_expense ?: 0.0
    val expenseCount = analytics?.expense_count ?: 0

    // The analytics response provides totals by category, so the largest
    // category total is the largest spending figure available here.
    val largestCategory = analytics
        ?.categories
        ?.maxByOrNull { it.total }

    val topCategoryPercentage =
        if (totalExpenses > 0.0 && largestCategory != null) {
            (largestCategory.total / totalExpenses) * 100.0
        } else {
            0.0
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "Trip Financial Summary",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "A quick view of this trip's spending.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TripInsightStat(
                    value = "₹${"%.2f".format(totalExpenses)}",
                    label = "Total Spent",
                    modifier = Modifier.weight(1f)
                )

                TripInsightStat(
                    value = "₹${"%.2f".format(averageExpense)}",
                    label = "Avg Expense",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TripInsightStat(
                    value = "₹${"%.2f".format(averageDailySpend)}",
                    label = "Avg / Day",
                    modifier = Modifier.weight(1f)
                )

                TripInsightStat(
                    value = expenseCount.toString(),
                    label = "Expenses",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TripInsightStat(
                    value = largestCategory?.let {
                        "₹${"%.2f".format(it.total)}"
                    } ?: "₹0.00",
                    label = "Top Category",
                    modifier = Modifier.weight(1f)
                )

                TripInsightStat(
                    value = placesCount.toString(),
                    label = "Places",
                    modifier = Modifier.weight(1f)
                )
            }

            largestCategory?.let { category ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Top spending category",
                            fontSize = 13.sp,
                            color =
                                MaterialTheme.colorScheme
                                    .onPrimaryContainer
                                    .copy(alpha = 0.75f)
                        )

                        Text(
                            text = category.category,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text =
                                "₹${"%.2f".format(category.total)} · " +
                                        "${"%.1f".format(topCategoryPercentage)}% of total spending",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartTripSummaryCard(
    trip: TripResponse,
    overview: TripOverviewResponse?,
    analytics: com.example.tripledger.data.remote.TripExpenseAnalyticsResponse?,
    placesCount: Int,
    memoryCount: Int
) {
    val durationDays = overview?.duration_days ?: 0
    val totalSpent = analytics?.total_expenses
        ?: overview?.total_expenses
        ?: 0.0
    val expenseCount = analytics?.expense_count ?: 0
    val averageExpense = analytics?.average_expense ?: 0.0

    val averagePerDay =
        if (durationDays > 0) totalSpent / durationDays else 0.0

    val topCategory = analytics
        ?.categories
        ?.maxByOrNull { it.total }

    val topCategoryPercentage =
        if (totalSpent > 0.0 && topCategory != null) {
            (topCategory.total / totalSpent) * 100.0
        } else {
            0.0
        }

    val status = remember(trip.start_date, trip.end_date) {
        runCatching {
            val today = java.time.LocalDate.now()
            val start = java.time.LocalDate.parse(trip.start_date)
            val end = java.time.LocalDate.parse(trip.end_date)

            when {
                today.isBefore(start) -> "Upcoming"
                today.isAfter(end) -> "Completed"
                else -> "Ongoing"
            }
        }.getOrDefault("Trip")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Smart Trip Summary",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Your trip at a glance",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = 0.75f
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TripInsightStat(
                    value = status,
                    label = "Status",
                    modifier = Modifier.weight(1f)
                )

                TripInsightStat(
                    value = durationDays.toString(),
                    label = if (durationDays == 1) "Day" else "Days",
                    modifier = Modifier.weight(1f)
                )

                TripInsightStat(
                    value = placesCount.toString(),
                    label = "Places",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TripInsightStat(
                    value = memoryCount.toString(),
                    label = if (memoryCount == 1) "Memory" else "Memories",
                    modifier = Modifier.weight(1f)
                )

                TripInsightStat(
                    value = expenseCount.toString(),
                    label = "Expenses",
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(
                        alpha = 0.7f
                    )
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Text(
                        text = "Spending snapshot",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    SmartSummaryRow(
                        label = "Total spent",
                        value = "₹${"%.2f".format(totalSpent)}"
                    )

                    SmartSummaryRow(
                        label = "Average expense",
                        value = "₹${"%.2f".format(averageExpense)}"
                    )

                    SmartSummaryRow(
                        label = "Average per day",
                        value = "₹${"%.2f".format(averagePerDay)}"
                    )

                    if (topCategory != null) {
                        SmartSummaryRow(
                            label = "Top category",
                            value =
                                "${topCategory.category} · " +
                                        "₹${"%.2f".format(topCategory.total)}"
                        )

                        SmartSummaryRow(
                            label = "Top category share",
                            value = "${"%.1f".format(topCategoryPercentage)}%"
                        )
                    } else {
                        SmartSummaryRow(
                            label = "Top category",
                            value = "No expense data"
                        )
                    }
                }
            }

            Text(
                text =
                    if (expenseCount > 0) {
                        "You have recorded $expenseCount " +
                                if (expenseCount == 1) "expense" else "expenses" +
                                        " across $placesCount " +
                                        if (placesCount == 1) "place." else "places."
                    } else {
                        "No expenses have been recorded for this trip yet."
                    },
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                    alpha = 0.8f
                )
            )
        }
    }
}

@Composable
private fun SmartSummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TripInsightStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                    alpha = 0.75f
                )
            )
        }
    }
}

@Composable
private fun TripOverviewLoading() {

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            CircularProgressIndicator(
                modifier = Modifier.size(26.dp)
            )

            Spacer(
                modifier = Modifier.size(12.dp)
            )

            Text(
                text = "Loading trip overview...",
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
private fun TripActivityInsightsCard(
    trip: TripResponse,
    analytics: com.example.tripledger.data.remote.TripExpenseAnalyticsResponse?,
    memoryCount: Int,
    placesCount: Int
) {
    val expenseCount = analytics?.expense_count ?: 0

    // The backend timeline contains:
    // 1 trip-start event + each expense + each memory + 1 trip-end event.
    val timelineEventCount = 2 + expenseCount + memoryCount

    val durationDays = runCatching {
        val start = java.time.LocalDate.parse(trip.start_date)
        val end = java.time.LocalDate.parse(trip.end_date)
        java.time.temporal.ChronoUnit.DAYS.between(start, end).toInt() + 1
    }.getOrDefault(0)

    val activityPerDay =
        if (durationDays > 0) {
            timelineEventCount.toDouble() / durationDays.toDouble()
        } else {
            0.0
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Trip Activity Insights",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Activity represented in your trip timeline",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TripInsightStat(
                    value = timelineEventCount.toString(),
                    label = if (timelineEventCount == 1) "Event" else "Events",
                    modifier = Modifier.weight(1f)
                )

                TripInsightStat(
                    value = expenseCount.toString(),
                    label = "Expenses",
                    modifier = Modifier.weight(1f)
                )

                TripInsightStat(
                    value = memoryCount.toString(),
                    label = if (memoryCount == 1) "Memory" else "Memories",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TripInsightStat(
                    value = placesCount.toString(),
                    label = if (placesCount == 1) "Place" else "Places",
                    modifier = Modifier.weight(1f)
                )

                TripInsightStat(
                    value = durationDays.toString(),
                    label = if (durationDays == 1) "Day" else "Days",
                    modifier = Modifier.weight(1f)
                )

                TripInsightStat(
                    value = String.format("%.1f", activityPerDay),
                    label = "Events / Day",
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Timeline coverage",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${trip.start_date} → ${trip.end_date}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text =
                            "The timeline records the trip start and end, " +
                                    "$expenseCount expense " +
                                    if (expenseCount == 1) "event" else "events" +
                                            ", and $memoryCount memory " +
                                            if (memoryCount == 1) "event." else "events.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                            alpha = 0.75f
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TripCompletionSummaryCard(
    trip: TripResponse,
    analytics: com.example.tripledger.data.remote.TripExpenseAnalyticsResponse?,
    memoryCount: Int,
    placesCount: Int
) {
    val today = java.time.LocalDate.now()

    val startDate = runCatching {
        java.time.LocalDate.parse(trip.start_date)
    }.getOrNull()

    val endDate = runCatching {
        java.time.LocalDate.parse(trip.end_date)
    }.getOrNull()

    val status =
        when {
            endDate != null && today.isAfter(endDate) -> "Completed"
            startDate != null && endDate != null &&
                    !today.isBefore(startDate) &&
                    !today.isAfter(endDate) -> "Ongoing"
            else -> "Upcoming"
        }

    val durationDays =
        if (startDate != null && endDate != null) {
            java.time.temporal.ChronoUnit.DAYS
                .between(startDate, endDate)
                .toInt() + 1
        } else {
            0
        }

    val expenseCount = analytics?.expense_count ?: 0
    val totalSpent = analytics?.total_expenses ?: 0.0
    val averageExpense = analytics?.average_expense ?: 0.0

    val topCategory = analytics?.categories
        ?.maxByOrNull { it.total }

    val statusLabel =
        when (status) {
            "Completed" -> "Trip completed"
            "Ongoing" -> "Trip in progress"
            else -> "Trip planned"
        }

    val statusDescription =
        when (status) {
            "Completed" ->
                "A final snapshot of the memories, places, activity, and spending recorded for this trip."
            "Ongoing" ->
                "A current snapshot of what has been recorded so far."
            else ->
                "A starting snapshot for this upcoming trip."
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Trip Completion Summary",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = statusLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = statusDescription,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                    alpha = 0.78f
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompletionStat(
                    value = durationDays.toString(),
                    label = if (durationDays == 1) "Day" else "Days",
                    modifier = Modifier.weight(1f)
                )

                CompletionStat(
                    value = placesCount.toString(),
                    label = if (placesCount == 1) "Place" else "Places",
                    modifier = Modifier.weight(1f)
                )

                CompletionStat(
                    value = memoryCount.toString(),
                    label = if (memoryCount == 1) "Memory" else "Memories",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompletionStat(
                    value = expenseCount.toString(),
                    label = if (expenseCount == 1) "Expense" else "Expenses",
                    modifier = Modifier.weight(1f)
                )

                CompletionStat(
                    value = "₹${String.format(java.util.Locale.US, "%.2f", totalSpent)}",
                    label = "Total Spent",
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text(
                        text = "At a glance",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    CompletionDetailRow(
                        label = "Average expense",
                        value = "₹${String.format(java.util.Locale.US, "%.2f", averageExpense)}"
                    )

                    CompletionDetailRow(
                        label = "Top spending category",
                        value = topCategory?.category ?: "No expenses"
                    )

                    CompletionDetailRow(
                        label = "Top category spending",
                        value = topCategory?.let {
                            "₹${String.format(java.util.Locale.US, "%.2f", it.total)}"
                        } ?: "₹0.00"
                    )

                    CompletionDetailRow(
                        label = "Trip dates",
                        value = "${trip.start_date} → ${trip.end_date}"
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletionStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 13.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompletionDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TripShareSummaryCard(
    trip: TripResponse,
    overview: TripOverviewResponse?,
    analytics: com.example.tripledger.data.remote.TripExpenseAnalyticsResponse?,
    placesCount: Int,
    memoryCount: Int
) {
    val durationDays = overview?.duration_days ?: 0
    val totalSpent = analytics?.total_expenses
        ?: overview?.total_expenses
        ?: 0.0
    val expenseCount = analytics?.expense_count ?: 0
    val averageExpense = analytics?.average_expense ?: 0.0

    val topCategory = analytics
        ?.categories
        ?.maxByOrNull { it.total }

    val topCategoryText = topCategory?.let {
        "${it.category} • ₹${String.format(java.util.Locale.US, "%.2f", it.total)}"
    } ?: "No spending category"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Trip Share Summary",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "A clean preview of the information ready for future sharing or export.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                    alpha = 0.75f
                )
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = trip.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = trip.destination,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "${trip.start_date} → ${trip.end_date}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                        alpha = 0.75f
                    )
                )
            }

            if (!trip.description.isNullOrBlank()) {
                Text(
                    text = trip.description,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TripShareStat(
                    value = durationDays.toString(),
                    label = if (durationDays == 1) "Day" else "Days",
                    modifier = Modifier.weight(1f)
                )

                TripShareStat(
                    value = placesCount.toString(),
                    label = "Places",
                    modifier = Modifier.weight(1f)
                )

                TripShareStat(
                    value = memoryCount.toString(),
                    label = "Memories",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TripShareStat(
                    value = expenseCount.toString(),
                    label = "Expenses",
                    modifier = Modifier.weight(1f)
                )

                TripShareStat(
                    value = "₹${String.format(java.util.Locale.US, "%.0f", totalSpent)}",
                    label = "Spent",
                    modifier = Modifier.weight(1f)
                )

                TripShareStat(
                    value = "₹${String.format(java.util.Locale.US, "%.0f", averageExpense)}",
                    label = "Avg. Expense",
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(
                        alpha = 0.65f
                    )
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Top spending category",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = topCategoryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Share/export actions will be connected in a later step.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                    alpha = 0.65f
                )
            )
        }
    }
}

@Composable
private fun TripShareStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = 0.65f
            )
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TripOverviewCard(
    overview: TripOverviewResponse
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "Trip Overview",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            TripOverviewStatCard(
                icon = Icons.Default.Schedule,
                value = "${overview.duration_days}",
                label = if (overview.duration_days == 1) {
                    "Day"
                } else {
                    "Days"
                },
                modifier = Modifier.weight(1f)
            )

            TripOverviewStatCard(
                icon = Icons.Default.AccountBalanceWallet,
                value = "₹${"%.2f".format(overview.total_expenses)}",
                label = "Expenses",
                modifier = Modifier.weight(1f)
            )

            TripOverviewStatCard(
                icon = Icons.Default.PhotoLibrary,
                value = "${overview.memory_count}",
                label = if (overview.memory_count == 1) {
                    "Memory"
                } else {
                    "Memories"
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TripOverviewStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme
                    .colorScheme
                    .secondaryContainer
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 14.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .primary,
                modifier = Modifier.size(23.dp)
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            Text(
                text = label,
                fontSize = 12.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSecondaryContainer
                        .copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun TripHeroCard(
    trip: TripResponse,
    coverPhotoUrl: String?,
    isUploadingCover: Boolean,
    onEditClick: () -> Unit,
    onCoverPhotoClick: () -> Unit
) {

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

        if (!coverPhotoUrl.isNullOrBlank()) {

            val imageUrl =
                if (coverPhotoUrl.startsWith("http")) {
                    coverPhotoUrl
                } else {
                    "$API_BASE_URL$coverPhotoUrl"
                }

            AsyncImage(
                model = imageUrl,
                contentDescription = "Trip cover photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                contentScale = ContentScale.Crop
            )
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Card(
                    modifier = Modifier.size(58.dp),
                    shape = MaterialTheme.shapes.large,
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
                                Icons.Default.LocationOn,
                            contentDescription =
                                "Destination",
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .onPrimary,
                            modifier =
                                Modifier.size(30.dp)
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
                        text = trip.destination,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onPrimaryContainer
                                .copy(alpha = 0.78f)
                    )
                }

                IconButton(
                    onClick = onEditClick
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Edit,
                        contentDescription =
                            "Edit trip",
                        tint =
                            MaterialTheme
                                .colorScheme
                                .onPrimaryContainer
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surface
                            .copy(alpha = 0.7f)
                ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(13.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.CalendarMonth,
                        contentDescription =
                            "Trip dates",
                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary,
                        modifier =
                            Modifier.size(21.dp)
                    )

                    Spacer(
                        modifier = Modifier.size(8.dp)
                    )

                    Text(
                        text =
                            "${trip.start_date}  →  ${trip.end_date}",
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
                            .onPrimaryContainer
                            .copy(alpha = 0.8f)
                )
            }

            OutlinedButton(
                onClick = onCoverPhotoClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploadingCover,
                shape = MaterialTheme.shapes.large
            ) {

                if (isUploadingCover) {

                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )

                    Spacer(
                        modifier = Modifier.size(8.dp)
                    )

                    Text("Uploading cover photo...")
                } else {

                    Icon(
                        imageVector =
                            Icons.Default.AddPhotoAlternate,
                        contentDescription = null
                    )

                    Text(
                        text =
                            if (coverPhotoUrl.isNullOrBlank()) {
                                " Add Cover Photo"
                            } else {
                                " Change Cover Photo"
                            },
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingGalleryState() {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
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
                text = "Loading your memories...",
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyGalleryState(
    onAddPhoto: () -> Unit
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
                .padding(28.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier.size(70.dp),
                shape = MaterialTheme.shapes.extraLarge,
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
                            Icons.Default.PhotoLibrary,
                        contentDescription =
                            "Memory gallery",
                        modifier =
                            Modifier.size(36.dp),
                        tint =
                            MaterialTheme
                                .colorScheme
                                .onPrimaryContainer
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "No memories yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text =
                    "Add photos to remember this journey.",
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = onAddPhoto,
                shape = MaterialTheme.shapes.large
            ) {

                Icon(
                    imageVector =
                        Icons.Default.AddPhotoAlternate,
                    contentDescription = null
                )

                Text(" Add First Memory")
            }
        }
    }
}

@Composable
private fun EditTripDialog(
    trip: TripResponse,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        destination: String,
        startDate: String,
        endDate: String,
        description: String?
    ) -> Unit
) {

    var title by remember {
        mutableStateOf(trip.title)
    }

    var destination by remember {
        mutableStateOf(trip.destination)
    }

    var startDate by remember {
        mutableStateOf(trip.start_date)
    }

    var endDate by remember {
        mutableStateOf(trip.end_date)
    }

    var description by remember {
        mutableStateOf(trip.description ?: "")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = "Edit Trip",
                fontWeight = FontWeight.Bold
            )
        },

        text = {

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
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
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                errorMessage?.let {

                    Text(
                        text = it,
                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    when {

                        title.isBlank() -> {
                            errorMessage =
                                "Enter a trip title"
                        }

                        destination.isBlank() -> {
                            errorMessage =
                                "Enter a destination"
                        }

                        startDate.isBlank() -> {
                            errorMessage =
                                "Enter a start date"
                        }

                        endDate.isBlank() -> {
                            errorMessage =
                                "Enter an end date"
                        }

                        else -> {

                            onSave(
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
                        }
                    }
                }
            ) {

                Text(
                    text = "Save Changes",
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
private fun MemoryPhotoViewer(
    photo: TripPhotoResponse,
    tripViewModel: TripViewModel,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var deleting by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {
            if (!deleting) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !deleting,
            dismissOnClickOutside = !deleting
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim)
        ) {
            AsyncImage(
                model = "$API_BASE_URL${photo.photo_url}",
                contentDescription = "Trip memory",
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onDismiss,
                enabled = !deleting,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        MaterialTheme.shapes.large
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close photo"
                )
            }

            if (
                !photo.caption.isNullOrBlank() ||
                !photo.memory_date.isNullOrBlank()
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            start = 16.dp,
                            bottom = 16.dp,
                            end = 80.dp
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.90f
                            )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(4.dp)
                    ) {
                        photo.memory_date?.let { date ->
                            Text(
                                text = "Memory date: $date",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        photo.caption?.let { caption ->
                            if (caption.isNotBlank()) {
                                Text(
                                    text = caption,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = {
                    if (deleting) return@IconButton
                    deleting = true
                    scope.launch {
                        tripViewModel.deleteTripPhoto(photo.id)
                            .onSuccess { onDeleted() }
                            .onFailure { deleting = false }
                    }
                },
                enabled = !deleting,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.95f),
                        MaterialTheme.shapes.large
                    )
            ) {
                if (deleting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete memory",
                        tint = MaterialTheme.colorScheme.onErrorContainer
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
    onPhotoClick: () -> Unit,
    onDeleted: () -> Unit
) {

    val scope = rememberCoroutineScope()

    var visible by remember {
        mutableStateOf(false)
    }

    var deleting by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(photo.id) {
        delay(80)
        visible = true
    }

    AnimatedVisibility(
        visible = visible && !deleting,
        enter =
            fadeIn(
                animationSpec = tween(400)
            ) +
                    scaleIn(
                        initialScale = 0.92f,
                        animationSpec =
                            tween(400)
                    ),
        exit =
            fadeOut(
                animationSpec = tween(220)
            ) +
                    scaleOut(
                        targetScale = 0.85f,
                        animationSpec =
                            tween(220)
                    )
    ) {

        val imageUrl =
            "$API_BASE_URL${photo.photo_url}"

        Card(
            onClick = onPhotoClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 3.dp
                )
        ) {

            Column {

                AsyncImage(
                    model = imageUrl,
                    contentDescription =
                        "Trip memory",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .graphicsLayer {
                            clip = true
                        },
                    contentScale =
                        ContentScale.Crop
                )

                if (!photo.memory_date.isNullOrBlank()) {
                    Text(
                        text = "📅 ${photo.memory_date}",
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        ),
                        fontWeight = FontWeight.Medium,
                        color =
                            MaterialTheme.colorScheme.primary
                    )
                }

                if (!photo.caption.isNullOrBlank()) {
                    Text(
                        text = photo.caption!!,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        ),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        ),
                    horizontalArrangement =
                        Arrangement.End,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {

                            deleting = true

                            scope.launch {

                                val result =
                                    tripViewModel
                                        .deleteTripPhoto(
                                            photo.id
                                        )

                                result
                                    .onSuccess {
                                        onDeleted()
                                    }
                                    .onFailure {
                                        deleting = false
                                    }
                            }
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Delete,
                            contentDescription =
                                "Delete memory",
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                    }
                }
            }
        }
    }
}