<template>
  <section class="phase-page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">UC-7 through UC-14</p>
        <h1>Teams</h1>
        <p class="helper">Shape section rosters, create teams, and keep assignments organized.</p>
      </div>
      <button class="icon-button" type="button" title="Reload teams" @click="loadAll">R</button>
    </div>

    <p v-if="message" class="notice success">{{ message }}</p>
    <p v-if="error" class="notice error">{{ error }}</p>

    <div class="layout-grid">
      <form class="panel" @submit.prevent="saveTeam">
        <h2>{{ selectedTeamId ? 'Edit Team' : 'Create Team' }}</h2>
        <label>
          Section
          <select v-model.number="teamForm.sectionId" required>
            <option disabled value="">Select a section</option>
            <option v-for="section in sections" :key="section.id" :value="section.id">
              {{ section.name }}
            </option>
          </select>
        </label>
        <label>
          Team name
          <input v-model="teamForm.name" required placeholder="Team Alpha" />
        </label>
        <div class="button-row">
          <button class="primary-button" type="submit" :disabled="savingTeam">
            {{ savingTeam ? 'Saving...' : 'Save Team' }}
          </button>
          <button class="text-button" type="button" @click="resetTeamForm">New</button>
        </div>
      </form>
    </div>

    <div class="panel">
      <div class="panel-heading">
        <h2>Find Teams</h2>
        <label>
          Section filter
          <select v-model.number="sectionFilter" @change="loadTeams">
            <option value="">All sections</option>
            <option v-for="section in sections" :key="section.id" :value="section.id">
              {{ section.name }}
            </option>
          </select>
        </label>
      </div>

      <div v-if="loading" class="empty-state">Loading teams...</div>
      <div v-else-if="teams.length === 0" class="empty-state">No teams found.</div>
      <article v-for="team in teams" v-else :key="team.id" class="team-item">
        <div>
          <strong>{{ team.name }}</strong>
          <p>
            {{ sectionName(team.sectionId) }} |
            {{ team.studentUserIds?.length || 0 }} students |
            {{ team.instructorUserIds?.length || 0 }} instructors
          </p>
          <p v-if="team.studentUserIds.length">Student IDs: {{ team.studentUserIds.join(', ') }}</p>
          <p v-if="team.instructorUserIds?.length">Instructor IDs: {{ team.instructorUserIds.join(', ') }}</p>
        </div>
        <div class="button-row">
          <button class="text-button" type="button" @click="selectTeam(team)">Edit</button>
          <button class="text-button" type="button" @click="selectedAssignmentTeam = team">Assign Students</button>
          <button class="text-button" type="button" @click="selectedInstructorAssignmentTeam = team">Assign Instructors</button>
          <button class="danger-button" type="button" @click="deleteTeam(team)">Delete</button>
        </div>
      </article>
    </div>

    <section class="panel">
      <h2>Assign Students</h2>
      <div v-if="!selectedAssignmentTeam" class="empty-state">Choose Assign on a team.</div>
      <form v-else class="assignment-form" @submit.prevent="assignStudent">
        <strong>{{ selectedAssignmentTeam.name }}</strong>
        <label>
          Student
          <select v-model.number="assignmentStudentId" required>
            <option disabled value="">Select a student</option>
            <option v-for="student in availableStudents" :key="student.id" :value="student.id">
              {{ student.displayName }} ({{ student.email }})
            </option>
          </select>
        </label>
        <p v-if="availableStudents.length === 0" class="empty-state mb-0">
          Every available student is already assigned to this team.
        </p>
        <button class="primary-button" type="submit">Assign Student</button>
        <div class="assigned-list">
          <button
            v-for="studentId in selectedAssignmentTeam.studentUserIds"
            :key="studentId"
            class="student-chip"
            type="button"
            title="Remove student from team"
            @click="removeStudent(studentId)"
          >
            {{ studentLabel(studentId) }} x
          </button>
        </div>
      </form>
    </section>

    <section class="panel">
      <h2>Assign Instructors</h2>
      <div v-if="!selectedInstructorAssignmentTeam" class="empty-state">Choose Assign Instructors on a team.</div>
      <form v-else class="assignment-form" @submit.prevent="assignInstructor">
        <strong>{{ selectedInstructorAssignmentTeam.name }}</strong>
        <label>
          Instructor
          <select v-model.number="assignmentInstructorId" required>
            <option disabled value="">Select an instructor</option>
            <option v-for="instructor in availableInstructors" :key="instructor.id" :value="instructor.id">
              {{ instructor.displayName }} ({{ instructor.email }})
            </option>
          </select>
        </label>
        <p v-if="availableInstructors.length === 0" class="empty-state mb-0">
          Every available instructor is already assigned to this team.
        </p>
        <button class="primary-button" type="submit">Assign Instructor</button>
        <div class="assigned-list">
          <button
            v-for="instructorId in selectedInstructorAssignmentTeam.instructorUserIds || []"
            :key="instructorId"
            class="student-chip"
            type="button"
            title="Remove instructor from team"
            @click="removeInstructor(instructorId)"
          >
            {{ instructorLabel(instructorId) }} x
          </button>
        </div>
      </form>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { sectionsService } from '../sections/sectionsService'
