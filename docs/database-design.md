# Database Design (Initial Draft)

This is a working schema draft to unblock parallel backend work. It will evolve as use cases are implemented.

## Phase 1 Domain Boundaries

The initial backend work is split across the `section`, `team`, and `user` modules so members can work in parallel without cross-module leakage.

- `section` owns Senior Design Section data and Active Week setup from UC-2 through UC-6.
- `team` owns Senior Design Team records and team membership/assignment flows from UC-7 through UC-14, plus instructor/team assignment entrypoints from UC-19 and UC-20.
- `user` owns account identity, role, status, and invitation/account setup flows from UC-15 through UC-18 and UC-21 through UC-26/UC-30.

Boundary rules:

- Controllers only call services in their own module.
- Services may reference another module by identifier fields such as `section_id`, `team_id`, or `user_id`; do not share persistence entities across module boundaries.
- Repositories stay private to their module package.
- Cross-module behavior should be coordinated through service methods or DTOs once the owning module exposes them.

## Core Entities (Prototype)

- `users`
  - `id` (PK)
  - `email` (unique)
  - `display_name`
  - `role` (ADMIN | INSTRUCTOR | STUDENT)
  - `status` (INVITED | ACTIVE | INACTIVE)
  - `created_at`
  - `updated_at`

- `invitations`
  - `id` (PK)
  - `email`
  - `role` (INSTRUCTOR | STUDENT for prototype flows)
  - `section_id` (nullable; used for student section invitations)
  - `created_at`

- `sections`
  - `id` (PK)
  - `name` (unique within academic year)
  - `academic_year` (for example, `2026-2027`)
  - `start_date`
  - `end_date`
  - `created_at`
  - `updated_at`

- `active_weeks`
  - `id` (PK)
  - `section_id` (FK → sections.id)
  - `week_start_date` (Monday date, unique per section)
  - `active` (boolean)
  - unique (`section_id`, `week_start_date`)

- `teams`
  - `id` (PK)
  - `section_id` (FK → sections.id)
  - `name` (unique within section)
  - `created_at`
  - `updated_at`
  - unique (`section_id`, `name`)

- `team_memberships`
  - `id` (PK)
  - `team_id` (FK → teams.id)
  - `student_user_id` (FK → users.id)
  - unique (`team_id`, `student_user_id`)

- `team_instructors`
  - `id` (PK)
  - `team_id` (FK → teams.id)
  - `instructor_user_id` (FK → users.id)
  - unique (`team_id`, `instructor_user_id`)

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
  - `active_week_id` (FK → active_weeks.id)
  - `team_id` (FK → teams.id)
  - `student_user_id` (FK → users.id)
  - `submitted_at` (nullable for drafts)
  - unique (`active_week_id`, `student_user_id`)

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
  - `active_week_id` (FK → active_weeks.id)
  - `team_id` (FK → teams.id)
  - `evaluator_user_id` (FK → users.id)
  - `evaluatee_user_id` (FK → users.id)
  - `rubric_id` (FK → rubrics.id)
  - `public_comments` (text, nullable)
  - `private_comments` (text, nullable)
  - `submitted_at`
  - unique (`active_week_id`, `evaluator_user_id`, `evaluatee_user_id`)

- `peer_evaluation_scores`
  - `id` (PK)
  - `peer_evaluation_id` (FK → peer_evaluations.id)
  - `rubric_criterion_id` (FK → rubric_criteria.id)
  - `score` (decimal)

## Notes

- Use case docs define "week starts Monday and ends Sunday"; `active_weeks.week_start_date` models this and should always be a Monday date.
- `active_weeks.active = false` covers holiday/break weeks where WAR and peer evaluation submissions are not required.
- Team membership should point to users with `role = STUDENT`; team instructors should point to users with `role = INSTRUCTOR`.
- Prototype invitation flows record emails in `invitations` and create or update a matching user with `status = INVITED`.
