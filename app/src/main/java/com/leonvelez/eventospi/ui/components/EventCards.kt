package com.leonvelez.eventospi.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.leonvelez.eventospi.data.model.EventParticipantResponse
import com.leonvelez.eventospi.data.model.EventResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

private fun participantStatusLabel(status: Any?): String {
    return when (status) {
        null -> "Sin estado"

        is Int -> {
            when (status) {
                0 -> "Pendiente"
                1 -> "Aprobado"
                2 -> "Rechazado"
                3 -> "Cancelado"
                4 -> "Asistió"
                else -> "Desconocido"
            }
        }

        is String -> {
            when (status.trim().lowercase()) {
                "0", "pending", "pendiente" -> "Pendiente"
                "1", "approved", "aprobado" -> "Aprobado"
                "2", "rejected", "rechazado" -> "Rechazado"
                "3", "cancelled", "canceled", "cancelado" -> "Cancelado"
                "4", "attended", "asistio", "asistió" -> "Asistió"
                else -> status
            }
        }

        else -> status.toString()
    }
}

@Composable
fun EventListCard(
    event: EventResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = event.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("ID: ${event.id}")
            Text("Dirección: ${event.address}")
            Text("Creado por: ${event.createdByUserName}")

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoPill("Categoría ${event.category}")
                InfoPill("Cupo ${event.maxParticipants}")
            }
        }
    }
}

@Composable
fun PendingParticipantCard(
    participant: EventParticipantResponse,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = participant.userName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Nombre: ${participant.userFirstName} ${participant.userLastName}")
            Text("Estado: ${participantStatusLabel(participant.status)}")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Aprobar")
                }

                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Rechazar")
                }
            }
        }
    }
}

@Composable
fun EventImageFromUrl(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = imageUrl) {
        value = null

        if (imageUrl.isNullOrBlank()) return@produceState

        value = try {
            withContext(Dispatchers.IO) {
                URL(imageUrl).openStream().use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .background(Color(0xFFF1F1F1), RoundedCornerShape(16.dp))
            .clickable(enabled = bitmapState.value != null && onClick != null) {
                onClick?.invoke()
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            imageUrl.isNullOrBlank() -> {
                Text("Sin imagen")
            }

            bitmapState.value == null -> {
                CircularProgressIndicator()
            }

            else -> {
                Image(
                    bitmap = bitmapState.value!!.asImageBitmap(),
                    contentDescription = "Imagen del evento",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun FullScreenEventImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = imageUrl) {
        value = try {
            withContext(Dispatchers.IO) {
                URL(imageUrl).openStream().use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            if (bitmapState.value == null) {
                CircularProgressIndicator()
            } else {
                Image(
                    bitmap = bitmapState.value!!.asImageBitmap(),
                    contentDescription = "Imagen del evento en pantalla completa",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
