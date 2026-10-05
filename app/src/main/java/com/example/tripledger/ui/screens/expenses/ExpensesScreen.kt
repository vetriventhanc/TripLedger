package com.example.tripledger.ui.screens.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripledger.data.remote.TripExpenseResponse
import com.example.tripledger.viewmodel.TripExpenseState
import com.example.tripledger.viewmodel.TripExpenseViewModel

@Composable
fun ExpensesScreen(
    tripId: Int,
    tripExpenseViewModel: TripExpenseViewModel = viewModel()
) {
    val state by tripExpenseViewModel.state.collectAsState()

    var showAddExpenseDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(tripId) {
        tripExpenseViewModel.loadExpenses(tripId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Trip Expenses"
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = {
                showAddExpenseDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Expense")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        when (val currentState = state) {

            TripExpenseState.Idle -> {
                Text("Loading expenses...")
            }

            TripExpenseState.Loading -> {
                CircularProgressIndicator()
            }

            is TripExpenseState.Error -> {
                Text(
                    text = currentState.message
                )
            }

            is TripExpenseState.Success -> {
                if (currentState.expenses.isEmpty()) {
                    Text(
                        text = "No expenses added yet."
                    )
                } else {
                    ExpenseList(
                        expenses = currentState.expenses,
                        onDelete = { expenseId ->
                            tripExpenseViewModel.deleteExpense(
                                tripId = tripId,
                                expenseId = expenseId
                            )
                        }
                    )
                }
            }

            is TripExpenseState.Created -> {
                Text("Expense added successfully.")
            }

            TripExpenseState.Deleted -> {
                Text("Expense deleted.")
            }
        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = {
                showAddExpenseDialog = false
            },
            onSave = { title, amount, category, date, notes ->

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
private fun ExpenseList(
    expenses: List<TripExpenseResponse>,
    onDelete: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = expenses,
            key = { it.id }
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
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = expense.title
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "₹${"%.2f".format(expense.amount)}"
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Category: ${expense.category}"
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Date: ${expense.expense_date}"
            )

            if (!expense.notes.isNullOrBlank()) {
                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Notes: ${expense.notes}"
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedButton(
                onClick = onDelete
            ) {
                Text("Delete")
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
            Text("Add Expense")
        },

        text = {
            Column {

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    label = {
                        Text("Title")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                    },
                    label = {
                        Text("Amount")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                    },
                    label = {
                        Text("Category")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it
                    },
                    label = {
                        Text("Date (YYYY-MM-DD)")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                    },
                    label = {
                        Text("Notes")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = errorMessage!!
                    )
                }
            }
        },

        confirmButton = {
            TextButton(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull()

                    when {
                        title.isBlank() -> {
                            errorMessage = "Enter a title"
                        }

                        parsedAmount == null -> {
                            errorMessage = "Enter a valid amount"
                        }

                        parsedAmount < 0 -> {
                            errorMessage = "Amount cannot be negative"
                        }

                        category.isBlank() -> {
                            errorMessage = "Enter a category"
                        }

                        date.isBlank() -> {
                            errorMessage = "Enter a date"
                        }

                        else -> {
                            onSave(
                                title.trim(),
                                parsedAmount,
                                category.trim(),
                                date.trim(),
                                notes.trim()
                                    .takeIf { it.isNotBlank() }
                            )
                        }
                    }
                }
            ) {
                Text("Save")
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