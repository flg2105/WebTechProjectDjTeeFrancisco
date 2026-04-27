<template>
<<<<<<< feature/uc-27-war-activities
  <section class="phase-page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">UC-27</p>
        <h1>Weekly Activity Report</h1>
        <p class="helper">
          Select a student and active week, then add, edit, or delete weekly activities.
        </p>
      </div>
      <button class="icon-button" type="button" title="Reload WAR data" @click="loadAll">
        R
      </button>
    </div>

    <p v-if="message" class="notice success">{{ message }}</p>
    <p v-if="error" class="notice error">{{ error }}</p>

    <section class="panel">
      <div class="panel-grid">
        <label>
          Student
          <select v-model.number="selectedStudentId">
            <option disabled value="">Select a student</option>
            <option v-for="student in students" :key="student.id" :value="student.id">
              {{ student.displayName }} (ID {{ student.id }})
            </option>
          </select>
        </label>

        <label>
          Active week
          <select v-model.number="selectedWeekId" :disabled="availableWeeks.length === 0">
            <option disabled value="">
              {{ availableWeeks.length ? 'Select an active week' : 'No available weeks' }}
            </option>
            <option v-for="week in availableWeeks" :key="week.id" :value="week.id">
              {{ week.weekStartDate }}
            </option>
          </select>
          <span v-if="assignedSection" class="helper">
            Section: {{ assignedSection.name }} | Team: {{ assignedTeam?.name || 'Unassigned' }}
          </span>
        </label>
      </div>

      <p v-if="isLoadingSetup" class="empty-state">Loading students, teams, and sections...</p>
      <p v-else-if="selectedStudentId && assignedTeams.length > 1" class="empty-state">
        Selected student is assigned to multiple teams. Resolve the team assignment, then return here.
      </p>
      <p v-else-if="selectedStudentId && assignedTeams.length === 0" class="empty-state">
        Selected student is not assigned to a team. Assign the student in Teams, then return here.
      </p>
      <p v-else-if="selectedStudentId && assignedTeam && availableWeeks.length === 0" class="empty-state">
        No active weeks are available for this student's section (future or inactive weeks are hidden).
      </p>
    </section>

    <div class="layout-grid">
      <section class="panel">
        <div class="section-header">
          <div>
            <h2>Activities</h2>
            <p v-if="warEntry" class="helper mb-0">
              Week of {{ warEntry.weekStartDate }} | {{ warEntry.activities.length }} activities
            </p>
          </div>
          <button
            class="text-button"
            type="button"
            :disabled="!canLoadWar || isLoadingWar"
            @click="loadWar"
          >
            {{ isLoadingWar ? 'Loading...' : 'Load' }}
          </button>
        </div>

        <div v-if="!canLoadWar" class="empty-state">
          Choose a student with a team assignment and an active week to load activities.
        </div>
        <div v-else-if="isLoadingWar" class="empty-state">Loading WAR activities...</div>
        <div v-else-if="!warEntry" class="empty-state">
          No WAR data is available for the selected student/week.
        </div>
        <div v-else-if="warEntry.activities.length === 0" class="empty-state">
          No activities yet for this week. Add the first activity using the form.
        </div>
        <div v-else class="table-wrap">
          <table class="activities-table">
            <thead>
              <tr>
                <th>Category</th>
                <th>Activity</th>
                <th>Hours</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="activity in warEntry.activities" :key="activity.id">
                <td>{{ activity.category }}</td>
                <td>
                  <strong>{{ activity.activity }}</strong>
                  <div class="helper">{{ activity.description }}</div>
                </td>
                <td>
                  <div class="hours">
                    <span class="helper">Planned</span>
                    <span>{{ activity.hoursPlanned }}</span>
                  </div>
                  <div class="hours">
                    <span class="helper">Actual</span>
                    <span>{{ activity.hoursActual }}</span>
                  </div>
                </td>
                <td>{{ activity.status }}</td>
                <td class="actions-cell">
                  <button class="text-button" type="button" @click="startEdit(activity)">Edit</button>
                  <button class="danger-button" type="button" @click="removeActivity(activity)">
                    Delete
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <div class="section-header">
          <div>
            <h2>{{ editingActivityId ? 'Edit Activity' : 'Add Activity' }}</h2>
            <p v-if="selectedWeekId" class="helper mb-0">Week ID {{ selectedWeekId }}</p>
          </div>
          <button v-if="editingActivityId" class="text-button" type="button" @click="resetForm">
            Cancel
          </button>
        </div>

        <form class="activity-form" :class="{ muted: !canEditWar }" @submit.prevent="submitActivity">
          <label>
            Category
            <select v-model="form.category" required :disabled="!canEditWar || isSaving">
              <option disabled value="">Select a category</option>
              <option v-for="category in categories" :key="category" :value="category">
                {{ category }}
              </option>
            </select>
          </label>

          <label>
            Activity
            <input
              v-model="form.activity"
              required
              placeholder="Implement login UI"
              :disabled="!canEditWar || isSaving"
            />
          </label>

          <label>
            Description
            <textarea
              v-model="form.description"
              required
              rows="3"
              placeholder="What did you work on and why?"
              :disabled="!canEditWar || isSaving"
            ></textarea>
          </label>

          <div class="two-column">
            <label>
              Planned hours
              <input
                v-model.number="form.hoursPlanned"
                required
                min="0"
                step="0.25"
                type="number"
                :disabled="!canEditWar || isSaving"
              />
            </label>
            <label>
              Actual hours
              <input
                v-model.number="form.hoursActual"
                required
                min="0"
                step="0.25"
                type="number"
                :disabled="!canEditWar || isSaving"
              />
            </label>
          </div>

          <label>
            Status
            <select v-model="form.status" required :disabled="!canEditWar || isSaving">
              <option disabled value="">Select a status</option>
              <option v-for="status in statuses" :key="status" :value="status">
                {{ status }}
              </option>
            </select>
          </label>

          <button class="primary-button" type="submit" :disabled="!canEditWar || isSaving">
            {{
              !canEditWar
                ? 'Select student + week'
                : isSaving
                  ? 'Saving...'
                  : editingActivityId
                    ? 'Save Changes'
                    : 'Add Activity'
            }}
          </button>
        </form>
      </section>
    </div>
  </section>
