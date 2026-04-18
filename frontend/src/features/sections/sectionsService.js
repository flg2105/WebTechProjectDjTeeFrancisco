import { apiClient } from '../../shared/services/apiClient'

const sectionsBasePath = '/api/sections'

export const sectionsService = {
  findAll: () => apiClient.get(sectionsBasePath),
  findById: (sectionId) => apiClient.get(`${sectionsBasePath}/${sectionId}`),
  create: (section) => apiClient.post(sectionsBasePath, section),
  update: (sectionId, section) => apiClient.put(`${sectionsBasePath}/${sectionId}`, section)
}
