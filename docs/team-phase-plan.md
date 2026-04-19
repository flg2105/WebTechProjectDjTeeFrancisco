# Team Phase Plan (Prototype Deadline: Monday, April 27, 2026)

This plan is the concrete execution path to a working prototype by **Monday, April 27, 2026** (10 days from **Friday, April 17, 2026**).

Owners:
- Member A: DJ BROWN
- Member B: FRANCISCO GONZALES
- Member C: TEE MOO

## Phases (4)

### Phase 1: Foundation (Apr 17–Apr 18)

Goal: everyone can run the app locally; CI is green; shared conventions are locked.

Member A (Backend baseline + system):
- Complete: `Result`, `StatusCode`, `GlobalExceptionHandler`, and `ApiErrorController` match `docs/api-guidelines.md` for success and error responses.
- Complete: integration tests cover `/api/health`, not found, and method-not-allowed responses as `Result` payloads.
- Complete: `docs/decisions/0001-auth-direction.md` records the auth direction: defer JWT enforcement for early prototype work, keep JWT-compatible API design.

Member B (Domain scaffolding + DB baseline):
- Complete: backend module folders exist for `section`, `team`, and `user`, each with `controller`, `service`, `repository`, `domain`, and `dto`.
- Complete: initial boundaries are locked in `docs/database-design.md` so modules reference each other by IDs/DTOs rather than sharing repositories or persistence entities.
- Complete: `docs/database-design.md` covers Section, Team, User, and Active Week concepts from the glossary/use cases.

Member C (Frontend baseline + API client):
- Complete: frontend feature folders exist for `rubrics`, `sections`, `teams`, `war`, and `peereval`.
- Complete: feature services call APIs through `frontend/src/shared/services/apiClient.js`.
- Complete: routes/placeholders exist for Rubrics, Sections, Teams, WAR, and Peer Eval.
- Complete: Home can call `/api/health` through the shared API client once the backend is running.

Shared:
- Complete: `docs/onboarding.md` lists required local dependencies and quickstart steps for JDK 17+, Node.js 20+, Docker Desktop, backend, frontend, and the health smoke test.
- Complete: prototype deadline plan remains documented here and summarized in `docs/development-plan.md`.

### Phase 2: Admin Setup Flows (Apr 19–Apr 22)

Goal: Admin can create/manage the “container” data needed by students/instructors.

Member A (Users + invitations):
- Implement UC-18 (invite instructors), UC-25/UC-30 (account setup scaffolding) as minimal API flows.
- Define roles: Admin/Instructor/Student (enforced later if needed).

Member B (Sections + teams):
- Implement UC-2..UC-6 (sections + active weeks) and UC-7..UC-14 (teams CRUD + assignments entrypoints).

Member C (Rubrics):
- Implement UC-1 (create rubric) and basic rubric retrieval for use by peer eval.

### Phase 3: Student Weekly Workflows (Apr 23–Apr 25)

Goal: Students can submit WAR and Peer Eval; instructor can view basic report output.

Member A (Account editing + student/instructor lookup stubs):
- Implement UC-26 (edit account) and UC-15..UC-17 (find/view/delete student) as minimal admin tools.

Member B (WAR):
- Implement UC-27 (manage activities in WAR) with a minimal weekly model tied to active weeks.

Member C (Peer evaluation):
- Implement UC-28 (submit peer eval) and UC-29 (view own report) using the rubric criteria.

### Phase 4: Reports + Hardening + Demo (Apr 26–Apr 27)

Goal: one coherent demo flow, stable API/UX, basic validations, and deployment readiness.

Member A (Instructor reports):
- Implement UC-33 (peer eval report of a student) and/or UC-34 (WAR report of the student), whichever is simpler to demo cleanly.

Member B (Section reports):
- Implement UC-31 (peer eval report of entire section) or a pared-down version for demo (paged list + exports later).

Member C (Frontend polish + integration):
- Ensure end-to-end UX for: Admin setup → Student submit → Instructor view report.
- Fix cross-feature UI issues and error states.

## Daily Integration Rules

- Rebase or merge `main` daily.
- PRs must include: UC ID(s), screenshots (frontend) or curl examples (backend), and test notes.
- If a change crosses module boundaries, coordinate before pushing.
