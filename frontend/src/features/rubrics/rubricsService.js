import { apiClient } from '../../shared/services/apiClient'

const rubricsBasePath = '/api/rubrics'

export const rubricsService = {
  findAll: () => apiClient.get(rubricsBasePath),
  findById: (id) => apiClient.get(`${rubricsBasePath}/${id}`),
  create: (rubric) => apiClient.post(rubricsBasePath, rubric)
}
