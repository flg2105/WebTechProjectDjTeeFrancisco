# WebTechProjectDjTeeFrancisco

## UC-30 implemented on this branch

This branch now contains a minimum full-stack implementation of `UC-30: The Instructor sets up an instructor account`.

### Stack

- Frontend: Vue 3 + Vuetify
- Backend: Spring Boot 4.x structure
- Database: MySQL-ready schema and seed data

### UC-30 flow covered

1. Instructor opens the invitation link.
2. The system loads the registration page from the token.
3. The instructor enters first name, middle initial, last name, password, and reentered password.
4. The system validates the inputs.
5. The system shows a confirmation screen.
6. The instructor confirms registration or returns to edit.
7. The system stores the account and marks the invitation as used.
8. The system redirects to `/login`.

### Demo invitation

- Frontend route: `/register/instructor-invite-demo`
- Seeded invited email: `instructor1@tcu.edu`

### Backend API

- `GET /api/registrations/{token}`
- `POST /api/registrations/instructor`

### Notes

- Maven is not installed in the current environment, so I could not compile the backend here.
- Frontend dependencies are declared, but `npm install` has not been run in this workspace.
