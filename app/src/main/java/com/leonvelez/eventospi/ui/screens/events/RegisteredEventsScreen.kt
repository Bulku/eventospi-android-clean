package com.leonvelez.eventospi.ui.screens.events

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
import com.leonvelez.eventospi.data.model.EventResponse
import com.leonvelez.eventospi.data.model.RegistrationRequest
import com.leonvelez.eventospi.data.remote.RetrofitInstance
import com.leonvelez.eventospi.ui.components.FormScreenContainer
import com.leonvelez.eventospi.ui.components.InfoPill
import com.leonvelez.eventospi.utils.cancelRegistrationFailureMessage
import com.leonvelez.eventospi.utils.eventCategoryDisplayLabel
import com.leonvelez.eventospi.utils.formatEventDateCompact
import kotlinx.coroutines.launch

@Composable
fun RegisteredEventsScreen(
    cancelledEventIds: Set<Int>,
    onBackToHome: () -> Unit,
    onRegisteredEventsChanged: (List<EventResponse>) -> Unit,
    onEventCancelled: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }

    var events by remember { mutableStateOf<List<EventResponse>>(emptyList()) }
    var screenCancelledEventIds by remember { mutableStateOf(cancelledEventIds) }
    var resultText by remember { mutableStateOf("Cargando mis inscripciones...") }

    LaunchedEffect(cancelledEventIds) {
        screenCancelledEventIds = cancelledEventIds
    }

    suspend fun loadRegisteredEvents() {
        val savedToken = tokenManager.getToken()

        if (savedToken.isNullOrBlank()) {
            events = emptyList()
            onRegisteredEventsChanged(emptyList())
            resultText = "No hay sesión activa"
            return
        }

        val response = RetrofitInstance.api.getEventsIAmRegistered(
            token = "Bearer $savedToken"
        )

        if (response.isSuccessful) {
            val loadedEvents = response.body().orEmpty()

            events = loadedEvents

            onRegisteredEventsChanged(
                loadedEvents.filterNot { event ->
                    screenCancelledEventIds.contains(event.id)
                }
            )

            resultText = if (loadedEvents.isEmpty()) {
                "No tienes eventos inscritos"
            } else {
                ""
            }
        } else {
            events = emptyList()
            onRegisteredEventsChanged(emptyList())
            resultText = "Error cargando mis inscripciones: ${response.code()}"
        }
    }

    LaunchedEffect(Unit) {
        try {
            loadRegisteredEvents()
        } catch (e: Exception) {
            resultText = "Excepción cargando mis inscripciones: ${e.message}"
        }
    }

    FormScreenContainer(
        title = "Mis inscripciones",
        subtitle = "Aquí puedes revisar y cancelar tus registros"
    ) {
        if (events.isEmpty()) {
            Text(resultText)
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                events.forEach { registeredEvent ->
                    val isCancelledLocally =
                        screenCancelledEventIds.contains(registeredEvent.id)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = registeredEvent.name,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = formatEventDateCompact(registeredEvent.startDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4F67A8)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = registeredEvent.address,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Creado por: ${registeredEvent.createdByUserName}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                InfoPill(eventCategoryDisplayLabel(registeredEvent.category))
                                InfoPill("Cupo ${registeredEvent.maxParticipants}")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (isCancelledLocally) {
                                        return@Button
                                    }

                                    scope.launch {
                                        try {
                                            val savedToken = tokenManager.getToken()

                                            if (savedToken.isNullOrBlank()) {
                                                resultText = "No hay sesión activa"
                                                return@launch
                                            }

                                            val response =
                                                RetrofitInstance.api.cancelRegistration(
                                                    token = "Bearer $savedToken",
                                                    request = RegistrationRequest(
                                                        eventId = registeredEvent.id,
                                                        cancellationReason = ""
                                                    )
                                                )

                                            val errorText =
                                                response.errorBody()?.string().orEmpty()

                                            if (response.isSuccessful) {
                                                val updatedCancelledIds =
                                                    screenCancelledEventIds + registeredEvent.id

                                                screenCancelledEventIds = updatedCancelledIds

                                                onEventCancelled(registeredEvent.id)

                                                onRegisteredEventsChanged(
                                                    events.filterNot { event ->
                                                        updatedCancelledIds.contains(event.id)
                                                    }
                                                )

                                                resultText = "Inscripción cancelada"
                                            } else {
                                                resultText = cancelRegistrationFailureMessage(
                                                    responseCode = response.code(),
                                                    errorText = errorText
                                                )
                                            }
                                        } catch (e: Exception) {
                                            resultText =
                                                "Excepción al cancelar inscripción: ${e.message}"
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                enabled = !isCancelledLocally
                            ) {
                                Text(
                                    if (isCancelledLocally) {
                                        "Inscripción cancelada"
                                    } else {
                                        "Cancelar inscripción"
                                    }
                                )
                            }
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