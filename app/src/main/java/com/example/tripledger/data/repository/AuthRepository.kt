package com.example.tripledger.data.repository

import com.example.tripledger.data.remote.LoginRequest
import com.example.tripledger.data.remote.RegisterRequest
import com.example.tripledger.data.remote.RetrofitClient
import com.example.tripledger.data.remote.TokenResponse
import com.example.tripledger.data.remote.UserResponse
import retrofit2.Response

class AuthRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): Result<UserResponse> {
        return try {
            val response = apiService.register(
                RegisterRequest(
                    name = name,
                    email = email,
                    password = password
                )
            )

            if (response.isSuccessful) {
                val user = response.body()

                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(
                        Exception("Empty response from server")
                    )
                }
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Registration failed"
                    )
                )
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<TokenResponse> {
        return try {
            val response = apiService.login(
                LoginRequest(
                    email = email,
                    password = password
                )
            )

            if (response.isSuccessful) {
                val token = response.body()

                if (token != null) {
                    Result.success(token)
                } else {
                    Result.failure(
                        Exception("Empty response from server")
                    )
                }
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Login failed"
                    )
                )
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getCurrentUser(
        token: String
    ): Result<UserResponse> {
        return try {
            val response = RetrofitClient
                .apiService
                .getCurrentUser(
                    authorization = "Bearer $token"
                )

            if (response.isSuccessful) {
                val user = response.body()

                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(
                        Exception("Empty response from server")
                    )
                }
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to get current user"
                    )
                )
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}