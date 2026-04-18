<template>
  <PlaceholderPage
    title="Sections"
    description="Find, view, create, edit, and configure active weeks for senior design sections."
    :use-cases="[
      'UC-2: Admin finds senior design sections',
      'UC-3: Admin views a senior design section',
      'UC-4: Admin creates a senior design section',
      'UC-5: Admin edits a senior design section',
      'UC-6: Admin sets up active weeks'
    ]"
    :next-work="[
      'Add section list and search controls',
      'Add create and edit flows after backend endpoints are ready',
      'Connect to the section API through sectionsService'
    ]"
  />
</template>

<script setup>
import PlaceholderPage from '../../shared/components/PlaceholderPage.vue'
</script>