import { usersService } from '../users/usersService'
import { teamsService } from './teamsService'

const teams = ref([])
const sections = ref([])
const students = ref([])
const instructors = ref([])
const loading = ref(false)
const savingTeam = ref(false)
const message = ref('')
const error = ref('')
const selectedTeamId = ref(null)
const selectedAssignmentTeam = ref(null)
const selectedInstructorAssignmentTeam = ref(null)
const sectionFilter = ref('')
const assignmentStudentId = ref('')
const assignmentInstructorId = ref('')
const teamForm = reactive({ sectionId: '', name: '' })
const availableStudents = computed(() => {
  if (!selectedAssignmentTeam.value) {
    return students.value
  }

  const assignedIds = new Set(selectedAssignmentTeam.value.studentUserIds || [])
  return students.value.filter((student) => !assignedIds.has(student.id))
})

const availableInstructors = computed(() => {
  if (!selectedInstructorAssignmentTeam.value) {
    return instructors.value
  }

  const assignedIds = new Set(selectedInstructorAssignmentTeam.value.instructorUserIds || [])
  return instructors.value.filter((instructor) => !assignedIds.has(instructor.id))
})

function sectionName(sectionId) {
  return sections.value.find((section) => section.id === sectionId)?.name || `Section ${sectionId}`
}

function studentLabel(studentId) {
  const student = students.value.find((item) => item.id === studentId)
  return student ? student.displayName : `Student ${studentId}`
}

function instructorLabel(instructorId) {
  const instructor = instructors.value.find((item) => item.id === instructorId)
  return instructor ? instructor.displayName : `Instructor ${instructorId}`
}

function resetTeamForm() {
  selectedTeamId.value = null
  teamForm.sectionId = ''
  teamForm.name = ''
}

function selectTeam(team) {
  selectedTeamId.value = team.id
  teamForm.sectionId = team.sectionId
  teamForm.name = team.name
}

