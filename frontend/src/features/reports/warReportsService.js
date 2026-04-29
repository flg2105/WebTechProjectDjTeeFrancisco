import { apiClient } from '../../shared/services/apiClient'

const warBasePath = '/api/wars'

export const warReportsService = {
  findStudentReport: (studentUserId, startActiveWeekId, endActiveWeekId) =>
    apiClient.get(
      `${warBasePath}/student-report?studentUserId=${Number(studentUserId)}&startActiveWeekId=${Number(startActiveWeekId)}&endActiveWeekId=${Number(endActiveWeekId)}`
    ),
  findTeamReport: (teamId, activeWeekId) =>
    apiClient.get(`${warBasePath}/team-report?teamId=${Number(teamId)}&activeWeekId=${Number(activeWeekId)}`)
}

