# TripLedger

TripLedger is a full-stack personal travel management application designed to help users organize trips, track travel expenses, save travel memories, record places visited, view trip activity, analyze spending, share trips, and generate detailed trip reports.

The project consists of an Android mobile application, a FastAPI backend, and a PostgreSQL database.

---

## Project Status

**Current Version:** `v1.0.0`

**Status:** Completed and release-ready

---

## Features

### Authentication

- User registration
- User login
- JWT-based authentication
- Secure password hashing
- Authenticated API requests
- User ownership validation
- Logout/session handling

### Trip Management

Users can:

- Create trips
- View trips
- Edit trips
- Delete trips
- View detailed trip information
- Search trips
- Filter trips by status
- Filter trips by date

Trip statuses are determined from the trip dates:

- Upcoming
- Ongoing
- Completed

---

### Expense Management

Users can record travel expenses with:

- Expense title
- Amount
- Category
- Expense date
- Notes

TripLedger provides expense intelligence and analytics including:

- Total spending
- Average expense
- Largest expense
- Top spending category
- Top category percentage
- Daily spending
- Category breakdown
- Expense search
- Category filtering

---

### Places & Locations

Users can record places visited during a trip.

Each place can contain:

- Place name
- Location
- Visit date
- Notes
- Optional photo reference

Users can view and delete saved places.

---

### Travel Memories

TripLedger includes a travel memory gallery for storing photographs from trips.

Memory features include:

- Upload travel photographs
- Memory captions
- Memory dates
- Full-screen photo viewing
- Memory filtering
- Photo deletion
- Trip cover photos

Memory filters include:

- All
- Date
- Caption

---

### Trip Timeline

TripLedger provides a chronological timeline of important trip activity.

Timeline events include:

- Trip start
- Expense events
- Memory events
- Trip end

This provides a chronological view of the trip experience.

---

### Trip Analytics & Insights

TripLedger provides several levels of trip analysis.

#### Trip Insights

Includes:

- Trip duration
- Places visited
- Memories
- Expense count
- Total spending
- Average expense
- Top spending category

#### Expense Intelligence

Includes:

- Average expense
- Largest expense
- Top spending category
- Top category percentage
- Total spending

#### Advanced Expense Analytics

Includes:

- Category breakdown
- Category percentages
- Daily spending
- Expense count
- Average expense
- Highest spending day

#### Financial Summary

Provides an overview of the financial activity of a trip.

#### Smart Trip Summary

Provides an automatic summary based on:

- Trip status
- Duration
- Places
- Memories
- Expenses
- Total spending
- Average spending
- Average spending per day
- Top spending category

#### Trip Activity Insights

Includes:

- Timeline event count
- Expense events
- Memory events
- Places visited
- Events per day
- Timeline coverage

#### Trip Completion Summary

Provides a final summary of a completed trip including:

- Trip status
- Duration
- Places
- Memories
- Expenses
- Total spending
- Average spending
- Top category
- Top category amount
- Trip dates

---

### Search & Filtering

Trips can be searched by:

- Trip title
- Destination

Trip status filters:

- All
- Upcoming
- Ongoing
- Completed

Trip date filters:

- All Dates
- This Month
- Next Month
- This Year
- Past

Expenses support:

- Text search
- Category filtering

Memories support:

- Date filtering
- Caption filtering

---

### Trip Sharing

TripLedger supports Android's native sharing functionality.

Users can share a trip summary through the Android system share sheet.

The shared information can include:

- Trip title
- Destination
- Dates
- Duration
- Description
- Places
- Memories
- Expense information
- Total spending
- Average spending
- Top spending category

---

### TXT Trip Reports

TripLedger can generate detailed TXT reports for trips.

Reports can contain:

- Trip overview
- Trip dates
- Duration
- Description
- Places visited
- Memories
- Captions
- Memory dates
- Financial summary
- Expense category breakdown
- Category percentages
- Top spending category
- Report notes

The report is designed to provide a readable summary of the complete trip.

---

# System Architecture

