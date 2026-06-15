package com.leonvelez.eventospi.data.model

import com.google.gson.annotations.SerializedName

data class RegistrationRequest(
    @SerializedName("EventId")
    val eventId: Int,

    @SerializedName("CancellationReason")
    val cancellationReason: String = ""
)