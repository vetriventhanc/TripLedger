from datetime import date

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Trip, TripExpense, TripPhoto, User
from app.routers.auth import get_current_user


router = APIRouter(
    prefix="/trips",
    tags=["Trip Timeline"],
)


def get_owned_trip(
    trip_id: int,
    current_user: User,
    db: Session,
) -> Trip:
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


@router.get(
    "/{trip_id}/timeline",
)
def get_trip_timeline(
    trip_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    trip = get_owned_trip(
        trip_id=trip_id,
        current_user=current_user,
        db=db,
    )

    timeline = []

    # Trip start
    timeline.append(
        {
            "type": "trip_start",
            "date": trip.start_date,
            "title": "Trip Started",
            "description": trip.title,
            "amount": None,
            "photo_url": None,
        }
    )

    # Expenses
    expenses = (
        db.query(TripExpense)
        .filter(
            TripExpense.trip_id == trip_id
        )
        .order_by(
            TripExpense.expense_date.asc(),
            TripExpense.id.asc(),
        )
        .all()
    )

    for expense in expenses:
        timeline.append(
            {
                "type": "expense",
                "date": expense.expense_date,
                "title": expense.title,
                "description": expense.category,
                "amount": float(expense.amount),
                "photo_url": None,
            }
        )

    # Trip photos / memories
    photos = (
        db.query(TripPhoto)
        .filter(
            TripPhoto.trip_id == trip_id
        )
        .order_by(
            TripPhoto.created_at.asc(),
            TripPhoto.id.asc(),
        )
        .all()
    )

    for photo in photos:
        timeline.append(
            {
                "type": "memory",
                "date": photo.created_at.date(),
                "title": "Memory Added",
                "description": photo.caption,
                "amount": None,
                "photo_url": photo.photo_url,
            }
        )

    # Trip end
    timeline.append(
        {
            "type": "trip_end",
            "date": trip.end_date,
            "title": "Trip Completed",
            "description": trip.title,
            "amount": None,
            "photo_url": None,
        }
    )

    # Chronological order
    timeline.sort(
        key=lambda item: (
            item["date"],
            item["type"],
        )
    )

    return {
        "trip_id": trip.id,
        "title": trip.title,
        "destination": trip.destination,
        "events": timeline,
    }