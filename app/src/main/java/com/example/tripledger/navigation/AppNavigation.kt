package com.example.tripledger.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tripledger.data.remote.TripResponse
import com.example.tripledger.ui.screens.auth.LoginScreen
import com.example.tripledger.ui.screens.auth.RegisterScreen
import com.example.tripledger.ui.screens.dashboard.DashboardScreen
import com.example.tripledger.ui.screens.expenses.ExpensesScreen
import com.example.tripledger.ui.screens.profile.ProfileScreen
import com.example.tripledger.ui.screens.trips.TripsScreen
import com.example.tripledger.ui.screens.tripdetail.TripDetailScreen
import com.example.tripledger.viewmodel.AuthState
import com.example.tripledger.viewmodel.AuthViewModel
import com.example.tripledger.viewmodel.TripExpenseViewModel
import com.example.tripledger.viewmodel.TripViewModel

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

            TripsScreen(
                onTripClick = { tripId ->
                    navController.navigate(
                        "tripDetail/$tripId"
                    )
                }
            )
        }

        composable(
            route = "tripDetail/{tripId}",
            arguments = listOf(
                navArgument("tripId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val tripId =
                backStackEntry.arguments
                    ?.getInt("tripId")

            if (tripId == null) {

                Text(
                    text = "Trip not found"
                )

            } else {

                TripDetailRoute(
                    tripId = tripId,
                    onExpensesClick = {
                        navController.navigate(
                            "expenses/$tripId"
                        )
                    }
                )
            }
        }

        composable(
            route = "expenses/{tripId}",
            arguments = listOf(
                navArgument("tripId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val tripId =
                backStackEntry.arguments
                    ?.getInt("tripId")

            if (tripId == null) {

                Text(
                    text = "Trip not found"
                )

            } else {

                TripExpensesRoute(
                    tripId = tripId
                )
            }
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


@Composable
private fun TripDetailRoute(
    tripId: Int,
    onExpensesClick: () -> Unit
) {
    val tripViewModel: TripViewModel = viewModel()

    var trip by remember {
        mutableStateOf<TripResponse?>(null)
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(tripId) {

        isLoading = true
        errorMessage = null

        val result =
            tripViewModel.getTrip(tripId)

        result
            .onSuccess {
                trip = it
            }
            .onFailure {
                errorMessage =
                    it.message ?: "Failed to load trip"
            }

        isLoading = false
    }

    when {

        isLoading -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        trip != null -> {

            TripDetailScreen(
                trip = trip!!,
                tripViewModel = tripViewModel,
                onExpensesClick = onExpensesClick
            )
        }

        else -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text =
                        errorMessage
                            ?: "Trip not found"
                )
            }
        }
    }
}


@Composable
private fun TripExpensesRoute(
    tripId: Int
) {
    val tripExpenseViewModel: TripExpenseViewModel =
        viewModel()

    ExpensesScreen(
        tripId = tripId,
        tripExpenseViewModel = tripExpenseViewModel
    )
}