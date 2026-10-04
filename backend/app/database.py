import os

from dotenv import load_dotenv
from sqlalchemy import create_engine
from sqlalchemy.engine import URL
from sqlalchemy.orm import declarative_base, sessionmaker

load_dotenv()

DATABASE_URL = URL.create(
    drivername="postgresql+psycopg",
    username=os.getenv("TRIPLEDGER_DB_USER", "postgres"),
    password=os.getenv("TRIPLEDGER_DB_PASSWORD"),
    host=os.getenv("TRIPLEDGER_DB_HOST", "localhost"),
    port=int(os.getenv("TRIPLEDGER_DB_PORT", "5432")),
    database=os.getenv("TRIPLEDGER_DB_NAME", "tripledger"),
)

engine = create_engine(DATABASE_URL, pool_pre_ping=True)

SessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=engine,
)

Base = declarative_base()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

import os

from sqlalchemy import create_engine
from sqlalchemy.engine import URL
from sqlalchemy.orm import declarative_base, sessionmaker


DATABASE_URL = URL.create(
    drivername="postgresql+psycopg",
    username=os.getenv("TRIPLEDGER_DB_USER", "postgres"),
    password=os.getenv("TRIPLEDGER_DB_PASSWORD"),
    host=os.getenv("TRIPLEDGER_DB_HOST", "localhost"),
    port=int(os.getenv("TRIPLEDGER_DB_PORT", "5432")),
    database=os.getenv("TRIPLEDGER_DB_NAME", "tripledger"),
)

engine = create_engine(
    DATABASE_URL,
    pool_pre_ping=True,
)

SessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=engine,
)

Base = declarative_base()


def get_db():
    db = SessionLocal()

    try:
        yield db
    finally:
        db.close()