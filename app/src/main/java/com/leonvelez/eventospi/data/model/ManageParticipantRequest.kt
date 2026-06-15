package com.leonvelez.eventospi.data.model

import com.google.gson.annotations.SerializedName

data class ManageParticipantRequest(
    @SerializedName("EventId")
    val eventId: Int,

    @SerializedName("UserId")
    val userId: String,

    @SerializedName("Approve")
    val approve: Boolean
)
