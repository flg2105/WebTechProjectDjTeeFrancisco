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
- Ensure `Result` wrapper and global exception handling match `docs/api-guidelines.md`.
- Add initial auth scaffolding decision note (JWT vs defer) and wire placeholders (no enforcement yet).

Member B (Domain scaffolding + DB baseline):
- Create initial domain module boundaries in backend (`section`, `team`, `user`) with DTO patterns.
- Draft initial schema in `docs/database-design.md` for Section/Team/User/week concepts.

Member C (Frontend baseline + API client):
- Create feature-based Vue structure and shared API client.
- Create placeholder pages/routes for Rubrics, Sections, Teams, WAR, Peer Eval.

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

