<template>
  <section class="phase-page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">UC-31 through UC-34</p>
        <h1>Reports</h1>
        <p class="helper">Generate instructor reports for WAR and peer evaluations.</p>
      </div>
      <button class="icon-button" type="button" title="Reload report context" @click="loadContext">R</button>
    </div>

    <p v-if="message" class="notice success">{{ message }}</p>
    <p v-if="error" class="notice error">{{ error }}</p>

    <div class="layout-grid">
      <section class="panel">
        <div class="panel-heading">
          <h2>WAR Team Report (UC-32)</h2>
        </div>

        <div class="two-column">
          <label>
            Section
            <select v-model.number="warTeamForm.sectionId" @change="onWarTeamSectionChange">
              <option disabled value="">Select a section</option>
              <option v-for="section in sections" :key="section.id" :value="section.id">
                {{ section.name }}
              </option>
            </select>
          </label>

          <label>
            Team
            <select v-model.number="warTeamForm.teamId">
              <option disabled value="">Select a team</option>
              <option v-for="team in teamsForSelectedSection" :key="team.id" :value="team.id">
                {{ team.name }}
              </option>
            </select>
          </label>
        </div>

        <label>
          Active week (Monday)
          <select v-model.number="warTeamForm.activeWeekId">
            <option disabled value="">Select a week</option>
            <option v-for="week in activeWeeksForSelectedSection" :key="week.id" :value="week.id">
              {{ week.weekStartDate }}
            </option>
          </select>
        </label>

        <button class="primary-button" type="button" :disabled="loadingWarTeamReport" @click="loadWarTeamReport">
          {{ loadingWarTeamReport ? 'Loading...' : 'Generate team WAR report' }}
        </button>

        <div v-if="warTeamReport" class="report-stack">
          <p class="helper mb-0">
            Team {{ warTeamReport.teamName }} | Week of {{ warTeamReport.weekStartDate }} |
            Missing: {{ warTeamReport.missingSubmissions.length }}
          </p>

          <div class="table-wrap">
            <table class="report-table">
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Submitted</th>
                  <th>Activities</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="member in warTeamReport.memberReports" :key="member.studentUserId">
                  <td>{{ member.studentDisplayName }}</td>
                  <td>{{ member.submitted ? 'Yes' : 'No' }}</td>
                  <td>{{ member.entry?.activities?.length || 0 }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-heading">
          <h2>WAR Student Report (UC-34)</h2>
        </div>

        <div class="two-column">
          <label>
            Section
            <select v-model.number="warStudentForm.sectionId" @change="onWarStudentSectionChange">
              <option disabled value="">Select a section</option>
              <option v-for="section in sections" :key="section.id" :value="section.id">
                {{ section.name }}
              </option>
            </select>
          </label>

          <label>
            Student
            <select v-model.number="warStudentForm.studentUserId">
              <option disabled value="">Select a student</option>
              <option v-for="student in students" :key="student.id" :value="student.id">
                {{ student.displayName }}
              </option>
            </select>
          </label>
        </div>

        <div class="two-column">
          <label>
            Start week
            <select v-model.number="warStudentForm.startActiveWeekId">
              <option disabled value="">Start week</option>
              <option v-for="week in activeWeeksForWarStudentSection" :key="week.id" :value="week.id">
                {{ week.weekStartDate }}
              </option>
            </select>
          </label>
          <label>
            End week
            <select v-model.number="warStudentForm.endActiveWeekId">
              <option disabled value="">End week</option>
              <option v-for="week in activeWeeksForWarStudentSection" :key="week.id" :value="week.id">
                {{ week.weekStartDate }}
              </option>
            </select>
          </label>
        </div>

        <button class="primary-button" type="button" :disabled="loadingWarStudentReport" @click="loadWarStudentReport">
          {{ loadingWarStudentReport ? 'Loading...' : 'Generate student WAR report' }}
        </button>

        <div v-if="warStudentReport" class="report-stack">
          <p class="helper mb-0">
            Weeks {{ warStudentReport.startWeekStartDate }} → {{ warStudentReport.endWeekStartDate }} |
            Entries: {{ warStudentReport.entries.length }}
          </p>

          <article v-for="entry in warStudentReport.entries" :key="entry.activeWeekId" class="list-item">
            <div>
              <strong>{{ entry.weekStartDate }}</strong>
              <p class="helper mb-0">{{ entry.activities.length }} activities</p>
            </div>
          </article>
        </div>
      </section>
    </div>

    <div class="layout-grid">
      <section class="panel">
        <div class="panel-heading">
          <h2>Peer Eval Section Report (UC-31)</h2>
        </div>

        <div class="two-column">
          <label>
            Section
            <select v-model.number="peerSectionForm.sectionId" @change="onPeerSectionChange">
              <option disabled value="">Select a section</option>
              <option v-for="section in sections" :key="section.id" :value="section.id">
                {{ section.name }}
              </option>
            </select>
          </label>

          <label>
            Week (Monday)
            <select v-model="peerSectionForm.weekStartDate">
              <option disabled value="">Select a week</option>
              <option v-for="week in activeWeeksForPeerSection" :key="week.id" :value="week.weekStartDate">
                {{ week.weekStartDate }}
              </option>
            </select>
          </label>
        </div>

        <button class="primary-button" type="button" :disabled="loadingPeerSectionReport" @click="loadPeerSectionReport">
          {{ loadingPeerSectionReport ? 'Loading...' : 'Generate section peer eval report' }}
        </button>

        <div v-if="peerSectionReport" class="report-stack">
          <p class="helper mb-0">
            Week of {{ peerSectionReport.weekStartDate }} | Missing: {{ peerSectionReport.missingSubmitters.length }}
          </p>

          <div class="table-wrap">
            <table class="report-table">
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Team</th>
                  <th>Avg total</th>
                  <th>Received</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in peerSectionReport.students" :key="row.studentUserId">
                  <td>{{ row.studentDisplayName }}</td>
                  <td>{{ row.teamName }}</td>
                  <td>{{ row.averageTotalScore }}</td>
                  <td>{{ row.receivedEvaluations }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section class="panel">
        <div class="panel-heading">
          <h2>Peer Eval Student Report (UC-33)</h2>
        </div>

        <div class="two-column">
          <label>
            Section
            <select v-model.number="peerStudentForm.sectionId" @change="onPeerStudentSectionChange">
              <option disabled value="">Select a section</option>
              <option v-for="section in sections" :key="section.id" :value="section.id">
                {{ section.name }}
              </option>
            </select>
          </label>

          <label>
            Student
            <select v-model.number="peerStudentForm.studentUserId">
              <option disabled value="">Select a student</option>
              <option v-for="student in students" :key="student.id" :value="student.id">
                {{ student.displayName }}
              </option>
            </select>
          </label>
        </div>

        <div class="two-column">
          <label>
            Start week
            <select v-model.number="peerStudentForm.startActiveWeekId">
              <option disabled value="">Start week</option>
              <option v-for="week in activeWeeksForPeerStudentSection" :key="week.id" :value="week.id">
                {{ week.weekStartDate }}
              </option>
            </select>
          </label>
          <label>
            End week
            <select v-model.number="peerStudentForm.endActiveWeekId">
              <option disabled value="">End week</option>
              <option v-for="week in activeWeeksForPeerStudentSection" :key="week.id" :value="week.id">
                {{ week.weekStartDate }}
              </option>
            </select>
          </label>
        </div>

        <button class="primary-button" type="button" :disabled="loadingPeerStudentReport" @click="loadPeerStudentReport">
          {{ loadingPeerStudentReport ? 'Loading...' : 'Generate student peer eval report' }}
        </button>

        <div v-if="peerStudentReport" class="report-stack">
          <p class="helper mb-0">
            {{ peerStudentReport.studentDisplayName }} | Weeks {{ peerStudentReport.startWeekStartDate }} → {{ peerStudentReport.endWeekStartDate }}
          </p>
          <div class="table-wrap">
            <table class="report-table">
              <thead>
                <tr>
                  <th>Week</th>
                  <th>Avg total</th>
                  <th>Received</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="week in peerStudentReport.weeks" :key="week.weekStartDate">
                  <td>{{ week.weekStartDate }}</td>
                  <td>{{ week.averageTotalScore }}</td>
                  <td>{{ week.receivedEvaluations }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { sectionsService } from '../sections/sectionsService'
import { teamsService } from '../teams/teamsService'
import { usersService } from '../users/usersService'
import { peerEvalReportsService } from './peerEvalReportsService'
import { warReportsService } from './warReportsService'

const message = ref('')
const error = ref('')

const sections = ref([])
const teams = ref([])
const students = ref([])

const loadingWarTeamReport = ref(false)
const loadingWarStudentReport = ref(false)
const loadingPeerSectionReport = ref(false)
const loadingPeerStudentReport = ref(false)

const warTeamReport = ref(null)
const warStudentReport = ref(null)
const peerSectionReport = ref(null)
const peerStudentReport = ref(null)

const warTeamForm = reactive({ sectionId: '', teamId: '', activeWeekId: '' })
const warStudentForm = reactive({ sectionId: '', studentUserId: '', startActiveWeekId: '', endActiveWeekId: '' })
const peerSectionForm = reactive({ sectionId: '', weekStartDate: '' })
const peerStudentForm = reactive({ sectionId: '', studentUserId: '', startActiveWeekId: '', endActiveWeekId: '' })

const selectedSection = computed(() => sections.value.find((s) => s.id === Number(warTeamForm.sectionId)) || null)
const teamsForSelectedSection = computed(() => teams.value.filter((t) => t.sectionId === Number(warTeamForm.sectionId)))
const activeWeeksForSelectedSection = computed(() => (selectedSection.value?.activeWeeks || []).filter((w) => w.active))

const warStudentSection = computed(() => sections.value.find((s) => s.id === Number(warStudentForm.sectionId)) || null)
const activeWeeksForWarStudentSection = computed(() => (warStudentSection.value?.activeWeeks || []).filter((w) => w.active))

const peerSection = computed(() => sections.value.find((s) => s.id === Number(peerSectionForm.sectionId)) || null)
const activeWeeksForPeerSection = computed(() => (peerSection.value?.activeWeeks || []).filter((w) => w.active))

const peerStudentSection = computed(() => sections.value.find((s) => s.id === Number(peerStudentForm.sectionId)) || null)
const activeWeeksForPeerStudentSection = computed(() => (peerStudentSection.value?.activeWeeks || []).filter((w) => w.active))

onMounted(loadContext)

async function loadContext() {
  error.value = ''
  message.value = ''
  try {
    const [sectionsResult, teamsResult, studentsResult] = await Promise.all([
      sectionsService.findAll(),
      teamsService.findAll(),
      usersService.findAll('STUDENT')
    ])
    sections.value = sectionsResult.data || []
    teams.value = teamsResult.data || []
    students.value = studentsResult.data || []
  } catch (err) {
    error.value = err.message
  }
}

function onWarTeamSectionChange() {
  warTeamForm.teamId = ''
  warTeamForm.activeWeekId = ''
}

function onWarStudentSectionChange() {
  warStudentForm.startActiveWeekId = ''
  warStudentForm.endActiveWeekId = ''
}

function onPeerSectionChange() {
  peerSectionForm.weekStartDate = ''
}

function onPeerStudentSectionChange() {
  peerStudentForm.startActiveWeekId = ''
  peerStudentForm.endActiveWeekId = ''
}

async function loadWarTeamReport() {
  error.value = ''
  message.value = ''
  warTeamReport.value = null
  if (!warTeamForm.teamId || !warTeamForm.activeWeekId) return

  loadingWarTeamReport.value = true
  try {
    const result = await warReportsService.findTeamReport(warTeamForm.teamId, warTeamForm.activeWeekId)
    warTeamReport.value = result.data
  } catch (err) {
    error.value = err.message
  } finally {
    loadingWarTeamReport.value = false
  }
}

async function loadWarStudentReport() {
  error.value = ''
  message.value = ''
  warStudentReport.value = null
  if (!warStudentForm.studentUserId || !warStudentForm.startActiveWeekId || !warStudentForm.endActiveWeekId) return

  loadingWarStudentReport.value = true
  try {
    const result = await warReportsService.findStudentReport(
      warStudentForm.studentUserId,
      warStudentForm.startActiveWeekId,
      warStudentForm.endActiveWeekId
    )
    warStudentReport.value = result.data
  } catch (err) {
    error.value = err.message
  } finally {
    loadingWarStudentReport.value = false
  }
}

async function loadPeerSectionReport() {
  error.value = ''
  message.value = ''
  peerSectionReport.value = null
  if (!peerSectionForm.sectionId || !peerSectionForm.weekStartDate) return

  loadingPeerSectionReport.value = true
  try {
    const result = await peerEvalReportsService.findSectionReport(peerSectionForm.sectionId, peerSectionForm.weekStartDate)
    peerSectionReport.value = result.data
  } catch (err) {
    error.value = err.message
  } finally {
    loadingPeerSectionReport.value = false
  }
}

async function loadPeerStudentReport() {
  error.value = ''
  message.value = ''
  peerStudentReport.value = null
  if (!peerStudentForm.studentUserId || !peerStudentForm.startActiveWeekId || !peerStudentForm.endActiveWeekId) return

  loadingPeerStudentReport.value = true
  try {
    const result = await peerEvalReportsService.findStudentReport(
      peerStudentForm.studentUserId,
      peerStudentForm.startActiveWeekId,
      peerStudentForm.endActiveWeekId
    )
    peerStudentReport.value = result.data
  } catch (err) {
    error.value = err.message
  } finally {
    loadingPeerStudentReport.value = false
  }
}
</script>

<style scoped>
.phase-page,
.panel,
.report-stack {
  display: grid;
  gap: 1rem;
}

.page-heading,
.panel-heading,
.two-column {
  align-items: center;
  display: flex;
  gap: 0.75rem;
  justify-content: space-between;
}

.layout-grid {
  display: grid;
  gap: 1.25rem;
  grid-template-columns: repeat(2, minmax(320px, 1fr));
}

.panel {
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(208, 218, 230, 0.8);
  border-radius: 26px;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
  padding: 1.4rem;
}

label {
  display: grid;
  gap: 0.45rem;
  width: 100%;
}

.table-wrap {
  overflow-x: auto;
}

.report-table {
  border-collapse: collapse;
  min-width: 520px;
  width: 100%;
}

.report-table th,
.report-table td {
  border-bottom: 1px solid rgba(208, 218, 230, 0.8);
  padding: 0.65rem;
  text-align: left;
  vertical-align: top;
}

.list-item {
  border: 1px solid rgba(208, 218, 230, 0.8);
  border-radius: 18px;
  display: flex;
  justify-content: space-between;
  padding: 0.9rem;
}

.mb-0 {
  margin-bottom: 0;
}

@media (max-width: 840px) {
  .layout-grid {
    grid-template-columns: 1fr;
  }

  .two-column {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>

