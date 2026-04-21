# Project Pulse

A web application for managing weekly activity reports (WAR) and peer evaluations in TCU's senior design courses.

## Problem

The current process for submitting and grading WARs and peer evaluations relies on Google Sheets, Excel spreadsheets, and manual file transfers through the university's LMS. This workflow is time-consuming, error-prone, and creates a heavy manual burden for both students and instructors.

## Solution

Project Pulse consolidates the entire workflow into a single web application where:

- **Students** submit weekly activity reports and peer evaluations directly in the system
- **Instructors** view reports, review peer evaluations, and access automatically generated grades and feedback
- **Admins** manage senior design sections, teams, and student/instructor assignments

## Key Features

- Manage senior design sections, teams, students, and instructors
- Submit and track weekly activity reports
- Submit peer evaluations with rubric-based scoring
- Generate peer evaluation and WAR reports automatically

## Tech Stack

- **Frontend:** Vue 3 + Vite
- **Backend:** Spring Boot (Java 17+)
- **Database:** MySQL
- **CI/CD:** GitHub Actions
- **Deployment:** Microsoft Azure

## Project Structure

```
project-pulse/
├── backend/          # Spring Boot application
├── frontend/         # Vue 3 application
├── requirements/     # Vision & scope, use cases, glossary
├── docs/             # Architecture, coding standards, API guidelines, etc.
└── .github/          # CI/CD workflows
```

## Documentation

- [Architecture](docs/architecture.md)
- [Tech Stack](docs/tech-stack.md)
- [API Guidelines](docs/api-guidelines.md)
- [Coding Standards](docs/coding-standards.md)
- [Development Plan](docs/development-plan.md)
- [Team Workflow](docs/team-workflow.md)
- [Testing Strategy](docs/testing-strategy.md)
- [Deployment](docs/deployment.md)
- [Onboarding](docs/onboarding.md)
- [Team Phase Plan](docs/team-phase-plan.md)

## Quickstart (Local)

```bash
cd backend && ./mvnw spring-boot:run
```

In a second terminal:

```bash
cd frontend && npm ci && npm run dev
```

If you hit CORS issues from `http://localhost:5173` → `http://localhost:8080`, set `APP_CORS_ALLOWED_ORIGINS` (see `.env.example`).

### Using MySQL locally (optional)

By default, the backend uses an in-memory H2 database for local development. If you want to run against MySQL for parity:

```bash
docker compose up -d
cd backend && SPRING_PROFILES_ACTIVE=mysql ./mvnw spring-boot:run
```

### Optional: use `.env` via scripts

If you copy `.env.example` to `.env`, you can run the app with environment variables loaded automatically:

```bash
docker compose up -d
./scripts/dev-backend.sh
```

In a second terminal:

```bash
./scripts/dev-frontend.sh
```

### Troubleshooting: MySQL “Access denied”

If backend startup fails with `Access denied for user 'projectpulse'@'localhost'`, it usually means your local MySQL container volume was initialized with different credentials.

- Confirm the DB container is running: `docker compose ps`
- Reset the DB user password (non-destructive):
  - `docker exec project-pulse-mysql mysql -uroot -proot -e "ALTER USER 'projectpulse'@'%' IDENTIFIED BY 'projectpulse'; FLUSH PRIVILEGES;"`
- If you don’t need existing DB data, recreate the DB from scratch (destructive):
  - `docker compose down -v && docker compose up -d`
