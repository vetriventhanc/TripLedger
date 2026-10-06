import os
import uuid
from pathlib import Path

from fastapi import (
    APIRouter,
    Depends,
    File,
    HTTPException,
    UploadFile,
    status,
)
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Trip, TripExpense, TripPhoto, User
from app.schemas import (
    TripCreate,
    TripOverviewResponse,
    TripResponse,
)
from app.security import decode_access_token


router = APIRouter(
    prefix="/trips",
    tags=["Trips"],
)

security = HTTPBearer()


# ---------------------------------------------------------
# Configuration
# ---------------------------------------------------------

MAX_COVER_PHOTO_SIZE = 50 * 1024 * 1024

ALLOWED_COVER_PHOTO_TYPES = {
    "image/jpeg": ".jpg",
    "image/png": ".png",
    "image/webp": ".webp",
}

UPLOAD_ROOT = Path("uploads")
COVER_UPLOAD_ROOT = UPLOAD_ROOT / "covers"


# ---------------------------------------------------------
# Authentication
# ---------------------------------------------------------

def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db),
) -> User:
    token = credentials.credentials

    payload = decode_access_token(token)

    if payload is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
        )

    user_id = payload.get("sub")

    if user_id is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token",
        )

    try:
        user_id = int(user_id)
    except (TypeError, ValueError):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token",
        )

    user = (
        db.query(User)
        .filter(User.id == user_id)
        .first()
    )

    if user is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="User not found",
        )

    return user


# ---------------------------------------------------------
# Create Trip
# ---------------------------------------------------------

@router.post(
    "",
    response_model=TripResponse,
    status_code=status.HTTP_201_CREATED,
)
def create_trip(
    trip_data: TripCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    if trip_data.end_date < trip_data.start_date:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="End date cannot be before start date",
        )

    new_trip = Trip(
        user_id=current_user.id,
        title=trip_data.title,
        destination=trip_data.destination,
        start_date=trip_data.start_date,
        end_date=trip_data.end_date,
        description=trip_data.description,
        cover_photo=trip_data.cover_photo,
    )

    db.add(new_trip)
    db.commit()
    db.refresh(new_trip)

    return new_trip


# ---------------------------------------------------------
# Get My Trips
# ---------------------------------------------------------

@router.get(
    "",
    response_model=list[TripResponse],
)
def get_my_trips(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    trips = (
        db.query(Trip)
        .filter(Trip.user_id == current_user.id)
        .order_by(Trip.start_date.desc())
        .all()
    )

    return trips


# ---------------------------------------------------------
# Get Single Trip
# ---------------------------------------------------------

@router.get(
    "/{trip_id}",
    response_model=TripResponse,
)
def get_trip(
    trip_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    trip = (
        db.query(Trip)
        .filter(
            Trip.id == trip_id,
            Trip.user_id == current_user.id,
        )
        .first()
    )

    if trip is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Trip not found",
        )

    return trip


# ---------------------------------------------------------
# Update Trip
# ---------------------------------------------------------

@router.put(
    "/{trip_id}",
    response_model=TripResponse,
)
def update_trip(
    trip_id: int,
    trip_data: TripCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    trip = (
        db.query(Trip)
        .filter(
            Trip.id == trip_id,
            Trip.user_id == current_user.id,
        )
        .first()
    )

    if trip is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Trip not found",
        )

    if trip_data.end_date < trip_data.start_date:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="End date cannot be before start date",
        )

    trip.title = trip_data.title
    trip.destination = trip_data.destination
    trip.start_date = trip_data.start_date
    trip.end_date = trip_data.end_date
    trip.description = trip_data.description
    trip.cover_photo = trip_data.cover_photo

    db.commit()
    db.refresh(trip)

    return trip


# ---------------------------------------------------------
# Upload / Replace Trip Cover Photo
# ---------------------------------------------------------

@router.post(
    "/{trip_id}/cover-photo",
    response_model=TripResponse,
)
async def upload_trip_cover_photo(
    trip_id: int,
    photo: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    trip = (
        db.query(Trip)
        .filter(
            Trip.id == trip_id,
            Trip.user_id == current_user.id,
        )
        .first()
    )

    if trip is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Trip not found",
        )

    content_type = (
        photo.content_type
        or ""
    ).lower()

    if content_type not in ALLOWED_COVER_PHOTO_TYPES:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=(
                "Only JPG, PNG, and WEBP images "
                "are allowed"
            ),
        )

    file_extension = ALLOWED_COVER_PHOTO_TYPES[
        content_type
    ]

    COVER_UPLOAD_ROOT.mkdir(
        parents=True,
        exist_ok=True,
    )

    unique_filename = (
        f"trip_{trip_id}_"
        f"{uuid.uuid4().hex}"
        f"{file_extension}"
    )

    file_path = (
        COVER_UPLOAD_ROOT /
        unique_filename
    )

    total_size = 0

    try:
        with file_path.open("wb") as buffer:

            while True:
                chunk = await photo.read(1024 * 1024)

                if not chunk:
                    break

                total_size += len(chunk)

                if total_size > MAX_COVER_PHOTO_SIZE:
                    buffer.close()

                    if file_path.exists():
                        file_path.unlink()

                    raise HTTPException(
                        status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                        detail=(
                            "Cover photo must be "
                            "50 MB or smaller"
                        ),
                    )

                buffer.write(chunk)

    finally:
        await photo.close()

    # Remove the previous cover photo
    if trip.cover_photo:
        old_cover_path = trip.cover_photo.lstrip("/")

        old_file = Path(old_cover_path)

        if old_file.exists():
            try:
                old_file.unlink()
            except OSError:
                pass

    trip.cover_photo = (
        f"/uploads/covers/{unique_filename}"
    )

    db.commit()
    db.refresh(trip)

    return trip


# ---------------------------------------------------------
# Delete Trip
# ---------------------------------------------------------

@router.delete(
    "/{trip_id}",
    status_code=status.HTTP_204_NO_CONTENT,
)
def delete_trip(
    trip_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    trip = (
        db.query(Trip)
        .filter(
            Trip.id == trip_id,
            Trip.user_id == current_user.id,
        )
        .first()
    )

    if trip is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Trip not found",
        )

    db.delete(trip)
    db.commit()


# ---------------------------------------------------------
# Trip Overview
# ---------------------------------------------------------

@router.get(
    "/{trip_id}/overview",
    response_model=TripOverviewResponse,
)
def get_trip_overview(
    trip_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    trip = (
        db.query(Trip)
        .filter(
            Trip.id == trip_id,
            Trip.user_id == current_user.id,
        )
        .first()
    )

    if trip is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Trip not found",
        )

    duration_days = (
        trip.end_date -
        trip.start_date
    ).days + 1

    total_expenses = (
        db.query(
            func.coalesce(
                func.sum(
                    TripExpense.amount
                ),
                0,
            )
        )
        .filter(
            TripExpense.trip_id == trip.id
        )
        .scalar()
    )

    memory_count = (
        db.query(
            func.count(TripPhoto.id)
        )
        .filter(
            TripPhoto.trip_id == trip.id
        )
        .scalar()
    )

    return TripOverviewResponse(
        trip_id=trip.id,
        title=trip.title,
        destination=trip.destination,
        start_date=trip.start_date,
        end_date=trip.end_date,
        duration_days=duration_days,
        total_expenses=float(
            total_expenses or 0
        ),
        memory_count=int(
            memory_count or 0
        ),
    )