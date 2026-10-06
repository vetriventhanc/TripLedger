package com.example.tripledger.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.tripledger.ui.screens.expenseanalytics.ExpenseAnalyticsScreen
import com.example.tripledger.ui.screens.expenses.ExpensesScreen
import com.example.tripledger.ui.screens.profile.ProfileScreen
import com.example.tripledger.ui.screens.tripdetail.TripDetailScreen
import com.example.tripledger.ui.screens.trips.TripsScreen
import com.example.tripledger.viewmodel.AuthViewModel
import com.example.tripledger.viewmodel.TripExpenseViewModel
import com.example.tripledger.viewmodel.TripViewModel

private const val ANIMATION_DURATION = 350

private fun forwardEnter(): EnterTransition {
    return fadeIn(
        animationSpec = tween(ANIMATION_DURATION)
    ) + slideInHorizontally(
        initialOffsetX = { fullWidth ->
            fullWidth / 3
        },
        animationSpec = tween(ANIMATION_DURATION)
    )
}

private fun forwardExit(): ExitTransition {
    return fadeOut(
        animationSpec = tween(ANIMATION_DURATION)
    ) + slideOutHorizontally(
        targetOffsetX = { fullWidth ->
            -fullWidth / 3
        },
        animationSpec = tween(ANIMATION_DURATION)
    )
}

private fun backEnter(): EnterTransition {
    return fadeIn(
        animationSpec = tween(ANIMATION_DURATION)
    ) + slideInHorizontally(
        initialOffsetX = { fullWidth ->
            -fullWidth / 3
        },
        animationSpec = tween(ANIMATION_DURATION)
    )
}

private fun backExit(): ExitTransition {
    return fadeOut(
        animationSpec = tween(ANIMATION_DURATION)
    ) + slideOutHorizontally(
        targetOffsetX = { fullWidth ->
            fullWidth / 3
        },
        animationSpec = tween(ANIMATION_DURATION)
    )
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard",

        enterTransition = {
            forwardEnter()
        },

        exitTransition = {
            forwardExit()
        },

        popEnterTransition = {
            backEnter()
        },

        popExitTransition = {
            backExit()
        }
    ) {

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("register") {
                            inclusive = true
                        }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
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
                backStackEntry.arguments?.getInt("tripId")

            if (tripId == null) {
                Text("Trip not found")
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
                backStackEntry.arguments?.getInt("tripId")

            if (tripId == null) {
                Text("Trip not found")
            } else {
                TripExpensesRoute(
                    tripId = tripId,
                    onAnalyticsClick = {
                        navController.navigate(
                            "expenseAnalytics/$tripId"
                        )
                    }
                )
            }
        }

        composable(
            route = "expenseAnalytics/{tripId}",
            arguments = listOf(
                navArgument("tripId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->

            val tripId =
                backStackEntry.arguments?.getInt("tripId")

            if (tripId == null) {
                Text("Trip not found")
            } else {
                ExpenseAnalyticsScreen(
                    tripId = tripId
                )
            }
        }

        composable("profile") {
            ProfileScreen(
                onLogout = {
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

    LaunchedEffect(tripId) {
        isLoading = true

        tripViewModel
            .getTrip(tripId)
            .onSuccess { loadedTrip ->
                trip = loadedTrip
            }

        isLoading = false
    }

    if (isLoading) {

        CircularProgressIndicator()

    } else if (trip == null) {

        Text("Trip not found")

    } else {

        TripDetailScreen(
            trip = trip!!,
            tripViewModel = tripViewModel,
            onExpensesClick = onExpensesClick
        )
    }
}

@Composable
private fun TripExpensesRoute(
    tripId: Int,
    onAnalyticsClick: () -> Unit
) {
    val tripExpenseViewModel: TripExpenseViewModel =
        viewModel()

    ExpensesScreen(
        tripId = tripId,
        tripExpenseViewModel = tripExpenseViewModel,
        onAnalyticsClick = onAnalyticsClick
    )
}