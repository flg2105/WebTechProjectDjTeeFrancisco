# Onboarding

This repo is intentionally scaffolded so **three team members can start parallel work immediately** on separate use-case branches.

## Prereqs

- Java 17+
- Node.js 20+ (LTS)
- Docker Desktop (for MySQL)

## Local Run

1. Start MySQL:

```bash
docker compose up -d
```

2. Backend:

```bash
cd backend
./mvnw spring-boot:run
```

3. Frontend:

```bash
cd frontend
npm ci
npm run dev
```

4. Smoke test:

- Backend health: `GET http://localhost:8080/api/health`
- Frontend: `http://localhost:5173`

## Branching

- One use case per branch: `feature/uc-<id>-short-name`
- Keep PRs small and traceable: link the UC ID in the PR description.

## Team Parallelism (Avoid Merge Conflicts)

- Keep changes scoped to your assigned domain module(s) and feature folder(s).
- Avoid editing shared scaffolding files (`docker-compose.yml`, `.github/workflows/ci.yml`, global docs) unless the team agrees.
