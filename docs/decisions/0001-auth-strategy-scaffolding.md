# ADR-0001: Authentication strategy and Phase 1 scaffolding

Date: 2026-04-18
Status: Accepted

## Context

Project Pulse requires authentication and role-based access (Admin/Instructor/Student) in later phases, but Phase 1 prioritizes:

- everyone can run the app locally
- CI is green
- shared API conventions are locked

The current `docs/api-guidelines.md` defines a target approach:

- login via HTTP Basic Auth
- subsequent requests via JWT `Authorization: Bearer <token>`

## Decision

- Phase 1 implements **auth scaffolding only** (API entrypoints + DTO/service placeholders) with **no enforcement**.
- The team will implement **JWT-based auth** later (Phase 2+), keeping `docs/api-guidelines.md` as the target convention.
- Controllers/services must continue to return the standard `Result` wrapper for both success and error responses.

## Consequences

- Frontend/backend integration can proceed with stable endpoints while domain features are built.
- Role/permission enforcement is deferred; all endpoints remain accessible until security is added.
- When JWT is implemented, tests and any placeholder token logic will be replaced with real issuance/validation.

