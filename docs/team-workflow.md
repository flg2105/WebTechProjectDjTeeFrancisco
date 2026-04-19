# Team Workflow

## Branching

- main = stable
- One use case per branch: `feature/uc-<id>-short-name`
- Rebase or merge `main` daily to avoid long-lived drift

---

## Pull Requests

- Required for all changes
- Must include:
  - Description
  - Related use case
  - Test evidence
  - Frontend: screenshot(s) where applicable

---

## Code Review

- Check:
  - Requirements alignment
  - Architecture compliance
  - Code clarity

---

## Rules

- No direct push to main
- No major architectural change without discussion
- Keep communication clear
- Keep modules isolated: controller/service/repository/domain/dto within each domain module

---

## Productivity

- No team member should overly rely on another to finish work
- Each team member should have evenly distributed work
- Team members should be working on separate tasks
- Team members should begin their work immediately 
- The team should have a finished prototype in 10 days (**Friday, April 17, 2026** -> **Monday, April 27, 2026**)

## Team Playbook

- Local setup: `docs/onboarding.md`
- Phase plan + per-member assignments: `docs/team-phase-plan.md`
