<template>
  <div class="p-4 bg-light border rounded">
    <h1 class="h3 mb-2">Project Pulse</h1>
    <p class="mb-3">
      Starter UI scaffold. Each page is a placeholder so feature branches can build in parallel.
    </p>
    <button class="btn btn-primary" @click="checkHealth">Check Backend Health</button>
    <div v-if="health" class="mt-3">
      <pre class="mb-0">{{ health }}</pre>
    </div>
    <p v-if="errorMessage" class="text-danger mt-3 mb-0">{{ errorMessage }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { apiClient } from '../../shared/services/apiClient'

const health = ref(null)
const errorMessage = ref('')

async function checkHealth() {
  errorMessage.value = ''
  health.value = null

  try {
    health.value = await apiClient.get('/api/health')
  } catch (error) {
    errorMessage.value = error.message
  }
}
</script>
