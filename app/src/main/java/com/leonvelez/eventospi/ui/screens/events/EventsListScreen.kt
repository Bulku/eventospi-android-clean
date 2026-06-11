package com.leonvelez.eventospi.ui.screens.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.leonvelez.eventospi.data.model.EventResponse
import com.leonvelez.eventospi.data.remote.RetrofitInstance
import com.leonvelez.eventospi.data.TokenManager
import com.leonvelez.eventospi.ui.components.EventImageFromUrl
import com.leonvelez.eventospi.ui.components.FormScreenContainer
import com.leonvelez.eventospi.ui.components.InfoPill
import com.leonvelez.eventospi.utils.eventCategoryDisplayLabel
import com.leonvelez.eventospi.utils.formatEventDateCompact

@Composable
fun EventsListScreen(
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var events by remember { mutableStateOf<List<EventResponse>>(emptyList()) }
    var resultText by remember { mutableStateOf("Cargando eventos...") }

    LaunchedEffect(Unit) {
        try {
            val savedToken = tokenManager.getToken()

            if (savedToken.isNullOrBlank()) {
                events = emptyList()
                resultText = "Inicia sesión para ver eventos"
                return@LaunchedEffect
            }

            val response = RetrofitInstance.api.getEvents(
                token = "Bearer $savedToken"
            )

            if (response.isSuccessful) {
                events = response.body().orEmpty()
                resultText = if (events.isEmpty()) {
                    "No hay eventos disponibles"
                } else {
                    ""
                }
            } else {
                events = emptyList()
                resultText = "Error cargando eventos: ${response.code()}"
            }
        } catch (e: Exception) {
            events = emptyList()
            resultText = "Excepción cargando eventos: ${e.message}"
        }
    }

    FormScreenContainer(
        title = "Eventos disponibles",
        subtitle = "Consulta los eventos publicados"
    ) {
        if (events.isEmpty()) {
            Text(resultText)
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                events.forEach { eventItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = eventItem.name,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = formatEventDateCompact(eventItem.startDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4F67A8)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = eventItem.address,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Creado por: ${eventItem.createdByUserName}",
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    InfoPill(eventCategoryDisplayLabel(eventItem.category))
                                    InfoPill("Cupo ${eventItem.maxParticipants}")
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (eventItem.price == null) {
                                        "Precio: Gratis"
                                    } else {
                                        "Precio: ${eventItem.price}"
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            EventImageFromUrl(
                                imageUrl = eventItem.imageUrl,
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                onClick = {}
                            )
                        }
                    }
                }
            }
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
