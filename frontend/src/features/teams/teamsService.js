import { apiClient } from '../../shared/services/apiClient'

const teamsBasePath = '/api/teams'

export const teamsService = {
  findAll: () => apiClient.get(teamsBasePath),
  findById: (teamId) => apiClient.get(`${teamsBasePath}/${teamId}`),
  create: (team) => apiClient.post(teamsBasePath, team),
  update: (teamId, team) => apiClient.put(`${teamsBasePath}/${teamId}`, team),
  remove: (teamId) => apiClient.del(`${teamsBasePath}/${teamId}`)
}
