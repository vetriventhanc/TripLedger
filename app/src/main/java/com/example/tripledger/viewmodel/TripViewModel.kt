package com.example.tripledger.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripledger.data.local.TokenManager
import com.example.tripledger.data.remote.ApiService
import com.example.tripledger.data.remote.TripCreateRequest
import com.example.tripledger.data.remote.TripExpenseAnalyticsResponse
import com.example.tripledger.data.remote.TripOverviewResponse
import com.example.tripledger.data.remote.TripPlaceCreateRequest
import com.example.tripledger.data.remote.TripPlaceResponse
import com.example.tripledger.data.remote.TripResponse
import com.example.tripledger.data.remote.TripTimelineResponse
import com.example.tripledger.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


sealed class TripState {
    data object Idle : TripState()

    data object Loading : TripState()

    data class Success(
        val trips: List<TripResponse>
    ) : TripState()

    data class Created(
        val trip: TripResponse
    ) : TripState()

    data class Updated(
        val trip: TripResponse
    ) : TripState()

    data object Deleted : TripState()

    data class Error(
        val message: String
    ) : TripState()
}


class TripViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val apiService: ApiService =
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(ApiService::class.java)

    private val repository = TripRepository(
        apiService = apiService
    )

    private val tokenManager = TokenManager(
        application.applicationContext
    )

    private val _tripState = MutableStateFlow<TripState>(
        TripState.Idle
    )

    val tripState: StateFlow<TripState> =
        _tripState.asStateFlow()


    fun loadTrips() {
        viewModelScope.launch {
            _tripState.value = TripState.Loading

            val token = tokenManager.token.first()

            if (token.isNullOrBlank()) {
                _tripState.value = TripState.Error(
                    "You are not logged in"
                )
                return@launch
            }

            val result = repository.getTrips(token)

            result
                .onSuccess { trips ->
                    _tripState.value = TripState.Success(trips)
                }
                .onFailure { exception ->
                    _tripState.value = TripState.Error(
                        exception.message
                            ?: "Failed to load trips"
                    )
                }
        }
    }


    fun createTrip(
        title: String,
        destination: String,
        startDate: String,
        endDate: String,
        description: String?
    ) {
        viewModelScope.launch {
            _tripState.value = TripState.Loading

            val token = tokenManager.token.first()

            if (token.isNullOrBlank()) {
                _tripState.value = TripState.Error(
                    "You are not logged in"
                )
                return@launch
            }

            val request = TripCreateRequest(
                title = title,
                destination = destination,
                start_date = startDate,
                end_date = endDate,
                description = description,
                cover_photo = null
            )

            val result = repository.createTrip(
                token = token,
                request = request
            )

            result
                .onSuccess { trip ->
                    _tripState.value = TripState.Created(trip)
                }
                .onFailure { exception ->
                    _tripState.value = TripState.Error(
                        exception.message
                            ?: "Failed to create trip"
                    )
                }
        }
    }


    fun updateTrip(
        tripId: Int,
        title: String,
        destination: String,
        startDate: String,
        endDate: String,
        description: String?,
        coverPhoto: String? = null
    ) {
        viewModelScope.launch {
            _tripState.value = TripState.Loading

            val token = tokenManager.token.first()

            if (token.isNullOrBlank()) {
                _tripState.value = TripState.Error(
                    "You are not logged in"
                )
                return@launch
            }

            val request = TripCreateRequest(
                title = title,
                destination = destination,
                start_date = startDate,
                end_date = endDate,
                description = description,
                cover_photo = coverPhoto
            )

            val result = repository.updateTrip(
                token = token,
                tripId = tripId,
                request = request
            )

            result
                .onSuccess { trip ->
                    _tripState.value = TripState.Updated(trip)
                }
                .onFailure { exception ->
                    _tripState.value = TripState.Error(
                        exception.message
                            ?: "Failed to update trip"
                    )
                }
        }
    }


    fun deleteTrip(
        tripId: Int
    ) {
        viewModelScope.launch {
            _tripState.value = TripState.Loading

            val token = tokenManager.token.first()

            if (token.isNullOrBlank()) {
                _tripState.value = TripState.Error(
                    "You are not logged in"
                )
                return@launch
            }

            val result = repository.deleteTrip(
                token = token,
                tripId = tripId
            )

            result
                .onSuccess {
                    _tripState.value = TripState.Deleted
                }
                .onFailure { exception ->
                    _tripState.value = TripState.Error(
                        exception.message
                            ?: "Failed to delete trip"
                    )
                }
        }
    }


    suspend fun getTrip(
        tripId: Int
    ): Result<TripResponse> {
        val token = tokenManager.token.first()

        if (token.isNullOrBlank()) {
            return Result.failure(
                Exception("You are not logged in")
            )
        }

        return repository.getTrip(
            token = token,
            tripId = tripId
        )
    }


    suspend fun getTripOverview(
        tripId: Int
    ): Result<TripOverviewResponse> {
        val token = tokenManager.token.first()

        if (token.isNullOrBlank()) {
            return Result.failure(
                Exception("You are not logged in")
            )
        }

        return repository.getTripOverview(
            token = token,
            tripId = tripId
        )
    }


    suspend fun getTripTimeline(
        tripId: Int
    ): Result<TripTimelineResponse> {
        val token = tokenManager.token.first()

        if (token.isNullOrBlank()) {
            return Result.failure(
                Exception("You are not logged in")
            )
        }

        return repository.getTripTimeline(
            token = token,
            tripId = tripId
        )
    }


    suspend fun getExpenseAnalytics(
        tripId: Int
    ): Result<TripExpenseAnalyticsResponse> {
        val token = tokenManager.token.first()

        if (token.isNullOrBlank()) {
            return Result.failure(
                Exception("You are not logged in")
            )
        }

        return repository.getExpenseAnalytics(
            token = token,
            tripId = tripId
        )
    }


    suspend fun createPlace(
        tripId: Int,
        name: String,
        location: String,
        visitDate: String,
        notes: String?,
        photoUrl: String? = null
    ): Result<TripPlaceResponse> {
        val token = tokenManager.token.first()

        if (token.isNullOrBlank()) {
            return Result.failure(
                Exception("You are not logged in")
            )
        }

        val request = TripPlaceCreateRequest(
            name = name,
            location = location,
            visit_date = visitDate,
            notes = notes,
            photo_url = photoUrl
        )

        return repository.createPlace(
            token = token,
            tripId = tripId,
            request = request
        )
    }


    suspend fun getTripPlaces(
        tripId: Int
    ): Result<List<TripPlaceResponse>> {
        val token = tokenManager.token.first()

        if (token.isNullOrBlank()) {
            return Result.failure(
                Exception("You are not logged in")
            )
        }

        return repository.getTripPlaces(
            token = token,
            tripId = tripId
        )
    }


    suspend fun getPlace(
        tripId: Int,
        placeId: Int
    ): Result<TripPlaceResponse> {
        val token = tokenManager.token.first()

        if (token.isNullOrBlank()) {
            return Result.failure(
                Exception("You are not logged in")
            )
        }

        return repository.getPlace(
            token = token,
            tripId = tripId,
            placeId = placeId
        )
    }


    suspend fun deletePlace(
        tripId: Int,
        placeId: Int
    ): Result<Unit> {
        val token = tokenManager.token.first()

        if (token.isNullOrBlank()) {
            return Result.failure(
                Exception("You are not logged in")
            )
        }

        return repository.deletePlace(
            token = token,
            tripId = tripId,
            placeId = placeId
        )
    }


    suspend fun uploadTripPhoto(
        tripId: Int,
        photo: MultipartBody.Part
    ) = run {
        val token = tokenManager.token.first()

        if (token.isNullOrBlank()) {
            Result.failure(
                Exception("You are not logged in")
            )
        } else {
            repository.uploadTripPhoto(
                token = token,
                tripId = tripId,
                photo = photo
            )
        }
    }


    suspend fun uploadTripCoverPhoto(
        tripId: Int,
        photo: MultipartBody.Part
    ) = run {
        val token = tokenManager.token.first()

        if (token.isNullOrBlank()) {
            Result.failure(
                Exception("You are not logged in")
            )
        } else {
            repository.uploadTripCoverPhoto(
                token = token,
                tripId = tripId,
                photo = photo
            )
        }
    }


    suspend fun getTripPhotos(
        tripId: Int
    ) = run {
        val token = tokenManager.token.first()

        if (token.isNullOrBlank()) {
            Result.failure(
                Exception("You are not logged in")
            )
        } else {
            repository.getTripPhotos(
                token = token,
                tripId = tripId
            )
        }
    }


    suspend fun deleteTripPhoto(
        photoId: Int
    ) = run {
        val token = tokenManager.token.first()

        if (token.isNullOrBlank()) {
            Result.failure(
                Exception("You are not logged in")
            )
        } else {
            repository.deleteTripPhoto(
                token = token,
                photoId = photoId
            )
        }
    }
}