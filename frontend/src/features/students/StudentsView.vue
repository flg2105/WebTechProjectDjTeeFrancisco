<template>
  <section class="phase-page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">UC-15 through UC-17</p>
        <h1>Students</h1>
        <p class="helper">Find students, inspect their section/team placement, and review WAR and peer-evaluation history.</p>
      </div>
      <button class="icon-button" type="button" title="Reload students" @click="loadAll">R</button>
    </div>

    <p v-if="error" class="notice error">{{ error }}</p>
    <p v-if="message" class="notice success">{{ message }}</p>

    <div class="layout-grid">
      <div class="stack-gap-md">
        <section class="panel">
          <div class="panel-heading">
            <h2>Create Student Account</h2>
          </div>

          <form class="setup-form" @submit.prevent="setupStudent">
            <label>
              Display name
              <input v-model="studentForm.displayName" required placeholder="Jane Student" />
            </label>
            <label>
              Email
              <input v-model="studentForm.email" required type="email" placeholder="student@tcu.edu" />
            </label>
            <label>
              Password
              <input
                v-model="studentForm.password"
                required
                type="password"
                minlength="8"
                placeholder="Minimum 8 characters"
              />
            </label>
            <button class="text-button" type="submit" :disabled="isCreatingStudent">
              {{ isCreatingStudent ? 'Creating...' : 'Create Student Account' }}
            </button>
          </form>
        </section>

        <section class="panel">
          <div class="panel-heading">
            <h2>Find Students</h2>
          </div>

          <form class="search-row" @submit.prevent="loadStudents">
            <input v-model.trim="searchQuery" placeholder="Search by name or email" />
            <button class="text-button" type="submit">Search</button>
          </form>

          <div v-if="isLoadingList" class="empty-state">Loading students...</div>
          <div v-else-if="students.length === 0" class="empty-state">No students match the current search.</div>
          <article
            v-for="student in students"
            v-else
            :key="student.id"
            :class="['list-item', { selected: selectedStudentId === student.id }]"
          >
            <button class="list-select" type="button" @click="selectStudent(student.id)">
              <div class="stack-gap-sm">
                <div>
                  <strong>{{ student.displayName }}</strong>
                  <p>{{ student.email }}</p>
                </div>
                <div class="helper-grid">
                  <p class="helper">Section: {{ student.sectionName || 'Not assigned' }}</p>
                  <p class="helper">Team: {{ student.teamName || 'Not assigned' }}</p>
                </div>
              </div>
            </button>
          </article>
        </section>
      </div>

      <section class="panel detail-panel">
        <div class="panel-heading">
          <h2>Student Details</h2>
        </div>

        <div v-if="isLoadingDetails" class="empty-state">Loading student details...</div>
        <div v-else-if="!selectedStudent" class="empty-state">Choose a student to inspect.</div>
        <div v-else class="stack-gap-md">
          <div v-if="isAdmin" class="delete-student-panel">
            <div>
              <h3>Delete Student Account</h3>
              <p class="helper mb-0">
                Permanently removes this student and their WAR and peer-evaluation records.
              </p>
            </div>
            <button class="danger-button" type="button" :disabled="isDeletingStudent" @click="deleteSelectedStudent">
              {{ isDeletingStudent ? 'Deleting...' : 'Delete Student' }}
            </button>
          </div>

          <div class="detail-grid">
            <div>
              <p class="detail-label">First name</p>
              <strong>{{ selectedStudent.firstName || 'Not captured' }}</strong>
            </div>
            <div>
              <p class="detail-label">Last name</p>
              <strong>{{ selectedStudent.lastName || 'Not captured' }}</strong>
            </div>
            <div>
              <p class="detail-label">Display name</p>
              <strong>{{ selectedStudent.displayName }}</strong>
            </div>
            <div>
              <p class="detail-label">Email</p>
              <strong>{{ selectedStudent.email }}</strong>
            </div>
            <div>
              <p class="detail-label">Section</p>
              <strong>{{ selectedStudent.sectionName || 'Not assigned' }}</strong>
            </div>
            <div>
              <p class="detail-label">Team</p>
              <strong>{{ selectedStudent.teamName || 'Not assigned' }}</strong>
            </div>
          </div>

          <div class="stack-gap-sm">
            <div class="section-heading align-start">
              <div>
                <h3>WARs</h3>
                <p class="helper mb-0">Weekly activity reports submitted by this student.</p>
              </div>
            </div>

            <div v-if="selectedStudent.wars.length === 0" class="empty-state">
              No WARs are recorded for this student yet.
            </div>
            <div v-else class="table-wrap">
              <table class="report-table">
                <thead>
                  <tr>
                    <th>Week</th>
                    <th>Team</th>
                    <th>Activities</th>
                    <th>Submitted</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="war in selectedStudent.wars" :key="war.id">
                    <td>{{ war.weekStartDate || 'Unknown week' }}</td>
                    <td>{{ war.teamName || 'Unknown team' }}</td>
                    <td>{{ war.activityCount }}</td>
                    <td>{{ formatDateTime(war.submittedAt) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="stack-gap-sm">
            <div class="section-heading align-start">
              <div>
                <h3>Peer Evaluations</h3>
                <p class="helper mb-0">Evaluations this student received from teammates.</p>
              </div>
            </div>

            <div v-if="selectedStudent.peerEvaluations.length === 0" class="empty-state">
              No peer evaluations are recorded for this student yet.
            </div>
            <div v-else class="table-wrap">
              <table class="report-table">
                <thead>
                  <tr>
                    <th>Week</th>
                    <th>Evaluator</th>
                    <th>Team</th>
                    <th>Average score</th>
                    <th>Public comment</th>
                    <th>Private comment</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="evaluation in selectedStudent.peerEvaluations" :key="evaluation.entryId">
                    <td>{{ evaluation.weekStartDate || 'Unknown week' }}</td>
                    <td>{{ evaluation.evaluatorDisplayName }}</td>
                    <td>{{ evaluation.teamName || 'Unknown team' }}</td>
                    <td>{{ evaluation.averageScore }}</td>
                    <td>{{ evaluation.publicComment || 'None' }}</td>
                    <td>{{ evaluation.privateComment || 'None' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { authSession } from '../../shared/services/authSession'
import { usersService } from '../users/usersService'

const students = ref([])
const selectedStudent = ref(null)
const selectedStudentId = ref(null)
const searchQuery = ref('')
const isLoadingList = ref(false)
const isLoadingDetails = ref(false)
const isCreatingStudent = ref(false)
const isDeletingStudent = ref(false)
const error = ref('')
const message = ref('')
const studentForm = reactive({ displayName: '', email: '', password: '' })
const isAdmin = computed(() => authSession.currentUser?.role === 'ADMIN')

onMounted(loadAll)

async function loadAll() {
  await loadStudents()
  if (selectedStudentId.value) {
    await loadStudentDetails(selectedStudentId.value)
  }
}

async function loadStudents() {
  isLoadingList.value = true
  error.value = ''
  message.value = ''
  try {
    const result = await usersService.findStudents(searchQuery.value)
    students.value = result.data || []
    if (selectedStudentId.value && !students.value.some((item) => item.id === selectedStudentId.value)) {
      selectedStudentId.value = null
      selectedStudent.value = null
    }
  } catch (err) {
    error.value = err.message
  } finally {
    isLoadingList.value = false
  }
}

async function selectStudent(studentId) {
  selectedStudentId.value = studentId
  await loadStudentDetails(studentId)
}

async function loadStudentDetails(studentId) {
  isLoadingDetails.value = true
  error.value = ''
  message.value = ''
  try {
    const result = await usersService.viewStudent(studentId)
    selectedStudent.value = result.data
  } catch (err) {
    error.value = err.message
    selectedStudent.value = null
  } finally {
    isLoadingDetails.value = false
  }
}

async function setupStudent() {
  isCreatingStudent.value = true
  error.value = ''
  message.value = ''
  try {
    await usersService.setupStudent({ ...studentForm })
    message.value = 'Student account created.'
    studentForm.displayName = ''
    studentForm.email = ''
    studentForm.password = ''
    await loadStudents()
  } catch (err) {
    error.value = err.message
  } finally {
    isCreatingStudent.value = false
  }
}

async function deleteSelectedStudent() {
  if (!selectedStudent.value) {
    return
  }
  const studentName = selectedStudent.value.displayName || selectedStudent.value.email
  const confirmed = window.confirm(
    `Delete ${studentName}? This permanently removes the student account, team assignments, invitations, WARs, and peer evaluations.`
  )
  if (!confirmed) {
    return
  }

  isDeletingStudent.value = true
  error.value = ''
  message.value = ''
  try {
    await usersService.deleteStudent(selectedStudent.value.id)
    message.value = 'Student account deleted.'
    selectedStudentId.value = null
    selectedStudent.value = null
    await loadStudents()
  } catch (err) {
    error.value = err.message
  } finally {
    isDeletingStudent.value = false
  }
}

function formatDateTime(value) {
  if (!value) {
    return 'Not submitted'
  }
  return new Date(value).toLocaleString()
}
</script>

<style scoped>
.phase-page,
.panel,
.stack-gap-md,
.stack-gap-sm,
.helper-grid {
  display: grid;
  gap: 1rem;
}

.page-heading,
.panel-heading,
.search-row {
  align-items: center;
  display: flex;
  gap: 0.75rem;
}

.page-heading,
.panel-heading {
  justify-content: space-between;
}

.eyebrow,
.helper,
.empty-state,
.detail-label {
  margin: 0;
}

.layout-grid {
  display: grid;
  align-items: start;
  gap: 1.25rem;
  grid-template-columns: minmax(320px, 0.9fr) minmax(420px, 1.1fr);
}

.panel,
.list-item {
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(208, 218, 230, 0.8);
  border-radius: 26px;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
  padding: 1.35rem;
}

.list-item + .list-item {
  margin-top: 0.85rem;
}

.list-item.selected {
  border-color: rgba(94, 122, 255, 0.34);
  box-shadow: 0 20px 50px rgba(94, 122, 255, 0.14);
}

.list-select {
  background: transparent;
  border: 0;
  color: inherit;
  cursor: pointer;
  display: block;
  font: inherit;
  padding: 0;
  text-align: left;
  width: 100%;
}

.detail-grid {
  display: grid;
  gap: 0.9rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.detail-label {
  color: var(--text-soft);
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.detail-panel {
  align-self: start;
}

.delete-student-panel {
  align-items: center;
  background: var(--danger-soft);
  border: 1px solid rgba(203, 95, 86, 0.34);
  border-radius: 18px;
  display: flex;
  gap: 1rem;
  justify-content: space-between;
  padding: 1rem;
}

.table-wrap {
  overflow-x: auto;
}

.mb-0 {
  margin-bottom: 0;
}

@media (max-width: 900px) {
  .layout-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .page-heading,
  .panel-heading,
  .delete-student-panel,
  .search-row {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
