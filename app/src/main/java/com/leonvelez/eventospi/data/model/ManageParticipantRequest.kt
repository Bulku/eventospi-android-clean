package com.leonvelez.eventospi.data.model

data class ManageParticipantRequest(
    val eventId: Int,
    val cancellationReason: String = "",
    val userId: Int? = null,
    val approve: Boolean? = null
)
