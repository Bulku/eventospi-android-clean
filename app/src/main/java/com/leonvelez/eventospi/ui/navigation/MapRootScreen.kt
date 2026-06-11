package com.leonvelez.eventospi.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.leonvelez.eventospi.data.model.EventResponse
import com.leonvelez.eventospi.data.model.ReactionSummaryResponse
import com.leonvelez.eventospi.data.remote.RetrofitInstance
import com.leonvelez.eventospi.data.TokenManager
import com.leonvelez.eventospi.ui.model.EventReactionType
import com.leonvelez.eventospi.ui.model.MapVisualFilter
import com.leonvelez.eventospi.ui.screens.auth.LoginScreen
import com.leonvelez.eventospi.ui.screens.auth.RegisterScreen
import com.leonvelez.eventospi.ui.screens.dashboard.DashboardScreen
import com.leonvelez.eventospi.ui.screens.events.CreateEventScreen
import com.leonvelez.eventospi.ui.screens.events.EventsListScreen
import com.leonvelez.eventospi.ui.screens.events.PendingRequestsScreen
import com.leonvelez.eventospi.ui.screens.events.RegisteredEventsScreen
import com.leonvelez.eventospi.ui.screens.events.UpdateEventScreen
import com.leonvelez.eventospi.ui.screens.map.MapShellScreen
import com.leonvelez.eventospi.ui.screens.profile.ChangePasswordScreen
import com.leonvelez.eventospi.ui.screens.profile.ProfileHubScreen
import com.leonvelez.eventospi.ui.screens.profile.ProfileImageScreen
import com.leonvelez.eventospi.utils.cancelRegistrationFailureMessage
import com.leonvelez.eventospi.utils.clearEventRegistrationCancelledForSession
import com.leonvelez.eventospi.utils.extractUserNameFromAuthMessage
import com.leonvelez.eventospi.utils.markEventRegistrationCancelledForSession
import com.leonvelez.eventospi.utils.registrationFailureMessage
import com.leonvelez.eventospi.utils.resetSessionCancelledRegistrations
import com.leonvelez.eventospi.utils.toBackendId
import kotlinx.coroutines.launch

