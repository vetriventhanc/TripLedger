package com.example.tripledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.tripledger.navigation.AppNavigation
import com.example.tripledger.ui.theme.TripLedgerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TripLedgerTheme {
                AppNavigation()
            }
        }
    }
}