import { apiClient } from '../../shared/services/apiClient'

const warBasePath = '/api/war'

export const warService = {
  findCurrent: () => apiClient.get(warBasePath),
  saveActivity: (activity) => apiClient.post(`${warBasePath}/activities`, activity),
  updateActivity: (activityId, activity) => apiClient.put(`${warBasePath}/activities/${activityId}`, activity),
  removeActivity: (activityId) => apiClient.del(`${warBasePath}/activities/${activityId}`)
}