=======
  <PlaceholderPage
    eyebrow="UC-27"
    title="Weekly Activity Reports"
    description="Weekly activity reporting is staged for the next build, with the dashboard structure already aligned to the rest of the workspace."
    :use-cases="['UC-27: Student manages activities in a Weekly Activity Report']"
    :next-work="[
      'Add activity entry and editing controls',
      'Support category, planned activity, description, hours, and status fields',
      'Connect the polished view to warService without changing the UX patterns'
    ]"
  />
>>>>>>> main
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { sectionsService } from '../sections/sectionsService'
import { teamsService } from '../teams/teamsService'
import { usersService } from '../users/usersService'
import { warService } from './warService'

const students = ref([])
const teams = ref([])
const sections = ref([])

const selectedStudentId = ref('')
const selectedWeekId = ref('')

const warEntry = ref(null)
const isLoadingSetup = ref(false)
const isLoadingWar = ref(false)
const isSaving = ref(false)

const message = ref('')
const error = ref('')

const categories = [
  'DEVELOPMENT',
  'TESTING',
  'BUGFIX',
  'COMMUNICATION',
  'DOCUMENTATION',
  'DESIGN',
  'PLANNING',
  'LEARNING',
  'DEPLOYMENT',
  'SUPPORT',
  'MISCELLANEOUS'
]

const statuses = ['IN_PROGRESS', 'UNDER_TESTING', 'DONE']

const editingActivityId = ref(null)

const form = reactive({
  category: '',
  activity: '',
  description: '',
  hoursPlanned: 0,
  hoursActual: 0,
  status: ''
})

