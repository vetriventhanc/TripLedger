package com.example.tripledger.data.remote

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String
)

data class TokenResponse(
    val access_token: String,
    val token_type: String
)