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

export const peerEvalService = {
  findCurrent: (studentUserId) => apiClient.get(withQuery(peerEvalBasePath, { studentUserId })),
  submit: (evaluation) => apiClient.post(peerEvalBasePath, evaluation),
  findOwnReport: (studentUserId, weekStartDate) =>
    apiClient.get(withQuery(`${peerEvalBasePath}/me/report`, { studentUserId, weekStartDate }))
}