@Composable
fun MapRootScreen() {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("map") }
    var registeredEmail by remember { mutableStateOf("") }
    var loginMessage by remember { mutableStateOf("Ingresa tus datos") }

    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var mapEvents by remember { mutableStateOf<List<EventResponse>>(emptyList()) }
    var registeredMapEvents by remember { mutableStateOf<List<EventResponse>>(emptyList()) }
    var selectedMapEvent by remember { mutableStateOf<EventResponse?>(null) }
    var selectedUpdateEventId by remember { mutableStateOf<Int?>(null) }
    var selectedEventParticipantsCount by remember { mutableStateOf<Int?>(null) }
    var mapMessage by remember { mutableStateOf("") }
    var cancelledEventIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var eventReactions by remember { mutableStateOf<Map<Int, EventReactionType>>(emptyMap()) }
    var reactionSummaries by remember { mutableStateOf<Map<Int, ReactionSummaryResponse>>(emptyMap()) }
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentUserName by remember { mutableStateOf("") }
    var visualFilter by remember { mutableStateOf(MapVisualFilter.Todos) }
    var selectedCategoryFilter by remember { mutableStateOf<Int?>(null) }

    fun reloadMapEvents() {
        scope.launch {
            try {
                val savedToken = tokenManager.getToken()

                if (savedToken.isNullOrBlank()) {
                    mapEvents = emptyList()
                    mapMessage = "Inicia sesión para cargar eventos"
                    return@launch
                }

                val response = RetrofitInstance.api.getEvents(
                    token = "Bearer $savedToken"
                )

                if (response.isSuccessful) {
                    mapEvents = response.body().orEmpty()
                    mapMessage = ""
                } else {
                    mapEvents = emptyList()
                    mapMessage = "Error cargando eventos del mapa: ${response.code()}"
                }
            } catch (e: Exception) {
                mapEvents = emptyList()
                mapMessage = "Excepción cargando eventos del mapa: ${e.message}"
            }
        }
    }
    fun reloadCurrentUser() {
        scope.launch {
            try {
                val savedToken = tokenManager.getToken()

                if (savedToken.isNullOrBlank()) {
                    currentUserName = ""
                    return@launch
                }

                val response = RetrofitInstance.api.getUserAuthenticated(
                    token = "Bearer $savedToken"
                )

                currentUserName = if (response.isSuccessful) {
                    extractUserNameFromAuthMessage(response.body()?.message)
                } else {
                    ""
                }
            } catch (_: Exception) {
                currentUserName = ""
            }
        }
    }

    fun reloadRegisteredMapEvents() {
        scope.launch {
            try {
                val savedToken = tokenManager.getToken()

                if (savedToken.isNullOrBlank()) {
                    registeredMapEvents = emptyList()
                    return@launch
                }

                val response = RetrofitInstance.api.getEventsIAmRegistered(
                    token = "Bearer $savedToken"
                )

                if (response.isSuccessful) {
                    registeredMapEvents = response.body().orEmpty()
                } else {
                    registeredMapEvents = emptyList()
                }
            } catch (_: Exception) {
                registeredMapEvents = emptyList()
            }
        }
    }

    fun reloadAllMapData() {
        if (isLoggedIn) {
            reloadMapEvents()
            reloadRegisteredMapEvents()
            reloadCurrentUser()
        } else {
            mapEvents = emptyList()
            registeredMapEvents = emptyList()
            cancelledEventIds = resetSessionCancelledRegistrations()
            selectedMapEvent = null
            mapMessage = "Inicia sesión para cargar eventos"
            currentUserName = ""
        }
    }

    fun registerToSelectedEvent() {
        val event = selectedMapEvent ?: return

        scope.launch {
            try {
                val savedToken = tokenManager.getToken()

                if (savedToken.isNullOrBlank()) {
                    selectedMapEvent = null
                    loginMessage = "Inicia sesión para inscribirte"
                    currentScreen = "login"
                    return@launch
                }

                val response = RetrofitInstance.api.registerToEvent(
                    token = "Bearer $savedToken",
                    eventId = event.id,
                    cancellationReason = ""
                )

                if (response.isSuccessful) {
                    cancelledEventIds = clearEventRegistrationCancelledForSession(cancelledEventIds, event.id)
                    reloadRegisteredMapEvents()
                    mapMessage = "Inscripción realizada correctamente"
                } else {
                    val errorText = response.errorBody()?.string().orEmpty()

                    mapMessage = registrationFailureMessage(
                        responseCode = response.code(),
                        errorText = errorText
                    )
                }
            } catch (e: Exception) {
                mapMessage = "Excepción al inscribirse: ${e.message}"
            }
        }
    }
    fun cancelRegistrationForSelectedEvent() {
        val event = selectedMapEvent ?: return

        scope.launch {
            try {
                val savedToken = tokenManager.getToken()

                if (savedToken.isNullOrBlank()) {
                    mapMessage = "No hay sesión activa"
                    return@launch
                }

                val response = RetrofitInstance.api.cancelRegistration(
                    token = "Bearer $savedToken",
                    eventId = event.id,
                    cancellationReason = ""
                )

                val errorText = response.errorBody()?.string().orEmpty()

                if (response.isSuccessful) {
                    cancelledEventIds = markEventRegistrationCancelledForSession(cancelledEventIds, event.id)
                    reloadRegisteredMapEvents()
                    mapMessage = "Inscripción cancelada"
                } else {
                    mapMessage = cancelRegistrationFailureMessage(
                        responseCode = response.code(),
                        errorText = errorText
                    )
                }
            } catch (e: Exception) {
                mapMessage = "Excepción al cancelar inscripción: ${e.message}"
            }
        }
    }

    fun deleteSelectedEvent() {
        val event = selectedMapEvent ?: return

        scope.launch {
            try {
                val savedToken = tokenManager.getToken()

                if (savedToken.isNullOrBlank()) {
                    mapMessage = "No hay sesión activa"
                    return@launch
                }

                val response = RetrofitInstance.api.deleteEvent(
                    token = "Bearer $savedToken",
                    id = event.id
                )

                if (response.isSuccessful) {
                    mapEvents = mapEvents.filterNot { it.id == event.id }
                    registeredMapEvents = registeredMapEvents.filterNot { it.id == event.id }
                    selectedMapEvent = null
                    mapMessage = "Evento eliminado correctamente"
                    reloadMapEvents()
                } else {
                    mapMessage = "Error al eliminar evento: ${response.code()}"
                }
            } catch (e: Exception) {
                mapMessage = "Excepción al eliminar evento: ${e.message}"
            }
        }
    }
    fun loadSelectedEventParticipantsCount(eventId: Int) {
        scope.launch {
            try {
                val savedToken = tokenManager.getToken()

                if (savedToken.isNullOrBlank()) {
                    selectedEventParticipantsCount = null
                    return@launch
                }

                val response = RetrofitInstance.api.getParticipantsByEventId(
                    token = "Bearer $savedToken",
                    eventId = eventId
                )

                selectedEventParticipantsCount = if (response.isSuccessful) {
                    response.body()?.size ?: 0
                } else {
                    null
                }
            } catch (_: Exception) {
                selectedEventParticipantsCount = null
            }
        }
    }
    fun loadReactionSummary(eventId: Int) {
        scope.launch {
            try {
                val savedToken = tokenManager.getToken()

                if (savedToken.isNullOrBlank()) {
                    return@launch
                }

                val response = RetrofitInstance.api.getReactionsByEventId(
                    token = "Bearer $savedToken",
                    eventId = eventId
                )

                if (response.isSuccessful) {
                    val summary = response.body() ?: ReactionSummaryResponse()
                    reactionSummaries = reactionSummaries + (eventId to summary)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun reactToSelectedEvent(reaction: EventReactionType) {
        scope.launch {
            val savedToken = tokenManager.getToken()
            val event = selectedMapEvent

            if (savedToken.isNullOrBlank()) {
                mapMessage = "No hay sesión activa"
                currentScreen = "login"
                return@launch
            }

            if (event == null) {
                return@launch
            }

            try {
                val currentSelected = eventReactions[event.id]

                val response = if (currentSelected == reaction) {
                    RetrofitInstance.api.deleteReaction(
                        token = "Bearer $savedToken",
                        eventId = event.id
                    )
                } else {
                    RetrofitInstance.api.reactToEvent(
                        token = "Bearer $savedToken",
                        eventId = event.id,
                        reactionTypeId = reaction.toBackendId()
                    )
                }

                if (response.isSuccessful) {
                    eventReactions =
                        if (currentSelected == reaction) {
                            eventReactions - event.id
                        } else {
                            eventReactions + (event.id to reaction)
                        }

                    loadReactionSummary(event.id)
                    mapMessage = ""
                } else {
                    mapMessage = "Error procesando reacción: ${response.code()}"
                }
            } catch (e: Exception) {
                mapMessage = "Excepción procesando reacción: ${e.message}"
            }
        }
    }

    LaunchedEffect(isLoggedIn) {
        reloadAllMapData()
    }
    LaunchedEffect(selectedMapEvent?.id) {
        val selectedId = selectedMapEvent?.id

        if (selectedId != null) {
            selectedEventParticipantsCount = null
            loadReactionSummary(selectedId)
        } else {
            selectedEventParticipantsCount = null
        }
    }

    when (currentScreen) {
        "map" -> {
            MapShellScreen(
                isLoggedIn = isLoggedIn,
                profileImageUri = profileImageUri,
                mapEvents = mapEvents,
                registeredEventIds = registeredMapEvents.map { it.id }.toSet(),
                cancelledEventIds = cancelledEventIds,
                currentUserName = currentUserName,
                selectedMapEvent = selectedMapEvent,
                selectedEventParticipantsCount = selectedEventParticipantsCount,
                currentReaction = selectedMapEvent?.let { eventReactions[it.id] },
                reactionSummary = selectedMapEvent?.let { reactionSummaries[it.id] },
                mapMessage = mapMessage,
                visualFilter = visualFilter,
                selectedCategoryFilter = selectedCategoryFilter,
                onFilterSelected = { filter ->
                    visualFilter = filter
                    if (filter != MapVisualFilter.PorCategoria) {
                        selectedCategoryFilter = null
                    }
                    selectedMapEvent = null
                    mapMessage = ""
                },
                onCategoryFilterSelected = { category ->
                    selectedCategoryFilter = category
                    selectedMapEvent = null
                    mapMessage = ""
                },
                onOpenLogin = { currentScreen = "login" },
                onOpenRegister = { currentScreen = "register" },
                onOpenCreateEvent = { currentScreen = "createEvent" },
                onOpenEventsList = { currentScreen = "eventsList" },
                onOpenRegisteredEvents = { currentScreen = "registeredEvents" },
                onOpenPendingRequests = { currentScreen = "pendingRequests" },
                onOpenDashboard = { currentScreen = "dashboard" },
                onOpenUpdateEvent = { currentScreen = "updateEvent" },
                onOpenProfileImage = { currentScreen = "profileHub" },
                onOpenChangePassword = { currentScreen = "changePassword" },
                onLogout = {
                    tokenManager.clearToken()
                    isLoggedIn = false
                    registeredEmail = ""
                    loginMessage = "Sesión cerrada"
                    selectedMapEvent = null
                    reactionSummaries = emptyMap()
                    eventReactions = emptyMap()
                    mapMessage = ""
                    registeredMapEvents = emptyList()
                    cancelledEventIds = resetSessionCancelledRegistrations()
                    currentUserName = ""
                    currentScreen = "map"
                },
                onMapLongPress = { lat, lng ->
                    if (isLoggedIn) {
                        selectedLatitude = lat
                        selectedLongitude = lng
                        currentScreen = "createEvent"
                    } else {
                        loginMessage = "Inicia sesión para crear un evento desde el mapa"
                        currentScreen = "login"
                    }
                },
                onMarkerClick = { event ->
                    selectedMapEvent = event
                    mapMessage = ""
                },
                onDismissSelectedEvent = {
                    selectedMapEvent = null
                    mapMessage = ""
                },
                onRegisterToSelectedEvent = {
                    registerToSelectedEvent()
                },
                onCancelRegistrationSelectedEvent = {
                    cancelRegistrationForSelectedEvent()
                },
                onDeleteSelectedEvent = {
                    deleteSelectedEvent()
                },
                onEditSelectedEvent = {
                    selectedUpdateEventId = selectedMapEvent?.id
                    currentScreen = "updateEvent"
                },
                onViewParticipantsSelectedEvent = {
                    val eventId = selectedMapEvent?.id
                    if (eventId != null) {
                        loadSelectedEventParticipantsCount(eventId)
                    }
                },
                onReactionSelected = { reaction: EventReactionType ->
                    reactToSelectedEvent(reaction)
                }
            )
        }

        "login" -> {
            LoginScreen(
                onGoToRegister = { currentScreen = "register" },
                onLoginSuccess = {
                    isLoggedIn = true
                    currentScreen = "map"
                    reloadAllMapData()
                },
                initialEmail = registeredEmail,
                initialMessage = loginMessage
            )
        }

        "register" -> {
            RegisterScreen(
                onBackToLogin = { currentScreen = "login" },
                onRegisterSuccess = { email ->
                    registeredEmail = email
                    loginMessage = "Cuenta creada correctamente. Ahora inicia sesión"
                    currentScreen = "login"
                }
            )
        }

        "createEvent" -> {
            CreateEventScreen(
                onBackToHome = {
                    reloadAllMapData()
                    currentScreen = "map"
                },
                initialLatitude = selectedLatitude,
                initialLongitude = selectedLongitude
            )
        }

        "eventsList" -> {
            EventsListScreen(
                onBackToHome = {
                    reloadAllMapData()
                    currentScreen = "map"
                }
            )
        }
        "dashboard" -> {
            DashboardScreen(
                onBackToHome = {
                    reloadAllMapData()
                    currentScreen = "map"
                }
            )
        }

        "registeredEvents" -> {
            RegisteredEventsScreen(
                onBackToHome = {
                    selectedMapEvent = null
                    mapMessage = ""
                    currentScreen = "map"
                },
                onRegisteredEventsChanged = { updatedEvents ->
                    registeredMapEvents = updatedEvents
                    selectedMapEvent = null
                    mapMessage = ""
                },
                onEventCancelled = { eventId ->
                    cancelledEventIds = markEventRegistrationCancelledForSession(cancelledEventIds, eventId)
                }
            )
        }

        "pendingRequests" -> {
            PendingRequestsScreen(
                onBackToHome = { currentScreen = "map" }
            )
        }

        "updateEvent" -> {
            UpdateEventScreen(
                onBackToHome = {
                    selectedUpdateEventId = null
                    reloadAllMapData()
                    currentScreen = "map"
                },
                initialSelectedEventId = selectedUpdateEventId
            )
        }
        "profileHub" -> {
            ProfileHubScreen(
                email = registeredEmail,
                userName = currentUserName,
                onBackToHome = {
                    currentScreen = "map"
                },
                onOpenProfileImage = {
                    currentScreen = "profileImage"
                },
                onOpenChangePassword = {
                    currentScreen = "changePassword"
                },
                onOpenPendingRequests = {
                    currentScreen = "pendingRequests"
                }
            )
        }
        "profileImage" -> {
            ProfileImageScreen(
                onBackToHome = { currentScreen = "map" },
                onImageUploaded = { uri ->
                    profileImageUri = uri
                }
            )
        }

        "changePassword" -> {
            ChangePasswordScreen(
                onBackToHome = { currentScreen = "map" }
            )
        }
    }
}
