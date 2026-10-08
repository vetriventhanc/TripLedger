from pathlib import Path

from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles

from app import models
from app.database import Base, engine
from app.routers import auth, expenses, photos, places, timeline, trips


Base.metadata.create_all(bind=engine)


app = FastAPI(
    title="TripLedger API",
    description="Backend API for the TripLedger travel management application",
    version="1.0.0",
)


# Create the uploads directory if it does not exist.
UPLOADS_DIR = Path("uploads")
UPLOADS_DIR.mkdir(
    parents=True,
    exist_ok=True,
)


# Serve uploaded trip photos.
app.mount(
    "/uploads",
    StaticFiles(directory=str(UPLOADS_DIR)),
    name="uploads",
)


app.include_router(auth.router)
app.include_router(trips.router)
app.include_router(photos.router)
app.include_router(expenses.router)
app.include_router(timeline.router)
app.include_router(places.router)


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
    except Exception:
        database_status = "error"

    return {
        "status": "healthy",
        "database": database_status,
    }