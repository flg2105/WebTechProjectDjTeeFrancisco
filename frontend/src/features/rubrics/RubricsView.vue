<template>
  <section class="page-stack">
    <div class="page-heading">
      <div>
        <p class="eyebrow">UC-1</p>
        <h1>Rubrics</h1>
        <p class="helper">
          Create the scoring guide students will use for weekly peer evaluations.
        </p>
      </div>
      <button class="icon-button" type="button" @click="loadRubrics">
        Refresh
      </button>
    </div>

    <p v-if="successMessage" class="notice success">{{ successMessage }}</p>
    <p v-if="errorMessage" class="notice error">{{ errorMessage }}</p>

    <div class="dashboard-grid rubric-layout">
      <div>
        <form class="surface-card stack-gap-md" @submit.prevent="createRubric">
          <h2>Create Rubric</h2>

          <div>
            <label for="rubric-name">Rubric name</label>
            <input
              id="rubric-name"
              v-model.trim="form.name"
              type="text"
              placeholder="Peer Eval Rubric v1"
              required
            />
          </div>

          <div class="section-heading">
            <h3>Criteria</h3>
            <button class="ghost-button" type="button" @click="addCriterion">
              Add Criterion
            </button>
          </div>

          <div
            v-for="(criterion, index) in form.criteria"
            :key="criterion.localId"
            class="nested-card stack-gap-md"
          >
            <div class="section-heading">
              <p class="card-title">Criterion {{ index + 1 }}</p>
              <button
                class="danger-button"
                type="button"
                :disabled="form.criteria.length === 1"
                @click="removeCriterion(index)"
              >
                Remove
              </button>
            </div>

            <div>
              <label :for="`criterion-name-${criterion.localId}`">Name</label>
              <input
                :id="`criterion-name-${criterion.localId}`"
                v-model.trim="criterion.name"
                type="text"
                placeholder="Quality of work"
                required
              />
            </div>

            <div>
              <label :for="`criterion-description-${criterion.localId}`">
                Description
              </label>
              <textarea
                :id="`criterion-description-${criterion.localId}`"
                v-model.trim="criterion.description"
                rows="3"
                placeholder="How do you rate this teammate?"
                required
              ></textarea>
            </div>

            <div>
              <label :for="`criterion-max-score-${criterion.localId}`">
                Max score
              </label>
              <input
                :id="`criterion-max-score-${criterion.localId}`"
                v-model.number="criterion.maxScore"
                type="number"
                min="0.01"
                step="0.01"
                required
              />
            </div>
          </div>

          <button class="primary-button" type="submit" :disabled="isSaving">
            {{ isSaving ? 'Creating...' : 'Create Rubric' }}
          </button>
        </form>
      </div>

      <div class="surface-card stack-gap-md">
        <div class="section-heading">
          <h2>Available Rubrics</h2>
          <span class="count-pill">{{ rubrics.length }}</span>
        </div>

        <p v-if="isLoading" class="empty-state">Loading rubrics...</p>
        <p v-else-if="rubrics.length === 0" class="empty-state">
          No rubrics have been created yet.
        </p>

        <div v-else class="stack-gap-sm">
          <article v-for="rubric in rubrics" :key="rubric.id" class="nested-card stack-gap-md">
            <div class="section-heading align-start">
              <div>
                <h3>{{ rubric.name }}</h3>
                <p class="helper">
                  {{ rubric.criteria.length }} criteria
                </p>
              </div>
              <button
                class="text-button"
                type="button"
                @click="selectRubric(rubric.id)"
              >
                View Details
              </button>
            </div>

            <ol class="feature-list compact-list">
              <li v-for="criterion in rubric.criteria" :key="criterion.id">
                <span class="criterion-name">{{ criterion.name }}</span>
                <span class="helper">{{ criterion.maxScore }} pts</span>
              </li>
            </ol>
          </article>
        </div>
      </div>
    </div>

    <div v-if="selectedRubric" class="surface-card stack-gap-md">
      <div class="section-heading align-start">
        <div>
          <h2>{{ selectedRubric.name }}</h2>
          <p class="helper">Criteria details for peer evaluation setup.</p>
        </div>
        <button class="text-button" type="button" @click="selectedRubric = null">
          Close
        </button>
      </div>

      <div class="table-wrap">
        <table class="report-table">
          <thead>
            <tr>
              <th>Order</th>
              <th>Criterion</th>
              <th>Description</th>
              <th>Max score</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="criterion in selectedRubric.criteria" :key="criterion.id">
              <td>{{ criterion.position }}</td>
              <td>{{ criterion.name }}</td>
              <td>{{ criterion.description }}</td>
              <td>{{ criterion.maxScore }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { rubricsService } from './rubricsService'

const rubrics = ref([])
const selectedRubric = ref(null)
const isLoading = ref(false)
const isSaving = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
let nextCriterionId = 1

const form = reactive({
  name: '',
  criteria: [newCriterion()]
})

onMounted(loadRubrics)

function newCriterion() {
  const localId = nextCriterionId
  nextCriterionId += 1

  return {
    localId,
    name: '',
    description: '',
    maxScore: 10
  }
}

function addCriterion() {
  form.criteria.push(newCriterion())
}

function removeCriterion(index) {
  if (form.criteria.length > 1) {
    form.criteria.splice(index, 1)
  }
}

async function loadRubrics() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const result = await rubricsService.findAll()
    rubrics.value = result.data || []
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isLoading.value = false
  }
}

async function createRubric() {
  errorMessage.value = ''
  successMessage.value = ''
  isSaving.value = true

  try {
    const payload = {
      name: form.name,
      criteria: form.criteria.map((criterion, index) => ({
        name: criterion.name,
        description: criterion.description,
        maxScore: criterion.maxScore,
        position: index + 1
      }))
    }

    const result = await rubricsService.create(payload)
    successMessage.value = `${result.data.name} created.`
    resetForm()
    await loadRubrics()
    selectedRubric.value = result.data
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isSaving.value = false
  }
}

async function selectRubric(id) {
  errorMessage.value = ''

  try {
    const result = await rubricsService.findById(id)
    selectedRubric.value = result.data
  } catch (error) {
    errorMessage.value = error.message
  }
}

function resetForm() {
  form.name = ''
  form.criteria.splice(0, form.criteria.length, newCriterion())
}
</script>

<style scoped>
.rubric-layout {
  grid-template-columns: minmax(320px, 0.9fr) minmax(0, 1.1fr);
}

.nested-card {
  background: rgba(247, 249, 252, 0.95);
  border: 1px solid rgba(204, 215, 230, 0.82);
  border-radius: 22px;
  padding: 1.1rem;
}

.card-title,
.criterion-name {
  font-weight: 700;
  margin: 0;
}

.compact-list {
  margin: 0;
  padding-left: 1.2rem;
}

@media (max-width: 960px) {
  .rubric-layout {
    grid-template-columns: 1fr;
  }
}
</style>
