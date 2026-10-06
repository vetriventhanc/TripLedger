package com.example.tripledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.tripledger.navigation.AppNavigation
import com.example.tripledger.ui.screens.opening.OpeningScreen
import com.example.tripledger.ui.theme.TripLedgerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TripLedgerTheme {

                var showOpeningScreen by remember {
                    mutableStateOf(true)
                }

                if (showOpeningScreen) {

                    OpeningScreen(
                        onFinished = {
                            showOpeningScreen = false
                        }
                    )

                } else {

                    AppNavigation()
                }
            }
        }
    }
}