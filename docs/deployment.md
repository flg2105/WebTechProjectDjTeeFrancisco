# Deployment

## Target Architecture

- Frontend: Azure App Service
- Backend: Azure App Service
- Database: Azure Database for MySQL
- CI/CD: GitHub Actions

## Required Azure Resources

1. Create an Azure App Service for the Spring Boot backend.
2. Create an Azure Database for MySQL server and a `project_pulse` database.
3. Create an Azure App Service for the Vue frontend.

## GitHub Workflows

The repo now includes:

- [deploy-backend.yml](/c:/Users/teemo/WebTechProjectDjTeeFrancisco/.github/workflows/deploy-backend.yml:1)
- [deploy-frontend.yml](/c:/Users/teemo/WebTechProjectDjTeeFrancisco/.github/workflows/deploy-frontend.yml:1)

These deploy from `main` and can also be run manually.

## GitHub Secrets

Add these repository secrets before deploying:

- `AZURE_BACKEND_APP_NAME`
- `AZURE_BACKEND_PUBLISH_PROFILE`
- `AZURE_FRONTEND_APP_NAME`
- `AZURE_FRONTEND_PUBLISH_PROFILE`
- `VITE_API_BASE_URL`

`VITE_API_BASE_URL` should point to the deployed backend, for example:

```text
https://your-backend-app.azurewebsites.net
```

## Backend App Settings

Configure these in Azure App Service:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_CORS_ALLOWED_ORIGINS`

Example values:

```text
SPRING_DATASOURCE_URL=jdbc:mysql://your-mysql-server.mysql.database.azure.com:3306/project_pulse?useSSL=true&requireSSL=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=projectpulse
APP_CORS_ALLOWED_ORIGINS=https://your-frontend-app.azurewebsites.net
```

The backend now reads `server.port` from `PORT`, which is compatible with Azure App Service.

## Frontend Configuration

- `VITE_API_BASE_URL` is compiled into the Vue build during the frontend deployment workflow.
- The workflow packages only the built `dist/` directory, [server.js](/c:/Users/teemo/WebTechProjectDjTeeFrancisco/frontend/server.js:1), and package metadata for App Service ZIP deploy.
- Configure the frontend App Service as a Node 20 app. The app starts with `npm start`, which serves the built Vue SPA and falls back to `index.html` for client-side routes.

## Deployment Order

1. Provision Azure MySQL and collect the connection values.
2. Create the backend App Service and configure its app settings.
3. Run the backend deployment workflow and confirm `https://<backend>/api/health` works.
4. Set `VITE_API_BASE_URL` to that backend URL.
5. Run the frontend deployment workflow.

## Notes

- [ci.yml](/c:/Users/teemo/WebTechProjectDjTeeFrancisco/.github/workflows/ci.yml:1) still handles validation only.
- Keep Azure credentials and publish profiles in GitHub Secrets, not in the repo.