async function loadTeams() {
  loading.value = true
  error.value = ''
  try {
    const result = await teamsService.findAll(sectionFilter.value)
    teams.value = result.data
    if (selectedAssignmentTeam.value) {
      selectedAssignmentTeam.value = teams.value.find((team) => team.id === selectedAssignmentTeam.value.id) || null
    }
    if (selectedInstructorAssignmentTeam.value) {
      selectedInstructorAssignmentTeam.value =
        teams.value.find((team) => team.id === selectedInstructorAssignmentTeam.value.id) || null
    }
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function loadSections() {
  const result = await sectionsService.findAll()
  sections.value = result.data
}

async function loadStudents() {
  const result = await usersService.findAll('STUDENT')
  students.value = result.data
}

async function loadInstructors() {
  const result = await usersService.findAll('INSTRUCTOR')
  instructors.value = result.data
}

async function loadAll() {
  await Promise.all([loadSections(), loadStudents(), loadInstructors()])
  await loadTeams()
}

async function saveTeam() {
  savingTeam.value = true
  message.value = ''
  error.value = ''
  try {
    const payload = { sectionId: Number(teamForm.sectionId), name: teamForm.name }
    if (selectedTeamId.value) {
      await teamsService.update(selectedTeamId.value, payload)
      message.value = 'Team updated.'
    } else {
      await teamsService.create(payload)
      message.value = 'Team created.'
    }
    resetTeamForm()
    await loadTeams()
  } catch (err) {
    error.value = err.message
  } finally {
    savingTeam.value = false
  }
}

async function deleteTeam(team) {
  error.value = ''
  message.value = ''
  try {
    await teamsService.remove(team.id)
    message.value = 'Team deleted.'
    await loadTeams()
  } catch (err) {
    error.value = err.message
  }
}

async function assignStudent() {
  error.value = ''
  message.value = ''
  if (!selectedAssignmentTeam.value || !assignmentStudentId.value) {
    return
  }
  try {
    const result = await teamsService.assignStudent(selectedAssignmentTeam.value.id, Number(assignmentStudentId.value))
    selectedAssignmentTeam.value = result.data
    assignmentStudentId.value = ''
    message.value = 'Student assigned.'
    await loadTeams()
  } catch (err) {
    error.value = err.message
  }
}

async function removeStudent(studentId) {
  error.value = ''
  message.value = ''
  try {
    const result = await teamsService.removeStudent(selectedAssignmentTeam.value.id, studentId)
    selectedAssignmentTeam.value = result.data
    message.value = 'Student removed.'
    await loadTeams()
  } catch (err) {
    error.value = err.message
  }
}

async function assignInstructor() {
  error.value = ''
  message.value = ''
  if (!selectedInstructorAssignmentTeam.value || !assignmentInstructorId.value) {
    return
  }
  try {
    const result = await teamsService.assignInstructors(selectedInstructorAssignmentTeam.value.id, [
      Number(assignmentInstructorId.value)
    ])
    selectedInstructorAssignmentTeam.value = result.data
    assignmentInstructorId.value = ''
    message.value = 'Instructor assigned.'
    await loadTeams()
  } catch (err) {
    error.value = err.message
  }
}

async function removeInstructor(instructorId) {
  error.value = ''
  message.value = ''
  try {
    const result = await teamsService.removeInstructor(selectedInstructorAssignmentTeam.value.id, instructorId)
    selectedInstructorAssignmentTeam.value = result.data
    message.value = 'Instructor removed.'
    await loadTeams()
  } catch (err) {
    error.value = err.message
  }
}

onMounted(loadAll)
</script>

<style scoped>
.phase-page,
.panel,
.setup-form,
.assignment-form {
  display: grid;
  gap: 1rem;
}

.page-heading,
.panel-heading,
.button-row,
.assigned-list {
  align-items: center;
  display: flex;
  gap: 0.75rem;
}

.page-heading,
.panel-heading,
.team-item {
  justify-content: space-between;
}

.eyebrow,
.empty-state,
.team-item p {
  margin: 0;
}

.layout-grid {
  display: grid;
  gap: 1.25rem;
  grid-template-columns: repeat(2, minmax(300px, 1fr));
}

.panel,
.team-item {
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(208, 218, 230, 0.8);
  border-radius: 26px;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
  padding: 1.4rem;
}

.team-item {
  align-items: center;
  display: flex;
  gap: 1rem;
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.team-item:hover {
  border-color: rgba(94, 122, 255, 0.24);
  box-shadow: 0 22px 54px rgba(15, 23, 42, 0.12);
  transform: translateY(-2px);
}

.team-item + .team-item {
  margin-top: 0.85rem;
}

label {
  display: grid;
  gap: 0.45rem;
}

.assigned-list {
  flex-wrap: wrap;
}

.mb-0 {
  margin-bottom: 0;
}

.student-chip {
  background: linear-gradient(180deg, rgba(245, 248, 255, 0.96), rgba(233, 239, 255, 0.96));
  border: 1px solid rgba(177, 193, 229, 0.82);
  border-radius: 999px;
  color: var(--text-strong);
  cursor: pointer;
  font: inherit;
  padding: 0.45rem 0.8rem;
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.student-chip:hover {
  border-color: rgba(94, 122, 255, 0.26);
  box-shadow: 0 10px 24px rgba(94, 122, 255, 0.16);
  transform: translateY(-1px);
}

@media (max-width: 760px) {
  .layout-grid {
    grid-template-columns: 1fr;
  }

  .panel-heading,
  .team-item {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
