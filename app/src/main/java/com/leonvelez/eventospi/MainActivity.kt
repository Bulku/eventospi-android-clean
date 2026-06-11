package com.leonvelez.eventospi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.leonvelez.eventospi.ui.navigation.MapRootScreen
import com.leonvelez.eventospi.ui.theme.EventosPITheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EventosPITheme {
                MaterialTheme {
                    MapRootScreen()
                }
            }
        }
    }
}

