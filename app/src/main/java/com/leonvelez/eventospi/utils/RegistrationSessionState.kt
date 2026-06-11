package com.leonvelez.eventospi.utils

/**
 * Centralizes the current frontend-only cancellation state.
 *
 * Today the backend can cancel a registration, but it does not yet expose the final
 * "no longer registered / can register again" state consistently across sessions.
 * Keep all temporary session behavior here so the backend contract can be replaced
 * later without touching map cards, lists, or navigation flow.
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
