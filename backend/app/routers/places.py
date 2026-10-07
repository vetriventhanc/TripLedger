from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Trip, TripPlace, User
from app.routers.auth import get_current_user
from app.schemas import (
    TripPlaceCreate,
    TripPlaceResponse,
)


router = APIRouter(
    prefix="/trips/{trip_id}/places",
    tags=["Trip Places"],
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


@router.post(
    "",
    response_model=TripPlaceResponse,
    status_code=status.HTTP_201_CREATED,
)
def create_place(
    trip_id: int,
    place: TripPlaceCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    get_owned_trip(
        trip_id=trip_id,
        current_user=current_user,
        db=db,
    )

    new_place = TripPlace(
        trip_id=trip_id,
        name=place.name,
        location=place.location,
        visit_date=place.visit_date,
        notes=place.notes,
        photo_url=place.photo_url,
    )

    db.add(new_place)
    db.commit()
    db.refresh(new_place)

    return new_place


@router.get(
    "",
    response_model=list[TripPlaceResponse],
)
def get_trip_places(
    trip_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    get_owned_trip(
        trip_id=trip_id,
        current_user=current_user,
        db=db,
    )

    places = (
        db.query(TripPlace)
        .filter(
            TripPlace.trip_id == trip_id
        )
        .order_by(
            TripPlace.visit_date.desc(),
            TripPlace.id.desc(),
        )
        .all()
    )

    return places


@router.get(
    "/{place_id}",
    response_model=TripPlaceResponse,
)
def get_place(
    trip_id: int,
    place_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    get_owned_trip(
        trip_id=trip_id,
        current_user=current_user,
        db=db,
    )

    place = (
        db.query(TripPlace)
        .filter(
            TripPlace.id == place_id,
            TripPlace.trip_id == trip_id,
        )
        .first()
    )

    if place is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Place not found",
        )

    return place


@router.delete(
    "/{place_id}",
    status_code=status.HTTP_204_NO_CONTENT,
)
def delete_place(
    trip_id: int,
    place_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    get_owned_trip(
        trip_id=trip_id,
        current_user=current_user,
        db=db,
    )

    place = (
        db.query(TripPlace)
        .filter(
            TripPlace.id == place_id,
            TripPlace.trip_id == trip_id,
        )
        .first()
    )

    if place is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Place not found",
        )

    db.delete(place)
    db.commit()