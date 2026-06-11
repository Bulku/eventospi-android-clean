package com.leonvelez.eventospi.data.model

import com.google.gson.annotations.SerializedName

data class UserAuthenticatedResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("userName")
    val userName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String?
)
