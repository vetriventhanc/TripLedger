package com.example.tripledger.data.repository

import com.example.tripledger.data.remote.ApiService
import com.example.tripledger.data.remote.TripExpenseCreateRequest
import com.example.tripledger.data.remote.TripExpenseResponse

class TripExpenseRepository(
    private val apiService: ApiService
) {

    suspend fun createExpense(
        token: String,
        tripId: Int,
        request: TripExpenseCreateRequest
    ): Result<TripExpenseResponse> {

        return try {
            val response = apiService.createExpense(
                authorization = "Bearer $token",
                tripId = tripId,
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to create expense"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTripExpenses(
        token: String,
        tripId: Int
    ): Result<List<TripExpenseResponse>> {

        return try {
            val response = apiService.getTripExpenses(
                authorization = "Bearer $token",
                tripId = tripId
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to load expenses"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExpense(
        token: String,
        tripId: Int,
        expenseId: Int
    ): Result<TripExpenseResponse> {

        return try {
            val response = apiService.getExpense(
                authorization = "Bearer $token",
                tripId = tripId,
                expenseId = expenseId
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to load expense"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(
        token: String,
        expenseId: Int
    ): Result<Unit> {

        return try {
            val response = apiService.deleteExpense(
                authorization = "Bearer $token",
                expenseId = expenseId
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to delete expense"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}