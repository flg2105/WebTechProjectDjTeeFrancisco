<template>
  <section class="phase-page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">UC-2 through UC-6, UC-11, UC-18</p>
        <h1>Sections</h1>
      </div>
      <button class="icon-button" type="button" title="Reload sections" @click="loadAll">R</button>
    </div>

    <p v-if="message" class="notice success">{{ message }}</p>
    <p v-if="error" class="notice error">{{ error }}</p>

    <div class="layout-grid">
      <form class="panel" @submit.prevent="saveSection">
        <h2>{{ selectedSectionId ? 'Edit Section' : 'Create Section' }}</h2>
        <label>
          Section name
          <input v-model="sectionForm.name" required placeholder="Senior Design 2026" />
        </label>
        <label>
          Academic year
          <input v-model="sectionForm.academicYear" required placeholder="2026-2027" />
        </label>
        <div class="two-column">
          <label>
            Start date
            <input v-model="sectionForm.startDate" required type="date" />
          </label>
          <label>
            End date
            <input v-model="sectionForm.endDate" required type="date" />
          </label>
        </div>
        <label>
          Rubric
          <select v-model.number="sectionForm.rubricId" required>
            <option disabled value="">Select a rubric</option>
            <option v-for="rubric in rubrics" :key="rubric.id" :value="rubric.id">
              {{ rubric.name }}
            </option>
          </select>
        </label>
        <div class="button-row">
          <button class="primary-button" type="submit" :disabled="savingSection">
            {{ savingSection ? 'Saving...' : 'Save Section' }}
          </button>
          <button class="text-button" type="button" @click="resetSectionForm">New</button>
        </div>
      </form>

      <div class="panel">
        <h2>Find Sections</h2>
        <form class="search-row" @submit.prevent="loadSections">
          <input v-model="searchName" placeholder="Search by section name" />
          <button class="text-button" type="submit">Search</button>
        </form>
        <div v-if="loading" class="empty-state">Loading sections...</div>
        <div v-else-if="sections.length === 0" class="empty-state">No sections found.</div>
        <article v-for="section in sections" v-else :key="section.id" class="list-item">
          <div>
            <strong>{{ section.name }}</strong>
            <p>{{ section.academicYear }} | {{ section.startDate }} to {{ section.endDate }}</p>
            <p>Rubric ID {{ section.rubricId }} | {{ activeCount(section) }} active weeks</p>
          </div>
          <div class="button-row">
            <button class="text-button" type="button" @click="selectSection(section)">Edit</button>
            <button class="text-button" type="button" @click="prepareActiveWeeks(section)">Weeks</button>
          </div>
        </article>
      </div>
    </div>

    <div class="layout-grid">
      <section class="panel">
        <h2>Active Weeks</h2>
        <p class="helper">Week IDs use Monday dates.</p>
        <div v-if="!activeWeeksSection" class="empty-state">Select a section and choose Weeks.</div>
        <form v-else class="weeks-form" @submit.prevent="saveActiveWeeks">
          <strong>{{ activeWeeksSection.name }}</strong>
          <div class="button-row">
            <button class="text-button" type="button" @click="generateWeeks">Generate Mondays</button>
            <button class="text-button" type="button" @click="addWeek">Add Week</button>
          </div>
          <div v-for="(week, index) in weekForm" :key="index" class="week-row">
            <input v-model="week.weekStartDate" required type="date" />
            <label class="checkbox-label">
              <input v-model="week.active" type="checkbox" />
              Active
            </label>
            <button class="danger-link" type="button" @click="weekForm.splice(index, 1)">Remove</button>
          </div>
          <button class="primary-button" type="submit">Save Active Weeks</button>
        </form>
      </section>

      <section class="panel">
        <h2>Invites and Setup</h2>
        <form class="invite-form" @submit.prevent="inviteStudents">
          <h3>Invite Students</h3>
          <label>
            Section
            <select v-model.number="studentInvite.sectionId">
              <option disabled value="">Select a section</option>
              <option v-for="section in sections" :key="section.id" :value="section.id">
                {{ section.name }}
              </option>
            </select>
          </label>
          <label>
            Student emails
            <textarea v-model="studentInvite.emails" rows="3" placeholder="one email per line"></textarea>
          </label>
          <button class="text-button" type="submit">Invite Students</button>
        </form>

        <form class="invite-form" @submit.prevent="inviteInstructors">
          <h3>Invite Instructors</h3>
          <label>
            Instructor emails
            <textarea v-model="instructorInvite.emails" rows="3" placeholder="one email per line"></textarea>
          </label>
          <button class="text-button" type="submit">Invite Instructors</button>
        </form>
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { rubricsService } from '../rubrics/rubricsService'
import { usersService } from '../users/usersService'
import { sectionsService } from './sectionsService'

const sections = ref([])
const rubrics = ref([])
const loading = ref(false)
const savingSection = ref(false)
const message = ref('')
const error = ref('')
const searchName = ref('')
const selectedSectionId = ref(null)
const activeWeeksSection = ref(null)
const weekForm = ref([])
const sectionForm = reactive({
  name: '',
  academicYear: '',
  startDate: '',
  endDate: '',
  rubricId: ''
})
const studentInvite = reactive({ sectionId: '', emails: '' })
const instructorInvite = reactive({ emails: '' })

