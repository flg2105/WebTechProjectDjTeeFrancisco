# Final Steps Checklist (Definition of “Complete”)

This checklist captures the remaining work needed for Project Pulse to be considered complete against the requirements and team conventions.

Source of truth:
- `requirements/2_Vision_&_Scope.md`
- `requirements/3_Use_Cases.md`
- `requirements/1_Project_Glossary.md`

Implementation conventions:
- `docs/architecture.md`
- `docs/api-guidelines.md`
- `docs/coding-standards.md`
- `docs/testing-strategy.md`
- `docs/decisions/0001-auth-direction.md` (auth deferred for prototype; required for “complete”)

Related planning:
- `docs/development-plan.md`
- `docs/team-phase-plan.md`

---

## Backend (Spring Boot) — Missing / Incomplete Use Cases

### Instructor ↔ Team assignment (missing)
- [x] UC-19: Admin assigns instructors to senior design teams (`requirements/3_Use_Cases.md`)
- [x] UC-20: Admin removes an instructor from a senior design team (`requirements/3_Use_Cases.md`)

Notes:
- Team APIs now include instructor assignment endpoints and `TeamResponse` includes `instructorUserIds`.
- Align storage and module boundaries with `docs/database-design.md` and layering rules in `docs/architecture.md`.

### Instructor management (missing/partial)
- [x] UC-22: Admin views an instructor (`requirements/3_Use_Cases.md`)
- [x] UC-23: Admin deactivates an instructor (`requirements/3_Use_Cases.md`)
- [x] UC-24: Admin reactivates an instructor (`requirements/3_Use_Cases.md`)

Notes:
- `GET /api/instructors` exists, but there is no instructor “details” read and no status transition endpoint.

### Reporting (missing / partially covered)
- [x] UC-32: Instructor/Student generates a WAR report of a senior design team (`requirements/3_Use_Cases.md`)
- [x] UC-31..UC-34: confirm all instructor reporting requirements are met and consistent with the report UIs planned in `docs/team-phase-plan.md`

Notes:
- WAR currently supports student report: `GET /api/wars/student-report?...` (UC-34 path in `docs/team-phase-plan.md`).
- Peer evaluation reporting endpoints exist for section/student, but UX + access control still need completion.

### “View details” completeness (partial)
- [x] UC-3: View a senior design section — ensure the section details view matches required contents (teams, members, instructors, unassigned lists, rubric) (`requirements/3_Use_Cases.md`)
- [x] UC-16: View a student — ensure `StudentDetailsResponse` is populated with the required details (`requirements/3_Use_Cases.md`)
- [x] UC-21: Find instructors — ensure results include any required associations (team assignment fields are currently blank) (`requirements/3_Use_Cases.md`)

### Student account deletion (complete)
- [x] UC-17: Admin deletes a student account and removes associated WAR and peer-evaluation records (`requirements/3_Use_Cases.md`)

---

## Frontend (Vue 3) — Missing / Incomplete Screens

### WAR student workflow (complete)
- [x] UC-27: Student manages activities in a Weekly Activity Report (WAR) (`requirements/3_Use_Cases.md`)

Notes:
- Implemented WAR add/edit/delete flow in `frontend/src/features/war/WarView.vue`.
- WAR API wiring lives in `frontend/src/features/war/warService.js` via `frontend/src/shared/services/apiClient.js`.

### Instructor reporting views (missing)
- [x] Add UI screens/routes for instructor reporting flows (UC-31..UC-34) (`requirements/3_Use_Cases.md`)
- [x] Wire report pages to backend endpoints and align messaging/error states with `docs/api-guidelines.md`

### Instructor administration (blocked by backend gaps)
- [x] Add UI for instructor view/deactivate/reactivate (UC-22..UC-24) once backend APIs exist (`requirements/3_Use_Cases.md`)
- [x] Add UI for assigning/removing instructors to teams (UC-19/UC-20) once backend APIs exist (`requirements/3_Use_Cases.md`)

---

## Security (Required for “Complete”)

Auth is explicitly deferred for the prototype per `docs/decisions/0001-auth-direction.md`, but is required for a complete system.

- [x] Implement login using HTTP Basic auth, then JWT for subsequent requests (`docs/api-guidelines.md`)
- [x] Enforce role-based authorization (Admin/Instructor/Student) per use case preconditions (`requirements/3_Use_Cases.md`)
- [x] Add backend tests covering unauthenticated + unauthorized access paths (`docs/testing-strategy.md`)
- [x] Update frontend API client to attach JWT (`frontend/src/shared/services/apiClient.js`)

---

## Quality Gate (Project “Done”)

- [x] Every implemented endpoint returns the standard `Result` envelope (`docs/api-guidelines.md`)
- [x] Core service logic has automated tests (at least 1 per new endpoint) (`docs/testing-strategy.md`)
- [x] CI is green: backend tests + frontend build (`.github/workflows/ci.yml`)
- [x] No use-case-critical placeholders remain in the UI (notably WAR and reporting pages)
