package com.leonvelez.eventospi.utils

import com.leonvelez.eventospi.data.model.EventResponse
import com.leonvelez.eventospi.data.model.UserAuthenticatedResponse

fun isEventOwnedByCurrentUser(
    currentUser: UserAuthenticatedResponse?,
    event: EventResponse
): Boolean {
    val currentUserName = currentUser?.userName?.trim()?.lowercase()
    val creatorUserName = event.createdByUserName.trim().lowercase()

    return !currentUserName.isNullOrBlank() && currentUserName == creatorUserName
}

fun normalizeOwnerText(value: String?): String {
    return value
        ?.trim()
        ?.lowercase()
        ?.replace("\\s+".toRegex(), "")
        .orEmpty()
}

fun isCurrentUserEventCreator(
    currentUser: UserAuthenticatedResponse?,
    event: EventResponse
): Boolean {
    if (currentUser == null) return false

    val owner = normalizeOwnerText(event.createdByUserName)
    if (owner.isBlank()) return false

    val candidates = listOf(
        currentUser.userName,
        currentUser.email,
        currentUser.firstName,
        currentUser.lastName,
        "${currentUser.firstName} ${currentUser.lastName}"
    ).map { normalizeOwnerText(it) }

    return candidates.any { it == owner }
}

fun extractUserNameFromAuthMessage(message: String?): String {
    if (message.isNullOrBlank()) return ""

    val regex = Regex("UserName\\s*=\\s*([^,\\s]+)", RegexOption.IGNORE_CASE)
    val match = regex.find(message)

    return match?.groupValues?.getOrNull(1)?.trim().orEmpty()
}

fun isEventOwnedByUserName(
    currentUserName: String,
    event: EventResponse
): Boolean {
    if (currentUserName.isBlank()) return false

    return currentUserName.trim().lowercase() ==
            event.createdByUserName.trim().lowercase()
}