function parseEmails(value) {
  return value.split(/[\n,]+/).map((email) => email.trim()).filter(Boolean)
}

function activeCount(section) {
  return section.activeWeeks.filter((week) => week.active).length
}

function resetSectionForm() {
  selectedSectionId.value = null
  sectionForm.name = ''
  sectionForm.academicYear = ''
  sectionForm.startDate = ''
  sectionForm.endDate = ''
  sectionForm.rubricId = ''
}

function selectSection(section) {
  selectedSectionId.value = section.id
  sectionForm.name = section.name
  sectionForm.academicYear = section.academicYear
  sectionForm.startDate = section.startDate
  sectionForm.endDate = section.endDate
  sectionForm.rubricId = section.rubricId
}

function prepareActiveWeeks(section) {
  activeWeeksSection.value = section
  weekForm.value = section.activeWeeks.map((week) => ({ ...week }))
}

function nextMonday(dateText) {
  const date = new Date(`${dateText}T00:00:00`)
  const day = date.getDay()
  const offset = day === 1 ? 0 : (8 - day) % 7
  date.setDate(date.getDate() + offset)
  return date
}

function toDateInput(date) {
  return date.toISOString().slice(0, 10)
}

function generateWeeks() {
  if (!activeWeeksSection.value) return
  const cursor = nextMonday(activeWeeksSection.value.startDate)
  const end = new Date(`${activeWeeksSection.value.endDate}T00:00:00`)
  const weeks = []
  while (cursor <= end) {
    weeks.push({ weekStartDate: toDateInput(cursor), active: true })
    cursor.setDate(cursor.getDate() + 7)
  }
  weekForm.value = weeks
}

function addWeek() {
  weekForm.value.push({ weekStartDate: '', active: true })
}

async function loadSections() {
  loading.value = true
  error.value = ''
  try {
    const result = await sectionsService.findAll(searchName.value)
    sections.value = result.data
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}

async function loadRubrics() {
  const result = await rubricsService.findAll()
  rubrics.value = result.data
}

async function loadAll() {
  await Promise.all([loadSections(), loadRubrics()])
}

async function saveSection() {
  savingSection.value = true
  message.value = ''
  error.value = ''
  try {
    const payload = { ...sectionForm, rubricId: Number(sectionForm.rubricId) }
    if (selectedSectionId.value) {
      await sectionsService.update(selectedSectionId.value, payload)
      message.value = 'Section updated.'
    } else {
      await sectionsService.create(payload)
      message.value = 'Section created.'
    }
    resetSectionForm()
    await loadSections()
  } catch (err) {
    error.value = err.message
  } finally {
    savingSection.value = false
  }
}

async function saveActiveWeeks() {
  error.value = ''
  message.value = ''
  try {
    await sectionsService.updateActiveWeeks(activeWeeksSection.value.id, weekForm.value)
    message.value = 'Active weeks saved.'
    await loadSections()
  } catch (err) {
    error.value = err.message
  }
}

async function inviteStudents() {
  error.value = ''
  message.value = ''
  try {
    await usersService.inviteStudents({
      sectionId: Number(studentInvite.sectionId),
      emails: parseEmails(studentInvite.emails)
    })
    message.value = 'Student invitations recorded.'
    studentInvite.emails = ''
  } catch (err) {
    error.value = err.message
  }
}

async function inviteInstructors() {
  error.value = ''
  message.value = ''
  try {
    await usersService.inviteInstructors({ emails: parseEmails(instructorInvite.emails) })
    message.value = 'Instructor invitations recorded.'
    instructorInvite.emails = ''
  } catch (err) {
    error.value = err.message
  }
}

onMounted(loadAll)
</script>

<style scoped>
.phase-page,
.panel,
.weeks-form,
.invite-form {
  display: grid;
  gap: 1rem;
}

.page-heading,
.button-row,
.search-row,
.week-row {
  align-items: center;
  display: flex;
  gap: 0.75rem;
}

.page-heading {
  justify-content: space-between;
}

.eyebrow,
.helper,
.empty-state,
.list-item p {
  color: #57606a;
}

.eyebrow,
.list-item p {
  margin: 0;
}

h1,
h2,
h3 {
  margin: 0;
}

.layout-grid {
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(2, minmax(280px, 1fr));
}

.panel,
.list-item {
  border: 1px solid #d8dee4;
  border-radius: 8px;
  padding: 1rem;
}

.list-item {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
}

.list-item + .list-item {
  margin-top: 0.75rem;
}

.two-column {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

label {
  display: grid;
  gap: 0.35rem;
}

input,
textarea,
select {
  border: 1px solid #afb8c1;
  border-radius: 6px;
  font: inherit;
  padding: 0.55rem 0.65rem;
}

.search-row input,
.week-row input {
  flex: 1;
}

.checkbox-label {
  align-items: center;
  display: flex;
  gap: 0.4rem;
}

.primary-button,
.text-button,
.icon-button {
  border: 1px solid #0969da;
  border-radius: 6px;
  cursor: pointer;
  font: inherit;
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

.danger-link {
  background: transparent;
  border: 0;
  color: #cf222e;
  cursor: pointer;
  padding: 0;
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
  .layout-grid,
  .two-column {
    grid-template-columns: 1fr;
  }

  .list-item,
  .week-row {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
