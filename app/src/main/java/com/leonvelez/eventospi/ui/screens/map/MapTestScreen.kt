package com.leonvelez.eventospi.ui.screens.map

import android.os.Bundle
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.leonvelez.eventospi.data.model.EventResponse
import com.leonvelez.eventospi.ui.model.MapVisualFilter
import com.leonvelez.eventospi.utils.createCircleMarkerBitmap
import com.leonvelez.eventospi.utils.dpToPx
import com.leonvelez.eventospi.utils.filterMapEvents
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun MapTestScreen(
    events: List<EventResponse>,
    registeredEventIds: Set<Int>,
    cancelledEventIds: Set<Int>,
    selectedEventId: Int?,
    visualFilter: MapVisualFilter,
    selectedCategory: Int?,
    onMapLongPress: (LatLng) -> Unit = {},
    onMarkerClick: (EventResponse) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val iconFactory = remember { org.maplibre.android.annotations.IconFactory.getInstance(context) }

    val greenIcon = remember {
        iconFactory.fromBitmap(
            createCircleMarkerBitmap(
                fillColor = android.graphics.Color.parseColor("#2E7D32"),
                sizePx = dpToPx(context, 24f)
            )
        )
    }

    val blueIcon = remember {
        iconFactory.fromBitmap(
            createCircleMarkerBitmap(
                fillColor = android.graphics.Color.parseColor("#1565C0"),
                sizePx = dpToPx(context, 24f)
            )
        )
    }
    val redIcon = remember {
        iconFactory.fromBitmap(
            createCircleMarkerBitmap(
                fillColor = android.graphics.Color.parseColor("#C62828"),
                sizePx = dpToPx(context, 24f)
            )
        )
    }

    val purpleIcon = remember {
        iconFactory.fromBitmap(
            createCircleMarkerBitmap(
                fillColor = android.graphics.Color.parseColor("#7B1FA2"),
                sizePx = dpToPx(context, 24f)
            )
        )
    }

    var mapLibreMapRef by remember {
        mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null)
    }
    var mapReady by remember { mutableStateOf(false) }
    val markerEventMap = remember { mutableMapOf<Long, EventResponse>() }

    val filteredEvents = remember(events, registeredEventIds, visualFilter, selectedCategory) {
        filterMapEvents(
            events = events,
            registeredEventIds = registeredEventIds,
            visualFilter = visualFilter,
            selectedCategory = selectedCategory
        )
    }

    val mapView = remember {
        MapLibre.getInstance(context)

        val mapOptions = MapLibreMapOptions()
            .textureMode(true)

        MapView(context, mapOptions).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            onCreate(Bundle())

            getMapAsync { map ->
                mapLibreMapRef = map

                map.setOnMarkerClickListener { marker ->
                    val event = markerEventMap[marker.id]
                    if (event != null) {
                        onMarkerClick(event)
                        true
                    } else {
                        false
                    }
                }

                map.addOnMapLongClickListener { point ->
                    onMapLongPress(point)
                    true
                }

                map.setStyle(
                    Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright")
                ) {
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(6.2442, -75.5812))
                        .zoom(10.5)
                        .build()

                    mapReady = true
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                mapView.onStart()
            }

            override fun onResume(owner: LifecycleOwner) {
                mapView.onResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                mapView.onPause()
            }

            override fun onStop(owner: LifecycleOwner) {
                mapView.onStop()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                mapView.onDestroy()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { mapView },
        update = {
            val map = mapLibreMapRef ?: return@AndroidView
            if (!mapReady) return@AndroidView

            map.removeAnnotations()
            markerEventMap.clear()

            filteredEvents.forEach { event ->
                val icon = when {
                    selectedEventId == event.id -> purpleIcon
                    cancelledEventIds.contains(event.id) -> redIcon
                    registeredEventIds.contains(event.id) -> blueIcon
                    else -> greenIcon
                }

                val marker = map.addMarker(
                    org.maplibre.android.annotations.MarkerOptions()
                        .position(LatLng(event.latitude, event.longitude))
                        .title(event.name)
                        .snippet(event.address)
                        .icon(icon)
                )

                markerEventMap[marker.id] = event
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
