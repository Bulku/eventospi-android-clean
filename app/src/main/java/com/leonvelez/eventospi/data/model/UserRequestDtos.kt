package com.leonvelez.eventospi.data.model

import com.google.gson.annotations.SerializedName

data class UserLoginRequest(
    @SerializedName("Email")
    val email: String,

    @SerializedName("Password")
    val password: String
)

data class UserRegisterRequest(
    @SerializedName("FirstName")
    val firstName: String,

    @SerializedName("LastName")
    val lastName: String,

    @SerializedName("UserName")
    val userName: String,

    @SerializedName("Email")
    val email: String,

    @SerializedName("Password")
    val password: String,

    @SerializedName("ConfirmPassword")
    val confirmPassword: String
)

data class UserChangePasswordRequest(
    @SerializedName("CurrentPassword")
    val currentPassword: String,

    @SerializedName("NewPassword")
    val newPassword: String,

    @SerializedName("ConfirmNewPassword")
    val confirmNewPassword: String
)