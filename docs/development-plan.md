# Development Plan

Prototype deadline: **Monday, April 27, 2026** (10 days from **Friday, April 17, 2026**; see `docs/team-workflow.md`).

Source of truth requirements:
- `requirements/2_Vision_&_Scope.md`
- `requirements/3_Use_Cases.md`
- `requirements/1_Project_Glossary.md`

## Phases (4)

The execution plan is tracked in `docs/team-phase-plan.md`. High-level breakdown:

### Phase 1 – Foundation (Apr 17–Apr 18)
- Scaffold backend + frontend + MySQL compose
- Establish `Result` wrapper + error handling conventions
- Create domain module boundaries to enable parallel work
- CI pipeline for backend tests + frontend build

### Phase 2 – Admin Setup Flows (Apr 19–Apr 22)
- Rubrics (UC-1)
- Sections + active weeks (UC-2..UC-6)
- Teams + assignments + invites (UC-7..UC-14, UC-11..UC-12, UC-18)

### Phase 3 – Student Weekly Workflows (Apr 23–Apr 25)
- WAR activities (UC-27)
- Peer evaluation submission and self-report (UC-28..UC-29)
- Account edits + basic find/view flows as needed (UC-26, UC-15..UC-17)

Current status:
- UC-28 and UC-29 are implemented in the `peereval` domain and frontend flow.

### Phase 4 – Reports + Hardening + Demo (Apr 26–Apr 27)
- Instructor report endpoints for demo (UC-31..UC-34, prioritized by demo value)
- Validation/error-state polish
- Deployment readiness checks

## Domain Ownership (Parallelizable)

Member A – DJ BROWN:
- `user` domain (Admin/Instructor/Student accounts, invites)

Member B – FRANCISCO GONZALES:
- `section` and `team` domains (sections, active weeks, team management)

Member C – TEE MOO:
- `rubric`, `war`, `peereval` domains (rubrics, weekly activity reports, peer evaluation flows)

## Definition of Done (Per PR)
- References UC ID(s) in PR description
- Follows module layering (controller/service/repository/domain/dto)
- Uses `Result` response wrapper
- Adds/updates tests for service logic or API contracts (at least 1 test per new endpoint)
- CI green
