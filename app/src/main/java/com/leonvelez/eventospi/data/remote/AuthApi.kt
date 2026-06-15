package com.leonvelez.eventospi.data.remote

import com.leonvelez.eventospi.data.model.EventParticipantResponse
import com.leonvelez.eventospi.data.model.EventRequest
import com.leonvelez.eventospi.data.model.EventResponse
import com.leonvelez.eventospi.data.model.LoginResponse
import com.leonvelez.eventospi.data.model.ReactionSummaryResponse
import com.leonvelez.eventospi.data.model.RegistrationRequest
import com.leonvelez.eventospi.data.model.UserAuthenticatedMessageResponse
import com.leonvelez.eventospi.data.model.UserChangePasswordRequest
import com.leonvelez.eventospi.data.model.UserLoginRequest
import com.leonvelez.eventospi.data.model.UserRegisterRequest
import com.leonvelez.eventospi.data.model.ManageParticipantRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthApi {

    // -------------------------
    // User
    // -------------------------

    @POST("User/Login")
    suspend fun login(
        @Body request: UserLoginRequest
    ): Response<LoginResponse>

    @POST("User/Register")
    suspend fun register(
        @Body request: UserRegisterRequest
    ): Response<ResponseBody>

    @GET("User/GetUserAuthenticated")
    suspend fun getUserAuthenticated(
        @Header("Authorization") token: String
    ): Response<UserAuthenticatedMessageResponse>

    @POST("User/ChangePassword")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: UserChangePasswordRequest
    ): Response<ResponseBody>

    @Multipart
    @POST("User/UploadImageProfileAsync")
    suspend fun uploadProfileImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>


    // -------------------------
    // Event
    // -------------------------

    @POST("Event/Create")
    suspend fun createEvent(
        @Header("Authorization") token: String,
        @Body event: EventRequest
    ): Response<EventResponse>

    @GET("Event/GetEvents")
    suspend fun getEvents(
        @Header("Authorization") token: String
    ): Response<List<EventResponse>>

    @GET("Event/GetEventsIAmRegistered")
    suspend fun getEventsIAmRegistered(
        @Header("Authorization") token: String
    ): Response<List<EventResponse>>

    @DELETE("Event/Delete/{id}")
    suspend fun deleteEvent(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Unit>

    @PUT("Event/Update")
    suspend fun updateEvent(
        @Header("Authorization") token: String,
        @Body event: EventRequest
    ): Response<EventResponse>

    @Multipart
    @POST("Event/UploadImageAsync")
    suspend fun uploadEventImage(
        @Header("Authorization") token: String,
        @Part("EventId") eventId: RequestBody,
        @Part formFile: MultipartBody.Part
    ): Response<ResponseBody>


    // -------------------------
    // EventParticipant
    // -------------------------

    @POST("EventParticipant/RegisterToEvent")
    suspend fun registerToEvent(
        @Header("Authorization") token: String,
        @Body request: RegistrationRequest
    ): Response<EventParticipantResponse>

    @PUT("EventParticipant/CancelRegistration")
    suspend fun cancelRegistration(
        @Header("Authorization") token: String,
        @Body request: RegistrationRequest
    ): Response<EventParticipantResponse>

    @GET("EventParticipant/GetParticipantsByEventId")
    suspend fun getParticipantsByEventId(
        @Header("Authorization") token: String,
        @Query("eventId") eventId: Int
    ): Response<List<EventParticipantResponse>>

    @GET("EventParticipant/GetPendingRequestsAsync")
    suspend fun getPendingRequestsAsync(
        @Header("Authorization") token: String,
        @Query("eventId") eventId: Int
    ): Response<List<EventParticipantResponse>>

    @PUT("EventParticipant/ApproveOrRejectParticipant")
    suspend fun approveOrRejectParticipant(
        @Header("Authorization") token: String,
        @Body request: ManageParticipantRequest
    ): Response<EventParticipantResponse>


    // -------------------------
    // Reaction
    // -------------------------

    @POST("Reaction/ReactToEvent")
    suspend fun reactToEvent(
        @Header("Authorization") token: String,
        @Query("eventId") eventId: Int,
        @Query("reactionTypeId") reactionTypeId: Int
    ): Response<Unit>

    @GET("Reaction/GetReactionsByEventId")
    suspend fun getReactionsByEventId(
        @Header("Authorization") token: String,
        @Query("eventId") eventId: Int
    ): Response<ReactionSummaryResponse>

    @DELETE("Reaction/Delete/{eventId}")
    suspend fun deleteReaction(
        @Header("Authorization") token: String,
        @Path("eventId") eventId: Int
    ): Response<Unit>
}