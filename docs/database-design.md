# Database Design

## Scope

This Phase 1 draft tightens the core schema for `Section`, `Team`, `User`, and `ActiveWeek` so it matches the glossary and the use cases currently in scope for setup, lookup, and assignment flows.

## Entity summary

### Section

Represents a senior design section for one academic year offering.

- Table: `sections`
- Primary key: `id`
- Business key: `section_code`
- Required fields:
  - `section_code` in `YYYY-YYYY` format
  - `display_name`
  - `start_year`
  - `end_year`
- Constraints:
  - one row per academic-year section
  - `section_code` must be unique

### User

Represents a system account for a student, instructor, or admin.

- Table: `users`
- Primary key: `id`
- Business key: `email`
- Required fields:
  - `role`
  - `first_name`
  - `last_name`
  - `email`
  - `password_hash`
  - `status`
- Optional fields:
  - `middle_initial`
- Constraints:
  - `email` must be unique
  - `role` must support `STUDENT`, `INSTRUCTOR`, and `ADMIN`
  - account lifecycle stays separate from team assignment

### ActiveWeek

Represents whether a section week is active for WAR and peer-evaluation submissions.

- Table: `active_weeks`
- Primary key: `id`
- Foreign key: `section_id -> sections.id`
- Required fields:
  - `section_id`
  - `week_start_date`
  - `active`
- Constraints:
  - week starts on Monday, per glossary
  - uniqueness on `(section_id, week_start_date)`
  - active week rows belong to a section, not to a team

### Team

Represents a senior design team within a section.

- Table: `teams`
- Primary key: `id`
- Foreign key: `section_id -> sections.id`
- Required fields:
  - `section_id`
  - `name`
- Optional fields:
  - `project_name`
- Constraints:
  - team names are unique within a section
  - a team cannot exist outside a section

## Supporting relationship

### TeamMembership

This supporting table is needed to satisfy the team assignment use cases without leaking team state into the user model.

- Table: `team_memberships`
- Primary key: `id`
- Foreign keys:
  - `team_id -> teams.id`
  - `user_id -> users.id`
- Required fields:
  - `team_id`
  - `user_id`
  - `membership_role`
- Constraints:
  - uniqueness on `(team_id, user_id)`
  - supports both `STUDENT` and `INSTRUCTOR` assignments

## Relationship summary

- One `Section` has many `Teams`.
- One `Section` has many `ActiveWeeks`.
- One `Team` has many `TeamMembership` rows.
- One `User` can appear in many `TeamMembership` rows across the academic lifecycle.

## Alignment with glossary and use cases

- Glossary `Senior Design Section` maps to `sections`.
- Glossary `Senior Design Team` maps to `teams`.
- Glossary `Active Week` maps to `active_weeks` and is section-scoped.
- Use cases for finding, viewing, creating, and editing sections/teams depend on `sections` and `teams`.
- Use cases for assigning students and instructors to teams depend on `team_memberships`.
- User lookup and account lifecycle use cases depend on `users`.

## Boundary decisions captured in the schema

- No team-specific columns are stored on `users`.
- No active-week fields are stored on `teams`.
- No duplicated section metadata is stored on `teams` beyond the foreign key.
- Team assignment is modeled as a relationship table instead of embedded lists or repeated user columns.
