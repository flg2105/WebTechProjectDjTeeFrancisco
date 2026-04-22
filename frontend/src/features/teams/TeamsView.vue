<template>
  <section class="phase-page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">UC-7 through UC-14</p>
        <h1>Teams</h1>
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

      <section class="panel">
        <h2>Student Setup</h2>
        <form class="setup-form" @submit.prevent="setupStudent">
          <label>
            Display name
            <input v-model="studentForm.displayName" required placeholder="Jane Student" />
          </label>
          <label>
            Email
            <input v-model="studentForm.email" required type="email" placeholder="student@tcu.edu" />
          </label>
          <button class="text-button" type="submit">Create Student Account</button>
        </form>
      </section>
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
          <p>{{ sectionName(team.sectionId) }} | {{ team.studentUserIds.length }} students</p>
          <p v-if="team.studentUserIds.length">Student IDs: {{ team.studentUserIds.join(', ') }}</p>
        </div>
        <div class="button-row">
          <button class="text-button" type="button" @click="selectTeam(team)">Edit</button>
          <button class="text-button" type="button" @click="selectedAssignmentTeam = team">Assign</button>
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
            <option v-for="student in students" :key="student.id" :value="student.id">
              {{ student.displayName }} ({{ student.email }})
            </option>
          </select>
        </label>
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
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { sectionsService } from '../sections/sectionsService'
import { usersService } from '../users/usersService'
import { teamsService } from './teamsService'

const teams = ref([])
const sections = ref([])
const students = ref([])
const loading = ref(false)
const savingTeam = ref(false)
const message = ref('')
const error = ref('')
const selectedTeamId = ref(null)
const selectedAssignmentTeam = ref(null)
const sectionFilter = ref('')
const assignmentStudentId = ref('')
const teamForm = reactive({ sectionId: '', name: '' })
const studentForm = reactive({ displayName: '', email: '' })

function sectionName(sectionId) {
  return sections.value.find((section) => section.id === sectionId)?.name || `Section ${sectionId}`
}

function studentLabel(studentId) {
  const student = students.value.find((item) => item.id === studentId)
  return student ? student.displayName : `Student ${studentId}`
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

async function loadAll() {
  await Promise.all([loadSections(), loadStudents()])
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

async function setupStudent() {
  error.value = ''
  message.value = ''
  try {
    await usersService.setupStudent({ ...studentForm })
    message.value = 'Student account created.'
    studentForm.displayName = ''
    studentForm.email = ''
    await loadStudents()
  } catch (err) {
    error.value = err.message
  }
}

async function assignStudent() {
  error.value = ''
  message.value = ''
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
  color: #57606a;
}

.eyebrow,
.team-item p {
  margin: 0;
}

h1,
h2 {
  margin: 0;
}

.layout-grid {
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(2, minmax(280px, 1fr));
}

.panel,
.team-item {
  border: 1px solid #d8dee4;
  border-radius: 8px;
  padding: 1rem;
}

.team-item {
  align-items: center;
  display: flex;
  gap: 1rem;
}

.team-item + .team-item {
  margin-top: 0.75rem;
}

label {
  display: grid;
  gap: 0.35rem;
}

input,
select {
  border: 1px solid #afb8c1;
  border-radius: 6px;
  font: inherit;
  padding: 0.55rem 0.65rem;
}

.primary-button,
.text-button,
.icon-button,
.danger-button,
.student-chip {
  border-radius: 6px;
  cursor: pointer;
  font: inherit;
}

.primary-button,
.text-button,
.icon-button {
  border: 1px solid #0969da;
}

.primary-button {
  background: #0969da;
  color: white;
  padding: 0.65rem 0.85rem;
}

.text-button,
.icon-button {
  background: white;
  color: #0969da;
  padding: 0.45rem 0.65rem;
}

.icon-button {
  aspect-ratio: 1;
  width: 2.5rem;
}

.danger-button {
  background: white;
  border: 1px solid #cf222e;
  color: #cf222e;
  padding: 0.45rem 0.65rem;
}

.assigned-list {
  flex-wrap: wrap;
}

.student-chip {
  background: #f6f8fa;
  border: 1px solid #afb8c1;
  padding: 0.35rem 0.55rem;
}

.notice {
  border-radius: 6px;
  margin: 0;
  padding: 0.7rem 0.85rem;
}

.success {
  background: #dafbe1;
  color: #116329;
}

.error {
  background: #ffebe9;
  color: #82071e;
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
