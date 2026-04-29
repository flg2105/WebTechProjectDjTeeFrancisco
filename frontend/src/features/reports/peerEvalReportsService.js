import { apiClient } from '../../shared/services/apiClient'

const peerEvalBasePath = '/api/peer-evaluations'

function withQuery(path, params) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      query.set(key, value)
    }
  })
  const suffix = query.toString()
  return suffix ? `${path}?${suffix}` : path
}

export const peerEvalReportsService = {
  findSectionReport: (sectionId, weekStartDate) =>
    apiClient.get(withQuery(`${peerEvalBasePath}/section-report`, { sectionId, weekStartDate })),
  findStudentReport: (studentUserId, startActiveWeekId, endActiveWeekId) =>
    apiClient.get(withQuery(`${peerEvalBasePath}/student-report`, { studentUserId, startActiveWeekId, endActiveWeekId }))
}

