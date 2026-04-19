# ADR-0001: Defer JWT Enforcement

Date: 2026-04-18
Status: Accepted

## Context

`docs/api-guidelines.md` defines HTTP Basic login followed by JWT-bearing requests. The prototype deadline is Monday, April 27, 2026, and Phase 1 needs shared backend/frontend conventions locked before the team splits into parallel domain work.

Authentication is required for the finished system because WARs and peer evaluations contain student performance data. However, enforcing JWT before the core Section, Team, User, WAR, and Peer Eval flows exist would slow early integration and add cross-cutting changes to every branch.

## Decision

For Phase 1 and the first prototype slices, defer JWT enforcement while keeping the API design compatible with JWT.

- Do not add Spring Security enforcement yet.
- Do not require `Authorization: Bearer <token>` on prototype endpoints yet.
- Keep all endpoint responses wrapped in `Result`.
- Keep auth-sensitive use cases labeled in PRs and docs so security can be added before production.
- When auth is implemented, follow `docs/api-guidelines.md`: Basic Auth for login and JWT for subsequent requests.

## Consequences

- Feature branches can build and test endpoints without coordinating security configuration changes.
- The frontend shared API client can later add an `Authorization` header in one place.
- Prototype data must be treated as local/demo data only until auth enforcement is added.
- Before deployment beyond a class demo environment, the team must add JWT enforcement, role checks, and tests for authenticated and unauthorized requests.
