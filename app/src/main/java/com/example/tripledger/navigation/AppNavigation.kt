package com.example.tripledger.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tripledger.ui.screens.auth.LoginScreen
import com.example.tripledger.ui.screens.auth.RegisterScreen
import com.example.tripledger.ui.screens.dashboard.DashboardScreen
import com.example.tripledger.ui.screens.expenses.ExpensesScreen
import com.example.tripledger.ui.screens.profile.ProfileScreen
import com.example.tripledger.ui.screens.trips.TripsScreen
import com.example.tripledger.viewmodel.AuthState
import com.example.tripledger.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel()

    val authState by authViewModel.authState
        .collectAsStateWithLifecycle()

    var checkedStoredLogin by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        authViewModel.checkStoredLogin()
        checkedStoredLogin = true
    }

    if (!checkedStoredLogin ||
        authState is AuthState.Loading
    ) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        return
    }

    val startDestination =
        when (authState) {
            is AuthState.AutoLoginSuccess ->
                "dashboard"

            else ->
                "login"
        }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("login") {

            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                },
                authViewModel = authViewModel
            )
        }

        composable("register") {

            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") {
                            inclusive = true
                        }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                },
                authViewModel = authViewModel
            )
        }

        composable("dashboard") {

            DashboardScreen(
                navController = navController
            )
        }

        composable("trips") {
            TripsScreen()
        }

        composable("expenses") {
            ExpensesScreen()
        }

        composable("profile") {
            ProfileScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}