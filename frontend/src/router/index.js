import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '../features/home/HomeView.vue'
import SectionsView from '../features/sections/SectionsView.vue'
import TeamsView from '../features/teams/TeamsView.vue'
import RubricsView from '../features/rubrics/RubricsView.vue'
import WarView from '../features/war/WarView.vue'
import PeerEvalView from '../features/peereval/PeerEvalView.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/sections', name: 'sections', component: SectionsView },
  { path: '/teams', name: 'teams', component: TeamsView },
  { path: '/rubrics', name: 'rubrics', component: RubricsView },
  { path: '/war', name: 'war', component: WarView },
  { path: '/peer-eval', name: 'peerEval', component: PeerEvalView }
]

export default createRouter({
  history: createWebHistory(),
  routes
})

