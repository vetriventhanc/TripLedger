package com.example.tripledger.ui.screens.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.tripledger.data.remote.TripTimelineEvent
import com.example.tripledger.data.remote.TripTimelineResponse
import com.example.tripledger.viewmodel.TripViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


private const val API_BASE_URL = "http://10.0.2.2:8000"


@Composable
fun TripTimelineScreen(
    tripId: Int,
    tripViewModel: TripViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()

    var timeline by remember {
        mutableStateOf<TripTimelineResponse?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var showContent by remember {
        mutableStateOf(false)
    }


    fun loadTimeline() {
        isLoading = true
        errorMessage = null

        scope.launch {

            tripViewModel
                .getTripTimeline(tripId)
                .onSuccess { result ->

                    timeline = result
                    isLoading = false
                }
                .onFailure { exception ->

                    errorMessage =
                        exception.message
                            ?: "Failed to load trip timeline"

                    isLoading = false
                }
        }
    }


    LaunchedEffect(tripId) {

        showContent = false

        loadTimeline()

        delay(100)

        showContent = true
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 18.dp,
                vertical = 16.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        item {

            AnimatedVisibility(
                visible = showContent,
                enter =
                    fadeIn(
                        animationSpec = tween(450)
                    ) +
                            slideInVertically(
                                initialOffsetY = { -30 },
                                animationSpec = tween(450)
                            )
            ) {

                TimelineHeader(
                    timeline = timeline
                )
            }
        }


        item {

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
                                initialOffsetY = { 25 },
                                animationSpec = tween(
                                    durationMillis = 450,
                                    delayMillis = 100
                                )
                            )
            ) {

                when {

                    isLoading -> {
                        LoadingTimelineCard()
                    }

                    errorMessage != null -> {
                        TimelineErrorCard(
                            message = errorMessage!!,
                            onRetry = {
                                loadTimeline()
                            }
                        )
                    }

                    timeline != null -> {

                        if (timeline!!.events.isEmpty()) {

                            EmptyTimelineCard()

                        } else {

                            TimelineEvents(
                                events = timeline!!.events
                            )
                        }
                    }
                }
            }
        }


        item {

            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}


@Composable
private fun TimelineHeader(
    timeline: TripTimelineResponse?
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
        elevation =
            CardDefaults.cardElevation(
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
                        MaterialTheme
                            .colorScheme
                            .primary
                ),
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                    )
            ) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription =
                            "Trip timeline",
                        tint =
                            MaterialTheme
                                .colorScheme
                                .onPrimary,
                        modifier =
                            Modifier.size(31.dp)
                    )
                }
            }


            Spacer(
                modifier = Modifier.width(14.dp)
            )


            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Trip Timeline",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )


                if (timeline != null) {

                    Text(
                        text = timeline.destination,
                        fontSize = 14.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onPrimaryContainer
                                .copy(alpha = 0.75f)
                    )

                } else {

                    Text(
                        text =
                            "Your journey, day by day",
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
}


@Composable
private fun TimelineEvents(
    events: List<TripTimelineEvent>
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        events.forEachIndexed { index, event ->

            TimelineEventItem(
                event = event,
                isLast = index == events.lastIndex
            )
        }
    }
}


