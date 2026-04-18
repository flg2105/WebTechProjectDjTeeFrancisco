import { apiClient } from '../../shared/services/apiClient'

const peerEvalBasePath = '/api/peer-evaluations'

export const peerEvalService = {
  findCurrent: () => apiClient.get(peerEvalBasePath),
  submit: (evaluation) => apiClient.post(peerEvalBasePath, evaluation),
  findOwnReport: () => apiClient.get(`${peerEvalBasePath}/me/report`)
}
