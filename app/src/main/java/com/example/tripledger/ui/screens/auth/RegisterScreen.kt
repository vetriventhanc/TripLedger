package com.example.tripledger.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripledger.viewmodel.AuthState
import com.example.tripledger.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {

    var name by remember {
        mutableStateOf("")
    }

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var passwordVisible by remember {
        mutableStateOf(false)
    }

    var confirmPasswordVisible by remember {
        mutableStateOf(false)
    }

    var showContent by remember {
        mutableStateOf(false)
    }

    var localError by remember {
        mutableStateOf<String?>(null)
    }

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    LaunchedEffect(authState) {

        if (authState is AuthState.RegisterSuccess) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 24.dp,
                vertical = 24.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(500)
                ) +
                        slideInVertically(
                            initialOffsetY = { -50 },
                            animationSpec = tween(500)
                        )
        ) {

            RegisterHeader()
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 500,
                        delayMillis = 120
                    )
                ) +
                        scaleIn(
                            initialScale = 0.94f,
                            animationSpec = tween(
                                durationMillis = 500,
                                delayMillis = 120
                            )
                        )
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape =
                    MaterialTheme.shapes.extraLarge,
                elevation =
                    CardDefaults.cardElevation(
                        defaultElevation = 3.dp
                    )
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(13.dp)
                ) {

                    Text(
                        text = "Create account",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text =
                            "Start organizing your journeys with TripLedger.",
                        fontSize = 13.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            localError = null
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text("Name")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Default.Person,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        enabled =
                            authState !is AuthState.Loading,
                        shape =
                            MaterialTheme.shapes.large
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            localError = null
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text("Email")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Default.Email,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Email
                            ),
                        enabled =
                            authState !is AuthState.Loading,
                        shape =
                            MaterialTheme.shapes.large
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localError = null
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text("Password")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    passwordVisible =
                                        !passwordVisible
                                }
                            ) {
                                Icon(
                                    imageVector =
                                        if (passwordVisible)
                                            Icons.Default.VisibilityOff
                                        else
                                            Icons.Default.Visibility,
                                    contentDescription =
                                        if (passwordVisible)
                                            "Hide password"
                                        else
                                            "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation =
                            if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Password
                            ),
                        enabled =
                            authState !is AuthState.Loading,
                        shape =
                            MaterialTheme.shapes.large
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            localError = null
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text("Confirm password")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector =
                                    Icons.Default.Lock,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    confirmPasswordVisible =
                                        !confirmPasswordVisible
                                }
                            ) {
                                Icon(
                                    imageVector =
                                        if (confirmPasswordVisible)
                                            Icons.Default.VisibilityOff
                                        else
                                            Icons.Default.Visibility,
                                    contentDescription =
                                        if (confirmPasswordVisible)
                                            "Hide password"
                                        else
                                            "Show password"
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation =
                            if (confirmPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    KeyboardType.Password
                            ),
                        enabled =
                            authState !is AuthState.Loading,
                        shape =
                            MaterialTheme.shapes.large
                    )

                    AnimatedVisibility(
                        visible =
                            localError != null ||
                                    authState is AuthState.Error,
                        enter =
                            fadeIn(
                                animationSpec =
                                    tween(250)
                            ) +
                                    slideInVertically(
                                        initialOffsetY = {
                                            -15
                                        },
                                        animationSpec =
                                            tween(250)
                                    )
                    ) {

                        val errorText =
                            localError
                                ?: if (
                                    authState is AuthState.Error
                                ) {
                                    (authState as AuthState.Error)
                                        .message
                                } else {
                                    null
                                }

                        if (errorText != null) {

                            Card(
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape =
                                    MaterialTheme
                                        .shapes
                                        .large,
                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            MaterialTheme
                                                .colorScheme
                                                .errorContainer
                                    )
                            ) {

                                Text(
                                    text = errorText,
                                    modifier =
                                        Modifier.padding(12.dp),
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onErrorContainer,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {

                            when {

                                name.isBlank() -> {
                                    localError =
                                        "Enter your name"
                                }

                                email.isBlank() -> {
                                    localError =
                                        "Enter your email"
                                }

                                password.isBlank() -> {
                                    localError =
                                        "Enter a password"
                                }

                                password.length < 6 -> {
                                    localError =
                                        "Password must be at least 6 characters"
                                }

                                confirmPassword.isBlank() -> {
                                    localError =
                                        "Confirm your password"
                                }

                                password != confirmPassword -> {
                                    localError =
                                        "Passwords do not match"
                                }

                                else -> {

                                    localError = null

                                    authViewModel.register(
                                        name = name.trim(),
                                        email = email.trim(),
                                        password = password
                                    )
                                }
                            }
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        enabled =
                            authState !is AuthState.Loading,
                        shape =
                            MaterialTheme.shapes.large,
                        contentPadding =
                            androidx.compose.foundation.layout
                                .PaddingValues(
                                    vertical = 14.dp
                                )
                    ) {

                        if (authState is AuthState.Loading) {

                            CircularProgressIndicator(
                                modifier =
                                    Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimary
                            )

                            Spacer(
                                modifier =
                                    Modifier.size(8.dp)
                            )

                            Text("Creating account...")

                        } else {

                            Icon(
                                imageVector =
                                    Icons.Default.HowToReg,
                                contentDescription =
                                    null
                            )

                            Spacer(
                                modifier =
                                    Modifier.size(8.dp)
                            )

                            Text(
                                text = "Create Account",
                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        AnimatedVisibility(
            visible = showContent,
            enter =
                fadeIn(
                    animationSpec = tween(
                        durationMillis = 450,
                        delayMillis = 250
                    )
                ) +
                        slideInVertically(
                            initialOffsetY = { 25 },
                            animationSpec = tween(
                                durationMillis = 450,
                                delayMillis = 250
                            )
                        )
        ) {

            TextButton(
                onClick = onLoginClick,
                enabled =
                    authState !is AuthState.Loading
            ) {

                Text(
                    text =
                        "Already have an account? Login",
                    fontWeight =
                        FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun RegisterHeader() {

    Column(
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier.size(78.dp),
            shape =
                MaterialTheme.shapes.extraLarge,
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .primary
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 4.dp
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
                        Icons.Default.Luggage,
                    contentDescription =
                        "TripLedger",
                    modifier =
                        Modifier.size(42.dp),
                    tint =
                        MaterialTheme
                            .colorScheme
                            .onPrimary
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = "Join TripLedger",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .primary
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text =
                "Start your journey with us.",
            fontSize = 14.sp,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}