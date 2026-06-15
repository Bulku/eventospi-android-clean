package com.leonvelez.eventospi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leonvelez.eventospi.data.model.EventResponse
import com.leonvelez.eventospi.data.model.ReactionSummaryResponse
import com.leonvelez.eventospi.ui.model.EventReactionType
import com.leonvelez.eventospi.ui.model.MAP_CATEGORY_OPTIONS
import com.leonvelez.eventospi.ui.model.MapVisualFilter
import com.leonvelez.eventospi.utils.eventCategoryDisplayLabel
import com.leonvelez.eventospi.utils.formatEventDateCompact
import com.leonvelez.eventospi.utils.mapCategoryLabel

@Composable
fun MapEventCard(
    event: EventResponse,
    isLoggedIn: Boolean,
    isRegistered: Boolean,
    isCreator: Boolean,
    isCancelledLocally: Boolean,
    participantsCount: Int?,
    currentReaction: EventReactionType?,
    reactionSummary: ReactionSummaryResponse?,
    message: String,
    onRegisterClick: () -> Unit,
    onCancelRegistrationClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onViewParticipantsClick: () -> Unit,
    onReactionSelected: (EventReactionType) -> Unit,
    onClose: () -> Unit,
    onImageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formatEventDateCompact(event.startDate),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp
                    ),
                    color = Color(0xFF4F67A8)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = event.address,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Creado por: ${event.createdByUserName}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InfoPill(eventCategoryDisplayLabel(event.category))
                    InfoPill("Cupo ${event.maxParticipants}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (event.price == null) {
                            "Precio: Gratis"
                        } else {
                            "Precio: ${event.price}"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        )
                    )

                    Text(
                        text = if (participantsCount == null) {
                            "Inscritos: --"
                        } else {
                            "Inscritos: $participantsCount"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp
                        )
                    )
                }

                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (isCreator) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onEditClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Modificar",
                                fontSize = 13.sp
                            )
                        }

                        Button(
                            onClick = onViewParticipantsClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Ver inscritos",
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Eliminar evento",
                            fontSize = 13.sp
                        )
                    }
                } else if (isCancelledLocally) {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Inscripción cancelada",
                            fontSize = 13.sp
                        )
                    }
                } else if (isLoggedIn && isRegistered) {
                    Button(
                        onClick = onCancelRegistrationClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Cancelar inscripción",
                            fontSize = 13.sp
                        )
                    }
                } else if (isLoggedIn) {
                    Button(
                        onClick = onRegisterClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (event.isPublic) {
                                "Inscribirme"
                            } else {
                                "Solicitar inscripción"
                            },
                            fontSize = 13.sp
                        )
                    }
                }else {
                    Text(
                        text = "Inicia sesión para interactuar",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(onClick = onClose) {
                    Text("Cerrar")
                }
            }

            Column(
                modifier = Modifier.width(118.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EventImageFromUrl(
                    imageUrl = event.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(118.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    onClick = onImageClick
                )

                EventReactionPanel(
                    selectedReaction = currentReaction,
                    reactionSummary = reactionSummary,
                    onReactionSelected = onReactionSelected
                )
            }
        }
    }
}

@Composable
fun MarkerLegendCard() {
    // =========================================================
    // 1) TAMAÑO GENERAL DEL BLOQUE
    // =========================================================
    val panelWidth = 133.dp          // Ancho total del bloque
    val panelStartPadding = 1.dp     // Separación desde el borde izquierdo
    val verticalSpacing = 3.dp       // Espacio entre filas
    val innerPadding = 6.dp          // Espacio interno del cuadro

    // =========================================================
    // 2) CUADRO / TRANSPARENCIA / BORDE
    // =========================================================
    val boxCorner = 12.dp
    val boxBackground = Color.White.copy(alpha = 0.90f)   // Fondo del cuadro
    val boxBorder = Color.Green.copy(alpha = 0.33f)       // Borde del cuadro
    val boxBorderWidth = 1.dp

    // Si quieres MÁS visible:
    // val boxBackground = Color.White.copy(alpha = 0.25f)
    // val boxBorder = Color.Black.copy(alpha = 0.10f)

    // Si quieres MÁS tenue:
    // val boxBackground = Color.White.copy(alpha = 0.10f)
    // val boxBorder = Color.Black.copy(alpha = 0.03f)

    // =========================================================
    // 3) TAMAÑO Y ESTILO DEL TEXTO
    // =========================================================
    val titleFontSize = 18.sp
    val itemFontSize = 15.sp

    val titleColor = Color.Black
    val itemColor = Color.Black

    val titleWeight = FontWeight.Bold
    val itemWeight = FontWeight.Bold

    // =========================================================
    // 4) TAMAÑO Y ESPACIADO DE LOS PUNTOS DE COLOR
    // =========================================================
    val dotSize = 13.dp
    val rowSpacing = 5.dp

    // =========================================================
    // 5) COLORES DE CADA ESTADO
    // =========================================================
    val activeColor = Color(0xFF2E7D32)
    val registeredColor = Color(0xFF1565C0)
    val selectedColor = Color(0xFF7B1FA2)

    Box(
        modifier = Modifier
            .width(panelWidth)
            .padding(start = panelStartPadding)
            .background(
                color = boxBackground,
                shape = RoundedCornerShape(boxCorner)
            )
            .border(
                width = boxBorderWidth,
                color = boxBorder,
                shape = RoundedCornerShape(boxCorner)
            )
            .padding(innerPadding)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(verticalSpacing)
        ) {
            Text(
                text = "Marcadores",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = titleWeight,
                    color = titleColor,
                    fontSize = titleFontSize
                )
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(rowSpacing)
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .background(activeColor, CircleShape)
                )
                Text(
                    text = "Activo",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = itemWeight,
                        color = itemColor,
                        fontSize = itemFontSize
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(rowSpacing)
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .background(registeredColor, CircleShape)
                )
                Text(
                    text = "Inscrito",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = itemWeight,
                        color = itemColor,
                        fontSize = itemFontSize
                    )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(rowSpacing)
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .background(selectedColor, CircleShape)
                )
                Text(
                    text = "Seleccionado",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = itemWeight,
                        color = itemColor,
                        fontSize = itemFontSize
                    )
                )
            }
        }
    }
}

