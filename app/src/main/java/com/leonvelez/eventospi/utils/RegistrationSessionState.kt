package com.leonvelez.eventospi.utils

import android.content.Context

/**
 * Maneja el estado local de cancelaciones.
 *
 * Se usa como solución temporal porque el backend puede seguir devolviendo eventos
 * cancelados dentro de "Mis inscripciones".
 *
 * Esta clase guarda los eventos cancelados en SharedPreferences para que el estado
 * se mantenga aunque el usuario cierre y abra la app.
 */
object RegistrationSessionState {

    private const val PREF_NAME = "registration_session_state"

    private fun cancelledKey(userKey: String): String {
        return "cancelled_events_$userKey"
    }

    fun getCancelledEventIds(
        context: Context,
        userKey: String
    ): Set<Int> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        return prefs.getStringSet(cancelledKey(userKey), emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    fun markEventAsCancelled(
        context: Context,
        userKey: String,
        eventId: Int
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val current = getCancelledEventIds(context, userKey)
            .map { it.toString() }
            .toMutableSet()

        current.add(eventId.toString())

        prefs.edit()
            .putStringSet(cancelledKey(userKey), current)
            .apply()
    }

    fun removeCancelledEvent(
        context: Context,
        userKey: String,
        eventId: Int
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        val current = getCancelledEventIds(context, userKey)
            .map { it.toString() }
            .toMutableSet()

        current.remove(eventId.toString())

        prefs.edit()
            .putStringSet(cancelledKey(userKey), current)
            .apply()
    }

    fun clearCancelledEventsForUser(
        context: Context,
        userKey: String
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .remove(cancelledKey(userKey))
            .apply()
    }
}

/**
 * Estas funciones se conservan para no romper el código actual de MapRootScreen.
 * Manejan el estado en memoria mientras la app está abierta.
 */
fun markEventRegistrationCancelledForSession(
    currentCancelledEventIds: Set<Int>,
    eventId: Int
): Set<Int> {
    return currentCancelledEventIds + eventId
}

fun clearEventRegistrationCancelledForSession(
    currentCancelledEventIds: Set<Int>,
    eventId: Int
): Set<Int> {
    return currentCancelledEventIds - eventId
}

fun resetSessionCancelledRegistrations(): Set<Int> {
    return emptySet()
}

fun registrationFailureMessage(
    responseCode: Int,
    errorText: String
): String {
    return when {
        errorText.contains("Ya estás registrado en este evento", ignoreCase = true) ->
            "Ya estás inscrito o tu registro anterior sigue existiendo"

        errorText.contains("Tu solicitud para este evento aún está pendiente", ignoreCase = true) ->
            "Tu solicitud para este evento aún está pendiente de aprobación"

        errorText.contains("Tu solicitud para este evento fue rechazada", ignoreCase = true) ->
            "Tu solicitud para este evento fue rechazada"

        errorText.contains("No puedes registrarte a tu propio evento", ignoreCase = true) ->
            "No puedes inscribirte a tu propio evento"

        errorText.contains("El evento ya finalizó", ignoreCase = true) ->
            "El evento ya finalizó"

        errorText.contains("El evento ya está lleno", ignoreCase = true) ->
            "El evento ya está lleno"

        errorText.contains("Evento no encontrado", ignoreCase = true) ->
            "Evento no encontrado"

        else ->
            "Error al inscribirse: $responseCode"
    }
}

fun cancelRegistrationFailureMessage(
    responseCode: Int,
    errorText: String
): String {
    return when {
        errorText.contains("No estás registrado en este evento", ignoreCase = true) ->
            "Ya no estabas inscrito en este evento"

        else ->
            "Error al cancelar inscripción: $responseCode - $errorText"
    }
}