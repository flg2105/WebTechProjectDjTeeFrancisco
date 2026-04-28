<template>
  <section class="phase-page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">UC-27</p>
        <h1>Weekly Activity Reports</h1>
        <p class="helper">Select a section, student, and active week to manage WAR activities.</p>
      </div>
      <button class="icon-button" type="button" title="Reload WAR data" @click="loadWar">R</button>
    </div>

    <p v-if="message" class="notice success">{{ message }}</p>
    <p v-if="error" class="notice error">{{ error }}</p>

    <section class="panel">
      <div class="panel-heading">
        <h2>Current WAR</h2>
      </div>

      <div v-if="isLoadingWar" class="empty-state">Loading WAR...</div>
      <div v-else-if="!canLoadWar" class="empty-state">Select context to load WAR.</div>
      <div v-else-if="!warEntry" class="empty-state">No WAR entry available.</div>
      <div v-else-if="warEntry.activities.length === 0" class="empty-state">No activities added yet.</div>
      <div v-else class="stack-gap-sm">
        <article v-for="item in warEntry.activities" :key="item.id" class="list-item">
          <div>
            <strong>{{ item.activity }}</strong>
            <p class="helper">
              {{ statusLabel(item.status) }} | {{ categoryLabel(item.category) }} | Planned {{ item.hoursPlanned }}h |
              Actual {{ item.hoursActual }}h
            </p>
            <p class="mb-0">{{ item.description }}</p>
          </div>
          <div class="button-row">
            <button class="text-button" type="button" @click="editActivity(item)">Edit</button>
            <button class="danger-button" type="button" @click="deleteActivity(item)">Delete</button>
          </div>
        </article>
      </div>
    </section>

    <div class="layout-grid">
      <section class="panel context-panel">
        <div class="panel-heading">
          <h2>Context</h2>
        </div>
        <div class="two-column">
          <label>
            Section
            <select v-model.number="selectedSectionId" @change="onSectionChange">
              <option disabled value="">Select a section</option>
              <option v-for="section in sections" :key="section.id" :value="section.id">
                {{ section.name }} ({{ section.academicYear }})
              </option>
            </select>
          </label>

          <label>
            Student
            <select v-model.number="selectedStudentId" @change="loadWar">
              <option disabled value="">Select a student</option>
              <option v-for="student in students" :key="student.id" :value="student.id">
                {{ student.displayName }} (ID {{ student.id }})
              </option>
            </select>
          </label>
        </div>

        <label>
          Active week (Monday)
          <select v-model.number="selectedActiveWeekId" @change="loadWar">
            <option disabled value="">Select a week</option>
            <option v-for="week in availableWeeks" :key="week.id" :value="week.id">
              {{ week.weekStartDate }}
            </option>
          </select>
        </label>

        <p v-if="isLoadingContext" class="empty-state">Loading sections and students...</p>
      </section>

      <form class="panel activity-panel" @submit.prevent="saveActivity">
        <div class="panel-heading">
          <h2>{{ editingActivityId ? 'Edit Activity' : 'Add Activity' }}</h2>
        </div>
        <p v-if="warEntry" class="helper mb-0">
          Week of {{ warEntry.weekStartDate }} | {{ warEntry.activities.length }} activities
        </p>

        <div v-if="!canLoadWar" class="empty-state">
          Choose a section, student, and week to begin.
        </div>

        <div v-else>
          <label>
            Category
            <select v-model="activityForm.category" required>
              <option v-for="category in categories" :key="category.value" :value="category.value">
                {{ category.label }}
              </option>
            </select>
          </label>

          <label>
            Planned activity
            <input v-model.trim="activityForm.activity" required placeholder="Implement WAR UI" />
          </label>

          <label>
            Description
            <textarea
              v-model.trim="activityForm.description"
              rows="3"
              required
              placeholder="What did you do this week?"
            ></textarea>
          </label>

          <div class="two-column">
            <label>
              Hours planned
              <input v-model.number="activityForm.hoursPlanned" min="0" step="0.25" type="number" required />
            </label>
            <label>
              Hours actual
              <input v-model.number="activityForm.hoursActual" min="0" step="0.25" type="number" required />
            </label>
          </div>

          <label>
            Status
            <select v-model="activityForm.status" required>
              <option v-for="status in statuses" :key="status.value" :value="status.value">
                {{ status.label }}
              </option>
            </select>
          </label>

          <div class="button-row">
            <button class="primary-button" type="submit" :disabled="isSaving || !warEntry">
              {{ isSaving ? 'Saving...' : editingActivityId ? 'Update Activity' : 'Add Activity' }}
            </button>
            <button class="text-button" type="button" :disabled="isSaving" @click="resetActivityForm">
              Clear
            </button>
          </div>
        </div>
      </form>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { authSession } from '../../shared/services/authSession'
