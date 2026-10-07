package com.example.tripledger.data.remote

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String
)

data class TokenResponse(
    val access_token: String,
    val token_type: String
)

data class TripCreateRequest(
    val title: String,
    val destination: String,
    val start_date: String,
    val end_date: String,
    val description: String?,
    val cover_photo: String?
)

data class TripResponse(
    val id: Int,
    val user_id: Int,
    val title: String,
    val destination: String,
    val start_date: String,
    val end_date: String,
    val description: String?,
    val cover_photo: String?
)

data class TripPhotoResponse(
    val id: Int,
    val trip_id: Int,
    val photo_url: String,
    val caption: String?,
    val created_at: String
)

data class TripExpenseCreateRequest(
    val title: String,
    val amount: Double,
    val category: String,
    val expense_date: String,
    val notes: String?
)

data class TripExpenseResponse(
    val id: Int,
    val trip_id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val expense_date: String,
    val notes: String?,
    val created_at: String
)

data class TripExpenseCategoryAnalytics(
    val category: String,
    val total: Double
)

data class TripExpenseDailyAnalytics(
    val date: String,
    val total: Double
)

data class TripExpenseAnalyticsResponse(
    val trip_id: Int,
    val total_expenses: Double,
    val expense_count: Int,
    val average_expense: Double,
    val categories: List<TripExpenseCategoryAnalytics>,
    val daily_spending: List<TripExpenseDailyAnalytics>
)

data class TripOverviewResponse(
    val trip_id: Int,
    val title: String,
    val destination: String,
    val start_date: String,
    val end_date: String,
    val duration_days: Int,
    val total_expenses: Double,
    val memory_count: Int
)


// ---------------------------------------------------------
// Trip Timeline
// ---------------------------------------------------------

data class TripTimelineEvent(
    val type: String,
    val date: String,
    val title: String,
    val description: String?,
    val amount: Double?,
    val photo_url: String?
)

data class TripTimelineResponse(
    val trip_id: Int,
    val title: String,
    val destination: String,
    val events: List<TripTimelineEvent>
)


// ---------------------------------------------------------
// Trip Places
// ---------------------------------------------------------

data class TripPlaceCreateRequest(
    val name: String,
    val location: String,
    val visit_date: String,
    val notes: String?,
    val photo_url: String?
)

data class TripPlaceResponse(
    val id: Int,
    val trip_id: Int,
    val name: String,
    val location: String,
    val visit_date: String,
    val notes: String?,
    val photo_url: String?,
    val created_at: String
)
