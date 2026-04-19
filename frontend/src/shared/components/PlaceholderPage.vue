<template>
  <section>
    <p class="text-uppercase text-muted small mb-2">{{ eyebrow }}</p>
    <h1 class="h3 mb-3">{{ title }}</h1>
    <p class="lead mb-4">{{ description }}</p>

    <div class="row g-3">
      <div class="col-md-6">
        <h2 class="h5">Use cases</h2>
        <ul class="mb-0">
          <li v-for="useCase in useCases" :key="useCase">{{ useCase }}</li>
        </ul>
      </div>

      <div class="col-md-6">
        <h2 class="h5">Next work</h2>
        <ul class="mb-0">
          <li v-for="item in nextWork" :key="item">{{ item }}</li>
        </ul>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  eyebrow: {
    type: String,
    default: 'Phase 1 placeholder'
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  },
  useCases: {
    type: Array,
    required: true
  },
  nextWork: {
    type: Array,
    required: true
  }
})
</script>
