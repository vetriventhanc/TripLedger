from datetime import date

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