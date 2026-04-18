# Domain Boundaries

## Phase 1 ownership

Phase 1 locks the initial backend domain boundaries so parallel work can start without modules reaching into each other unpredictably.

### User module

- Owns account identity, invitation-driven registration, roles, and account status.
- Owns the `users` and `registration_invitations` tables.
- Must not own section calendars or direct team structure.

### Section module

- Owns senior design section metadata and the active week calendar.
- Owns the `sections` and `active_weeks` tables.
- Must not own passwords, invitation state, or team roster assignments.

### Team module

- Owns team records and roster assignments inside a section.
- Owns the `teams` and `team_memberships` tables.
- Must reference `section` and `user` through relationships or ids rather than copying their fields.

## Cross-module rules

- `ActiveWeek` belongs to a `Section`, not to a `Team`.
- `Team` belongs to exactly one `Section`.
- `User` identity and auth fields stay in the user module even when the user is assigned to teams.
- Team assignment uses a join table so students and instructors are not hard-coded into the `users` table or duplicated across team records.
- Controllers and services may coordinate across modules, but persistence ownership stays within the owning module.

## Current repo note

The existing registration flow remains intact, but the package structure now makes the long-term module ownership explicit:

- `com.projectpulse.backend.user`
- `com.projectpulse.backend.section`
- `com.projectpulse.backend.team`
