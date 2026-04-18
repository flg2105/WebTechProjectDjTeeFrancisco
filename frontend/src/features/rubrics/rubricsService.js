import { apiClient } from '../../shared/services/apiClient'

const rubricsBasePath = '/api/rubrics'

export const rubricsService = {
  findAll: () => apiClient.get(rubricsBasePath),
  create: (rubric) => apiClient.post(rubricsBasePath, rubric)
}