import { sectionsService } from '../sections/sectionsService'
import { usersService } from '../users/usersService'
import { warService } from './warService'

const sections = ref([])
const students = ref([])
const selectedSectionId = ref('')
const selectedStudentId = ref('')
const selectedActiveWeekId = ref('')
const warEntry = ref(null)

const isLoadingContext = ref(false)
const isLoadingWar = ref(false)
const isSaving = ref(false)
const message = ref('')
const error = ref('')

const editingActivityId = ref(null)
const activityForm = reactive({
  category: 'DEVELOPMENT',
  activity: '',
  description: '',
  hoursPlanned: 0,
  hoursActual: 0,
  status: 'IN_PROGRESS'
})

const categories = [
  { value: 'DEVELOPMENT', label: 'Development' },
  { value: 'TESTING', label: 'Testing' },
  { value: 'BUGFIX', label: 'Bugfix' },
  { value: 'COMMUNICATION', label: 'Communication' },
  { value: 'DOCUMENTATION', label: 'Documentation' },
  { value: 'DESIGN', label: 'Design' },
  { value: 'PLANNING', label: 'Planning' },
  { value: 'LEARNING', label: 'Learning' },
  { value: 'DEPLOYMENT', label: 'Deployment' },
  { value: 'SUPPORT', label: 'Support' },
  { value: 'MISCELLANEOUS', label: 'Miscellaneous' }
]

const statuses = [
  { value: 'IN_PROGRESS', label: 'In progress' },
  { value: 'UNDER_TESTING', label: 'Under testing' },
  { value: 'DONE', label: 'Done' }
]

const canLoadWar = computed(() => Boolean(selectedStudentId.value && selectedActiveWeekId.value))

const availableWeeks = computed(() => {
  const section = sections.value.find((item) => item.id === selectedSectionId.value)
  const weeks = section?.activeWeeks || []
  const today = new Date()
  return weeks
    .filter((week) => week.active)
    .filter((week) => new Date(`${week.weekStartDate}T00:00:00`) <= today)
})

onMounted(loadContext)

async function loadContext() {
  isLoadingContext.value = true
  error.value = ''
  try {
    const [sectionsResult, studentsResult] = await Promise.all([
      sectionsService.findAll(),
      loadStudentsForRole()
    ])
    sections.value = sectionsResult.data || []
    students.value = studentsResult.data || []

    if (!selectedSectionId.value && sections.value.length > 0) {
      selectedSectionId.value = sections.value[0].id
    }
    if (!selectedStudentId.value && students.value.length > 0) {
      selectedStudentId.value = students.value[0].id
    }

    ensureWeekSelection()
    await loadWar()
  } catch (err) {
    error.value = err.message
  } finally {
    isLoadingContext.value = false
  }
}

async function loadStudentsForRole() {
  if (authSession.currentUser?.role === 'STUDENT') {
    return {
      data: [
        {
          id: authSession.currentUser.userId,
          displayName: authSession.currentUser.displayName,
          email: authSession.currentUser.email
        }
      ]
    }
  }

  return usersService.findStudents()
}

function ensureWeekSelection() {
  if (selectedActiveWeekId.value) {
    const stillValid = availableWeeks.value.some((week) => week.id === selectedActiveWeekId.value)
    if (stillValid) return
  }

  const newestWeek = availableWeeks.value[availableWeeks.value.length - 1]
  selectedActiveWeekId.value = newestWeek?.id || ''
}

