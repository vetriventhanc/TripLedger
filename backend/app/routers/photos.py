import uuid
from pathlib import Path

from fastapi import (
    APIRouter,
    Depends,
    File,
    Form,
    HTTPException,
    UploadFile,
    status,
)
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Trip, TripPhoto, User
from app.routers.trips import get_current_user
from app.schemas import TripPhotoResponse


router = APIRouter(
    prefix="/trips",
    tags=["Trip Photos"],
)


UPLOAD_ROOT = Path("uploads") / "trips"

ALLOWED_CONTENT_TYPES = {
    "image/jpeg": ".jpg",
    "image/png": ".png",
    "image/webp": ".webp",
}

MAX_FILE_SIZE = 50 * 1024 * 1024  # 50 MB


@router.post(
    "/{trip_id}/photos",
    response_model=TripPhotoResponse,
    status_code=status.HTTP_201_CREATED,
)
async def upload_trip_photo(
    trip_id: int,
    photo: UploadFile = File(...),
    caption: str | None = Form(None),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # Verify that the trip belongs to the logged-in user.
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

    # Validate content type.
    if photo.content_type not in ALLOWED_CONTENT_TYPES:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Only JPG, PNG, and WEBP images are allowed",
        )

    # Read the uploaded file.
    file_data = await photo.read()

    # Validate file size.
    if len(file_data) > MAX_FILE_SIZE:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Image size must not exceed 50 MB",
        )

    # Create a unique filename.
    extension = ALLOWED_CONTENT_TYPES[photo.content_type]
    filename = f"{uuid.uuid4().hex}{extension}"

    # Create the trip-specific upload directory.
    trip_upload_dir = UPLOAD_ROOT / str(trip_id)
    trip_upload_dir.mkdir(
        parents=True,
        exist_ok=True,
    )

    file_path = trip_upload_dir / filename

    # Save the image.
    file_path.write_bytes(file_data)

    # Store the relative URL in the database.
    photo_url = f"/uploads/trips/{trip_id}/{filename}"

    # Clean up an empty caption.
    cleaned_caption = caption.strip() if caption else None

    new_photo = TripPhoto(
        trip_id=trip_id,
        photo_url=photo_url,
        caption=cleaned_caption or None,
    )

    db.add(new_photo)
    db.commit()
    db.refresh(new_photo)

    return new_photo


@router.get(
    "/{trip_id}/photos",
    response_model=list[TripPhotoResponse],
)
def get_trip_photos(
    trip_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    # Verify trip ownership.
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

    photos = (
        db.query(TripPhoto)
        .filter(TripPhoto.trip_id == trip_id)
        .order_by(TripPhoto.created_at.desc())
        .all()
    )

    return photos


@router.delete(
    "/photos/{photo_id}",
    status_code=status.HTTP_204_NO_CONTENT,
)
def delete_trip_photo(
    photo_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    photo = (
        db.query(TripPhoto)
        .join(
            Trip,
            Trip.id == TripPhoto.trip_id,
        )
        .filter(
            TripPhoto.id == photo_id,
            Trip.user_id == current_user.id,
        )
        .first()
    )

    if photo is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Photo not found",
        )

    # Delete the physical file if it exists.
    relative_path = photo.photo_url.lstrip("/")
    file_path = Path(relative_path)

    if file_path.exists():
        file_path.unlink()

    db.delete(photo)
    db.commit()