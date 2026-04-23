import { apiClient } from '../../shared/services/apiClient'

const teamsBasePath = '/api/teams'

export const teamsService = {
  findAll: (sectionId = '') => apiClient.get(`${teamsBasePath}${sectionId ? `?sectionId=${sectionId}` : ''}`),
  findById: (teamId) => apiClient.get(`${teamsBasePath}/${teamId}`),
  create: (team) => apiClient.post(teamsBasePath, team),
  update: (teamId, team) => apiClient.put(`${teamsBasePath}/${teamId}`, team),
  remove: (teamId) => apiClient.del(`${teamsBasePath}/${teamId}`),
  assignStudent: (teamId, studentUserId) => apiClient.post(`${teamsBasePath}/${teamId}/students`, { studentUserId }),
  removeStudent: (teamId, studentUserId) => apiClient.del(`${teamsBasePath}/${teamId}/students/${studentUserId}`)
}
