import { apiClient } from '../../shared/services/apiClient'

const warBasePath = '/api/wars'

export const warService = {
  findWar: (studentUserId, activeWeekId) =>
    apiClient.get(`${warBasePath}?studentUserId=${Number(studentUserId)}&activeWeekId=${Number(activeWeekId)}`),
  addActivity: (activity) => apiClient.post(`${warBasePath}/activities`, activity),
  updateActivity: (activityId, activity) => apiClient.put(`${warBasePath}/activities/${activityId}`, activity),
  removeActivity: (activityId, studentUserId, activeWeekId) =>
    apiClient.del(
      `${warBasePath}/activities/${activityId}?studentUserId=${Number(studentUserId)}&activeWeekId=${Number(activeWeekId)}`
    )
}
