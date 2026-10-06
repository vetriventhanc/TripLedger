from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.database import get_db
from app.routers.auth import get_current_user
from app.models import Trip, TripExpense, User
from app.schemas import (
    TripExpenseAnalyticsResponse,
    TripExpenseCategoryAnalytics,
    TripExpenseCreate,
    TripExpenseDailyAnalytics,
    TripExpenseResponse,
)

router = APIRouter(
    prefix="/trips/{trip_id}/expenses",
    tags=["Expenses"],
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
    response_model=TripExpenseResponse,
    status_code=status.HTTP_201_CREATED,
)
def create_expense(
    trip_id: int,
    expense: TripExpenseCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    get_owned_trip(
        trip_id=trip_id,
        current_user=current_user,
        db=db,
    )

    new_expense = TripExpense(
        trip_id=trip_id,
        title=expense.title,
        amount=expense.amount,
        category=expense.category,
        expense_date=expense.expense_date,
        notes=expense.notes,
    )

    db.add(new_expense)
    db.commit()
    db.refresh(new_expense)

    return new_expense


@router.get(
    "",
    response_model=list[TripExpenseResponse],
)
def get_trip_expenses(
    trip_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    get_owned_trip(
        trip_id=trip_id,
        current_user=current_user,
        db=db,
    )

    expenses = (
        db.query(TripExpense)
        .filter(
            TripExpense.trip_id == trip_id
        )
        .order_by(
            TripExpense.expense_date.desc(),
            TripExpense.id.desc(),
        )
        .all()
    )

    return expenses


@router.get(
    "/analytics",
    response_model=TripExpenseAnalyticsResponse,
)
def get_expense_analytics(
    trip_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    get_owned_trip(
        trip_id=trip_id,
        current_user=current_user,
        db=db,
    )

    total_expenses = (
        db.query(
            func.coalesce(
                func.sum(TripExpense.amount),
                0,
            )
        )
        .filter(
            TripExpense.trip_id == trip_id
        )
        .scalar()
    )

    expense_count = (
        db.query(
            func.count(TripExpense.id)
        )
        .filter(
            TripExpense.trip_id == trip_id
        )
        .scalar()
    )

    average_expense = (
        db.query(
            func.coalesce(
                func.avg(TripExpense.amount),
                0,
            )
        )
        .filter(
            TripExpense.trip_id == trip_id
        )
        .scalar()
    )

    category_rows = (
        db.query(
            TripExpense.category,
            func.sum(TripExpense.amount).label("total"),
        )
        .filter(
            TripExpense.trip_id == trip_id
        )
        .group_by(
            TripExpense.category
        )
        .order_by(
            func.sum(TripExpense.amount).desc()
        )
        .all()
    )

    daily_rows = (
        db.query(
            TripExpense.expense_date,
            func.sum(TripExpense.amount).label("total"),
        )
        .filter(
            TripExpense.trip_id == trip_id
        )
        .group_by(
            TripExpense.expense_date
        )
        .order_by(
            TripExpense.expense_date.asc()
        )
        .all()
    )

    categories = [
        TripExpenseCategoryAnalytics(
            category=row.category,
            total=float(row.total or 0),
        )
        for row in category_rows
    ]

    daily_spending = [
        TripExpenseDailyAnalytics(
            date=row.expense_date,
            total=float(row.total or 0),
        )
        for row in daily_rows
    ]

    return TripExpenseAnalyticsResponse(
        trip_id=trip_id,
        total_expenses=float(
            total_expenses or 0
        ),
        expense_count=int(
            expense_count or 0
        ),
        average_expense=float(
            average_expense or 0
        ),
        categories=categories,
        daily_spending=daily_spending,
    )


@router.get(
    "/{expense_id}",
    response_model=TripExpenseResponse,
)
def get_expense(
    trip_id: int,
    expense_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    get_owned_trip(
        trip_id=trip_id,
        current_user=current_user,
        db=db,
    )

    expense = (
        db.query(TripExpense)
        .filter(
            TripExpense.id == expense_id,
            TripExpense.trip_id == trip_id,
        )
        .first()
    )

    if expense is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Expense not found",
        )

    return expense


@router.delete(
    "/{expense_id}",
    status_code=status.HTTP_204_NO_CONTENT,
)
def delete_expense(
    trip_id: int,
    expense_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    get_owned_trip(
        trip_id=trip_id,
        current_user=current_user,
        db=db,
    )

    expense = (
        db.query(TripExpense)
        .filter(
            TripExpense.id == expense_id,
            TripExpense.trip_id == trip_id,
        )
        .first()
    )

    if expense is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Expense not found",
        )

    db.delete(expense)
    db.commit()