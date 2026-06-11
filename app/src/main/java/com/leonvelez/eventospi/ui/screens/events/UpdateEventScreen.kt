package com.leonvelez.eventospi.ui.screens.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.leonvelez.eventospi.data.model.EventRequest
import com.leonvelez.eventospi.data.model.EventResponse
import com.leonvelez.eventospi.data.remote.RetrofitInstance
import com.leonvelez.eventospi.data.TokenManager
import com.leonvelez.eventospi.ui.components.CategoryDropdownField
import com.leonvelez.eventospi.ui.components.EventListCard
import com.leonvelez.eventospi.ui.components.FormScreenContainer
import com.leonvelez.eventospi.ui.components.FormSectionTitle
import com.leonvelez.eventospi.ui.model.EventCategory
import com.leonvelez.eventospi.utils.extractUserNameFromAuthMessage
import com.leonvelez.eventospi.utils.isEventOwnedByUserName
import kotlinx.coroutines.launch

@Composable
fun UpdateEventScreen(
    onBackToHome: () -> Unit,
    initialSelectedEventId: Int? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }

    var events by remember { mutableStateOf<List<EventResponse>>(emptyList()) }
    var selectedEvent by remember { mutableStateOf<EventResponse?>(null) }
    var resultText by remember { mutableStateOf("Cargando eventos...") }

    var id by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<EventCategory?>(null) }
    var price by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val savedToken = tokenManager.getToken()

            if (savedToken.isNullOrBlank()) {
                resultText = "No hay sesión activa"
                events = emptyList()
                return@LaunchedEffect
            }

            val currentUserResponse = RetrofitInstance.api.getUserAuthenticated(
                token = "Bearer $savedToken"
            )

            val loggedUserName = if (currentUserResponse.isSuccessful) {
                extractUserNameFromAuthMessage(currentUserResponse.body()?.message)
            } else {
                ""
            }

            val response = RetrofitInstance.api.getEvents(
                token = "Bearer $savedToken"
            )

            if (response.isSuccessful) {
                val allEvents = response.body().orEmpty()

                events = allEvents.filter { event ->
                    isEventOwnedByUserName(
                        currentUserName = loggedUserName,
                        event = event
                    )
                }

                resultText = if (events.isEmpty()) {
                    "No tienes eventos creados"
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

    LaunchedEffect(events, initialSelectedEventId) {
        if (initialSelectedEventId != null && events.isNotEmpty() && selectedEvent == null) {
            selectedEvent = events.firstOrNull { it.id == initialSelectedEventId }
        }
    }

    LaunchedEffect(selectedEvent) {
        selectedEvent?.let { event ->
            id = event.id.toString()
            name = event.name
            description = event.description
            startDate = event.startDate
            endDate = event.endDate
            latitude = event.latitude.toString()
            longitude = event.longitude.toString()
            address = event.address
            maxParticipants = event.maxParticipants.toString()
            selectedCategory = EventCategory.fromBackendValue(event.category)
            price = event.price?.toString().orEmpty()
        }
    }

    if (selectedEvent == null) {
        FormScreenContainer(
            title = "Actualizar evento",
            subtitle = "Selecciona el evento que deseas editar"
        ) {
            if (events.isEmpty()) {
                Text(resultText)
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    events.forEach { event ->
                        EventListCard(
                            event = event,
                            onClick = { selectedEvent = event }
                        )
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
            title = "Actualizar evento",
            subtitle = "Edita la información del evento seleccionado"
        ) {
            FormSectionTitle("Información general")

            OutlinedTextField(
                value = id,
                onValueChange = {},
                label = { Text("ID") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))
            FormSectionTitle("Fecha y horario")

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Fecha inicio") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("Fecha fin") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            FormSectionTitle("Ubicación")

            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitud") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitud") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            FormSectionTitle("Detalles adicionales")

            OutlinedTextField(
                value = maxParticipants,
                onValueChange = { maxParticipants = it },
                label = { Text("Máximo participantes") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            CategoryDropdownField(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Precio opcional") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch {
                        try {
                            val savedToken = tokenManager.getToken()

                            if (savedToken.isNullOrBlank()) {
                                resultText = "No hay sesión activa"
                                return@launch
                            }

                            if (selectedCategory == null) {
                                resultText = "Selecciona una categoría"
                                return@launch
                            }

                            val event = EventRequest(
                                id = id.toInt(),
                                name = name,
                                description = description,
                                startDate = startDate,
                                endDate = endDate,
                                latitude = latitude.toDouble(),
                                longitude = longitude.toDouble(),
                                address = address,
                                maxParticipants = maxParticipants.toInt(),
                                isPublic = true,
                                category = selectedCategory!!.backendValue,
                                price = if (price.isBlank()) null else price.toDouble(),
                                imageUrl = null
                            )

                            val response = RetrofitInstance.api.updateEvent(
                                token = "Bearer $savedToken",
                                event = event
                            )

                            if (response.isSuccessful) {
                                resultText = "Evento actualizado correctamente"
                                onBackToHome()
                            } else {
                                resultText = "Error al actualizar evento: ${response.code()}"
                            }
                        } catch (e: Exception) {
                            resultText = "Excepción al actualizar evento: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Guardar cambios")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { selectedEvent = null },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver a la lista")
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
                    color = if (
                        resultText.contains("correctamente", ignoreCase = true)
                    ) Color(0xFF2E7D32) else Color.DarkGray
                )
            }
        }
    }
}
