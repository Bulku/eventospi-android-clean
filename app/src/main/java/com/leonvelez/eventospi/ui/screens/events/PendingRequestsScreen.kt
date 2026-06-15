package com.leonvelez.eventospi.ui.screens.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.leonvelez.eventospi.data.TokenManager
import com.leonvelez.eventospi.data.model.EventParticipantResponse
import com.leonvelez.eventospi.data.model.EventResponse
import com.leonvelez.eventospi.data.model.ManageParticipantRequest
import com.leonvelez.eventospi.data.remote.RetrofitInstance
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
    var creatorPrivateEvents by remember { mutableStateOf<List<EventResponse>>(emptyList()) }
    var selectedEvent by remember { mutableStateOf<EventResponse?>(null) }
    var pendingParticipants by remember { mutableStateOf<List<EventParticipantResponse>>(emptyList()) }
    var resultText by remember { mutableStateOf("Cargando...") }
    var isLoading by remember { mutableStateOf(false) }

    fun participantName(participant: EventParticipantResponse): String {
        val fullName = "${participant.userFirstName} ${participant.userLastName}".trim()

        return when {
            fullName.isNotBlank() -> fullName
            participant.userName.isNotBlank() -> participant.userName
            else -> "Usuario ID: ${participant.userId}"
        }
    }

    suspend fun loadCurrentUserAndPrivateEvents() {
        val savedToken = tokenManager.getToken()

        if (savedToken.isNullOrBlank()) {
            currentUserName = ""
            creatorPrivateEvents = emptyList()
            resultText = "No hay sesión activa"
            return
        }

        isLoading = true
        resultText = "Cargando eventos privados..."

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

            creatorPrivateEvents = allEvents.filter { event ->
                isEventOwnedByUserName(
                    currentUserName = currentUserName,
                    event = event
                ) && !event.isPublic
            }

            resultText = if (creatorPrivateEvents.isEmpty()) {
                "No tienes eventos privados creados"
            } else {
                ""
            }
        } else {
            creatorPrivateEvents = emptyList()
            resultText = "Error cargando eventos: ${eventsResponse.code()}"
        }

        isLoading = false
    }

    suspend fun loadPendingParticipantsForSelectedEvent() {
        val savedToken = tokenManager.getToken()
        val event = selectedEvent

        if (savedToken.isNullOrBlank()) {
            pendingParticipants = emptyList()
            resultText = "No hay sesión activa"
            return
        }

        if (event == null) {
            pendingParticipants = emptyList()
            return
        }

        isLoading = true
        resultText = "Cargando solicitudes pendientes..."

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
            val errorText = response.errorBody()?.string().orEmpty()
            resultText = "Error cargando solicitudes: ${response.code()} - $errorText"
        }

        isLoading = false
    }

    fun manageParticipant(
        participant: EventParticipantResponse,
        approve: Boolean
    ) {
        scope.launch {
            val savedToken = tokenManager.getToken()
            val event = selectedEvent

            if (savedToken.isNullOrBlank()) {
                resultText = "No hay sesión activa"
                return@launch
            }

            if (event == null) {
                resultText = "No hay evento seleccionado"
                return@launch
            }

            val participantUserId = participant.userId

            if (participantUserId.isNullOrBlank()) {
                resultText = "No se pudo obtener el usuario del participante"
                return@launch
            }

            try {
                isLoading = true
                resultText = if (approve) {
                    "Aprobando participante..."
                } else {
                    "Rechazando participante..."
                }

                val response = RetrofitInstance.api.approveOrRejectParticipant(
                    token = "Bearer $savedToken",
                    request = ManageParticipantRequest(
                        eventId = event.id,
                        userId = participantUserId,
                        approve = approve
                    )
                )

                if (response.isSuccessful) {
                    pendingParticipants = pendingParticipants.filterNot {
                        it.userId == participant.userId
                    }

                    resultText = if (approve) {
                        "Participante aprobado correctamente"
                    } else {
                        "Participante rechazado correctamente"
                    }
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()

                    if (response.code() == 500) {
                        loadPendingParticipantsForSelectedEvent()

                        val stillPending = pendingParticipants.any {
                            it.userId == participant.userId
                        }

                        resultText = if (!stillPending) {
                            if (approve) {
                                "Participante aprobado correctamente"
                            } else {
                                "Participante rechazado correctamente"
                            }
                        } else {
                            if (approve) {
                                "Error al aprobar: 500 - $errorText"
                            } else {
                                "Error al rechazar: 500 - $errorText"
                            }
                        }
                    } else {
                        resultText = if (approve) {
                            "Error al aprobar: ${response.code()} - $errorText"
                        } else {
                            "Error al rechazar: ${response.code()} - $errorText"
                        }
                    }
                }
            } catch (e: Exception) {
                resultText = if (approve) {
                    "Excepción al aprobar: ${e.message}"
                } else {
                    "Excepción al rechazar: ${e.message}"
                }
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            loadCurrentUserAndPrivateEvents()
        } catch (e: Exception) {
            isLoading = false
            resultText = "Excepción cargando datos: ${e.message}"
        }
    }

    LaunchedEffect(selectedEvent?.id) {
        if (selectedEvent != null) {
            try {
                loadPendingParticipantsForSelectedEvent()
            } catch (e: Exception) {
                isLoading = false
                resultText = "Excepción cargando solicitudes: ${e.message}"
            }
        }
    }

    if (selectedEvent == null) {
        FormScreenContainer(
            title = "Solicitudes pendientes",
            subtitle = "Selecciona un evento privado para revisar solicitudes"
        ) {
            if (creatorPrivateEvents.isEmpty()) {
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    creatorPrivateEvents.forEach { eventItem ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedEvent = eventItem
                                    resultText = ""
                                },
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
                                    text = "Evento privado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4F67A8)
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
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
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
                                    text = participantName(participant),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Usuario: ${participant.userName}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Estado: Pendiente",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4F67A8)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            manageParticipant(
                                                participant = participant,
                                                approve = true
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        enabled = !isLoading
                                    ) {
                                        Text("Aprobar")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            manageParticipant(
                                                participant = participant,
                                                approve = false
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        enabled = !isLoading
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
                onClick = {
                    selectedEvent = null
                    pendingParticipants = emptyList()
                    resultText = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver a mis eventos privados")
            }

            TextButton(
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al mapa")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (resultText.isNotBlank() && pendingParticipants.isNotEmpty()) {
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
            }
        }
    }
}