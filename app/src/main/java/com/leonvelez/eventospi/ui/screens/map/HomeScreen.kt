package com.leonvelez.eventospi.ui.screens.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onGoToCreateEvent: () -> Unit,
    onGoToEventsList: () -> Unit,
    onGoToUpdateEvent: () -> Unit,
    onGoToChangePassword: () -> Unit,
    onLogout: () -> Unit
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onGoToCreateEvent,
            modifier = Modifier.width(220.dp)
        ) {
            Text("Crear evento")
        }
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onGoToEventsList,
            modifier = Modifier.width(220.dp)
        ) {
            Text("Ver eventos")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onGoToUpdateEvent,
            modifier = Modifier.width(220.dp)
        ) {
            Text("Actualizar evento")
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onGoToChangePassword,
            modifier = Modifier.width(220.dp)
        ) {
            Text("Cambiar contraseña")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.width(220.dp)
        ) {
            Text("Cerrar sesión")
        }
    }
}