@Composable
private fun TimelineEventItem(
    event: TripTimelineEvent,
    isLast: Boolean
) {

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            TimelineIcon(
                eventType = event.type
            )


            if (!isLast) {

                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(82.dp)
                        .background(
                            MaterialTheme
                                .colorScheme
                                .primary
                                .copy(alpha = 0.25f)
                        )
                )
            }
        }


        Spacer(
            modifier = Modifier.width(12.dp)
        )


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = if (isLast) 0.dp else 14.dp
                ),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor =
                    when (event.type) {

                        "trip_start",
                        "trip_end" ->
                            MaterialTheme
                                .colorScheme
                                .primaryContainer

                        "expense" ->
                            MaterialTheme
                                .colorScheme
                                .secondaryContainer

                        "memory" ->
                            MaterialTheme
                                .colorScheme
                                .surface

                        else ->
                            MaterialTheme
                                .colorScheme
                                .surface
                    }
            ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 1.dp
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                verticalArrangement =
                    Arrangement.spacedBy(7.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = event.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        Text(
                            text = formatTimelineDate(
                                event.date
                            ),
                            fontSize = 12.sp,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }


                    EventTypeLabel(
                        eventType = event.type
                    )
                }


                if (!event.description.isNullOrBlank()) {

                    Text(
                        text = event.description,
                        fontSize = 14.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }


                if (event.amount != null) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default
                                    .AccountBalanceWallet,
                            contentDescription = null,
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .primary,
                            modifier =
                                Modifier.size(18.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            text =
                                "₹${
                                    String.format(
                                        Locale.US,
                                        "%.2f",
                                        event.amount
                                    )
                                }",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }


                if (
                    event.type == "memory" &&
                    !event.photo_url.isNullOrBlank()
                ) {

                    MemoryImage(
                        photoUrl = event.photo_url
                    )
                }
            }
        }
    }
}


@Composable
private fun TimelineIcon(
    eventType: String
) {

    val backgroundColor =
        when (eventType) {

            "trip_start",
            "trip_end" ->
                MaterialTheme
                    .colorScheme
                    .primary

            "expense" ->
                MaterialTheme
                    .colorScheme
                    .secondary

            "memory" ->
                MaterialTheme
                    .colorScheme
                    .tertiary

            else ->
                MaterialTheme
                    .colorScheme
                    .primary
        }


    Card(
        modifier = Modifier.size(42.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {

            Icon(
                imageVector =
                    when (eventType) {

                        "trip_start" ->
                            Icons.Default.LocationOn

                        "expense" ->
                            Icons.Default
                                .AccountBalanceWallet

                        "memory" ->
                            Icons.Default.Photo

                        "trip_end" ->
                            Icons.Default.CheckCircle

                        else ->
                            Icons.Default.Schedule
                    },
                contentDescription = null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .onPrimary,
                modifier =
                    Modifier.size(23.dp)
            )
        }
    }
}


@Composable
private fun EventTypeLabel(
    eventType: String
) {

    val label =
        when (eventType) {

            "trip_start" -> "START"

            "expense" -> "EXPENSE"

            "memory" -> "MEMORY"

            "trip_end" -> "END"

            else -> "EVENT"
        }


    Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color =
            MaterialTheme
                .colorScheme
                .primary
    )
}


@Composable
private fun MemoryImage(
    photoUrl: String
) {

    val imageUrl =
        if (photoUrl.startsWith("http")) {
            photoUrl
        } else {
            "$API_BASE_URL$photoUrl"
        }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        shape = MaterialTheme.shapes.large,
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {

        AsyncImage(
            model = imageUrl,
            contentDescription =
                "Trip memory",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}


@Composable
private fun LoadingTimelineCard() {

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            CircularProgressIndicator(
                modifier = Modifier.size(28.dp)
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Text(
                text = "Loading trip timeline...",
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
private fun TimelineErrorCard(
    message: String,
    onRetry: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme
                    .colorScheme
                    .errorContainer
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
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "Unable to load timeline",
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
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )

                Spacer(
                    modifier = Modifier.width(6.dp)
                )

                Text(
                    text = "Retry"
                )
            }
        }
    }
}


@Composable
private fun EmptyTimelineCard() {

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
                .padding(24.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Text(
                text = "No timeline events yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text =
                    "Your trip activities will appear here.",
                fontSize = 14.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


private fun formatTimelineDate(
    date: String
): String {

    return try {

        val parts = date.split("-")

        if (parts.size == 3) {

            val year = parts[0]
            val month = parts[1]
            val day = parts[2]

            "$day/$month/$year"

        } else {

            date
        }

    } catch (
        exception: Exception
    ) {

        date
    }
}