package com.leonvelez.eventospi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.leonvelez.eventospi.data.model.EventResponse
import com.leonvelez.eventospi.ui.model.MapVisualFilter
import org.maplibre.android.maps.Style

fun filterMapEvents(
    events: List<EventResponse>,
    registeredEventIds: Set<Int>,
    visualFilter: MapVisualFilter,
    selectedCategory: Int?
): List<EventResponse> {
    return when (visualFilter) {
        MapVisualFilter.Todos -> events
        MapVisualFilter.MisInscripciones -> events.filter { registeredEventIds.contains(it.id) }
        MapVisualFilter.PorCategoria -> {
            if (selectedCategory == null) emptyList()
            else events.filter { it.category == selectedCategory }
        }
    }
}

fun createCircleMarkerBitmap(
    fillColor: Int,
    sizePx: Int = 72,
    strokePx: Float = 6f
): Bitmap {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
    }

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = strokePx
    }

    val radius = (sizePx / 2f) - strokePx
    val center = sizePx / 2f

    canvas.drawCircle(center, center, radius, fillPaint)
    canvas.drawCircle(center, center, radius, strokePaint)

    return bitmap
}

fun dpToPx(context: android.content.Context, dp: Float): Int {
    return (dp * context.resources.displayMetrics.density).toInt()
}
