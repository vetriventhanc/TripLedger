package com.example.tripledger.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripledger.data.local.TokenManager
import com.example.tripledger.data.remote.ApiService
import com.example.tripledger.data.remote.TripExpenseCreateRequest
import com.example.tripledger.data.remote.TripExpenseResponse
import com.example.tripledger.data.repository.TripExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class TripExpenseState {
    data object Idle : TripExpenseState()

    data object Loading : TripExpenseState()

    data class Success(
        val expenses: List<TripExpenseResponse>
    ) : TripExpenseState()

    data class Created(
        val expense: TripExpenseResponse
    ) : TripExpenseState()

    data object Deleted : TripExpenseState()

    data class Error(
        val message: String
    ) : TripExpenseState()
}

class TripExpenseViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager =
        TokenManager(application.applicationContext)

    private val apiService: ApiService =
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(ApiService::class.java)

    private val repository =
        TripExpenseRepository(
            apiService = apiService
        )

    private val _state =
        MutableStateFlow<TripExpenseState>(
            TripExpenseState.Idle
        )

    val state =
        _state.asStateFlow()

    fun loadExpenses(tripId: Int) {
        viewModelScope.launch {
            _state.value =
                TripExpenseState.Loading

            val token =
                tokenManager.token.first()

            if (token.isNullOrBlank()) {
                _state.value =
                    TripExpenseState.Error(
                        "You are not logged in"
                    )
                return@launch
            }

            repository.getTripExpenses(
                token = token,
                tripId = tripId
            ).onSuccess { expenses ->

                _state.value =
                    TripExpenseState.Success(
                        expenses = expenses
                    )

            }.onFailure { error ->

                _state.value =
                    TripExpenseState.Error(
                        error.message
                            ?: "Failed to load expenses"
                    )
            }
        }
    }

    fun createExpense(
        tripId: Int,
        title: String,
        amount: Double,
        category: String,
        expenseDate: String,
        notes: String?
    ) {
        viewModelScope.launch {

            _state.value =
                TripExpenseState.Loading

            val token =
                tokenManager.token.first()

            if (token.isNullOrBlank()) {
                _state.value =
                    TripExpenseState.Error(
                        "You are not logged in"
                    )
                return@launch
            }

            val request =
                TripExpenseCreateRequest(
                    title = title,
                    amount = amount,
                    category = category,
                    expense_date = expenseDate,
                    notes = notes
                )

            repository.createExpense(
                token = token,
                tripId = tripId,
                request = request
            ).onSuccess { expense ->

                _state.value =
                    TripExpenseState.Created(
                        expense = expense
                    )

                loadExpenses(tripId)

            }.onFailure { error ->

                _state.value =
                    TripExpenseState.Error(
                        error.message
                            ?: "Failed to create expense"
                    )
            }
        }
    }

    fun deleteExpense(
        tripId: Int,
        expenseId: Int
    ) {
        viewModelScope.launch {

            _state.value =
                TripExpenseState.Loading

            val token =
                tokenManager.token.first()

            if (token.isNullOrBlank()) {
                _state.value =
                    TripExpenseState.Error(
                        "You are not logged in"
                    )
                return@launch
            }

            repository.deleteExpense(
                token = token,
                expenseId = expenseId
            ).onSuccess {

                _state.value =
                    TripExpenseState.Deleted

                loadExpenses(tripId)

            }.onFailure { error ->

                _state.value =
                    TripExpenseState.Error(
                        error.message
                            ?: "Failed to delete expense"
                    )
            }
        }
    }
}