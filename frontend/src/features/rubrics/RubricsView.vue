<template>
  <section>
    <div class="d-flex flex-column flex-lg-row justify-content-between gap-3 mb-4">
      <div>
        <p class="text-uppercase text-secondary small mb-1">UC-1</p>
        <h1 class="h3 mb-2">Rubrics</h1>
        <p class="text-secondary mb-0">
          Create the scoring guide students will use for weekly peer evaluations.
        </p>
      </div>
      <button class="btn btn-outline-secondary align-self-start" type="button" @click="loadRubrics">
        Refresh
      </button>
    </div>

    <div v-if="successMessage" class="alert alert-success" role="status">
      {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="alert alert-danger" role="alert">
      {{ errorMessage }}
    </div>

    <div class="row g-4">
      <div class="col-12 col-xl-5">
        <form class="border rounded p-3" @submit.prevent="createRubric">
          <h2 class="h5 mb-3">Create Rubric</h2>

          <div class="mb-3">
            <label class="form-label" for="rubric-name">Rubric name</label>
            <input
              id="rubric-name"
              v-model.trim="form.name"
              class="form-control"
              type="text"
              placeholder="Peer Eval Rubric v1"
              required
            />
          </div>

          <div class="d-flex justify-content-between align-items-center mb-2">
            <h3 class="h6 mb-0">Criteria</h3>
            <button class="btn btn-sm btn-outline-primary" type="button" @click="addCriterion">
              Add Criterion
            </button>
          </div>

          <div
            v-for="(criterion, index) in form.criteria"
            :key="criterion.localId"
            class="border rounded p-3 mb-3"
          >
            <div class="d-flex justify-content-between align-items-center gap-3 mb-3">
              <p class="fw-semibold mb-0">Criterion {{ index + 1 }}</p>
              <button
                class="btn btn-sm btn-outline-danger"
                type="button"
                :disabled="form.criteria.length === 1"
                @click="removeCriterion(index)"
              >
                Remove
              </button>
            </div>

            <div class="mb-3">
              <label class="form-label" :for="`criterion-name-${criterion.localId}`">Name</label>
              <input
                :id="`criterion-name-${criterion.localId}`"
                v-model.trim="criterion.name"
                class="form-control"
                type="text"
                placeholder="Quality of work"
                required
              />
            </div>

            <div class="mb-3">
              <label class="form-label" :for="`criterion-description-${criterion.localId}`">
                Description
              </label>
              <textarea
                :id="`criterion-description-${criterion.localId}`"
                v-model.trim="criterion.description"
                class="form-control"
                rows="3"
                placeholder="How do you rate this teammate?"
                required
              ></textarea>
            </div>

            <div>
              <label class="form-label" :for="`criterion-max-score-${criterion.localId}`">
                Max score
              </label>
              <input
                :id="`criterion-max-score-${criterion.localId}`"
                v-model.number="criterion.maxScore"
                class="form-control"
                type="number"
                min="0.01"
                step="0.01"
                required
              />
            </div>
          </div>

          <button class="btn btn-primary" type="submit" :disabled="isSaving">
            {{ isSaving ? 'Creating...' : 'Create Rubric' }}
          </button>
        </form>
      </div>

      <div class="col-12 col-xl-7">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h2 class="h5 mb-0">Available Rubrics</h2>
          <span class="badge text-bg-secondary">{{ rubrics.length }}</span>
        </div>

        <p v-if="isLoading" class="text-secondary">Loading rubrics...</p>
        <p v-else-if="rubrics.length === 0" class="text-secondary">
          No rubrics have been created yet.
        </p>

        <div v-else class="d-grid gap-3">
          <article v-for="rubric in rubrics" :key="rubric.id" class="border rounded p-3">
            <div class="d-flex flex-column flex-md-row justify-content-between gap-2 mb-3">
              <div>
                <h3 class="h5 mb-1">{{ rubric.name }}</h3>
                <p class="text-secondary small mb-0">
                  {{ rubric.criteria.length }} criteria
                </p>
              </div>
              <button
                class="btn btn-sm btn-outline-secondary align-self-start"
                type="button"
                @click="selectRubric(rubric.id)"
              >
                View Details
              </button>
            </div>

            <ol class="mb-0">
              <li v-for="criterion in rubric.criteria" :key="criterion.id" class="mb-2">
                <span class="fw-semibold">{{ criterion.name }}</span>
                <span class="text-secondary"> - {{ criterion.maxScore }} pts</span>
              </li>
            </ol>
          </article>
        </div>
      </div>
    </div>

    <div v-if="selectedRubric" class="border rounded p-3 mt-4">
      <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
        <div>
          <h2 class="h5 mb-1">{{ selectedRubric.name }}</h2>
          <p class="text-secondary mb-0">Criteria details for peer evaluation setup.</p>
        </div>
        <button class="btn btn-sm btn-outline-secondary" type="button" @click="selectedRubric = null">
          Close
        </button>
      </div>

      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead>
            <tr>
              <th scope="col">Order</th>
              <th scope="col">Criterion</th>
              <th scope="col">Description</th>
              <th scope="col">Max score</th>
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
