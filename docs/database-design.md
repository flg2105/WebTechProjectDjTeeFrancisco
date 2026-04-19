# Database Design (Initial Draft)

This is a working schema draft to unblock parallel backend work. It will evolve as use cases are implemented.

## Core Entities (Prototype)

- `users`
  - `id` (PK)
  - `email` (unique)
  - `display_name`
  - `role` (ADMIN | INSTRUCTOR | STUDENT)
  - `status` (ACTIVE | INACTIVE)

- `sections`
  - `id` (PK)
  - `name` (unique within academic year)
  - `start_date`
  - `end_date`
  - `created_at`

- `section_weeks`
  - `id` (PK)
  - `section_id` (FK → sections.id)
  - `week_start_date` (Monday date, unique per section)
  - `active` (boolean)

- `teams`
  - `id` (PK)
  - `section_id` (FK → sections.id)
  - `name` (unique within section)

- `team_memberships`
  - `id` (PK)
  - `team_id` (FK → teams.id)
  - `student_user_id` (FK → users.id)

- `rubrics`
  - `id` (PK)
  - `name` (unique)
  - `created_at`

- `rubric_criteria`
  - `id` (PK)
  - `rubric_id` (FK → rubrics.id)
  - `name`
  - `description`
  - `max_score` (decimal)
  - `position` (int)

- `war_entries`
  - `id` (PK)
  - `section_week_id` (FK → section_weeks.id)
  - `team_id` (FK → teams.id)
  - `student_user_id` (FK → users.id)
  - `submitted_at` (nullable for drafts)

- `war_activities`
  - `id` (PK)
  - `war_entry_id` (FK → war_entries.id)
  - `category`
  - `planned_activity`
  - `description`
  - `hours_planned` (decimal)
  - `hours_actual` (decimal)
  - `status`

- `peer_evaluations`
  - `id` (PK)
  - `section_week_id` (FK → section_weeks.id)
  - `team_id` (FK → teams.id)
  - `evaluator_user_id` (FK → users.id)
  - `evaluatee_user_id` (FK → users.id)
  - `rubric_id` (FK → rubrics.id)
  - `public_comments` (text, nullable)
  - `private_comments` (text, nullable)
  - `submitted_at`

- `peer_evaluation_scores`
  - `id` (PK)
  - `peer_evaluation_id` (FK → peer_evaluations.id)
  - `rubric_criterion_id` (FK → rubric_criteria.id)
  - `score` (decimal)

## Notes

- Use case docs define “week starts Monday and ends Sunday”; `section_weeks.week_start_date` models this.
- Prototype can treat “invites” as emails recorded in an `invitations` table later if needed; start with simple flows first.
