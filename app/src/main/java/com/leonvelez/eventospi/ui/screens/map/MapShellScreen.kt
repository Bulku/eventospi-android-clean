package com.leonvelez.eventospi.ui.screens.map

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leonvelez.eventospi.data.model.EventResponse
import com.leonvelez.eventospi.data.model.ReactionSummaryResponse
import com.leonvelez.eventospi.ui.components.FullScreenEventImageDialog
import com.leonvelez.eventospi.ui.components.MapEventCard
import com.leonvelez.eventospi.ui.components.MapFilterCard
import com.leonvelez.eventospi.ui.components.MarkerLegendCard
import com.leonvelez.eventospi.ui.components.MenuActionButton
import com.leonvelez.eventospi.ui.components.ProfileAvatarButton
import com.leonvelez.eventospi.ui.model.EventReactionType
import com.leonvelez.eventospi.ui.model.MapVisualFilter
import com.leonvelez.eventospi.utils.isEventOwnedByUserName

@Composable
fun MapShellScreen(
    isLoggedIn: Boolean,
    profileImageUri: Uri?,
    mapEvents: List<EventResponse>,
    registeredEventIds: Set<Int>,
    cancelledEventIds: Set<Int>,
    currentUserName: String,
    selectedMapEvent: EventResponse?,
    selectedEventParticipantsCount: Int?,
    currentReaction: EventReactionType?,
    reactionSummary: ReactionSummaryResponse?,
    mapMessage: String,
    visualFilter: MapVisualFilter,
    selectedCategoryFilter: Int?,
    onFilterSelected: (MapVisualFilter) -> Unit,
    onCategoryFilterSelected: (Int?) -> Unit,
    onOpenLogin: () -> Unit,
    onOpenRegister: () -> Unit,
    onOpenCreateEvent: () -> Unit,
    onOpenEventsList: () -> Unit,
    onOpenRegisteredEvents: () -> Unit,
    onOpenPendingRequests: () -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenUpdateEvent: () -> Unit,
    onOpenProfileImage: () -> Unit,
    onOpenChangePassword: () -> Unit,
    onLogout: () -> Unit,
    onMapLongPress: (Double, Double) -> Unit,
    onMarkerClick: (EventResponse) -> Unit,
    onDismissSelectedEvent: () -> Unit,
    onRegisterToSelectedEvent: () -> Unit,
    onCancelRegistrationSelectedEvent: () -> Unit,
    onViewParticipantsSelectedEvent: () -> Unit,
    onDeleteSelectedEvent: () -> Unit,
    onEditSelectedEvent: () -> Unit,
    onReactionSelected: (EventReactionType) -> Unit
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    var showFullScreenImage by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        MapTestScreen(
            events = mapEvents,
            registeredEventIds = registeredEventIds,
            cancelledEventIds = cancelledEventIds,
            selectedEventId = selectedMapEvent?.id,
            visualFilter = visualFilter,
            selectedCategory = selectedCategoryFilter,
            onMapLongPress = { point ->
                onMapLongPress(point.latitude, point.longitude)
            },
            onMarkerClick = onMarkerClick
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 56.dp, start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MarkerLegendCard()

            MapFilterCard(
                visualFilter = visualFilter,
                selectedCategory = selectedCategoryFilter,
                onFilterSelected = onFilterSelected,
                onCategorySelected = onCategoryFilterSelected
            )
        }

        if (isMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.18f))
                    .clickable { isMenuOpen = false }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp, end = 20.dp),
            horizontalAlignment = Alignment.End
        ) {
            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { isMenuOpen = !isMenuOpen },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Abrir menú"
                    )
                }
            }

            if (isMenuOpen) {
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.width(280.dp),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Menú",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.weight(1f)
                            )

                            if (isLoggedIn) {
                                ProfileAvatarButton(
                                    profileImageUri = profileImageUri,
                                    onClick = {
                                        isMenuOpen = false
                                        onOpenProfileImage()
                                    }
                                )
                            }
                        }

                        if (!isLoggedIn) {
                            MenuActionButton("Iniciar sesión") {
                                isMenuOpen = false
                                onOpenLogin()
                            }

                            MenuActionButton("Registrarse") {
                                isMenuOpen = false
                                onOpenRegister()
                            }
                        } else {
                            MenuActionButton("Perfil") {
                                isMenuOpen = false
                                onOpenProfileImage()
                            }
                            MenuActionButton("Crear evento") {
                                isMenuOpen = false
                                onOpenCreateEvent()
                            }

                            MenuActionButton("Ver eventos") {
                                isMenuOpen = false
                                onOpenEventsList()
                            }

                            MenuActionButton("Mis inscripciones") {
                                isMenuOpen = false
                                onOpenRegisteredEvents()
                            }


                            MenuActionButton("Actualizar evento") {
                                isMenuOpen = false
                                onOpenUpdateEvent()
                            }

                            MenuActionButton("Dashboard") {
                                isMenuOpen = false
                                onOpenDashboard()
                            }


                            MenuActionButton("Cerrar sesión") {
                                isMenuOpen = false
                                onLogout()
                            }
                        }
                    }
                }
            }
        }

        if (selectedMapEvent != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                MapEventCard(
                    event = selectedMapEvent,
                    isLoggedIn = isLoggedIn,
                    isRegistered = registeredEventIds.contains(selectedMapEvent.id) &&
                            !cancelledEventIds.contains(selectedMapEvent.id),
                    isCreator = isEventOwnedByUserName(
                        currentUserName = currentUserName,
                        event = selectedMapEvent
                    ),
                    isCancelledLocally = cancelledEventIds.contains(selectedMapEvent.id),
                    participantsCount = selectedEventParticipantsCount,
                    currentReaction = currentReaction,
                    reactionSummary = reactionSummary,
                    message = mapMessage,
                    onRegisterClick = onRegisterToSelectedEvent,
                    onCancelRegistrationClick = onCancelRegistrationSelectedEvent,
                    onEditClick = onEditSelectedEvent,
                    onDeleteClick = onDeleteSelectedEvent,
                    onViewParticipantsClick = onViewParticipantsSelectedEvent,
                    onReactionSelected = onReactionSelected,
                    onClose = {
                        showFullScreenImage = false
                        onDismissSelectedEvent()
                    },
                    onImageClick = {
                        if (!selectedMapEvent.imageUrl.isNullOrBlank()) {
                            showFullScreenImage = true
                        }
                    }
                )
            }
        }

        if (
            showFullScreenImage &&
            selectedMapEvent != null &&
            !selectedMapEvent.imageUrl.isNullOrBlank()
        ) {
            FullScreenEventImageDialog(
                imageUrl = selectedMapEvent.imageUrl!!,
                onDismiss = { showFullScreenImage = false }
            )
        }

        if (selectedMapEvent == null && mapMessage.isNotBlank()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = mapMessage,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}
