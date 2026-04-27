<template>
  <section class="phase-page">
    <div class="page-heading">
      <div>
        <p class="eyebrow">UC-28 and UC-29</p>
        <h1>Peer Evaluation</h1>
        <p class="helper">
          Select a student, submit teammate evaluations for the previous active week, and view the
          student-safe weekly report.
        </p>
      </div>
      <button class="icon-button" type="button" title="Reload peer evaluation data" @click="loadAll">
        R
      </button>
    </div>

    <p v-if="message" class="notice success">{{ message }}</p>
    <p v-if="error" class="notice error">{{ error }}</p>

    <section class="panel">
      <label>
        Student
        <select v-model.number="selectedStudentId" @change="loadAll">
          <option disabled value="">Select a student</option>
          <option v-for="student in students" :key="student.id" :value="student.id">
            {{ student.displayName }} (ID {{ student.id }})
          </option>
        </select>
      </label>
      <p v-if="isLoadingStudents" class="empty-state">Loading students...</p>
    </section>

    <div class="layout-grid">
      <section class="panel">
        <div class="section-header">
          <div>
            <h2>Weekly Submission</h2>
            <p v-if="currentForm" class="helper mb-0">
              Week of {{ currentForm.weekStartDate }} | {{ currentForm.teammates.length }} teammates
            </p>
          </div>
          <span v-if="currentForm?.alreadySubmitted" class="status-chip">Submitted</span>
        </div>

        <div v-if="isLoadingForm" class="empty-state">Loading peer evaluation form...</div>
        <div v-else-if="!selectedStudentId" class="empty-state">Choose a student to load the form.</div>
        <div v-else-if="!currentForm" class="empty-state">
          No peer evaluation form is available for this student right now.
        </div>

        <form v-else class="submission-form" @submit.prevent="submitEvaluation">
          <div class="submission-stack" :class="{ compact: evaluationForm.evaluations.length > 2 }">
            <div
              v-for="evaluation in evaluationForm.evaluations"
              :key="evaluation.evaluateeStudentUserId"
              class="evaluation-card"
            >
              <div class="card-header">
                <strong>{{ evaluation.displayName }}</strong>
                <span class="helper">Evaluate teammate</span>
              </div>

              <div class="score-grid">
                <label
                  v-for="criterion in currentForm.criteria"
                  :key="`${evaluation.evaluateeStudentUserId}-${criterion.rubricCriterionId}`"
                >
                  {{ criterion.name }}
                  <span class="helper">{{ criterion.description }}</span>
                  <input
                    v-model.number="evaluation.scoresByCriterion[criterion.rubricCriterionId]"
                    :max="criterion.maxScore"
                    min="0"
                    step="0.01"
                    type="number"
                    required
                  />
                </label>
              </div>

              <label>
                Public comment
                <textarea
                  v-model="evaluation.publicComment"
                  rows="2"
                  placeholder="Optional note the teammate can see"
                ></textarea>
              </label>

              <label>
                Private comment
                <textarea
                  v-model="evaluation.privateComment"
                  rows="2"
                  placeholder="Optional note for the instructor only"
                ></textarea>
              </label>
            </div>
          </div>

          <button class="primary-button" :disabled="isSubmitting || currentForm.alreadySubmitted" type="submit">
            {{
              currentForm.alreadySubmitted
                ? 'Already submitted'
                : isSubmitting
                  ? 'Submitting...'
                  : 'Submit Peer Evaluation'
            }}
          </button>
        </form>
      </section>

      <section class="panel report-panel">
        <div class="section-header">
          <div>
            <h2>Own Report</h2>
            <p class="helper mb-0">This view only shows averaged criterion scores and public comments.</p>
          </div>
        </div>

        <div v-if="isLoadingReport" class="empty-state">Loading report...</div>
        <div v-else-if="!selectedStudentId" class="empty-state">Choose a student to load the report.</div>
        <div v-else-if="!report" class="empty-state">
          No peer evaluation report is available for the selected week yet.
        </div>

        <div v-else class="report-grid">
          <div class="report-summary">
            <p><strong>Student:</strong> {{ report.studentDisplayName }}</p>
            <p><strong>Week:</strong> {{ report.weekStartDate }}</p>
            <p><strong>Received evaluations:</strong> {{ report.receivedEvaluations }}</p>
            <p><strong>Average total score:</strong> {{ report.averageTotalScore }}</p>
          </div>

          <div class="table-wrap">
            <table class="report-table">
              <thead>
                <tr>
                  <th>Criterion</th>
                  <th>Average</th>
                  <th>Max</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="criterion in report.criterionAverages" :key="criterion.rubricCriterionId">
                  <td>
                    <strong>{{ criterion.name }}</strong>
                    <div class="helper">{{ criterion.description }}</div>
                  </td>
                  <td>{{ criterion.averageScore }}</td>
                  <td>{{ criterion.maxScore }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div>
            <h3>Public comments</h3>
            <p v-if="report.publicComments.length === 0" class="empty-state mb-0">
              No public comments were submitted for this week.
            </p>
            <ul v-else class="comment-list">
              <li v-for="comment in report.publicComments" :key="comment">{{ comment }}</li>
            </ul>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { usersService } from '../users/usersService'
import { peerEvalService } from './peerEvalService'

const students = ref([])
const selectedStudentId = ref('')
const currentForm = ref(null)
const report = ref(null)
const message = ref('')
const error = ref('')
const isLoadingStudents = ref(false)
const isLoadingForm = ref(false)
const isLoadingReport = ref(false)
const isSubmitting = ref(false)

const evaluationForm = reactive({
  weekStartDate: '',
  evaluations: []
})

onMounted(async () => {
  await loadStudents()
  if (selectedStudentId.value) {
    await loadAll()
  }
})

async function loadStudents() {
  isLoadingStudents.value = true
  error.value = ''

  try {
    const result = await usersService.findAll('STUDENT')
    students.value = result.data || []
    if (!selectedStudentId.value && students.value.length > 0) {
      selectedStudentId.value = students.value[0].id
    }
  } catch (err) {
    error.value = err.message
  } finally {
    isLoadingStudents.value = false
  }
}

async function loadAll() {
  message.value = ''
  error.value = ''

  if (!selectedStudentId.value) {
    currentForm.value = null
    report.value = null
    return
  }

  await Promise.all([loadCurrentForm(), loadReport()])
}

async function loadCurrentForm() {
  isLoadingForm.value = true

  try {
    const result = await peerEvalService.findCurrent(selectedStudentId.value)
    currentForm.value = result.data
    buildEvaluationForm()
  } catch (err) {
    currentForm.value = null
    evaluationForm.weekStartDate = ''
    evaluationForm.evaluations = []
    error.value = err.message
  } finally {
    isLoadingForm.value = false
  }
}

async function loadReport() {
  isLoadingReport.value = true

  try {
    const weekStartDate = currentForm.value?.weekStartDate
    const result = await peerEvalService.findOwnReport(selectedStudentId.value, weekStartDate)
    report.value = result.data
  } catch (err) {
    report.value = null
    if (!String(err.message || '').includes('No peer evaluation report available')) {
      error.value = err.message
    }
  } finally {
    isLoadingReport.value = false
  }
}

function buildEvaluationForm() {
  if (!currentForm.value) return

  evaluationForm.weekStartDate = currentForm.value.weekStartDate
  evaluationForm.evaluations = currentForm.value.teammates.map((teammate) => ({
    evaluateeStudentUserId: teammate.studentUserId,
    displayName: teammate.displayName,
    publicComment: '',
    privateComment: '',
    scoresByCriterion: Object.fromEntries(
      currentForm.value.criteria.map((criterion) => [criterion.rubricCriterionId, criterion.maxScore])
    )
  }))
}

async function submitEvaluation() {
  if (!currentForm.value) return

  isSubmitting.value = true
  message.value = ''
  error.value = ''

  try {
    const payload = {
      evaluatorStudentUserId: currentForm.value.evaluatorStudentUserId,
      weekStartDate: evaluationForm.weekStartDate,
      evaluations: evaluationForm.evaluations.map((evaluation) => ({
        evaluateeStudentUserId: evaluation.evaluateeStudentUserId,
        publicComment: evaluation.publicComment,
        privateComment: evaluation.privateComment,
        scores: currentForm.value.criteria.map((criterion) => ({
          rubricCriterionId: criterion.rubricCriterionId,
          score: Number(evaluation.scoresByCriterion[criterion.rubricCriterionId])
        }))
      }))
    }

    await peerEvalService.submit(payload)
    message.value = 'Peer evaluation submitted.'
    await loadAll()
  } catch (err) {
    error.value = err.message
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.phase-page,
.panel,
.submission-form,
.report-grid {
  display: grid;
  gap: 1rem;
}

.page-heading,
.section-header,
.card-header {
  align-items: start;
  display: flex;
  gap: 0.75rem;
  justify-content: space-between;
}

.eyebrow,
.helper,
.empty-state,
.report-summary p,
h1,
h2,
h3 {
  margin: 0;
}

.layout-grid {
  display: grid;
  align-items: start;
  gap: 1.25rem;
  grid-template-columns: repeat(2, minmax(320px, 1fr));
}

.panel,
.evaluation-card {
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(208, 218, 230, 0.8);
  border-radius: 26px;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
  padding: 1.4rem;
}

.evaluation-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(247, 249, 252, 0.96));
  transition: transform 180ms ease, box-shadow 180ms ease, border-color 180ms ease;
}

.evaluation-card:hover {
  border-color: rgba(94, 122, 255, 0.24);
  box-shadow: 0 22px 54px rgba(15, 23, 42, 0.12);
  transform: translateY(-2px);
}

.score-grid {
  display: grid;
  gap: 0.85rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.submission-stack {
  display: grid;
  gap: 1rem;
}

.submission-stack.compact {
  max-height: 52rem;
  overflow-y: auto;
  padding-right: 0.25rem;
}

label {
  display: grid;
  gap: 0.45rem;
}

.status-chip {
  background: rgba(116, 185, 139, 0.18);
  border: 1px solid rgba(116, 185, 139, 0.28);
  border-radius: 999px;
  color: #23613c;
  font-size: 0.875rem;
  padding: 0.35rem 0.8rem;
}

.report-summary {
  display: grid;
  gap: 0.45rem;
}

.table-wrap {
  overflow-x: auto;
}

.report-table {
  width: 100%;
}

.comment-list {
  margin: 0;
  padding-left: 1.25rem;
}

.report-panel {
  align-self: start;
}

.mb-0 {
  margin-bottom: 0;
}

@media (max-width: 900px) {
  .layout-grid,
  .score-grid {
    grid-template-columns: 1fr;
  }
}
</style>
