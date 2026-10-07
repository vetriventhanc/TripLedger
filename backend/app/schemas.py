from datetime import date, datetime

from pydantic import BaseModel, ConfigDict, EmailStr


class UserRegister(BaseModel):
    name: str
    email: EmailStr
    password: str


class UserResponse(BaseModel):
    id: int
    name: str
    email: EmailStr

    model_config = ConfigDict(from_attributes=True)


class UserLogin(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str


class TripCreate(BaseModel):
    title: str
    destination: str
    start_date: date
    end_date: date
    description: str | None = None
    cover_photo: str | None = None


class TripResponse(BaseModel):
    id: int
    user_id: int
    title: str
    destination: str
    start_date: date
    end_date: date
    description: str | None
    cover_photo: str | None

    model_config = ConfigDict(from_attributes=True)


class TripPhotoResponse(BaseModel):
    id: int
    trip_id: int
    photo_url: str
    caption: str | None
    memory_date: date | None
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


class TripExpenseCreate(BaseModel):
    title: str
    amount: float
    category: str
    expense_date: date
    notes: str | None = None


class TripExpenseResponse(BaseModel):
    id: int
    trip_id: int
    title: str
    amount: float
    category: str
    expense_date: date
    notes: str | None
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


class TripExpenseCategoryAnalytics(BaseModel):
    category: str
    total: float


class TripExpenseDailyAnalytics(BaseModel):
    date: date
    total: float


class TripExpenseAnalyticsResponse(BaseModel):
    trip_id: int
    total_expenses: float
    expense_count: int
    average_expense: float
    categories: list[TripExpenseCategoryAnalytics]
    daily_spending: list[TripExpenseDailyAnalytics]


class TripOverviewResponse(BaseModel):
    trip_id: int
    title: str
    destination: str
    start_date: date
    end_date: date
    duration_days: int
    total_expenses: float
    average_expense: float | None = None
    memory_count: int


class TripPlaceCreate(BaseModel):
    name: str
    location: str
    visit_date: date
    notes: str | None = None
    photo_url: str | None = None


class TripPlaceResponse(BaseModel):
    id: int
    trip_id: int
    name: str
    location: str
    visit_date: date
    notes: str | None
    photo_url: str | None
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)