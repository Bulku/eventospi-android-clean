package com.leonvelez.eventospi.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leonvelez.eventospi.ui.components.FormScreenContainer
import com.leonvelez.eventospi.ui.components.ProfileInfoRow

@Composable
fun ProfileHubScreen(
    email: String,
    userName: String,
    onBackToHome: () -> Unit,
    onOpenProfileImage: () -> Unit,
    onOpenChangePassword: () -> Unit,
    onOpenPendingRequests: () -> Unit
) {
    FormScreenContainer(
        title = "Mi perfil",
        subtitle = "Gestiona tu cuenta y tus opciones personales"
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                ProfileInfoRow(
                    icon = Icons.Default.Email,
                    label = "Correo",
                    value = email.ifBlank { "No disponible" }
                )

                ProfileInfoRow(
                    icon = Icons.Default.Badge,
                    label = "Nombre de usuario",
                    value = userName.ifBlank { "No disponible" }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenProfileImage,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Imagen de perfil"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cambiar imagen de perfil")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onOpenChangePassword,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = "Cambiar contraseña"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cambiar contraseña")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onOpenPendingRequests,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ListAlt,
                contentDescription = "Solicitudes pendientes"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Solicitudes pendientes")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al mapa")
        }
    }
}
