<template>
  <PlaceholderPage
    title="Weekly Activity Reports"
    description="Students will manage weekly activities with planned work, actual hours, and status."
    :use-cases="['UC-27: Student manages activities in a Weekly Activity Report']"
    :next-work="[
      'Add activity entry and editing controls in Phase 3',
      'Support category, planned activity, description, hours, and status fields',
      'Connect to the WAR API through warService'
    ]"
  />
</template>

<script setup>
import PlaceholderPage from '../../shared/components/PlaceholderPage.vue'
</script>
