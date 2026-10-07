package com.example.tripledger.data.remote

import okhttp3.MultipartBody
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

interface ApiService {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<UserResponse>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<UserResponse>

    @POST("trips")
    suspend fun createTrip(
        @Header("Authorization") authorization: String,
        @Body request: TripCreateRequest
    ): Response<TripResponse>

    @GET("trips")
    suspend fun getTrips(
        @Header("Authorization") authorization: String
    ): Response<List<TripResponse>>

    @GET("trips/{tripId}")
    suspend fun getTrip(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int
    ): Response<TripResponse>

    @GET("trips/{tripId}/overview")
    suspend fun getTripOverview(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int
    ): Response<TripOverviewResponse>

    @GET("trips/{tripId}/timeline")
    suspend fun getTripTimeline(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int
    ): Response<TripTimelineResponse>

    @PUT("trips/{tripId}")
    suspend fun updateTrip(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int,
        @Body request: TripCreateRequest
    ): Response<TripResponse>

    @DELETE("trips/{tripId}")
    suspend fun deleteTrip(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int
    ): Response<Unit>

    @Multipart
    @POST("trips/{tripId}/photos")
    suspend fun uploadTripPhoto(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int,
        @Part photo: MultipartBody.Part
    ): Response<TripPhotoResponse>

    @Multipart
    @POST("trips/{tripId}/cover-photo")
    suspend fun uploadTripCoverPhoto(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int,
        @Part photo: MultipartBody.Part
    ): Response<TripResponse>

    @GET("trips/{tripId}/photos")
    suspend fun getTripPhotos(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int
    ): Response<List<TripPhotoResponse>>

    @DELETE("trips/photos/{photoId}")
    suspend fun deleteTripPhoto(
        @Header("Authorization") authorization: String,
        @Path("photoId") photoId: Int
    ): Response<Unit>

    @POST("trips/{tripId}/expenses")
    suspend fun createExpense(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int,
        @Body request: TripExpenseCreateRequest
    ): Response<TripExpenseResponse>

    @GET("trips/{tripId}/expenses")
    suspend fun getTripExpenses(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int
    ): Response<List<TripExpenseResponse>>

    @GET("trips/{tripId}/expenses/analytics")
    suspend fun getExpenseAnalytics(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int
    ): Response<TripExpenseAnalyticsResponse>

    @GET("trips/{tripId}/expenses/{expenseId}")
    suspend fun getExpense(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int,
        @Path("expenseId") expenseId: Int
    ): Response<TripExpenseResponse>

    @DELETE("trips/expenses/{expenseId}")
    suspend fun deleteExpense(
        @Header("Authorization") authorization: String,
        @Path("expenseId") expenseId: Int
    ): Response<Unit>


    @POST("trips/{tripId}/places")
    suspend fun createPlace(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int,
        @Body request: TripPlaceCreateRequest
    ): Response<TripPlaceResponse>

    @GET("trips/{tripId}/places")
    suspend fun getTripPlaces(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int
    ): Response<List<TripPlaceResponse>>

    @GET("trips/{tripId}/places/{placeId}")
    suspend fun getPlace(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int,
        @Path("placeId") placeId: Int
    ): Response<TripPlaceResponse>

    @DELETE("trips/{tripId}/places/{placeId}")
    suspend fun deletePlace(
        @Header("Authorization") authorization: String,
        @Path("tripId") tripId: Int,
        @Path("placeId") placeId: Int
    ): Response<Unit>
}