```text
┌───────────────────────────────────┐
│        Android Application        │
│                                   │
│      Kotlin + Jetpack Compose    │
│                                   │
│        Retrofit + DataStore       │
└─────────────────┬─────────────────┘
                  │
                  │ REST API
                  │
                  ▼
┌───────────────────────────────────┐
│          FastAPI Backend          │
│                                   │
│          Python + FastAPI         │
│              │                    │
│          SQLAlchemy               │
│              │                    │
│          JWT Security             │
└─────────────────┬─────────────────┘
                  │
                  │ SQL
                  │
                  ▼
┌───────────────────────────────────┐
│           PostgreSQL              │
│             Database              │
└───────────────────────────────────┘
```

---

# Technology Stack

## Android

- Kotlin
- Jetpack Compose
- Android Studio
- Navigation Compose
- Retrofit
- DataStore
- Coil

## Backend

- Python
- FastAPI
- SQLAlchemy
- Pydantic
- python-jose
- Passlib
- bcrypt
- python-dotenv
- python-multipart

## Database

- PostgreSQL

## Development Tools

- Android Studio
- Visual Studio Code
- PostgreSQL
- pgAdmin
- Git
- GitHub

---

# Project Structure

```text
TripLedger/
│
├── app/
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           │
│           ├── java/
│           │   └── com/
│           │       └── example/
│           │           └── tripledger/
│           │               ├── data/
│           │               ├── navigation/
│           │               ├── ui/
│           │               └── ...
│           │
│           └── res/
│
├── backend/
│   ├── app/
│   │   ├── routers/
│   │   ├── models/
│   │   ├── schemas/
│   │   ├── database.py
│   │   ├── security.py
│   │   └── main.py
│   │
│   ├── .env
│   └── .venv/
│
├── .gitignore
├── README.md
└── ...
```

> The `.env` file and other sensitive/local files should not be committed to GitHub.

---

# Getting Started

## Prerequisites

Install the following software before running the project:

- Android Studio
- JDK
- Python 3.x
- PostgreSQL
- Git

---

# Clone the Repository

```bash
git clone https://github.com/vetriventhanc/TripLedger.git
```

Enter the project directory:

```bash
cd TripLedger
```

---

# Backend Setup

Navigate to the backend:

```bash
cd backend
```

Create a Python virtual environment:

```bash
python -m venv .venv
```

Activate the virtual environment on Windows:

```powershell
.\.venv\Scripts\Activate.ps1
```

Install the required dependencies:

```powershell
pip install fastapi uvicorn sqlalchemy "psycopg[binary]" python-jose "passlib[bcrypt]==1.7.4" "bcrypt==4.0.1" pydantic[email] python-dotenv python-multipart
```

---

# Environment Configuration

Create a `.env` file inside the `backend` directory.

Example:

```env
TRIPLEDGER_DB_USER=postgres
TRIPLEDGER_DB_PASSWORD=your_database_password
TRIPLEDGER_DB_HOST=localhost
TRIPLEDGER_DB_PORT=5432
TRIPLEDGER_DB_NAME=tripledger

TRIPLEDGER_SECRET_KEY=your_secret_key
TRIPLEDGER_ACCESS_TOKEN_EXPIRE_MINUTES=60
```

Replace the example values with your local PostgreSQL configuration.

### Security Notice

Never commit the `.env` file to GitHub.

Never publish:

- Database passwords
- JWT secret keys
- API keys
- Private credentials

---

# PostgreSQL Setup

Create a PostgreSQL database named:

```text
tripledger
```

Make sure PostgreSQL is running before starting the backend.

The FastAPI backend uses SQLAlchemy to communicate with PostgreSQL.

---

# Run the Backend

From the `backend` directory:

```powershell
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

The backend will be available at:

```text
http://127.0.0.1:8000
```

FastAPI Swagger documentation:

```text
http://127.0.0.1:8000/docs
```

Health endpoint:

```text
http://127.0.0.1:8000/health
```

A healthy backend should return a response similar to:

```json
{
  "status": "healthy",
  "database": "connected"
}
```

---

# Android Setup

Open the project in Android Studio:

```text
TripLedger/
```

The Android application communicates with the FastAPI backend.

For the Android Studio emulator, the backend base URL is:

```text
http://10.0.2.2:8000/
```

Make sure the FastAPI backend is running before testing API-dependent features.

---

# Running the Application

## 1. Start PostgreSQL

Make sure the PostgreSQL service is running.

## 2. Start FastAPI

From:

```text
TripLedger/backend
```

run:

```powershell
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## 3. Start Android Studio

