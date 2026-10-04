package com.example.tripledger.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(
    navController: NavController
) {

    NavigationBar {

        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("dashboard")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = {
                Text("Home")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("trips")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.TravelExplore,
                    contentDescription = "Trips"
                )
            },
            label = {
                Text("Trips")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("expenses")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Expenses"
                )
            },
            label = {
                Text("Expenses")
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate("profile")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile"
                )
            },
            label = {
                Text("Profile")
            }
        )
    }
}