import { apiClient } from '../../shared/services/apiClient'

const sectionsBasePath = '/api/sections'

export const sectionsService = {
  findAll: (name = '') => apiClient.get(`${sectionsBasePath}${name ? `?name=${encodeURIComponent(name)}` : ''}`),
  findById: (sectionId) => apiClient.get(`${sectionsBasePath}/${sectionId}`),
  create: (section) => apiClient.post(sectionsBasePath, section),
  update: (sectionId, section) => apiClient.put(`${sectionsBasePath}/${sectionId}`, section),
  updateActiveWeeks: (sectionId, activeWeeks) => apiClient.put(`${sectionsBasePath}/${sectionId}/active-weeks`, activeWeeks)
}
