import { createRouter, createWebHistory } from 'vue-router'
import { authSession } from '../shared/services/authSession'

import HomeView from '../features/home/HomeView.vue'
import LoginView from '../features/auth/LoginView.vue'
import SectionsView from '../features/sections/SectionsView.vue'
import TeamsView from '../features/teams/TeamsView.vue'
import RubricsView from '../features/rubrics/RubricsView.vue'
import WarView from '../features/war/WarView.vue'
import PeerEvalView from '../features/peereval/PeerEvalView.vue'
import ReportsView from '../features/reports/ReportsView.vue'

const routes = [
  { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
  { path: '/', name: 'home', component: HomeView },
  { path: '/sections', name: 'sections', component: SectionsView },
  { path: '/teams', name: 'teams', component: TeamsView },
  { path: '/rubrics', name: 'rubrics', component: RubricsView },
  { path: '/war', name: 'war', component: WarView },
  { path: '/peer-eval', name: 'peerEval', component: PeerEvalView },
  { path: '/reports', name: 'reports', component: ReportsView }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  if (to.meta.public) {
    if (to.name === 'login' && authSession.isAuthenticated) {
      return { path: '/' }
    }
    return true
  }

  if (!authSession.isAuthenticated) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  try {
    if (!authSession.currentUser?.role) {
      await authSession.refreshCurrentUser()
    }
    return true
  } catch {
    authSession.logout()
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
