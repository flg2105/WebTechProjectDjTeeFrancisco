<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
      <RouterLink class="navbar-brand" to="/">Project Pulse</RouterLink>
      <button
        class="navbar-toggler"
        type="button"
        data-bs-toggle="collapse"
        data-bs-target="#nav"
        aria-controls="nav"
        aria-expanded="false"
        aria-label="Toggle navigation"
      >
        <span class="navbar-toggler-icon"></span>
      </button>
      <div id="nav" class="collapse navbar-collapse">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <RouterLink class="nav-link" to="/sections">Sections</RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink class="nav-link" to="/teams">Teams</RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink class="nav-link" to="/rubrics">Rubrics</RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink class="nav-link" to="/war">WAR</RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink class="nav-link" to="/peer-eval">Peer Eval</RouterLink>
          </li>
        </ul>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { RouterLink } from 'vue-router'
</script>

