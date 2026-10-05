package com.example.tripledger.data.repository

import com.example.tripledger.data.remote.ApiService
import com.example.tripledger.data.remote.TripCreateRequest
import com.example.tripledger.data.remote.TripPhotoResponse
import com.example.tripledger.data.remote.TripResponse
import okhttp3.MultipartBody

class TripRepository(
    private val apiService: ApiService
) {

    suspend fun createTrip(
        token: String,
        request: TripCreateRequest
    ): Result<TripResponse> {
        return try {
            val response = apiService.createTrip(
                authorization = "Bearer $token",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to create trip"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrips(
        token: String
    ): Result<List<TripResponse>> {
        return try {
            val response = apiService.getTrips(
                authorization = "Bearer $token"
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to load trips"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrip(
        token: String,
        tripId: Int
    ): Result<TripResponse> {
        return try {
            val response = apiService.getTrip(
                authorization = "Bearer $token",
                tripId = tripId
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to load trip"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTrip(
        token: String,
        tripId: Int
    ): Result<Unit> {
        return try {
            val response = apiService.deleteTrip(
                authorization = "Bearer $token",
                tripId = tripId
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to delete trip"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadTripPhoto(
        token: String,
        tripId: Int,
        photo: MultipartBody.Part
    ): Result<TripPhotoResponse> {
        return try {
            val response = apiService.uploadTripPhoto(
                authorization = "Bearer $token",
                tripId = tripId,
                photo = photo
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to upload photo"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTripPhotos(
        token: String,
        tripId: Int
    ): Result<List<TripPhotoResponse>> {
        return try {
            val response = apiService.getTripPhotos(
                authorization = "Bearer $token",
                tripId = tripId
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to load trip photos"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTripPhoto(
        token: String,
        photoId: Int
    ): Result<Unit> {
        return try {
            val response = apiService.deleteTripPhoto(
                authorization = "Bearer $token",
                photoId = photoId
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        response.errorBody()?.string()
                            ?: "Failed to delete photo"
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}