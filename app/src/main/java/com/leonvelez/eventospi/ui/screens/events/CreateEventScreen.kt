package com.leonvelez.eventospi.ui.screens.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.leonvelez.eventospi.data.model.EventRequest
import com.leonvelez.eventospi.data.remote.RetrofitInstance
import com.leonvelez.eventospi.ui.components.CategoryDropdownField
import com.leonvelez.eventospi.ui.components.FormScreenContainer
import com.leonvelez.eventospi.ui.components.FormSectionTitle
import com.leonvelez.eventospi.ui.components.PickerLikeField
import com.leonvelez.eventospi.ui.model.EventCategory
import com.leonvelez.eventospi.utils.createImagePartFromUri
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

@Composable
fun CreateEventScreen(
    onBackToHome: () -> Unit,
    initialLatitude: Double?,
    initialLongitude: Double?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenManager = remember { TokenManager(context) }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<EventCategory?>(null) }

    var isPublic by remember { mutableStateOf(true) }

    var latitude by remember { mutableStateOf(initialLatitude?.toString().orEmpty()) }
    var longitude by remember { mutableStateOf(initialLongitude?.toString().orEmpty()) }

    var eventDateText by remember { mutableStateOf("") }
    var startTimeText by remember { mutableStateOf("") }
    var endTimeText by remember { mutableStateOf("") }

    var startDateIso by remember { mutableStateOf("") }
    var endDateIso by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val dateFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    val timeFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("HH:mm") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    fun rebuildIsoDates(
        selectedDate: java.time.LocalDate?,
        startTime: java.time.LocalTime?,
        endTime: java.time.LocalTime?
    ): Pair<String, String> {
        if (selectedDate == null || startTime == null || endTime == null) {
            return "" to ""
        }

        val zone = java.time.ZoneId.systemDefault()

        val startUtc = java.time.LocalDateTime.of(selectedDate, startTime)
            .atZone(zone)
            .withZoneSameInstant(java.time.ZoneOffset.UTC)

        val endUtc = java.time.LocalDateTime.of(selectedDate, endTime)
            .atZone(zone)
            .withZoneSameInstant(java.time.ZoneOffset.UTC)

        val formatter = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

        return startUtc.format(formatter) to endUtc.format(formatter)
    }

    var selectedDate by remember { mutableStateOf<java.time.LocalDate?>(null) }
    var selectedStartTime by remember { mutableStateOf<java.time.LocalTime?>(null) }
    var selectedEndTime by remember { mutableStateOf<java.time.LocalTime?>(null) }

    FormScreenContainer(
        title = "Crear evento",
        subtitle = "Completa la información del evento y publícalo"
    ) {
        FormSectionTitle("Información general")

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del evento") },
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

        PickerLikeField(
            label = "Fecha del evento",
            value = eventDateText,
            placeholder = "Seleccionar fecha",
            onClick = {
                val now = java.util.Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val date = java.time.LocalDate.of(year, month + 1, dayOfMonth)
                        selectedDate = date
                        eventDateText = date.format(dateFormatter)

                        val rebuilt = rebuildIsoDates(
                            selectedDate,
                            selectedStartTime,
                            selectedEndTime
                        )
                        startDateIso = rebuilt.first
                        endDateIso = rebuilt.second
                    },
                    now.get(java.util.Calendar.YEAR),
                    now.get(java.util.Calendar.MONTH),
                    now.get(java.util.Calendar.DAY_OF_MONTH)
                ).show()
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        PickerLikeField(
            label = "Hora de inicio",
            value = startTimeText,
            placeholder = "Seleccionar hora",
            onClick = {
                val now = java.util.Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val time = java.time.LocalTime.of(hour, minute)
                        selectedStartTime = time
                        startTimeText = time.format(timeFormatter)

                        val rebuilt = rebuildIsoDates(
                            selectedDate,
                            selectedStartTime,
                            selectedEndTime
                        )
                        startDateIso = rebuilt.first
                        endDateIso = rebuilt.second
                    },
                    now.get(java.util.Calendar.HOUR_OF_DAY),
                    now.get(java.util.Calendar.MINUTE),
                    true
                ).show()
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        PickerLikeField(
            label = "Hora de finalización",
            value = endTimeText,
            placeholder = "Seleccionar hora",
            onClick = {
                val now = java.util.Calendar.getInstance()
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val time = java.time.LocalTime.of(hour, minute)
                        selectedEndTime = time
                        endTimeText = time.format(timeFormatter)

                        val rebuilt = rebuildIsoDates(
                            selectedDate,
                            selectedStartTime,
                            selectedEndTime
                        )
                        startDateIso = rebuilt.first
                        endDateIso = rebuilt.second
                    },
                    now.get(java.util.Calendar.HOUR_OF_DAY),
                    now.get(java.util.Calendar.MINUTE),
                    true
                ).show()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
        FormSectionTitle("Ubicación")

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitud") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = initialLatitude != null
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitud") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = initialLongitude != null
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
            label = { Text("Máximo de participantes") },
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

        Spacer(modifier = Modifier.height(14.dp))

        FormSectionTitle("Tipo de evento")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { isPublic = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                enabled = !isPublic
            ) {
                Text("Público")
            }

            Button(
                onClick = { isPublic = false },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                enabled = isPublic
            ) {
                Text("Privado")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isPublic) {
                "Los usuarios quedarán inscritos directamente al evento."
            } else {
                "Los usuarios deberán enviar una solicitud y esperar aprobación."
            },
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(14.dp))

        PickerLikeField(
            label = "Imagen del evento",
            value = if (selectedImageUri == null) "" else "Imagen seleccionada",
            placeholder = "Seleccionar imagen",
            onClick = {
                imagePickerLauncher.launch("image/*")
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (
                    name.isBlank() ||
                    description.isBlank() ||
                    address.isBlank() ||
                    latitude.isBlank() ||
                    longitude.isBlank() ||
                    maxParticipants.isBlank() ||
                    selectedCategory == null ||
                    startDateIso.isBlank() ||
                    endDateIso.isBlank()
                ) {
                    resultText = "Completa todos los campos obligatorios"
                    return@Button
                }

                scope.launch {
                    try {
                        isLoading = true
                        resultText = "Creando evento..."

                        val savedToken = tokenManager.getToken()
                        if (savedToken.isNullOrBlank()) {
                            resultText = "No hay sesión activa"
                            return@launch
                        }

                        val eventRequest = EventRequest(
                            id = 0,
                            name = name,
                            description = description,
                            startDate = startDateIso,
                            endDate = endDateIso,
                            latitude = latitude.toDouble(),
                            longitude = longitude.toDouble(),
                            address = address,
                            maxParticipants = maxParticipants.toInt(),
                            isPublic = isPublic,
                            category = selectedCategory!!.backendValue,
                            price = if (price.isBlank()) null else price.toDouble(),
                            imageUrl = null
                        )

                        val response = RetrofitInstance.api.createEvent(
                            token = "Bearer $savedToken",
                            event = eventRequest
                        )

                        if (response.isSuccessful) {
                            val createdEvent = response.body()

                            if (createdEvent != null && selectedImageUri != null) {
                                try {
                                    val imagePart = createImagePartFromUri(
                                        context = context,
                                        uri = selectedImageUri!!
                                    )

                                    val eventIdPart = createdEvent.id
                                        .toString()
                                        .toRequestBody("text/plain".toMediaTypeOrNull())

                                    val imageResponse = RetrofitInstance.api.uploadEventImage(
                                        token = "Bearer $savedToken",
                                        eventId = eventIdPart,
                                        formFile = imagePart
                                    )

                                    resultText = if (imageResponse.isSuccessful) {
                                        "Evento creado correctamente"
                                    } else {
                                        "Evento creado, pero falló la imagen: ${imageResponse.code()}"
                                    }
                                } catch (e: Exception) {
                                    resultText = "Evento creado, pero falló la imagen: ${e.message}"
                                }
                            } else {
                                resultText = "Evento creado correctamente"
                            }

                            onBackToHome()
                        } else {
                            val errorBody = response.errorBody()?.string().orEmpty()
                            resultText = "Error al crear evento: ${response.code()} - $errorBody"
                        }
                    } catch (e: Exception) {
                        resultText = "Excepción al crear evento: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Crear evento")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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