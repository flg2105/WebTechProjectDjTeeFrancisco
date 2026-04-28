<template>
  <div class="app-shell">
    <TopNav v-if="$route.name !== 'login'" />
    <main class="app-content">
      <RouterView v-slot="{ Component, route }">
        <Transition mode="out-in" name="page-fade">
          <component :is="Component" :key="route.fullPath" />
        </Transition>
      </RouterView>
    </main>
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
import TopNav from './shared/components/TopNav.vue'
</script>
