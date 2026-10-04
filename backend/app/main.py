from fastapi import FastAPI

from app.database import Base, engine
from app import models
from app.routers import auth


Base.metadata.create_all(bind=engine)


app = FastAPI(
    title="TripLedger API",
    description="Backend API for the TripLedger travel management application",
    version="1.0.0",
)


app.include_router(auth.router)


@app.get("/")
def root():
    return {
        "message": "TripLedger API is running"
    }


@app.get("/health")
def health_check():
    try:
        with engine.connect():
            database_status = "connected"
    except Exception as error:
        database_status = f"error: {str(error)}"

    return {
        "status": "healthy",
        "database": database_status,
    }