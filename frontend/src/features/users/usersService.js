import { apiClient } from '../../shared/services/apiClient'

const usersBasePath = '/api/users'

export const usersService = {
  findAll: (role = '') => apiClient.get(`${usersBasePath}${role ? `?role=${role}` : ''}`),
  findStudents: (q = '') => apiClient.get(`/api/students${q ? `?q=${encodeURIComponent(q)}` : ''}`),
  viewStudent: (id) => apiClient.get(`/api/students/${Number(id)}`),
  findInstructors: ({ firstName = '', lastName = '', teamName = '', status = '' } = {}) => {
    const params = new URLSearchParams()
    if (firstName) params.set('firstName', firstName)
    if (lastName) params.set('lastName', lastName)
    if (teamName) params.set('teamName', teamName)
    if (status) params.set('status', status)
    const query = params.toString()
    return apiClient.get(`/api/instructors${query ? `?${query}` : ''}`)
  },
  viewInstructor: (id) => apiClient.get(`/api/instructors/${Number(id)}`),
  deactivateInstructor: (id, reason) => apiClient.post(`/api/instructors/${Number(id)}/deactivate`, { reason }),
  reactivateInstructor: (id) => apiClient.post(`/api/instructors/${Number(id)}/reactivate`),
  createStudent: (account) => apiClient.post('/api/students', account),
  createInstructor: (account) => apiClient.post('/api/instructors', account),
  setupStudent: (account) => apiClient.post(`${usersBasePath}/student-setup`, account),
  setupInstructor: (account) => apiClient.post(`${usersBasePath}/instructor-setup`, account),
  inviteStudents: (payload) => apiClient.post('/api/invitations/students', payload),
  inviteInstructors: (payload) => apiClient.post('/api/invitations/instructors', payload)
}
