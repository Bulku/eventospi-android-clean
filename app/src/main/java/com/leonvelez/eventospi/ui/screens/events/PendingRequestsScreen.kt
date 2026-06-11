package com.leonvelez.eventospi.ui.screens.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.leonvelez.eventospi.data.model.EventParticipantResponse
import com.leonvelez.eventospi.data.model.EventResponse
import com.leonvelez.eventospi.data.model.ManageParticipantRequest
import com.leonvelez.eventospi.data.remote.RetrofitInstance
import com.leonvelez.eventospi.data.TokenManager
import com.leonvelez.eventospi.ui.components.FormScreenContainer
import com.leonvelez.eventospi.utils.extractUserNameFromAuthMessage
import com.leonvelez.eventospi.utils.formatEventDateCompact
import com.leonvelez.eventospi.utils.isEventOwnedByUserName
import kotlinx.coroutines.launch

@Composable
fun PendingRequestsScreen(
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }

    var currentUserName by remember { mutableStateOf("") }
    var creatorEvents by remember { mutableStateOf<List<EventResponse>>(emptyList()) }
    var selectedEvent by remember { mutableStateOf<EventResponse?>(null) }
    var pendingParticipants by remember { mutableStateOf<List<EventParticipantResponse>>(emptyList()) }
    var resultText by remember { mutableStateOf("Cargando...") }

    suspend fun loadCurrentUserAndEvents() {
        val savedToken = tokenManager.getToken()

        if (savedToken.isNullOrBlank()) {
            currentUserName = ""
            creatorEvents = emptyList()
            resultText = "No hay sesión activa"
            return
        }

        val userResponse = RetrofitInstance.api.getUserAuthenticated(
            token = "Bearer $savedToken"
        )

        currentUserName = if (userResponse.isSuccessful) {
            extractUserNameFromAuthMessage(userResponse.body()?.message)
        } else {
            ""
        }

        val eventsResponse = RetrofitInstance.api.getEvents(
            token = "Bearer $savedToken"
        )

        if (eventsResponse.isSuccessful) {
            val allEvents = eventsResponse.body().orEmpty()

            creatorEvents = allEvents.filter { event ->
                isEventOwnedByUserName(
                    currentUserName = currentUserName,
                    event = event
                )
            }

            resultText = if (creatorEvents.isEmpty()) {
                "No tienes eventos creados"
            } else {
                ""
            }
        } else {
            creatorEvents = emptyList()
            resultText = "Error cargando eventos: ${eventsResponse.code()}"
        }
    }

    suspend fun loadPendingParticipantsForSelectedEvent() {
        val savedToken = tokenManager.getToken()
        val event = selectedEvent

        if (savedToken.isNullOrBlank() || event == null) {
            pendingParticipants = emptyList()
            return
        }

        val response = RetrofitInstance.api.getPendingRequestsAsync(
            token = "Bearer $savedToken",
            eventId = event.id
        )

        if (response.isSuccessful) {
            pendingParticipants = response.body().orEmpty()
            resultText = if (pendingParticipants.isEmpty()) {
                "No hay solicitudes pendientes para este evento"
            } else {
                ""
            }
        } else {
            pendingParticipants = emptyList()
            resultText = "Error cargando solicitudes: ${response.code()}"
        }
    }

    LaunchedEffect(Unit) {
        try {
            loadCurrentUserAndEvents()
        } catch (e: Exception) {
            resultText = "Excepción cargando datos: ${e.message}"
        }
    }

    LaunchedEffect(selectedEvent) {
        if (selectedEvent != null) {
            try {
                loadPendingParticipantsForSelectedEvent()
            } catch (e: Exception) {
                resultText = "Excepción cargando solicitudes: ${e.message}"
            }
        }
    }

    if (selectedEvent == null) {
        FormScreenContainer(
            title = "Solicitudes pendientes",
            subtitle = "Selecciona uno de tus eventos para revisar participantes"
        ) {
            if (creatorEvents.isEmpty()) {
                Text(resultText)
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    creatorEvents.forEach { eventItem ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedEvent = eventItem },
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
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
    } else {
        FormScreenContainer(
            title = "Solicitudes pendientes",
            subtitle = "Evento: ${selectedEvent!!.name}"
        ) {
            if (pendingParticipants.isEmpty()) {
                Text(resultText)
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pendingParticipants.forEach { participant ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Usuario ID: ${participant.userId}",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val savedToken = tokenManager.getToken()

                                                if (savedToken.isNullOrBlank()) {
                                                    resultText = "No hay sesión activa"
                                                    return@launch
                                                }

                                                val participantUserId = participant.userId.toIntOrNull()

                                                if (participantUserId == null) {
                                                    resultText = "No se pudo interpretar el ID del participante"
                                                    return@launch
                                                }

                                                try {
                                                    val response = RetrofitInstance.api.approveOrRejectParticipant(
                                                        token = "Bearer $savedToken",
                                                        request = ManageParticipantRequest(
                                                            eventId = selectedEvent!!.id,
                                                            userId = participantUserId,
                                                            approve = true
                                                        )
                                                    )

                                                    if (response.isSuccessful) {
                                                        pendingParticipants =
                                                            pendingParticipants.filterNot {
                                                                it.userId == participant.userId
                                                            }
                                                        resultText = "Participante aprobado"
                                                    } else {
                                                        val errorText = response.errorBody()?.string().orEmpty()
                                                        resultText = "Error al aprobar: ${response.code()} - $errorText"
                                                    }
                                                } catch (e: Exception) {
                                                    resultText = "Excepción al aprobar: ${e.message}"
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Aprobar")
                                    }

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                val savedToken = tokenManager.getToken()

                                                if (savedToken.isNullOrBlank()) {
                                                    resultText = "No hay sesión activa"
                                                    return@launch
                                                }

                                                val participantUserId = participant.userId.toIntOrNull()

                                                if (participantUserId == null) {
                                                    resultText = "No se pudo interpretar el ID del participante"
                                                    return@launch
                                                }

                                                try {
                                                    val response = RetrofitInstance.api.approveOrRejectParticipant(
                                                        token = "Bearer $savedToken",
                                                        request = ManageParticipantRequest(
                                                            eventId = selectedEvent!!.id,
                                                            userId = participantUserId,
                                                            approve = false
                                                        )
                                                    )

                                                    if (response.isSuccessful) {
                                                        pendingParticipants =
                                                            pendingParticipants.filterNot {
                                                                it.userId == participant.userId
                                                            }
                                                        resultText = "Participante rechazado"
                                                    } else {
                                                        val errorText = response.errorBody()?.string().orEmpty()
                                                        resultText = "Error al rechazar: ${response.code()} - $errorText"
                                                    }
                                                } catch (e: Exception) {
                                                    resultText = "Excepción al rechazar: ${e.message}"
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Rechazar")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { selectedEvent = null },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver a mis eventos")
            }

            TextButton(
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al mapa")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (resultText.isNotBlank()) {
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}
