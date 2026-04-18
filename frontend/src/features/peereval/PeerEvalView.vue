<template>
  <PlaceholderPage
    title="Peer Evaluation"
    description="Students will submit rubric-based teammate evaluations and view their own report."
    :use-cases="[
      'UC-28: Student submits a peer evaluation for the previous week',
      'UC-29: Student views her own peer evaluation report'
    ]"
    :next-work="[
      'Add teammate evaluation form in Phase 3',
      'Show rubric criteria, public comments, and private comments',
      'Connect to the peer evaluation API through peerEvalService'
    ]"
  />
</template>

<script setup>
import PlaceholderPage from '../../shared/components/PlaceholderPage.vue'
</script>
