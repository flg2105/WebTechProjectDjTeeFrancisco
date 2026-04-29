<template>
  <section class="phase-page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">UC-21 through UC-24</p>
        <h1>Instructor Management</h1>
        <p class="helper">Search instructors, inspect supervised teams, and manage account access.</p>
      </div>
      <button class="icon-button" type="button" title="Reload instructors" @click="loadAll">R</button>
    </div>

    <p v-if="message" class="notice success">{{ message }}</p>
    <p v-if="error" class="notice error">{{ error }}</p>

    <div class="layout-grid">
      <section class="panel">
        <div class="panel-heading">
          <h2>Find Instructors</h2>
        </div>

        <form class="search-row" @submit.prevent="loadInstructors">
          <input v-model.trim="searchQuery" placeholder="Search by name or email" />
          <select v-model="statusFilter">
            <option value="">All statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Deactivated</option>
            <option value="INVITED">Invited</option>
          </select>
          <button class="text-button" type="submit">Search</button>
        </form>

        <div v-if="isLoadingList" class="empty-state">Loading instructors...</div>
        <div v-else-if="filteredInstructors.length === 0" class="empty-state">
          No instructors match the current search.
        </div>
        <article
          v-for="instructor in filteredInstructors"
          v-else
          :key="instructor.id"
          :class="['list-item', { selected: selectedInstructorId === instructor.id }]"
        >
          <button class="list-select" type="button" @click="selectInstructor(instructor.id)">
            <div>
              <strong>{{ instructor.displayName }}</strong>
              <p>{{ instructor.email }}</p>
              <p class="helper">
                {{ instructor.status === 'INACTIVE' ? 'Deactivated' : instructor.status }}
              </p>
            </div>
            <span :class="['status-badge', instructor.status === 'ACTIVE' ? 'success' : 'neutral']">
              {{ instructor.status === 'INACTIVE' ? 'Deactivated' : instructor.status }}
            </span>
          </button>
        </article>
      </section>

      <section class="panel detail-panel">
        <div class="panel-heading">
          <h2>Instructor Details</h2>
        </div>

        <div v-if="isLoadingDetails" class="empty-state">Loading instructor details...</div>
        <div v-else-if="!selectedInstructor" class="empty-state">Choose an instructor to inspect.</div>
        <div v-else class="stack-gap-md">
          <div class="detail-grid">
            <div>
              <p class="detail-label">First name</p>
              <strong>{{ selectedInstructor.firstName || 'Not captured' }}</strong>
            </div>
            <div>
              <p class="detail-label">Last name</p>
              <strong>{{ selectedInstructor.lastName || 'Not captured' }}</strong>
            </div>
            <div>
              <p class="detail-label">Display name</p>
              <strong>{{ selectedInstructor.displayName }}</strong>
            </div>
            <div>
              <p class="detail-label">Status</p>
              <strong>{{ selectedInstructor.status === 'INACTIVE' ? 'Deactivated' : selectedInstructor.status }}</strong>
            </div>
            <div class="detail-span">
              <p class="detail-label">Email</p>
              <strong>{{ selectedInstructor.email }}</strong>
            </div>
          </div>

          <div class="stack-gap-sm">
            <div class="section-heading align-start">
              <div>
                <h3>Supervised Teams</h3>
                <p class="helper mb-0">Teams are grouped by section name in the detail list below.</p>
              </div>
            </div>

            <div v-if="selectedInstructor.supervisedTeams.length === 0" class="empty-state">
              This instructor is not supervising any teams yet.
            </div>
            <div v-else class="table-wrap">
              <table class="report-table">
                <thead>
                  <tr>
                    <th>Section</th>
                    <th>Team</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="team in selectedInstructor.supervisedTeams" :key="team.teamId">
                    <td>{{ team.sectionName }}</td>
                    <td>{{ team.teamName }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div class="status-actions">
            <form
              v-if="selectedInstructor.status === 'ACTIVE'"
              class="stack-gap-sm"
              @submit.prevent="deactivateSelectedInstructor"
            >
              <label>
                Deactivation reason
                <textarea
                  v-model.trim="deactivationReason"
                  rows="3"
                  placeholder="Explain why this instructor should lose access"
                  required
                ></textarea>
              </label>
              <button class="danger-button" type="submit" :disabled="isSavingStatus">
                {{ isSavingStatus ? 'Deactivating...' : 'Deactivate Instructor' }}
              </button>
            </form>

            <div v-else class="stack-gap-sm">
              <p class="helper mb-0">
                Reactivating this instructor restores system access without removing any historical records.
              </p>
              <button class="primary-button" type="button" :disabled="isSavingStatus" @click="reactivateSelectedInstructor">
                {{ isSavingStatus ? 'Reactivating...' : 'Reactivate Instructor' }}
              </button>
            </div>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { usersService } from '../users/usersService'

const instructors = ref([])
const selectedInstructor = ref(null)
const selectedInstructorId = ref(null)
const searchQuery = ref('')
const statusFilter = ref('')
const deactivationReason = ref('')
const isLoadingList = ref(false)
const isLoadingDetails = ref(false)
const isSavingStatus = ref(false)
const message = ref('')
const error = ref('')

const filteredInstructors = computed(() => {
  if (!statusFilter.value) {
    return instructors.value
  }
  return instructors.value.filter((instructor) => instructor.status === statusFilter.value)
})

onMounted(loadAll)

async function loadAll() {
  await loadInstructors()
  if (selectedInstructorId.value) {
    await loadInstructorDetails(selectedInstructorId.value)
  }
}

async function loadInstructors() {
  isLoadingList.value = true
  error.value = ''
  try {
    const result = await usersService.findInstructors(searchQuery.value)
    instructors.value = result.data || []
    if (selectedInstructorId.value && !instructors.value.some((item) => item.id === selectedInstructorId.value)) {
      selectedInstructorId.value = null
      selectedInstructor.value = null
    }
  } catch (err) {
    error.value = err.message
  } finally {
    isLoadingList.value = false
  }
}

async function selectInstructor(instructorId) {
  selectedInstructorId.value = instructorId
  await loadInstructorDetails(instructorId)
}

async function loadInstructorDetails(instructorId) {
  isLoadingDetails.value = true
  error.value = ''
  try {
    const result = await usersService.viewInstructor(instructorId)
    selectedInstructor.value = result.data
    deactivationReason.value = ''
  } catch (err) {
    error.value = err.message
    selectedInstructor.value = null
  } finally {
    isLoadingDetails.value = false
  }
}

async function deactivateSelectedInstructor() {
  if (!selectedInstructor.value) {
    return
  }

  isSavingStatus.value = true
  error.value = ''
  message.value = ''
  try {
    await usersService.deactivateInstructor(selectedInstructor.value.id, deactivationReason.value)
    message.value = 'Instructor deactivated.'
    await refreshSelectedInstructor()
  } catch (err) {
    error.value = err.message
  } finally {
    isSavingStatus.value = false
  }
}

async function reactivateSelectedInstructor() {
  if (!selectedInstructor.value) {
    return
  }

  isSavingStatus.value = true
  error.value = ''
  message.value = ''
  try {
    await usersService.reactivateInstructor(selectedInstructor.value.id)
    message.value = 'Instructor reactivated.'
    await refreshSelectedInstructor()
  } catch (err) {
    error.value = err.message
  } finally {
    isSavingStatus.value = false
  }
}

async function refreshSelectedInstructor() {
  await loadInstructors()
  if (selectedInstructorId.value) {
    await loadInstructorDetails(selectedInstructorId.value)
  }
}
</script>

<style scoped>
.phase-page,
.panel,
.status-actions {
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
  grid-template-columns: minmax(320px, 0.9fr) minmax(360px, 1.1fr);
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
  align-items: center;
  background: transparent;
  border: 0;
  color: inherit;
  cursor: pointer;
  display: flex;
  font: inherit;
  gap: 1rem;
  justify-content: space-between;
  padding: 0;
  text-align: left;
  width: 100%;
}

.detail-grid {
  display: grid;
  gap: 0.9rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.detail-span {
  grid-column: 1 / -1;
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
  .search-row {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