async function onSectionChange() {
  ensureWeekSelection()
  await loadWar()
}

async function loadWar() {
  message.value = ''
  error.value = ''

  if (!canLoadWar.value) {
    warEntry.value = null
    return
  }

  isLoadingWar.value = true
  try {
    const result = await warService.findWar(selectedStudentId.value, selectedActiveWeekId.value)
    warEntry.value = result.data
    resetActivityForm()
  } catch (err) {
    warEntry.value = null
    error.value = err.message
  } finally {
    isLoadingWar.value = false
  }
}

function resetActivityForm() {
  editingActivityId.value = null
  activityForm.category = 'DEVELOPMENT'
  activityForm.activity = ''
  activityForm.description = ''
  activityForm.hoursPlanned = 0
  activityForm.hoursActual = 0
  activityForm.status = 'IN_PROGRESS'
}

function editActivity(item) {
  message.value = ''
  error.value = ''
  editingActivityId.value = item.id
  activityForm.category = item.category
  activityForm.activity = item.activity
  activityForm.description = item.description
  activityForm.hoursPlanned = Number(item.hoursPlanned)
  activityForm.hoursActual = Number(item.hoursActual)
  activityForm.status = item.status
}

async function saveActivity() {
  message.value = ''
  error.value = ''

  if (!canLoadWar.value) return

  isSaving.value = true
  try {
    const payload = {
      studentUserId: Number(selectedStudentId.value),
      activeWeekId: Number(selectedActiveWeekId.value),
      category: activityForm.category,
      activity: activityForm.activity,
      description: activityForm.description,
      hoursPlanned: Number(activityForm.hoursPlanned),
      hoursActual: Number(activityForm.hoursActual),
      status: activityForm.status
    }

    if (editingActivityId.value) {
      await warService.updateActivity(editingActivityId.value, payload)
      message.value = 'Activity updated.'
    } else {
      await warService.addActivity(payload)
      message.value = 'Activity added.'
    }

    await loadWar()
  } catch (err) {
    error.value = err.message
  } finally {
    isSaving.value = false
  }
}

async function deleteActivity(item) {
  message.value = ''
  error.value = ''
  if (!canLoadWar.value) return

  if (!window.confirm('Delete this activity?')) {
    return
  }

  try {
    await warService.removeActivity(item.id, selectedStudentId.value, selectedActiveWeekId.value)
    message.value = 'Activity deleted.'
    await loadWar()
  } catch (err) {
    error.value = err.message
  }
}

function categoryLabel(value) {
  return categories.find((item) => item.value === value)?.label || value
}

function statusLabel(value) {
  return statuses.find((item) => item.value === value)?.label || value
}
</script>

<style scoped>
.phase-page,
.panel,
form {
  display: grid;
  gap: 0.75rem;
}

.page-heading,
.panel-heading,
.button-row {
  align-items: center;
  display: flex;
  gap: 0.6rem;
}

.page-heading,
.panel-heading {
  justify-content: space-between;
}

.eyebrow,
.helper,
.empty-state,
.list-item p {
  margin: 0;
}

.layout-grid {
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(2, minmax(300px, 1fr));
}

.panel,
.list-item {
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(208, 218, 230, 0.8);
  border-radius: 26px;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
  padding: 1.15rem;
}

.context-panel {
  align-self: start;
  max-width: 480px;
  padding: 1rem;
}

.context-panel .two-column {
  gap: 0.65rem;
}

.context-panel label {
  gap: 0.35rem;
}

.context-panel select {
  max-width: 100%;
}

.list-item {
  display: flex;
  gap: 1rem;
  justify-content: space-between;
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.list-item:hover {
  border-color: rgba(94, 122, 255, 0.24);
  box-shadow: 0 22px 54px rgba(15, 23, 42, 0.12);
  transform: translateY(-2px);
}

.two-column {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

label {
  display: grid;
  gap: 0.4rem;
}

.mb-0 {
  margin-bottom: 0;
}

@media (max-width: 760px) {
  .layout-grid {
    grid-template-columns: 1fr;
  }

  .page-heading,
  .panel-heading,
  .list-item {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
