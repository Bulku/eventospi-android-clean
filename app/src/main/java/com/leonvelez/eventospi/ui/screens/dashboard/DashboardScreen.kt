package com.leonvelez.eventospi.ui.screens.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leonvelez.eventospi.data.model.EventParticipantResponse
import com.leonvelez.eventospi.data.remote.RetrofitInstance
import com.leonvelez.eventospi.data.TokenManager
import com.leonvelez.eventospi.ui.components.DashboardCategoryRow
import com.leonvelez.eventospi.ui.components.DashboardStatCard
import com.leonvelez.eventospi.ui.components.FormScreenContainer
import com.leonvelez.eventospi.ui.model.DashboardStats
import com.leonvelez.eventospi.ui.model.EventCategory
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }

    var stats by remember { mutableStateOf<DashboardStats?>(null) }
    var resultText by remember { mutableStateOf("Cargando estadísticas...") }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val savedToken = tokenManager.getToken()

                if (savedToken.isNullOrBlank()) {
                    stats = null
                    resultText = "No hay sesión activa"
                    return@launch
                }

                val eventsResponse = RetrofitInstance.api.getEvents(
                    token = "Bearer $savedToken"
                )

                if (!eventsResponse.isSuccessful) {
                    stats = null
                    resultText = "Error cargando eventos: ${eventsResponse.code()}"
                    return@launch
                }

                val events = eventsResponse.body().orEmpty()
                val allParticipants = mutableListOf<EventParticipantResponse>()

                events.forEach { event ->
                    try {
                        val participantsResponse = RetrofitInstance.api.getParticipantsByEventId(
                            token = "Bearer $savedToken",
                            eventId = event.id
                        )

                        if (participantsResponse.isSuccessful) {
                            allParticipants += participantsResponse.body().orEmpty()
                        }
                    } catch (_: Exception) {
                    }
                }

                val eventsByCategory = events
                    .groupingBy {
                        EventCategory.fromBackendValue(it.category)?.label ?: "Sin categoría"
                    }
                    .eachCount()
                    .toList()
                    .sortedByDescending { it.second }

                val topCreator = events
                    .groupingBy { creator ->
                        creator.createdByUserName.ifBlank { "Sin datos" }
                    }
                    .eachCount()
                    .maxByOrNull { it.value }

                val topRegisteredUser = allParticipants
                    .groupingBy { participant ->
                        participant.userName.ifBlank {
                            val fullName = "${participant.userFirstName} ${participant.userLastName}".trim()
                            if (fullName.isBlank()) "Usuario ${participant.userId}" else fullName
                        }
                    }
                    .eachCount()
                    .maxByOrNull { it.value }

                stats = DashboardStats(
                    totalEvents = events.size,
                    totalRegistrations = allParticipants.size,
                    uniqueRegisteredUsersInEvents = allParticipants.map { it.userId }.distinct().size,
                    eventsByCategory = eventsByCategory,
                    topCreatorName = topCreator?.key ?: "Sin datos",
                    topCreatorCount = topCreator?.value ?: 0,
                    topRegisteredUserName = topRegisteredUser?.key ?: "Sin datos",
                    topRegisteredUserCount = topRegisteredUser?.value ?: 0
                )

                resultText = ""
            } catch (e: Exception) {
                stats = null
                resultText = "Excepción cargando dashboard: ${e.message}"
            }
        }
    }

    FormScreenContainer(
        title = "Dashboard",
        subtitle = "Resumen general de la aplicación"
    ) {
        if (stats == null) {
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            val dashboard = stats!!

            DashboardStatCard(
                title = "Eventos creados",
                value = dashboard.totalEvents.toString()
            )

            Spacer(modifier = Modifier.height(12.dp))

            DashboardStatCard(
                title = "Inscripciones totales",
                value = dashboard.totalRegistrations.toString(),
                subtitle = "Suma de participantes en todos los eventos"
            )

            Spacer(modifier = Modifier.height(12.dp))

            DashboardStatCard(
                title = "Usuarios inscritos únicos",
                value = dashboard.uniqueRegisteredUsersInEvents.toString(),
                subtitle = "Usuarios distintos inscritos en al menos un evento"
            )

            Spacer(modifier = Modifier.height(12.dp))

            DashboardStatCard(
                title = "Usuario con más eventos creados",
                value = dashboard.topCreatorName,
                subtitle = "Cantidad: ${dashboard.topCreatorCount}"
            )

            Spacer(modifier = Modifier.height(12.dp))

            DashboardStatCard(
                title = "Usuario inscrito en más eventos",
                value = dashboard.topRegisteredUserName,
                subtitle = "Cantidad: ${dashboard.topRegisteredUserCount}"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Eventos por categoría",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val maxCount = dashboard.eventsByCategory.maxOfOrNull { it.second } ?: 1

                    if (dashboard.eventsByCategory.isEmpty()) {
                        Text("No hay eventos para mostrar.")
                    } else {
                        dashboard.eventsByCategory.forEach { (category, count) ->
                            DashboardCategoryRow(
                                label = category,
                                count = count,
                                maxCount = maxCount
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

        }

        TextButton(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver al mapa")
        }
    }
}
