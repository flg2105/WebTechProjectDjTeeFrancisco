import { apiClient } from '../../shared/services/apiClient'

const usersBasePath = '/api/users'

export const usersService = {
  findAll: (role = '') => apiClient.get(`${usersBasePath}${role ? `?role=${role}` : ''}`),
  findStudents: (q = '') => apiClient.get(`/api/students${q ? `?q=${encodeURIComponent(q)}` : ''}`),
  findInstructors: (q = '') => apiClient.get(`/api/instructors${q ? `?q=${encodeURIComponent(q)}` : ''}`),
  viewInstructor: (id) => apiClient.get(`/api/instructors/${Number(id)}`),
  deactivateInstructor: (id, reason) => apiClient.post(`/api/instructors/${Number(id)}/deactivate`, { reason }),
  reactivateInstructor: (id) => apiClient.post(`/api/instructors/${Number(id)}/reactivate`),
  setupStudent: (account) => apiClient.post(`${usersBasePath}/student-setup`, account),
  setupInstructor: (account) => apiClient.post(`${usersBasePath}/instructor-setup`, account),
  inviteStudents: (payload) => apiClient.post('/api/invitations/students', payload),
  inviteInstructors: (payload) => apiClient.post('/api/invitations/instructors', payload)
}