@Composable
fun MapFilterCard(
    visualFilter: MapVisualFilter,
    selectedCategory: Int?,
    onFilterSelected: (MapVisualFilter) -> Unit,
    onCategorySelected: (Int?) -> Unit
) {
    var filterExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // =========================================================
    // 1) TAMAÑO GENERAL DEL BLOQUE
    // =========================================================
    val panelWidth = 145.dp              // Ancho total del bloque "Filtro"
    val verticalSpacing = 8.dp           // Espacio entre título y campos

    // =========================================================
    // 2) ANCHO DE LOS CAMPOS
    // =========================================================
    val viewFieldWidth = 105.dp          // Ancho del campo "Vista" (ej: Todos)
    val categoryFieldWidth = 105.dp      // Ancho del campo "Categoría" (ej: Gastronomía)

    // =========================================================
    // 3) TAMAÑO Y ESTILO DE TEXTO
    // =========================================================
    val titleFontSize = 18.sp            // Tamaño de "Filtro"
    val labelFontSize = 15.sp            // Tamaño de "Vista" y "Categoría"
    val valueFontSize = 15.sp            // Tamaño de "Todos", "Gastronomía", etc.
    val dropdownFontSize = 15.sp         // Tamaño de las opciones del menú desplegable

    val titleColor = Color.Black
    val labelColor = Color.Black
    val valueColor = Color.Black
    val placeholderColor = Color.Black.copy(alpha = 0.8f)

    val titleWeight = FontWeight.Bold
    val labelWeight = FontWeight.Bold
    val valueWeight = FontWeight.Bold
    val dropdownWeight = FontWeight.Bold

    // =========================================================
    // 4) TRANSPARENCIA / BORDE DEL CAMPO
    // =========================================================
    // alpha 0.01f = casi invisible
    // alpha 0.03f = muy suave
    // alpha 0.05f = suave pero visible
    val subtleBorder = Color.White.copy(alpha = 0.90f)      // Borde normal
    val subtleBackground = Color.White.copy(alpha = 0.90f)  // Fondo normal
    val focusedBorder = Color.Black.copy(alpha = 0.08f)     // Borde al tocar
    val cursorColor = Color.Black.copy(alpha = 0.4f)        // Cursor

    Column(
        modifier = Modifier.width(panelWidth),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        Text(
            text = "Filtro",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = titleWeight,
                color = titleColor,
                fontSize = titleFontSize
            )
        )

        // =====================================================
        // 5) CAMPO "VISTA"
        // =====================================================
        Box {
            OutlinedTextField(
                value = visualFilter.label,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = valueWeight,
                    color = valueColor,
                    fontSize = valueFontSize
                ),
                modifier = Modifier.width(viewFieldWidth),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = subtleBorder,
                    focusedBorderColor = focusedBorder,
                    unfocusedContainerColor = subtleBackground,
                    focusedContainerColor = subtleBackground,
                    focusedTextColor = valueColor,
                    unfocusedTextColor = valueColor,
                    focusedLabelColor = labelColor,
                    unfocusedLabelColor = labelColor.copy(alpha = 0.9f),
                    cursorColor = cursorColor
                )
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { filterExpanded = true }
            )

            DropdownMenu(
                expanded = filterExpanded,
                onDismissRequest = { filterExpanded = false }
            ) {
                MapVisualFilter.entries.forEach { filter ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = filter.label,
                                fontWeight = dropdownWeight,
                                fontSize = dropdownFontSize
                            )
                        },
                        onClick = {
                            onFilterSelected(filter)
                            filterExpanded = false
                        }
                    )
                }
            }
        }

        // =====================================================
        // 6) CAMPO "CATEGORÍA" (solo aparece cuando aplica)
        // =====================================================
        if (visualFilter == MapVisualFilter.PorCategoria) {
            Box {
                OutlinedTextField(
                    value = selectedCategory?.let { mapCategoryLabel(it) }.orEmpty(),
                    onValueChange = {},
                    label = {
                        Text(
                            "Categoría",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = labelWeight,
                                color = labelColor,
                                fontSize = labelFontSize
                            )
                        )
                    },
                    placeholder = {
                        Text(
                            "Elegir",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = valueWeight,
                                color = placeholderColor,
                                fontSize = valueFontSize
                            )
                        )
                    },
                    readOnly = true,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = valueWeight,
                        color = valueColor,
                        fontSize = valueFontSize
                    ),
                    modifier = Modifier.width(categoryFieldWidth),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = subtleBorder,
                        focusedBorderColor = focusedBorder,
                        unfocusedContainerColor = subtleBackground,
                        focusedContainerColor = subtleBackground,
                        focusedTextColor = valueColor,
                        unfocusedTextColor = valueColor,
                        focusedLabelColor = labelColor,
                        unfocusedLabelColor = labelColor.copy(alpha = 0.8f),
                        cursorColor = cursorColor
                    )
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { categoryExpanded = true }
                )

                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    MAP_CATEGORY_OPTIONS.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.second,
                                    fontWeight = dropdownWeight,
                                    fontSize = dropdownFontSize
                                )
                            },
                            onClick = {
                                onCategorySelected(option.first)
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
