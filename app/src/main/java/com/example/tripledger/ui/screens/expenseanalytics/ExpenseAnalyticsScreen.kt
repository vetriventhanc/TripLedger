package com.example.tripledger.ui.screens.expenseanalytics

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripledger.data.remote.TripExpenseAnalyticsResponse
import com.example.tripledger.viewmodel.TripViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


@Composable
fun ExpenseAnalyticsScreen(
    tripId: Int,
    tripViewModel: TripViewModel = viewModel()
) {

    var analytics by remember {
        mutableStateOf<TripExpenseAnalyticsResponse?>(null)
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

    val scope = rememberCoroutineScope()


    fun loadAnalytics() {

        isLoading = true
        errorMessage = null

        scope.launch {

            tripViewModel
                .getExpenseAnalytics(tripId)
                .onSuccess { result ->

                    analytics = result
                    isLoading = false
                }
                .onFailure { exception ->

                    errorMessage =
                        exception.message
                            ?: "Failed to load expense analytics"

                    isLoading = false
                }
        }
    }


    LaunchedEffect(tripId) {

        showContent = false

        loadAnalytics()

        delay(100)

        showContent = true
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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

                AnalyticsHeader()
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
                        LoadingAnalyticsCard()
                    }

                    errorMessage != null -> {
                        AnalyticsErrorCard(
                            message = errorMessage!!,
                            onRetry = {
                                loadAnalytics()
                            }
                        )
                    }

                    analytics != null -> {
                        AnalyticsContent(
                            analytics = analytics!!
                        )
                    }
                }
            }
        }


        item {

            Spacer(
                modifier = Modifier.height(30.dp)
            )
        }
    }
}


@Composable
private fun AnalyticsHeader() {

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

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription =
                            "Expense analytics",
                        tint =
                            MaterialTheme
                                .colorScheme
                                .onPrimary,
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
                    text = "Expense Analytics",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(3.dp)
                )

                Text(
                    text =
                        "Understand where your trip money goes",
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
private fun AnalyticsContent(
    analytics: TripExpenseAnalyticsResponse
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        AnalyticsSummaryCards(
            analytics = analytics
        )

        CategoryBreakdownCard(
            analytics = analytics
        )

        DailySpendingCard(
            analytics = analytics
        )
    }
}


@Composable
private fun AnalyticsSummaryCards(
    analytics: TripExpenseAnalyticsResponse
) {

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "Summary",
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            AnalyticsStatCard(
                icon =
                    Icons.Default.AccountBalanceWallet,
                value =
                    "₹${
                        String.format(
                            Locale.US,
                            "%.2f",
                            analytics.total_expenses
                        )
                    }",
                label = "Total",
                modifier = Modifier.weight(1f)
            )


            AnalyticsStatCard(
                icon = Icons.Default.ReceiptLong,
                value =
                    "${analytics.expense_count}",
                label = "Expenses",
                modifier = Modifier.weight(1f)
            )


            AnalyticsStatCard(
                icon = Icons.Default.Analytics,
                value =
                    "₹${
                        String.format(
                            Locale.US,
                            "%.2f",
                            analytics.average_expense
                        )
                    }",
                label = "Average",
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
private fun AnalyticsStatCard(
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
                    horizontal = 8.dp,
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
                fontSize = 15.sp,
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
private fun CategoryBreakdownCard(
    analytics: TripExpenseAnalyticsResponse
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme
                    .colorScheme
                    .surface
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text(
                    text = "Spending by Category",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            if (analytics.categories.isEmpty()) {

                Text(
                    text =
                        "No category spending available.",
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

            } else {

                analytics.categories.forEach { category ->

                    CategoryRow(
                        category = category.category,
                        total = category.total,
                        overallTotal =
                            analytics.total_expenses
                    )
                }
            }
        }
    }
}


@Composable
private fun CategoryRow(
    category: String,
    total: Double,
    overallTotal: Double
) {

    val percentage =
        if (overallTotal > 0) {
            total / overallTotal * 100.0
        } else {
            0.0
        }


    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(5.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = category,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium
            )

            Text(
                text =
                    "₹${
                        String.format(
                            Locale.US,
                            "%.2f",
                            total
                        )
                    }",
                fontWeight = FontWeight.Bold
            )
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Card(
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.small,
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

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                )
            }


            Spacer(
                modifier = Modifier.size(8.dp)
            )


            Text(
                text =
                    String.format(
                        Locale.US,
                        "%.1f%%",
                        percentage
                    ),
                fontSize = 12.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


@Composable
private fun DailySpendingCard(
    analytics: TripExpenseAnalyticsResponse
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme
                    .colorScheme
                    .surface
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )

                Spacer(
                    modifier = Modifier.size(8.dp)
                )

                Text(
                    text = "Daily Spending",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
            }


            if (analytics.daily_spending.isEmpty()) {

                Text(
                    text =
                        "No daily spending available.",
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

            } else {

                analytics.daily_spending.forEach { daily ->

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text = daily.date,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text =
                                "₹${
                                    String.format(
                                        Locale.US,
                                        "%.2f",
                                        daily.total
                                    )
                                }",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun LoadingAnalyticsCard() {

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
                .padding(22.dp),
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
                text =
                    "Loading expense analytics...",
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
private fun AnalyticsErrorCard(
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

            Text(
                text =
                    "Unable to load analytics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onErrorContainer
            )

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
                    modifier = Modifier.size(6.dp)
                )

                Text(
                    text = "Retry"
                )
            }
        }
    }
}