Open:

```text
TripLedger/
```

## 4. Run the Android application

Select an Android emulator and click:

```text
Run ▶
```

The application should launch on the emulator.

---

# Testing

TripLedger v1.0.0 was tested across the major application workflows.

## Authentication

- Registration
- Login
- JWT authentication
- Logout

## Dashboard

- Trip statistics
- Recent trips
- Places visited
- Expense statistics
- Refresh behavior

## Trips

- Create trip
- Edit trip
- Delete trip
- Search trips
- Status filters
- Date filters

## Trip Details

- Trip overview
- Expenses
- Timeline
- Places
- Memories
- Cover photo
- Trip insights
- Financial summary
- Smart trip summary
- Activity insights
- Completion summary
- Share summary

## Expenses

- Add expense
- Delete expense
- Search expenses
- Category filtering
- Expense intelligence
- Expense analytics

## Memories

- Upload memory
- Caption
- Memory date
- Gallery
- Full-screen viewer
- Filters
- Delete memory
- Cover photo

## Sharing & Export

- Android sharing
- TXT report generation
- Detailed trip report
- Polished TXT report

## Backend

- FastAPI startup
- PostgreSQL connection
- Health endpoint
- Authentication
- Ownership validation
- Upload validation

---

# Security

Security was considered throughout the development of TripLedger.

The project includes:

- JWT authentication
- bcrypt password hashing
- Environment-based configuration
- Database credentials stored outside source code
- User ownership validation
- Trip ownership validation
- Expense ownership validation
- Place ownership validation
- Photo ownership validation
- Upload validation
- Hardened health endpoint

The health endpoint does not expose raw database exception details.

---

# Performance

A performance and stability review was completed before the v1.0.0 release.

The review covered:

- Memory usage
- Long lists
- Photo loading
- Scrolling
- Recomposition behavior
- API calls
- Slow screens
- Crash-prone areas

Remote images are loaded asynchronously using Coil.

The Trip Detail screen was also reviewed to ensure proper whole-page scrolling without problematic nested vertical scrolling.

No additional performance changes were required for v1.0.0.

---

# Release

Current release:

```text
TripLedger v1.0.0
```

Git release tag:

```text
v1.0.0
```

Release APK:

```text
app-release.apk
```

The release APK was successfully generated and verified.

Release artifacts are excluded from Git:

```gitignore
app/release/
```

---

# Release Verification

The v1.0.0 release passed the following checks:

```text
Android build                  ✓
FastAPI startup                ✓
PostgreSQL connection          ✓
Health endpoint                ✓
Git secret protection          ✓
Full application smoke test   ✓
UI consistency review         ✓
Backend/API review             ✓
Security hardening             ✓
Performance review             ✓
Release APK generation         ✓
Git release tag                ✓
Clean Git working tree         ✓
```

---

# Future Scope

Possible future improvements include:

- Interactive maps for visited places
- Location-based trip visualization
- Cloud image storage
- More advanced travel analytics
- Offline-first functionality
- Additional export formats
- Cloud deployment
- Push notifications
- Multi-device synchronization

Future features will be considered separately from the stable v1.0.0 release.

---

# Project Documentation

A detailed project journal was created for TripLedger covering:

- Project overview
- Objectives
- Technologies
- System architecture
- Major modules
- Analytics
- Security
- Testing
- Performance review
- Release information
- Future scope

---

# Author

**Vetriventhan**

GitHub:

https://github.com/vetriventhanc/TripLedger

---

# License

This project is currently maintained as a personal project.

If you plan to distribute or reuse this project as an open-source project, add an appropriate open-source license before doing so.

---

# Project Status

```text
TripLedger v1.0.0

Status: COMPLETED
```

Built with:

```text
Kotlin
Jetpack Compose
FastAPI
Python
PostgreSQL
SQLAlchemy
Retrofit
JWT
Git
GitHub
```

---

**TripLedger — Organize your trips. Track your journey. Preserve your memories.**
