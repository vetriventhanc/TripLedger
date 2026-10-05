from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Trip, TripExpense
from app.schemas import TripExpenseCreate, TripExpenseResponse
from app.routers.auth import get_current_user


router = APIRouter(
    prefix="/trips",
    tags=["Trip Expenses"],
)


@router.post(
    "/{trip_id}/expenses",
    response_model=TripExpenseResponse,
    status_code=status.HTTP_201_CREATED,
)
def create_expense(
    trip_id: int,
    expense_data: TripExpenseCreate,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    trip = (
        db.query(Trip)
        .filter(
            Trip.id == trip_id,
            Trip.user_id == current_user.id,
        )
        .first()
    )

    if not trip:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Trip not found",
        )

    if expense_data.amount < 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Expense amount cannot be negative",
        )

    expense = TripExpense(
        trip_id=trip_id,
        title=expense_data.title,
        amount=expense_data.amount,
        category=expense_data.category,
        expense_date=expense_data.expense_date,
        notes=expense_data.notes,
    )

    db.add(expense)
    db.commit()
    db.refresh(expense)

    return expense


@router.get(
    "/{trip_id}/expenses",
    response_model=list[TripExpenseResponse],
)
def get_trip_expenses(
    trip_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    trip = (
        db.query(Trip)
        .filter(
            Trip.id == trip_id,
            Trip.user_id == current_user.id,
        )
        .first()
    )

    if not trip:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Trip not found",
        )

    expenses = (
        db.query(TripExpense)
        .filter(TripExpense.trip_id == trip_id)
        .order_by(TripExpense.expense_date.desc())
        .all()
    )

    return expenses


@router.get(
    "/{trip_id}/expenses/{expense_id}",
    response_model=TripExpenseResponse,
)
def get_expense(
    trip_id: int,
    expense_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    trip = (
        db.query(Trip)
        .filter(
            Trip.id == trip_id,
            Trip.user_id == current_user.id,
        )
        .first()
    )

    if not trip:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Trip not found",
        )

    expense = (
        db.query(TripExpense)
        .filter(
            TripExpense.id == expense_id,
            TripExpense.trip_id == trip_id,
        )
        .first()
    )

    if not expense:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Expense not found",
        )

    return expense


@router.delete(
    "/expenses/{expense_id}",
    status_code=status.HTTP_204_NO_CONTENT,
)
def delete_expense(
    expense_id: int,
    db: Session = Depends(get_db),
    current_user=Depends(get_current_user),
):
    expense = (
        db.query(TripExpense)
        .join(Trip, Trip.id == TripExpense.trip_id)
        .filter(
            TripExpense.id == expense_id,
            Trip.user_id == current_user.id,
        )
        .first()
    )

    if not expense:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Expense not found",
        )

    db.delete(expense)
    db.commit()

    return None