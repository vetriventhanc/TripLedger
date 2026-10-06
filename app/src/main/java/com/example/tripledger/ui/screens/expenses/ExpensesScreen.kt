package com.example.tripledger.ui.screens.expenses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripledger.data.remote.TripExpenseResponse
import com.example.tripledger.viewmodel.TripExpenseState
import com.example.tripledger.viewmodel.TripExpenseViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun ExpensesScreen(
    tripId: Int,
    tripExpenseViewModel: TripExpenseViewModel = viewModel(),
    onAnalyticsClick: () -> Unit
) {
    val state by tripExpenseViewModel.state.collectAsState()

    var showAddExpenseDialog by remember {
        mutableStateOf(false)
    }

    var showContent by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(tripId) {
        showContent = false
        tripExpenseViewModel.loadExpenses(tripId)

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
            ExpensesHeader()
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

            when (val currentState = state) {

                TripExpenseState.Idle -> {
                    LoadingExpensesCard()
                }

                TripExpenseState.Loading -> {
                    LoadingExpensesCard()
                }

                is TripExpenseState.Error -> {
                    ErrorExpensesCard(
                        message = currentState.message,
                        onRetry = {
                            tripExpenseViewModel.loadExpenses(
                                tripId
                            )
                        }
                    )
                }

                is TripExpenseState.Success -> {

                    ExpenseContent(
                        expenses = currentState.expenses,
                        onAddExpense = {
                            showAddExpenseDialog = true
                        },
                        onAnalyticsClick = onAnalyticsClick,
                        onDelete = { expenseId ->
                            tripExpenseViewModel.deleteExpense(
                                tripId = tripId,
                                expenseId = expenseId
                            )
                        }
                    )
                }

                is TripExpenseState.Created -> {
                    LoadingExpensesCard()
                }

                TripExpenseState.Deleted -> {
                    LoadingExpensesCard()
                }
            }
        }
    }

    if (showAddExpenseDialog) {

        AddExpenseDialog(
            onDismiss = {
                showAddExpenseDialog = false
            },
            onSave = {
                    title,
                    amount,
                    category,
                    date,
                    notes ->

                tripExpenseViewModel.createExpense(
                    tripId = tripId,
                    title = title,
                    amount = amount,
                    category = category,
                    expenseDate = date,
                    notes = notes
                )

                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
private fun ExpensesHeader() {

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
                            Icons.Default.AccountBalanceWallet,
                        contentDescription =
                            "Trip expenses",
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
                    text = "Trip Expenses",
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
                        "Track every expense from your journey",
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
private fun ExpenseContent(
    expenses: List<TripExpenseResponse>,
    onAddExpense: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onDelete: (Int) -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        AnimatedVisibility(
            visible = expenses.isNotEmpty(),
            enter =
                fadeIn(
                    animationSpec = tween(400)
                ) +
                        scaleIn(
                            initialScale = 0.95f,
                            animationSpec = tween(400)
                        )
        ) {

            ExpenseSummaryCard(
                expenses = expenses
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Button(
                onClick = onAddExpense,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large,
                contentPadding = androidx.compose.foundation.layout
                    .PaddingValues(vertical = 13.dp)
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ReceiptLong,
                    contentDescription = null
                )

                Text(
                    text = "  Add Expense",
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onAnalyticsClick,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large,
                contentPadding = androidx.compose.foundation.layout
                    .PaddingValues(vertical = 13.dp)
            ) {

                Icon(
                    imageVector =
                        Icons.Default.TravelExplore,
                    contentDescription =
                        "Expense analytics"
                )

                Text(
                    text = "  Analytics",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (expenses.isEmpty()) {

            EmptyExpensesCard(
                onAddExpense = onAddExpense
            )

        } else {

            ExpenseList(
                expenses = expenses,
                onDelete = onDelete
            )
        }
    }
}

@Composable
private fun ExpenseSummaryCard(
    expenses: List<TripExpenseResponse>
) {

    val total = expenses.sumOf {
        it.amount
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme
                    .colorScheme
                    .secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                imageVector =
                    Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                tint =
                    MaterialTheme
                        .colorScheme
                        .onSecondaryContainer
            )

            Spacer(
                modifier = Modifier.size(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "Total Spent",
                    fontSize = 13.sp,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSecondaryContainer
                            .copy(alpha = 0.75f)
                )

                Text(
                    text = formatCurrency(total),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSecondaryContainer
                )
            }

            Column(
                horizontalAlignment =
                    Alignment.End
            ) {

                Text(
                    text = "${expenses.size}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSecondaryContainer
                )

                Text(
                    text =
                        if (expenses.size == 1)
                            "expense"
                        else
                            "expenses",
                    fontSize = 12.sp,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSecondaryContainer
                            .copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ExpenseList(
    expenses: List<TripExpenseResponse>,
    onDelete: (Int) -> Unit
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            androidx.compose.foundation.layout
                .PaddingValues(bottom = 100.dp),
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        items(
            items = expenses,
            key = {
                it.id
            }
        ) { expense ->

            ExpenseCard(
                expense = expense,
                onDelete = {
                    onDelete(expense.id)
                }
            )
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: TripExpenseResponse,
    onDelete: () -> Unit
) {

    var visible by remember {
        mutableStateOf(false)
    }

    var deleting by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(expense.id) {
        delay(60)
        visible = true
    }

    AnimatedVisibility(
        visible = visible && !deleting,
        enter =
            fadeIn(
                animationSpec = tween(350)
            ) +
                    scaleIn(
                        initialScale = 0.94f,
                        animationSpec = tween(350)
                    ),
        exit =
            fadeOut(
                animationSpec = tween(220)
            ) +
                    scaleOut(
                        targetScale = 0.85f,
                        animationSpec = tween(220)
                    )
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(9.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Card(
                        modifier = Modifier.size(46.dp),
                        shape =
                            MaterialTheme.shapes.large,
                        colors =
                            CardDefaults.cardColors(
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
                                    Icons.Default.ReceiptLong,
                                contentDescription =
                                    null,
                                modifier =
                                    Modifier.size(24.dp),
                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimaryContainer
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
                            text = expense.title,
                            fontSize = 17.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text = expense.category,
                            fontSize = 13.sp,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    Text(
                        text =
                            formatCurrency(
                                expense.amount
                            ),
                        fontSize = 18.sp,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.CalendarMonth,
                        contentDescription =
                            null,
                        modifier =
                            Modifier.size(17.dp),
                        tint =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.size(5.dp)
                    )

                    Text(
                        text = expense.expense_date,
                        fontSize = 13.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            deleting = true
                            onDelete()
                        }
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Delete,
                            contentDescription =
                                "Delete expense",
                            tint =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )
                    }
                }

                if (!expense.notes.isNullOrBlank()) {

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            MaterialTheme.shapes.large,
                        colors =
                            CardDefaults.cardColors(
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

                        Text(
                            text =
                                expense.notes!!,
                            modifier =
                                Modifier.padding(11.dp),
                            fontSize = 13.sp,
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
}

@Composable
private fun EmptyExpensesCard(
    onAddExpense: () -> Unit
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
                shape =
                    MaterialTheme.shapes.extraLarge,
                colors =
                    CardDefaults.cardColors(
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
                            Icons.Default.AccountBalanceWallet,
                        contentDescription =
                            "No expenses",
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
                text = "No expenses yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text =
                    "Keep track of your spending during this trip.",
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = onAddExpense,
                shape = MaterialTheme.shapes.large
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ReceiptLong,
                    contentDescription = null
                )

                Text("  Add First Expense")
            }
        }
    }
}

@Composable
private fun LoadingExpensesCard() {

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
                .padding(32.dp),
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
                text = "Loading expenses...",
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorExpensesCard(
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
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = "Couldn't load expenses",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onErrorContainer
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = message,
                color =
                    MaterialTheme
                        .colorScheme
                        .onErrorContainer
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            OutlinedButton(
                onClick = onRetry
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Refresh,
                    contentDescription = null
                )

                Text("  Retry")
            }
        }
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        amount: Double,
        category: String,
        date: String,
        notes: String?
    ) -> Unit
) {

    var title by remember {
        mutableStateOf("")
    }

    var amount by remember {
        mutableStateOf("")
    }

    var category by remember {
        mutableStateOf("")
    }

    var date by remember {
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
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        Icons.Default.ReceiptLong,
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
                    text = "Add Expense",
                    fontWeight = FontWeight.Bold
                )
            }
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
                        Text("Title")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                    },
                    label = {
                        Text("Amount")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                    },
                    label = {
                        Text("Category")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector =
                                Icons.Default.Category,
                            contentDescription = null
                        )
                    }
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                    },
                    label = {
                        Text("Date (YYYY-MM-DD)")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector =
                                Icons.Default.CalendarMonth,
                            contentDescription = null
                        )
                    }
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                    },
                    label = {
                        Text("Notes")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                AnimatedVisibility(
                    visible =
                        errorMessage != null,
                    enter =
                        fadeIn(
                            animationSpec =
                                tween(250)
                        ) +
                                expandVertically(
                                    animationSpec =
                                        tween(250)
                                ),
                    exit =
                        fadeOut(
                            animationSpec =
                                tween(200)
                        ) +
                                shrinkVertically(
                                    animationSpec =
                                        tween(200)
                                )
                ) {

                    errorMessage?.let {
                        Text(
                            text = it,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error,
                            fontWeight =
                                FontWeight.Medium
                        )
                    }
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {

                    val parsedAmount =
                        amount.toDoubleOrNull()

                    when {

                        title.isBlank() -> {
                            errorMessage =
                                "Enter a title"
                        }

                        parsedAmount == null -> {
                            errorMessage =
                                "Enter a valid amount"
                        }

                        parsedAmount < 0 -> {
                            errorMessage =
                                "Amount cannot be negative"
                        }

                        category.isBlank() -> {
                            errorMessage =
                                "Enter a category"
                        }

                        date.isBlank() -> {
                            errorMessage =
                                "Enter a date"
                        }

                        else -> {

                            onSave(
                                title.trim(),
                                parsedAmount,
                                category.trim(),
                                date.trim(),
                                notes.trim()
                                    .takeIf {
                                        it.isNotBlank()
                                    }
                            )
                        }
                    }
                }
            ) {

                Text(
                    text = "Save Expense",
                    fontWeight =
                        FontWeight.SemiBold
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

private fun formatCurrency(
    amount: Double
): String {

    return String.format(
        Locale.US,
        "₹%,.2f",
        amount
    )
}