const todayText = computed(() => {
  const now = new Date()
  const yyyy = now.getFullYear()
  const mm = String(now.getMonth() + 1).padStart(2, '0')
  const dd = String(now.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
})

const assignedTeams = computed(() => {
  if (!selectedStudentId.value) return []
  return teams.value.filter((team) => (team.studentUserIds || []).includes(selectedStudentId.value))
})

const assignedTeam = computed(() => {
  if (assignedTeams.value.length !== 1) return null
  return assignedTeams.value[0]
})

const assignedSection = computed(() => {
  if (!assignedTeam.value) return null
  return sections.value.find((section) => section.id === assignedTeam.value.sectionId) || null
})

const availableWeeks = computed(() => {
  const section = assignedSection.value
  if (!section) return []
  const weeks = (section.activeWeeks || []).filter((week) => week.active)
  return weeks
    .filter((week) => week.weekStartDate && week.weekStartDate <= todayText.value)
    .sort((left, right) => right.weekStartDate.localeCompare(left.weekStartDate))
})

const canLoadWar = computed(() => Boolean(selectedStudentId.value && assignedTeam.value && selectedWeekId.value))
const canEditWar = computed(() => canLoadWar.value && warEntry.value)

function resetForm() {
  editingActivityId.value = null
  form.category = ''
  form.activity = ''
  form.description = ''
  form.hoursPlanned = 0
  form.hoursActual = 0
  form.status = ''
}

function startEdit(activity) {
  editingActivityId.value = activity.id
  form.category = activity.category
  form.activity = activity.activity
  form.description = activity.description
  form.hoursPlanned = Number(activity.hoursPlanned)
  form.hoursActual = Number(activity.hoursActual)
  form.status = activity.status
  message.value = ''
  error.value = ''
}

async function loadStudents() {
  const result = await usersService.findAll('STUDENT')
  students.value = result.data || []
}

async function loadTeams() {
  const result = await teamsService.findAll()
  teams.value = result.data || []
}

async function loadSections() {
  const result = await sectionsService.findAll()
  sections.value = result.data || []
}

async function loadAll() {
  isLoadingSetup.value = true
  message.value = ''
  error.value = ''
  try {
    await Promise.all([loadStudents(), loadTeams(), loadSections()])
  } catch (err) {
    error.value = err.message
  } finally {
    isLoadingSetup.value = false
  }
}

function chooseDefaultWeek() {
  if (!assignedTeam.value) {
    selectedWeekId.value = ''
    return
  }
  const weeks = availableWeeks.value
  selectedWeekId.value = weeks.length ? weeks[0].id : ''
}

async function loadWar() {
  if (!canLoadWar.value) {
    warEntry.value = null
    return
  }

  isLoadingWar.value = true
  message.value = ''
  error.value = ''
  resetForm()

  try {
    const result = await warService.findWar(selectedStudentId.value, selectedWeekId.value)
    warEntry.value = result.data
  } catch (err) {
    warEntry.value = null
    error.value = err.message
  } finally {
    isLoadingWar.value = false
  }
}

async function submitActivity() {
  if (!canLoadWar.value) return
  if (!form.category || !form.activity || !form.description || !form.status) return

  const payload = {
    studentUserId: Number(selectedStudentId.value),
    activeWeekId: Number(selectedWeekId.value),
    category: form.category,
    activity: form.activity,
    description: form.description,
    hoursPlanned: Number(form.hoursPlanned),
    hoursActual: Number(form.hoursActual),
    status: form.status
  }

  const confirmText = editingActivityId.value
    ? 'Save changes to this activity?'
    : 'Add this activity to the WAR?'
  if (!window.confirm(confirmText)) return

  isSaving.value = true
  message.value = ''
  error.value = ''
  try {
    const result = editingActivityId.value
      ? await warService.updateActivity(editingActivityId.value, payload)
      : await warService.saveActivity(payload)
    warEntry.value = result.data
    message.value = editingActivityId.value ? 'Activity updated.' : 'Activity added.'
    resetForm()
  } catch (err) {
    error.value = err.message
  } finally {
    isSaving.value = false
  }
}

async function removeActivity(activity) {
  if (!canLoadWar.value) return
  if (!window.confirm('Delete this activity?')) return

  isSaving.value = true
  message.value = ''
  error.value = ''
  try {
    const result = await warService.removeActivity(activity.id, selectedStudentId.value, selectedWeekId.value)
    warEntry.value = result.data
    message.value = 'Activity deleted.'
    if (editingActivityId.value === activity.id) {
      resetForm()
    }
  } catch (err) {
    error.value = err.message
  } finally {
    isSaving.value = false
  }
}

watch(selectedStudentId, async () => {
  message.value = ''
  error.value = ''
  warEntry.value = null
  resetForm()
  chooseDefaultWeek()
})

watch(selectedWeekId, async () => {
  message.value = ''
  error.value = ''
  warEntry.value = null
  resetForm()
  if (selectedWeekId.value) {
    await loadWar()
  }
})

onMounted(async () => {
  await loadAll()
  if (!selectedStudentId.value && students.value.length > 0) {
    selectedStudentId.value = students.value[0].id
  }
})
</script>

<style scoped>
.phase-page,
.panel,
.activity-form {
  display: grid;
  gap: 1rem;
}

.page-heading,
.button-row,
.section-header {
  align-items: center;
  display: flex;
  gap: 0.75rem;
  justify-content: space-between;
}

.eyebrow,
.helper,
.empty-state {
  color: #57606a;
}

.helper {
  display: inline-block;
  font-size: 0.95rem;
}

.mb-0 {
  margin-bottom: 0;
}

.eyebrow {
  margin: 0;
}

h1,
h2 {
  margin: 0;
}

.layout-grid {
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(2, minmax(320px, 1fr));
}

.panel {
  border: 1px solid #d8dee4;
  border-radius: 8px;
  padding: 1rem;
}

.panel-grid {
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(2, minmax(240px, 1fr));
}

label {
  display: grid;
  gap: 0.35rem;
}

input,
select,
textarea {
  border: 1px solid #afb8c1;
  border-radius: 6px;
  font: inherit;
  padding: 0.55rem 0.65rem;
}

textarea {
  resize: vertical;
}

.two-column {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.primary-button,
.text-button,
.icon-button,
.danger-button {
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

.table-wrap {
  overflow-x: auto;
}

.activities-table {
  border-collapse: collapse;
  min-width: 680px;
  width: 100%;
}

.activities-table th,
.activities-table td {
  border-bottom: 1px solid #d8dee4;
  padding: 0.75rem 0.5rem;
  text-align: left;
  vertical-align: top;
}

.actions-cell {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
  white-space: nowrap;
}

.hours {
  display: grid;
  gap: 0.15rem;
  margin-bottom: 0.5rem;
}

.muted {
  opacity: 0.7;
}

@media (max-width: 860px) {
  .layout-grid {
    grid-template-columns: 1fr;
  }

  .panel-grid {
    grid-template-columns: 1fr;
  }

  .two-column {
    grid-template-columns: 1fr;
  }
}
</style>
