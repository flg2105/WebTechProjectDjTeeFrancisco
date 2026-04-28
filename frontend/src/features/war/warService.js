import { apiClient } from '../../shared/services/apiClient'

const warBasePath = '/api/wars'

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

export const warService = {
  findWar: (studentUserId, activeWeekId) =>
    apiClient.get(withQuery(warBasePath, { studentUserId, activeWeekId })),
  saveActivity: (activity) => apiClient.post(`${warBasePath}/activities`, activity),
  updateActivity: (activityId, activity) => apiClient.put(`${warBasePath}/activities/${activityId}`, activity),
  removeActivity: (activityId, studentUserId, activeWeekId) =>
    apiClient.del(withQuery(`${warBasePath}/activities/${activityId}`, { studentUserId, activeWeekId }))
}
