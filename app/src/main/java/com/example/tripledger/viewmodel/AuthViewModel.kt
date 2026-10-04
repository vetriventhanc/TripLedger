package com.example.tripledger.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripledger.data.local.TokenManager
import com.example.tripledger.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()

    data class LoginSuccess(
        val token: String
    ) : AuthState()

    data class RegisterSuccess(
        val userId: Int,
        val name: String,
        val email: String
    ) : AuthState()

    data class AutoLoginSuccess(
        val userId: Int,
        val name: String,
        val email: String
    ) : AuthState()

    data object LoggedOut : AuthState()

    data class Error(
        val message: String
    ) : AuthState()
}

class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    private val tokenManager =
        TokenManager(application.applicationContext)

    private val _authState =
        MutableStateFlow<AuthState>(AuthState.Idle)

    val authState: StateFlow<AuthState> =
        _authState.asStateFlow()

    fun login(
        email: String,
        password: String
    ) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error(
                "Please enter email and password"
            )
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.login(
                email = email.trim(),
                password = password
            )

            result
                .onSuccess { tokenResponse ->

                    tokenManager.saveToken(
                        tokenResponse.access_token
                    )

                    _authState.value =
                        AuthState.LoginSuccess(
                            token = tokenResponse.access_token
                        )
                }
                .onFailure { exception ->

                    _authState.value =
                        AuthState.Error(
                            exception.message
                                ?: "Login failed"
                        )
                }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String
    ) {
        if (
            name.isBlank() ||
            email.isBlank() ||
            password.isBlank()
        ) {
            _authState.value = AuthState.Error(
                "Please fill in all fields"
            )
            return
        }

        if (password.length < 8) {
            _authState.value = AuthState.Error(
                "Password must contain at least 8 characters"
            )
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.register(
                name = name.trim(),
                email = email.trim(),
                password = password
            )

            result
                .onSuccess { user ->

                    _authState.value =
                        AuthState.RegisterSuccess(
                            userId = user.id,
                            name = user.name,
                            email = user.email
                        )
                }
                .onFailure { exception ->

                    _authState.value =
                        AuthState.Error(
                            exception.message
                                ?: "Registration failed"
                        )
                }
        }
    }

    fun checkStoredLogin() {

        viewModelScope.launch {

            _authState.value = AuthState.Loading

            val token = tokenManager.token.first()

            if (token.isNullOrBlank()) {
                _authState.value = AuthState.Idle
                return@launch
            }

            val result =
                repository.getCurrentUser(token)

            result
                .onSuccess { user ->

                    _authState.value =
                        AuthState.AutoLoginSuccess(
                            userId = user.id,
                            name = user.name,
                            email = user.email
                        )
                }
                .onFailure {

                    tokenManager.clearToken()

                    _authState.value =
                        AuthState.Idle
                }
        }
    }

    fun logout() {

        viewModelScope.launch {

            tokenManager.clearToken()

            _authState.value =
                AuthState.LoggedOut
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}